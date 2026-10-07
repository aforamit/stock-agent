package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Minimal Anthropic Messages API client (plain HTTP, no SDK). Reads ANTHROPIC_API_KEY and optional ANTHROPIC_WORKSPACE_ID (or anthropic_workspace_id in config.yaml). */
final class Llm {
    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    private final Config cfg;
    private final String apiKey;
    private final String workspaceId;

    Llm(Config cfg) {
        this.cfg = cfg;
        this.apiKey = System.getenv("ANTHROPIC_API_KEY");
        String ws = System.getenv("ANTHROPIC_WORKSPACE_ID");
        this.workspaceId = ws == null || ws.isBlank() ? cfg.str("/anthropic_workspace_id") : ws;
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set the ANTHROPIC_API_KEY environment variable.");
        }
    }

    String ask(String system, String user, boolean webSearch) throws Exception {
        ObjectNode body = Json.MAPPER.createObjectNode();
        body.put("model", cfg.str("/model"));
        body.put("max_tokens", cfg.intVal("/max_tokens", 4000));
        body.put("system", system);
        body.putArray("messages").addObject().put("role", "user").put("content", user);
        if (webSearch && cfg.bool("/use_web_search", false)) {
            ObjectNode tool = body.putArray("tools").addObject();
            tool.put("type", "web_search_20250305");
            tool.put("name", "web_search");
            tool.put("max_uses", 5);
        }

        Log.detail("LLM request (web_search=" + body.has("tools") + ")", "--- system ---\n" + system + "\n--- user ---\n" + user);

        HttpRequest.Builder rb =HttpRequest.newBuilder(URI.create(ENDPOINT))
                .timeout(Duration.ofMinutes(5))
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.MAPPER.writeValueAsString(body)));
        if (workspaceId != null && !workspaceId.isBlank()) {
            rb.header("anthropic-workspace-id", workspaceId.trim()); // required for keys not scoped to a workspace
        }
        HttpRequest req = rb.build();

        HttpResponse<String> resp = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            int sc = resp.statusCode();
            if (sc == 429 || sc == 500 || sc == 502 || sc == 503 || sc == 529) {
                Thread.sleep(1500L << attempt); // exponential backoff
                continue;
            }
            break;
        }
        if (resp.statusCode() >= 400) {
            Log.detail("LLM error " + resp.statusCode(), resp.body());
            throw new RuntimeException("Anthropic API error " + resp.statusCode() + ": " + resp.body());
        }
        JsonNode root = Json.MAPPER.readTree(resp.body());
        JsonNode content = root.path("content");
        StringBuilder sb = new StringBuilder();
        if (content instanceof ArrayNode arr) {
            for (JsonNode block : arr) {
                if ("text".equals(block.path("type").asText())) sb.append(block.path("text").asText());
            }
        }
        // Thinking counts against max_tokens, so a low cap can end the turn before any text is written.
        String stop = root.path("stop_reason").asText();
        Log.detail("LLM response (stop_reason=" + stop + ", usage=" + root.path("usage") + ")", sb.toString());
        if ("max_tokens".equals(stop) || sb.isEmpty()) {
            throw new RuntimeException("Model stopped without a complete answer (stop_reason=" + stop
                    + ", output_tokens=" + root.at("/usage/output_tokens").asInt() + "); raise max_tokens in config.yaml");
        }
        return sb.toString();
    }

    JsonNode askJson(String system, String user, boolean webSearch) throws Exception {
        Exception last = null;
        String sys = system + "\nReturn ONLY a valid JSON object. No prose, no markdown fences.";
        for (int i = 0; i < 3; i++) {
            try {
                return Json.extract(ask(sys, user, webSearch));
            } catch (Exception e) {
                last = e; // malformed JSON: retry
            }
        }
        throw new RuntimeException("Model failed to return valid JSON: " + last, last);
    }
}
