package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockagent.Models.MarketData;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Market data from Yahoo Finance's unofficial endpoints (cookie + crumb flow).
 * These endpoints are undocumented and can change; swap this class for a paid provider (FMP, Polygon, ...)
 * if you need reliability.
 */
final class YahooClient {
    private static final String UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36";

    private static final Map<String, String> TS_TYPES = new LinkedHashMap<>();
    static {
        TS_TYPES.put("annualFreeCashFlow", "free_cash_flow");
        TS_TYPES.put("annualOperatingCashFlow", "operating_cash_flow");
        TS_TYPES.put("annualCapitalExpenditure", "capex");
        TS_TYPES.put("annualTotalRevenue", "revenue");
        TS_TYPES.put("annualNetIncome", "net_income");
        TS_TYPES.put("annualOperatingIncome", "operating_income");
    }

    private final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private String crumb;

    private String get(String url, boolean failOnError) throws IOException, InterruptedException {
        HttpRequest r = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(25)).header("User-Agent", UA).GET().build();
        HttpResponse<String> resp = http.send(r, HttpResponse.BodyHandlers.ofString());
        if (failOnError && resp.statusCode() >= 400) {
            throw new IOException("Yahoo HTTP " + resp.statusCode() + " for " + url);
        }
        return resp.body();
    }

    private synchronized String crumb() throws Exception {
        if (crumb == null) {
            get("https://fc.yahoo.com", false); // sets the session cookie (returns 404 by design)
            crumb = get("https://query1.finance.yahoo.com/v1/test/getcrumb", true).trim();
        }
        return crumb;
    }

    private static Double num(JsonNode root, String ptr) {
        JsonNode n = root.at(ptr);
        return n.isNumber() ? n.asDouble() : null;
    }

    MarketData fetch(String ticker) throws Exception {
        String c = URLEncoder.encode(crumb(), StandardCharsets.UTF_8);
        String sym = URLEncoder.encode(ticker, StandardCharsets.UTF_8);

        String qs = get("https://query1.finance.yahoo.com/v10/finance/quoteSummary/" + sym
                + "?modules=price,summaryDetail,defaultKeyStatistics,financialData,assetProfile&crumb=" + c, true);
        JsonNode r = Json.MAPPER.readTree(qs).at("/quoteSummary/result/0");
        if (r.isMissingNode()) throw new IOException("No quote data for " + ticker);

        Double de = num(r, "/financialData/debtToEquity/raw");
        Double price = num(r, "/price/regularMarketPrice/raw");

        Map<String, TreeMap<String, Double>> history = fetchHistory(sym, c);

        String company = r.at("/price/longName").asText(r.at("/price/shortName").asText(ticker));
        return new MarketData(
                ticker, company, r.at("/price/currency").asText(null), price,
                num(r, "/defaultKeyStatistics/sharesOutstanding/raw"),
                num(r, "/summaryDetail/marketCap/raw"),
                num(r, "/financialData/totalDebt/raw"),
                num(r, "/financialData/totalCash/raw"),
                num(r, "/summaryDetail/trailingPE/raw"),
                num(r, "/summaryDetail/forwardPE/raw"),
                num(r, "/defaultKeyStatistics/enterpriseToEbitda/raw"),
                num(r, "/defaultKeyStatistics/priceToBook/raw"),
                num(r, "/financialData/returnOnEquity/raw"),
                de == null ? null : de / 100.0, // Yahoo reports D/E in percent
                r.at("/assetProfile/sector").asText(null),
                r.at("/assetProfile/industry").asText(null),
                history);
    }

    private Map<String, TreeMap<String, Double>> fetchHistory(String sym, String crumbEnc) {
        Map<String, TreeMap<String, Double>> out = new LinkedHashMap<>();
        try {
            long p2 = Instant.now().getEpochSecond();
            long p1 = p2 - 8L * 365 * 24 * 3600;
            String types = String.join(",", TS_TYPES.keySet());
            String body = get("https://query1.finance.yahoo.com/ws/fundamentals-timeseries/v1/finance/timeseries/"
                    + sym + "?type=" + types + "&merge=false&period1=" + p1 + "&period2=" + p2
                    + "&crumb=" + crumbEnc, true);
            for (JsonNode item : Json.MAPPER.readTree(body).at("/timeseries/result")) {
                String type = item.at("/meta/type/0").asText();
                JsonNode arr = item.get(type);
                String key = TS_TYPES.get(type);
                if (key == null || arr == null || !arr.isArray()) continue;
                TreeMap<String, Double> m = new TreeMap<>();
                for (JsonNode p : arr) {
                    if (p == null || p.isNull()) continue;
                    JsonNode v = p.at("/reportedValue/raw");
                    if (v.isNumber()) m.put(p.path("asOfDate").asText(), v.asDouble());
                }
                if (!m.isEmpty()) out.put(key, m);
            }
            // Derive FCF if Yahoo didn't supply it directly: operating cash flow + (negative) capex
            if (!out.containsKey("free_cash_flow") && out.containsKey("operating_cash_flow") && out.containsKey("capex")) {
                TreeMap<String, Double> fcf = new TreeMap<>();
                TreeMap<String, Double> capex = out.get("capex");
                out.get("operating_cash_flow").forEach((d, ocf) -> {
                    if (capex.containsKey(d)) fcf.put(d, ocf + capex.get(d));
                });
                if (!fcf.isEmpty()) out.put("free_cash_flow", fcf);
            }
        } catch (Exception e) {
            Log.info("   ! Yahoo history unavailable: " + e.getMessage());
        }
        return out;
    }
}
