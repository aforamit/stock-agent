package com.stockagent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/** Fetches a URL (HTML or PDF) and returns cleaned, truncated plain text. */
final class WebFetcher {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private WebFetcher() {}

    static String fetch(String url, Config cfg, Map<String, String> extraHeaders) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(cfg.intVal("/fetch/timeout_seconds", 25)))
                .header("User-Agent", cfg.str("/fetch/user_agent"))
                .GET();
        if (extraHeaders != null) extraHeaders.forEach(rb::header);

        HttpResponse<byte[]> r = HTTP.send(rb.build(), HttpResponse.BodyHandlers.ofByteArray());
        if (r.statusCode() >= 400) throw new RuntimeException("HTTP " + r.statusCode());

        String ctype = r.headers().firstValue("Content-Type").orElse("").toLowerCase();
        String text;
        if (ctype.contains("pdf") || url.toLowerCase().endsWith(".pdf")) {
            text = PdfText.extract(r.body());
        } else {
            Document doc = Jsoup.parse(new String(r.body(), StandardCharsets.UTF_8), url);
            doc.select("script, style, noscript, nav, footer").remove();
            // Hidden content, e.g. the inline-XBRL metadata block that opens every SEC filing (30-50k chars)
            doc.select("ix|header, [style~=(?i)display:\\s*none]").remove();
            text = doc.body() == null ? doc.text() : doc.body().wholeText();
        }
        text = text.replaceAll("[ \\t]+", " ").replaceAll("\\n\\s*\\n+", "\n\n").strip();
        int max = cfg.intVal("/fetch/max_chars_per_doc", 40000);
        return text.length() > max ? text.substring(0, max) : text;
    }
}
