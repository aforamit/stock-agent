package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stockagent.Models.Collected;
import com.stockagent.Models.Decision;
import com.stockagent.Models.Filing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Runs one ticker through the two tiers:
 * "check" - price vs the stored valuation, no LLM; "full" - the whole agent pipeline;
 * "auto" - check, then full only if a trigger fires (no stored analysis, stale analysis, new filing, big price move).
 */
final class Monitor {
    record Result(String ticker, String tier, double price, String signal, String note) {}

    private final Config cfg;
    private final String outDir, stateDir;
    private final YahooClient yahoo = new YahooClient();
    private final DataCollector collector;
    private Agents agents;

    Monitor(Config cfg, String outDir) {
        this.cfg = cfg;
        this.outDir = outDir;
        String sd = cfg.str("/monitor/state_dir");
        this.stateDir = sd.isBlank() ? "work/state" : sd;
        this.collector = new DataCollector(cfg, yahoo);
    }

    private static void step(String msg) {
        Log.info("\n>>> " + msg);
    }

    private static String now() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    /** Created on first use so the check tier runs without an API key. */
    private Agents agents() {
        if (agents == null) agents = new Agents(new Llm(cfg), cfg);
        return agents;
    }

    private Set<String> triggerForms() {
        Set<String> forms = new LinkedHashSet<>();
        cfg.at("/monitor/trigger_forms").forEach(f -> forms.add(f.asText()));
        return forms;
    }

    Result run(String ticker, String tier, boolean held, boolean refresh) throws Exception {
        ObjectNode state = State.load(stateDir, ticker);
        Set<String> forms = new LinkedHashSet<>(DataCollector.ANALYSIS_FORMS);
        forms.addAll(triggerForms());
        List<Filing> filings = collector.latestFilings(ticker, forms);

        String ran = "check", note = "";
        double price;
        if (tier.equals("full")) {
            price = fullAnalysis(ticker, state, filings, refresh);
            ran = "full";
        } else {
            price = yahoo.price(ticker);
            List<String> due = triggers(state, filings, price);
            Log.info(String.format("   price %.2f; %s", price,
                    due.isEmpty() ? "no trigger for a full analysis" : "full analysis due: " + String.join("; ", due)));
            if (!due.isEmpty() && tier.equals("auto")) {
                price = fullAnalysis(ticker, state, filings, refresh);
                ran = "full";
            } else if (!due.isEmpty()) {
                note = "full analysis due: " + String.join("; ", due);
            }
        }
        state.putObject("last_check").put("at", now()).put("price", price).put("tier", ran);

        String signal;
        if (!state.has("analysis")) {
            signal = "NO DATA";
            Log.info("   no stored analysis for " + ticker + " yet - run it with --tier full");
        } else {
            Signal.Result s = Signal.evaluate(cfg, state.get("analysis"), price, held,
                    state.at("/signal/current").asText(null));
            signal = confirm(state, ticker, ran, price, s);
        }
        State.save(stateDir, ticker, state);
        return new Result(ticker, ran, price, signal, note);
    }

    /** Reasons a full analysis is due; empty when the stored one is still good. */
    private List<String> triggers(ObjectNode state, List<Filing> filings, double price) {
        if (!state.has("analysis")) return List.of("no stored analysis");
        JsonNode a = state.get("analysis");
        List<String> due = new ArrayList<>();

        long age = ChronoUnit.DAYS.between(OffsetDateTime.parse(a.path("at").asText()), OffsetDateTime.now());
        if (age >= cfg.intVal("/monitor/reanalyze_after_days", 30)) due.add("analysis is " + age + " days old");

        if (filings != null) {
            Set<String> watched = triggerForms();
            for (Filing f : filings) {
                if (watched.contains(f.form()) && !f.accession().equals(state.path("seen_filings").path(f.form()).asText())) {
                    due.add("new " + f.form() + " filed " + f.date());
                }
            }
        }

        double then = a.path("price").asDouble();
        double move = then > 0 ? price / then - 1 : 0;
        if (Math.abs(move) >= cfg.at("/monitor/price_move_trigger").asDouble(0.15)) {
            due.add(String.format("price moved %+.1f%% since the analysis", move * 100));
        }
        return due;
    }

    private static String filingsKey(List<Filing> filings) {
        if (filings == null) return "";
        return filings.stream().filter(f -> DataCollector.ANALYSIS_FORMS.contains(f.form()))
                .map(f -> f.form() + ":" + f.accession()).sorted().collect(Collectors.joining(","));
    }

    /** The full agent pipeline. Writes the report, stores the analysis in state, returns the price used. */
    private double fullAnalysis(String ticker, ObjectNode state, List<Filing> filings, boolean refresh) throws Exception {
        Agents agents = agents();
        // The filings are ~70% of the tokens of a run, so their analysis is reused until a new 10-K/10-Q appears.
        String key = filingsKey(filings);
        boolean cached = !refresh && !key.isEmpty() && key.equals(state.path("filings_analysis_key").asText())
                && state.has("filings_analysis");

        step("Agent 1: collecting data");
        Collected data = collector.collect(ticker, filings, !cached);
        if (data.marketData().price() == null) throw new IOException("No price for " + ticker);
        data.documents().forEach(d -> Log.info("   - " + d.name() + ": "
                + (d.error() != null ? "ERROR " + d.error() : d.text().length() + " chars")));

        JsonNode filingsAnalysis;
        if (cached) {
            step("Agent 2: filings analyst - reusing the stored analysis (no new 10-K/10-Q)");
            filingsAnalysis = state.get("filings_analysis");
        } else {
            step("Agent 2: filings analyst");
            filingsAnalysis = agents.filingsAnalyst(data);
        }
        JsonNode filingsJ = filingsAnalysis;

        step("Agents 3 + 4: valuation (code) and qualitative analyst, in parallel");
        JsonNode valuation, qualitative;
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            var valF = ex.submit(() -> (JsonNode) Valuation.compute(cfg, data, filingsJ));
            var qualF = ex.submit(() -> agents.qualitativeAnalyst(data, filingsJ));
            valuation = valF.get();
            qualitative = qualF.get();
        }

        step("Agent 5: skeptic / red team (panel of " + Math.max(1, cfg.intVal("/skeptic_runs", 3)) + ")");
        JsonNode skeptic = agents.skepticPanel(data, filingsJ, qualitative, valuation);
        Log.info("   severities " + skeptic.path("panel_severities") + " => " + skeptic.path("severity").asText());

        step("Decision rule (code)");
        Decision decision = Valuation.decide(cfg, data, valuation, skeptic, filingsJ);
        Log.info("   => " + decision.recommendation());
        Log.detail("Decision checks", Json.pretty(decision.checks()));

        // Store the analysis before the report is written: it is what the signal and later checks depend on.
        ObjectNode a = state.putObject("analysis");
        a.put("at", now());
        a.put("price", data.marketData().price());
        a.put("recommendation", decision.recommendation());
        a.set("checks", decision.checks());
        ObjectNode v = a.putObject("valuation");
        v.put("reliable", valuation.path("reliable").asBoolean(false));
        v.set("notes", valuation.path("notes"));
        for (String s : List.of("bear", "base", "bull")) {
            JsonNode iv = valuation.at("/scenarios/" + s + "/intrinsic_value_per_share");
            if (iv.isNumber()) v.put(s, iv.asDouble());
        }
        if (filings != null) {
            ObjectNode seen = state.putObject("seen_filings");
            filings.forEach(f -> seen.put(f.form(), f.accession()));
        }
        if (!cached && !key.isEmpty()) {
            state.put("filings_analysis_key", key);
            state.set("filings_analysis", filingsJ);
        }
        State.save(stateDir, ticker, state);

        step("Agent 6: synthesizer");
        String report = agents.synthesizer(data, filingsJ, qualitative, valuation, skeptic, decision);

        Files.createDirectories(Path.of(outDir));
        String base = Path.of(outDir, ticker.replace('.', '_')).toString();
        Files.writeString(Path.of(base + "_report.md"), report, StandardCharsets.UTF_8);

        ObjectNode raw = Json.MAPPER.createObjectNode();
        raw.set("market_data", Json.MAPPER.valueToTree(data.marketData()));
        raw.set("filings", filingsJ);
        raw.set("valuation", valuation);
        raw.set("qualitative", qualitative);
        raw.set("skeptic", skeptic);
        raw.put("recommendation", decision.recommendation());
        raw.set("checks", decision.checks());
        Files.writeString(Path.of(base + "_raw.json"), Json.pretty(raw), StandardCharsets.UTF_8);

        Log.info("\nSaved: " + base + "_report.md and " + base + "_raw.json");
        return data.marketData().price();
    }

    /**
     * A changed signal must repeat on confirm_runs consecutive runs before it replaces the current one,
     * and only then is it alerted. Every evaluation is appended to signals.csv.
     */
    private String confirm(ObjectNode state, String ticker, String tier, double price, Signal.Result s) throws IOException {
        ObjectNode sig = state.has("signal") ? (ObjectNode) state.get("signal") : state.putObject("signal");
        String current = sig.path("current").asText(null);
        int need = Math.max(1, cfg.intVal("/monitor/confirm_runs", 2));
        String status;
        if (s.signal().equals(current)) {
            sig.remove(List.of("pending", "pending_count"));
            status = "unchanged";
        } else {
            int count = s.signal().equals(sig.path("pending").asText(null)) ? sig.path("pending_count").asInt() + 1 : 1;
            if (count >= need) {
                sig.put("current", s.signal()).put("since", now());
                sig.remove(List.of("pending", "pending_count"));
                status = "confirmed";
                String msg = String.format("%s: %s -> %s at %.2f - %s",
                        ticker, current == null ? "(new)" : current, s.signal(), price, s.reason());
                if (Signal.isAction(s.signal()) || Signal.isAction(current)) Log.alert(msg);
                else Log.info("   signal change " + msg);
            } else {
                sig.put("pending", s.signal()).put("pending_count", count);
                status = "pending " + count + "/" + need;
            }
        }
        Log.info("   signal: " + s.signal() + " (" + status + ") - " + s.reason());

        JsonNode a = state.get("analysis"), v = a.path("valuation");
        State.appendSignal(stateDir, now(), ticker, tier, price,
                v.path("bear").asText(""), v.path("base").asText(""), v.path("bull").asText(""),
                a.path("recommendation").asText(), s.signal(), status, s.reason());
        return status.startsWith("pending") ? s.signal() + " (" + status + ")" : s.signal();
    }
}
