package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.stockagent.Models.Collected;
import com.stockagent.Models.Doc;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/** Agents 2, 4, 5, 6: filings analyst, qualitative analyst, skeptic (red team), synthesizer. */
final class Agents {
    private static final List<String> SEVERITIES = List.of("none", "minor", "major");

    private final Llm llm;
    private final Config cfg;

    Agents(Llm llm, Config cfg) {
        this.llm = llm;
        this.cfg = cfg;
    }

    private static String docsBlock(Collected data) {
        String s = data.documents().stream()
                .filter(d -> d.text() != null && !d.text().isBlank())
                .map((Doc d) -> "=== SOURCE: " + d.name() + " | " + d.url() + " ===\n" + d.text() + "\n")
                .collect(Collectors.joining("\n"));
        return s.isBlank() ? "(no documents retrieved)" : s;
    }

    // ---------------- Agent 2: Filings analyst ----------------
    JsonNode filingsAnalyst(Collected data) throws Exception {
        String system = "You are a forensic equity analyst. Use ONLY the provided documents and market data. "
                + "Cite the SOURCE name for every figure. If something is not in the material, use null and say so; never guess.";
        String user = "Company: " + data.marketData().company() + " (" + data.marketData().ticker() + ")\n\n"
                + "MARKET DATA:\n" + Json.pretty(data.marketData()) + "\n\n"
                + "DOCUMENTS:\n" + docsBlock(data) + "\n\n"
                + """
                Return JSON:
                {
                 "business_summary": str,
                 "key_figures": [{"metric": str, "value": str, "period": str, "source": str}],
                 "recommended_base_growth_rate": float|null,   // annual FCF growth for first 5 years, decimal e.g. 0.08
                 "growth_rationale": str,
                 "growth_sources": [str],
                 "stated_risks": [str],
                 "accounting_red_flags": [{"flag": str, "evidence": str, "source": str}],
                 "data_gaps": [str]
                }""";
        return llm.askJson(system, user, false);
    }

    // ---------------- Agent 4: Qualitative analyst ----------------
    JsonNode qualitativeAnalyst(Collected data, JsonNode filings) throws Exception {
        String company = data.marketData().company();
        StringBuilder queries = new StringBuilder();
        for (JsonNode q : cfg.at("/search_queries")) {
            queries.append("- ").append(q.asText().replace("{company}", company)).append("\n");
        }
        String system = "You are a long-term quality investor assessing moat, management and industry. "
                + "Use web search when enabled and cite URLs. Say 'unknown' rather than guess.";
        String f = filings.toString();
        String user = "Company: " + company + ". Sector: " + data.marketData().sector()
                + ", industry: " + data.marketData().industry() + ".\n"
                + "Filings analysis summary: " + f.substring(0, Math.min(6000, f.length())) + "\n\n"
                + "Research topics:\n" + queries + "\n"
                + """
                Return JSON:
                {
                 "moat": {"rating": "none|narrow|wide|unknown", "sources_of_moat": [str], "evidence": str},
                 "management": {"rating": "weak|average|strong|unknown", "capital_allocation": str, "governance_concerns": [str]},
                 "industry": {"growth_outlook": str, "competitive_intensity": str, "key_competitors": [str]},
                 "catalysts": [str],
                 "citations": [str]
                }""";
        return llm.askJson(system, user, true);
    }

    // ---------------- Agent 5: Skeptic ----------------
    JsonNode skeptic(Collected data, JsonNode filings, JsonNode qualitative, JsonNode valuation) throws Exception {
        String system = "You are a skeptical short-seller analyst. Your job is to find the strongest reasons the "
                + "bullish thesis is WRONG and to catch errors or inconsistencies in the analysis. Be specific.";
        String user = "Company: " + data.marketData().company() + "\n"
                + "Market data: " + Json.pretty(data.marketData()) + "\n"
                + "Filings analysis: " + filings + "\n"
                + "Qualitative analysis: " + qualitative + "\n"
                + "Valuation (computed in code): " + valuation + "\n\n"
                + """
                Return JSON:
                {
                 "bear_case": [str],
                 "assumption_challenges": [{"assumption": str, "why_questionable": str}],
                 "inconsistencies_found": [str],
                 "severity": "none|minor|major",     // 'major' = a reason to avoid regardless of valuation
                 "severity_reason": str
                }""";
        return llm.askJson(system, user, true);
    }

    /**
     * Runs the skeptic skeptic_runs times in parallel and keeps the reply with the median severity, so one
     * unusually harsh or lenient run cannot flip the recommendation on its own.
     */
    JsonNode skepticPanel(Collected data, JsonNode filings, JsonNode qualitative, JsonNode valuation) throws Exception {
        int n = Math.max(1, cfg.intVal("/skeptic_runs", 3));
        List<JsonNode> votes = new ArrayList<>();
        Exception last = null;
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<JsonNode>> runs = new ArrayList<>();
            for (int i = 0; i < n; i++) runs.add(ex.submit(() -> skeptic(data, filings, qualitative, valuation)));
            for (var r : runs) {
                try {
                    votes.add(r.get());
                } catch (ExecutionException e) {
                    last = e;
                    Log.error("Skeptic run failed", e.getCause());
                }
            }
        }
        if (votes.isEmpty()) throw new RuntimeException("All skeptic runs failed", last);
        votes.sort(Comparator.comparingInt(v -> SEVERITIES.indexOf(v.path("severity").asText("none"))));
        ObjectNode chosen = (ObjectNode) votes.get(votes.size() / 2);
        var panel = chosen.putArray("panel_severities");
        votes.forEach(v -> panel.add(v.path("severity").asText("none")));
        return chosen;
    }

    // ---------------- Agent 6: Synthesizer ----------------
    /** @param profiles risk profile name -> {recommendation, checks}, as decided by the rule engine */
    String synthesizer(Collected data, JsonNode filings, JsonNode qualitative, JsonNode valuation,
                       JsonNode skeptic, JsonNode profiles) throws Exception {
        String system = "You are a senior investment analyst writing a research report. The recommendation for each "
                + "investor risk profile has ALREADY been decided by a rule engine; you must report each one exactly and "
                + "explain the reasoning. Do not change any numbers. Clearly state uncertainties. "
                + "You are not a licensed financial advisor.";
        String user = "Write a markdown report for " + data.marketData().company()
                + " (" + data.marketData().ticker() + ").\n\n"
                + "RECOMMENDATION PER RISK PROFILE (fixed), with the rule checks behind each: " + profiles + "\n"
                + "Thresholds each risk profile applies: " + Json.MAPPER.valueToTree(cfg.riskProfiles()) + "\n\n"
                + "Valuation: " + valuation + "\n"
                + "Filings: " + filings + "\n"
                + "Qualitative: " + qualitative + "\n"
                + "Skeptic: " + skeptic + "\n\n"
                + "Sections: 1. Executive summary (open with a table of the recommendation for every risk profile) "
                + "2. Business overview 3. Financial snapshot "
                + "4. Valuation (bear/base/bull table vs price) 5. Moat & management 6. Risks & bear case "
                + "7. Decision by risk profile (one table: each rule check, the threshold and pass/fail per profile; "
                + "then explain why the profiles agree or differ) 8. Data gaps & caveats 9. Sources.\n"
                + "Cite sources inline. End with a note that this is research support, not financial advice.";
        return llm.ask(system, user, false);
    }
}
