package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Command line entry point: one ticker or the whole watchlist, through the check and/or full tier. */
public final class Main {
    private static final List<String> TIERS = List.of("check", "full", "auto");

    private static void usage() {
        System.out.println("""
                Usage: java -jar stock-agent.jar <TICKER> | --watchlist  [options]
                  --tier check   price vs the stored valuation, no LLM calls
                  --tier full    run the full agent analysis
                  --tier auto    check, then full only if a trigger fires (default: monitor.default_tier)
                  --refresh      full analysis re-reads the filings instead of reusing the stored filings analysis
                  --config FILE  config file (default config.yaml, else the bundled one)
                  --out DIR      report directory (default work/output)
                Examples: AAPL --tier full | RELIANCE.NS --tier check | --watchlist""");
    }

    public static void main(String[] args) throws Exception {
        try {
            if (!run(args)) System.exit(1);
        } catch (Exception e) {
            Log.error("Run failed: " + e, e);
            throw e;
        }
    }

    /** @return false if any ticker failed */
    private static boolean run(String[] args) throws Exception {
        String ticker = null, tier = null, configPath = "config.yaml", outDir = "work/output";
        boolean watchlist = false, refresh = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--watchlist" -> watchlist = true;
                case "--refresh" -> refresh = true;
                case "--tier", "--config", "--out" -> {
                    if (i + 1 >= args.length) throw new IllegalArgumentException(args[i] + " needs a value");
                    String v = args[++i];
                    switch (args[i - 1]) {
                        case "--tier" -> tier = v.toLowerCase();
                        case "--config" -> configPath = v;
                        default -> outDir = v;
                    }
                }
                default -> {
                    if (args[i].startsWith("--")) throw new IllegalArgumentException("Unknown option " + args[i]);
                    ticker = args[i].toUpperCase();
                }
            }
        }
        if (ticker == null && !watchlist) {
            usage();
            return true;
        }

        Config cfg = Config.load(configPath);
        if (tier == null) tier = cfg.at("/monitor/default_tier").asText("auto");
        if (!TIERS.contains(tier)) throw new IllegalArgumentException("--tier must be one of " + TIERS);

        // ticker -> held (shares > 0); SELL signals are only raised for held positions
        Map<String, Boolean> listed = new LinkedHashMap<>();
        for (JsonNode w : cfg.at("/watchlist")) {
            String t = (w.isTextual() ? w.asText() : w.path("ticker").asText()).toUpperCase();
            if (!t.isBlank()) listed.put(t, w.path("shares").asDouble(0) > 0);
        }
        Map<String, Boolean> targets = new LinkedHashMap<>();
        if (ticker != null) targets.put(ticker, listed.getOrDefault(ticker, false));
        else targets.putAll(listed);
        if (targets.isEmpty()) throw new IllegalStateException("The watchlist in " + configPath + " is empty");

        String logDir = cfg.str("/log_dir");
        Log.init(logDir.isBlank() ? "work/logs" : logDir, ticker != null ? ticker : "WATCHLIST");
        Log.info("Stock agent: " + String.join(", ", targets.keySet()) + " | tier " + tier
                + " | model " + cfg.str("/model") + " | log " + Log.file());

        Monitor monitor = new Monitor(cfg, outDir);
        List<Monitor.Result> results = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (var t : targets.entrySet()) {
            Log.info("\n=== " + t.getKey() + (t.getValue() ? " (held)" : "") + " ===");
            try {
                results.add(monitor.run(t.getKey(), tier, t.getValue(), refresh));
            } catch (Exception e) {
                if (ticker != null) throw e;
                failed.add(t.getKey());
                Log.info("   ! " + t.getKey() + " failed: " + e.getMessage());
                Log.error(t.getKey() + " failed", e);
            }
        }

        Log.info("\n=== Summary ===");
        for (var r : results) {
            Log.info(String.format("%-12s %-5s %10.2f  %s%s", r.ticker(), r.tier(), r.price(), r.signal(),
                    r.note().isEmpty() ? "" : "  [" + r.note() + "]"));
        }
        failed.forEach(f -> Log.info(String.format("%-12s FAILED (see log)", f)));
        return failed.isEmpty();
    }
}
