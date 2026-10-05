package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockagent.Models.Collected;
import com.stockagent.Models.Doc;
import com.stockagent.Models.MarketData;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Agent 1: market data (Yahoo), SEC filings (US), and every website configured in config.yaml (fetched in parallel). */
final class DataCollector {
    private final Config cfg;

    DataCollector(Config cfg) {
        this.cfg = cfg;
    }

    static String marketOf(String ticker) {
        String t = ticker.toUpperCase();
        return (t.endsWith(".NS") || t.endsWith(".BO")) ? "IN" : "US";
    }

    static String symbolOf(String ticker) {
        int i = ticker.indexOf('.');
        return i < 0 ? ticker : ticker.substring(0, i);
    }

    Collected collect(String ticker) throws Exception {
        String market = marketOf(ticker);
        MarketData md = new YahooClient().fetch(ticker);
        String company = URLEncoder.encode(md.company(), StandardCharsets.UTF_8);

        List<Callable<List<Doc>>> tasks = new ArrayList<>();

        if (market.equals("US")) tasks.add(() -> secFilings(ticker));

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

    /** Latest 10-K and 10-Q via SEC EDGAR. */
    private List<Doc> secFilings(String ticker) {
        List<Doc> docs = new ArrayList<>();
        Map<String, String> ua = Map.of("User-Agent", cfg.str("/sec_user_agent"));
        try {
            String tickersJson = fetchRaw("https://www.sec.gov/files/company_tickers.json", ua);
            String sym = symbolOf(ticker).toUpperCase();
            Long cik = null;
            for (JsonNode v : Json.MAPPER.readTree(tickersJson)) {
                if (v.path("ticker").asText().equalsIgnoreCase(sym)) {
                    cik = v.path("cik_str").asLong();
                    break;
                }
            }
            if (cik == null) return docs;

            JsonNode recent = Json.MAPPER.readTree(
                    fetchRaw(String.format("https://data.sec.gov/submissions/CIK%010d.json", cik), ua))
                    .at("/filings/recent");
            JsonNode forms = recent.path("form");
            for (String wanted : List.of("10-K", "10-Q")) {
                for (int i = 0; i < forms.size(); i++) {
                    if (!forms.get(i).asText().equals(wanted)) continue;
                    String acc = recent.path("accessionNumber").get(i).asText().replace("-", "");
                    String url = "https://www.sec.gov/Archives/edgar/data/" + cik + "/" + acc + "/"
                            + recent.path("primaryDocument").get(i).asText();
                    docs.add(fetchDoc("SEC " + wanted + " (" + recent.path("filingDate").get(i).asText() + ")", url, ua));
                    break;
                }
            }
        } catch (Exception e) {
            docs.add(Doc.failed("SEC EDGAR", "", String.valueOf(e.getMessage())));
        }
        return docs;
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
