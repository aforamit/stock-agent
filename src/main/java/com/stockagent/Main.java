package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stockagent.Models.Collected;
import com.stockagent.Models.Decision;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

/** Orchestrator. Usage: java -jar stock-agent.jar AAPL [--config config.yaml] [--out output] */
public final class Main {
    private static void step(String msg) {
        Log.info("\n>>> " + msg);
    }

    public static void main(String[] args) throws Exception {
        try {
            run(args);
        } catch (Exception e) {
            Log.error("Run failed: " + e, e);
            throw e;
        }
    }

    private static void run(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar stock-agent.jar <TICKER> [--config config.yaml] [--out output]");
            System.out.println("Examples: AAPL, MSFT, RELIANCE.NS, TCS.NS");
            return;
        }
        String ticker = args[0].toUpperCase();
        String configPath = "config.yaml", outDir = "output";
        for (int i = 1; i + 1 < args.length; i++) {
            if (args[i].equals("--config")) configPath = args[i + 1];
            if (args[i].equals("--out")) outDir = args[i + 1];
        }

        Config cfg = Config.load(configPath);
        String logDir = cfg.str("/log_dir");
        Log.init(logDir.isBlank() ? "work/logs" : logDir, ticker);
        Log.info("Stock agent run for " + ticker + " (model " + cfg.str("/model") + "), log: " + Log.file());
        Llm llm = new Llm(cfg);
        Agents agents = new Agents(llm, cfg);

        step("Agent 1: collecting data");
        Collected data = new DataCollector(cfg).collect(ticker);
        data.documents().forEach(d -> Log.info("   - " + d.name() + ": "
                + (d.error() != null ? "ERROR " + d.error() : d.text().length() + " chars")));

        step("Agent 2: filings analyst");
        JsonNode filings = agents.filingsAnalyst(data);

        step("Agents 3 + 4: valuation (code) and qualitative analyst, in parallel");
        JsonNode valuation, qualitative;
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            var valF = ex.submit(() -> (JsonNode) Valuation.compute(cfg, data, filings));
            var qualF = ex.submit(() -> agents.qualitativeAnalyst(data, filings));
            valuation = valF.get();
            qualitative = qualF.get();
        }

        step("Agent 5: skeptic / red team");
        JsonNode skeptic = agents.skeptic(data, filings, qualitative, valuation);

        step("Decision rule (code)");
        Decision decision = Valuation.decide(cfg, data, valuation, skeptic, filings);
        Log.info("   => " + decision.recommendation());
        Log.detail("Decision checks", Json.pretty(decision.checks()));

        step("Agent 6: synthesizer");
        String report = agents.synthesizer(data, filings, qualitative, valuation, skeptic, decision);

        Files.createDirectories(Path.of(outDir));
        String base = Path.of(outDir, ticker.replace('.', '_')).toString();
        Files.writeString(Path.of(base + "_report.md"), report, StandardCharsets.UTF_8);

        ObjectNode raw = Json.MAPPER.createObjectNode();
        raw.set("market_data", Json.MAPPER.valueToTree(data.marketData()));
        raw.set("filings", filings);
        raw.set("valuation", valuation);
        raw.set("qualitative", qualitative);
        raw.set("skeptic", skeptic);
        raw.put("recommendation", decision.recommendation());
        raw.set("checks", decision.checks());
        Files.writeString(Path.of(base + "_raw.json"), Json.pretty(raw), StandardCharsets.UTF_8);

        Log.info("\nSaved: " + base + "_report.md and " + base + "_raw.json");
    }
}
