package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockagent.Models.Collected;
import com.stockagent.Models.Doc;
import com.stockagent.Models.Filing;
import com.stockagent.Models.MarketData;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Agent 1: market data (Yahoo), SEC filings (US), and every website configured in config.yaml (fetched in parallel). */
final class DataCollector {
    /** Forms whose full text is sent to the filings analyst. */
    static final List<String> ANALYSIS_FORMS = List.of("10-K", "10-Q");

    private final Config cfg;
    private final YahooClient yahoo;

    DataCollector(Config cfg, YahooClient yahoo) {
        this.cfg = cfg;
        this.yahoo = yahoo;
    }

    static String marketOf(String ticker) {
        String t = ticker.toUpperCase();
        return (t.endsWith(".NS") || t.endsWith(".BO")) ? "IN" : "US";
    }

    static String symbolOf(String ticker) {
        int i = ticker.indexOf('.');
        return i < 0 ? ticker : ticker.substring(0, i);
    }

    private Map<String, String> secHeaders() {
        return Map.of("User-Agent", cfg.str("/sec_user_agent"));
    }

    /**
     * Latest filing of each wanted form on SEC EDGAR (two small requests, no document download).
     * Empty for non-US tickers; null when EDGAR could not be reached, so callers can tell "none" from "unknown".
     */
    List<Filing> latestFilings(String ticker, Collection<String> wantedForms) {
        if (!marketOf(ticker).equals("US")) return List.of();
        List<Filing> out = new ArrayList<>();
        try {
            String tickersJson = fetchRaw("https://www.sec.gov/files/company_tickers.json", secHeaders());
            String sym = symbolOf(ticker).toUpperCase();
            Long cik = null;
            for (JsonNode v : Json.MAPPER.readTree(tickersJson)) {
                if (v.path("ticker").asText().equalsIgnoreCase(sym)) {
                    cik = v.path("cik_str").asLong();
                    break;
                }
            }
            if (cik == null) return out;

            JsonNode recent = Json.MAPPER.readTree(
                    fetchRaw(String.format("https://data.sec.gov/submissions/CIK%010d.json", cik), secHeaders()))
                    .at("/filings/recent");
            JsonNode forms = recent.path("form");
            for (String wanted : wantedForms) {
                for (int i = 0; i < forms.size(); i++) {
                    if (!forms.get(i).asText().equals(wanted)) continue;
                    String acc = recent.path("accessionNumber").get(i).asText();
                    String url = "https://www.sec.gov/Archives/edgar/data/" + cik + "/" + acc.replace("-", "") + "/"
                            + recent.path("primaryDocument").get(i).asText();
                    out.add(new Filing(wanted, acc, recent.path("filingDate").get(i).asText(), url));
                    break;
                }
            }
            return out;
        } catch (Exception e) {
            Log.info("   ! SEC EDGAR unavailable: " + e.getMessage());
            return null;
        }
    }

    /** Market data, plus (when withDocs) the 10-K/10-Q among the given filings and every configured source. */
    Collected collect(String ticker, List<Filing> filings, boolean withDocs) throws Exception {
        String market = marketOf(ticker);
        MarketData md = yahoo.fetch(ticker);
        if (!withDocs) return new Collected(market, md, List.of());
        String company = URLEncoder.encode(md.company(), StandardCharsets.UTF_8);

        List<Callable<List<Doc>>> tasks = new ArrayList<>();

        if (filings == null) {
            tasks.add(() -> List.of(Doc.failed("SEC EDGAR", "", "unavailable")));
        } else {
            for (Filing f : filings) {
                if (!ANALYSIS_FORMS.contains(f.form())) continue;
                tasks.add(() -> List.of(fetchDoc("SEC " + f.form() + " (" + f.date() + ")", f.url(), secHeaders())));
            }
        }

        for (JsonNode s : cfg.at("/sources")) {
            if (!s.path("enabled").asBoolean(true)) continue;
            String m = s.path("market").asText("ALL");
            if (!m.equals("ALL") && !m.equals(market)) continue;
            String url = s.path("url").asText()
                    .replace("{ticker}", ticker)
                    .replace("{symbol}", symbolOf(ticker))
                    .replace("{company}", company);
            String name = s.path("name").asText(url);
            tasks.add(() -> List.of(fetchDoc(name, url, null)));
        }

        for (JsonNode d : cfg.at("/extra_documents").path(ticker)) {
            String name = d.path("name").asText();
            String url = d.path("url").asText();
            tasks.add(() -> List.of(fetchDoc(name, url, null)));
        }

        List<Doc> docs = new ArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<List<Doc>>> futures = new ArrayList<>();
            for (var t : tasks) futures.add(executor.submit(t));
            for (var f : futures) docs.addAll(f.get());
        }
        return new Collected(market, md, docs);
    }

    private Doc fetchDoc(String name, String url, Map<String, String> headers) {
        try {
            return Doc.ok(name, url, WebFetcher.fetch(url, cfg, headers));
        } catch (Exception e) {
            return Doc.failed(name, url, String.valueOf(e.getMessage()));
        }
    }

    private static String fetchRaw(String url, Map<String, String> headers) throws Exception {
        var rb = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                .timeout(java.time.Duration.ofSeconds(25)).GET();
        headers.forEach(rb::header);
        var resp = java.net.http.HttpClient.newHttpClient()
                .send(rb.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException("HTTP " + resp.statusCode() + " " + url);
        return resp.body();
    }
}
