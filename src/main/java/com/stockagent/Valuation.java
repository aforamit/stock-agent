package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stockagent.Models.Collected;
import com.stockagent.Models.Decision;
import com.stockagent.Models.MarketData;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/** Agent 3: quant/valuation (pure Java, no LLM arithmetic) plus the rule-based decision engine. */
final class Valuation {
    private Valuation() {}

    static double dcf(double fcf0, double growth, double discount, double terminalG, int years, boolean fade) {
        double fcf = fcf0, pv = 0;
        for (int y = 1; y <= years; y++) {
            double g = fade ? growth + (terminalG - growth) * (y - 1) / Math.max(years - 1, 1) : growth;
            fcf *= 1 + g;
            pv += fcf / Math.pow(1 + discount, y);
        }
        double terminal = fcf * (1 + terminalG) / (discount - terminalG);
        return pv + terminal / Math.pow(1 + discount, years);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    static ObjectNode compute(Config cfg, Collected data, JsonNode filings) {
        MarketData md = data.marketData();
        ObjectNode res = Json.MAPPER.createObjectNode();
        res.put("reliable", false);
        var notes = res.putArray("notes");
        var scenarios = res.putObject("scenarios");

        TreeMap<String, Double> hist = md.history().get("free_cash_flow");
        if (hist == null || hist.isEmpty()) {
            notes.add("No free cash flow history available.");
            return res;
        }
        var vals = hist.values().stream().toList();
        var last3 = vals.subList(Math.max(0, vals.size() - 3), vals.size());
        double fcf0 = last3.stream().mapToDouble(Double::doubleValue).average().orElse(0); // smooths cyclicality
        res.put("base_fcf_used", fcf0);
        res.set("fcf_history", Json.MAPPER.valueToTree(hist));

        if (fcf0 <= 0) {
            notes.add("Average FCF <= 0: DCF not meaningful (also true for banks/insurers - use P/B & ROE instead).");
            return res;
        }
        if (md.shares() == null || md.price() == null) {
            notes.add("Missing share count or price.");
            return res;
        }

        double netDebt = (md.totalDebt() == null ? 0 : md.totalDebt()) - (md.totalCash() == null ? 0 : md.totalCash());
        double floor = cfg.dbl("/valuation/growth_floor"), cap = cfg.dbl("/valuation/growth_cap");
        JsonNode g = filings.path("recommended_base_growth_rate");
        double llmGrowth;
        if (g.isNumber()) {
            llmGrowth = g.asDouble();
        } else {
            llmGrowth = 0.05;
            notes.add("Filings analyst gave no growth estimate; defaulted to 5%.");
        }
        double baseGrowth = clamp(llmGrowth, floor, cap);
        int years = cfg.intVal("/valuation/projection_years", 10);
        boolean fade = cfg.bool("/valuation/growth_fade", true);

        Iterator<Map.Entry<String, JsonNode>> it = cfg.at("/valuation/scenarios").fields();
        while (it.hasNext()) {
            var e = it.next();
            JsonNode s = e.getValue();
            double growth = clamp(baseGrowth + s.path("growth_offset").asDouble(), floor, cap);
            double disc = s.path("discount_rate").asDouble(), tg = s.path("terminal_growth").asDouble();
            if (disc <= tg) {
                notes.add(e.getKey() + ": discount <= terminal growth, skipped.");
                continue;
            }
            double ev = dcf(fcf0, growth, disc, tg, years, fade);
            double perShare = (ev - netDebt) / md.shares();
            ObjectNode sc = scenarios.putObject(e.getKey());
            sc.put("growth", growth);
            sc.put("discount_rate", disc);
            sc.put("terminal_growth", tg);
            sc.put("intrinsic_value_per_share", perShare);
            sc.put("upside_vs_price", perShare / md.price() - 1);
        }
        res.put("price", md.price());
        if (md.currency() != null) res.put("currency", md.currency());
        ObjectNode mult = res.putObject("multiples");
        if (md.trailingPe() != null) mult.put("trailing_pe", md.trailingPe());
        if (md.forwardPe() != null) mult.put("forward_pe", md.forwardPe());
        if (md.evToEbitda() != null) mult.put("ev_to_ebitda", md.evToEbitda());
        if (md.priceToBook() != null) mult.put("price_to_book", md.priceToBook());
        res.put("reliable", scenarios.has("base"));
        return res;
    }

    /** Final call is made here, in code, using thresholds from config.yaml. */
    static Decision decide(Config cfg, Collected data, JsonNode valuation, JsonNode skeptic, JsonNode filings) {
        MarketData md = data.marketData();
        ObjectNode checks = Json.MAPPER.createObjectNode();

        if (!valuation.path("reliable").asBoolean(false)) {
            checks.put("valuation_reliable", false);
            checks.set("notes", valuation.path("notes"));
            return new Decision("AVOID / INSUFFICIENT DATA", checks);
        }

        double price = valuation.path("price").asDouble();
        double base = valuation.at("/scenarios/base/intrinsic_value_per_share").asDouble();
        JsonNode bearNode = valuation.at("/scenarios/bear/intrinsic_value_per_share");
        double bear = bearNode.isNumber() ? bearNode.asDouble() : base;

        double mos = cfg.dbl("/decision/margin_of_safety");
        boolean mosOk = price <= base * (1 - mos);
        checks.put("margin_of_safety_ok", mosOk);
        checks.put("margin_of_safety_actual", 1 - price / base);
        boolean bearOk = bear >= price * (1 - cfg.dbl("/decision/max_bear_downside"));
        checks.put("bear_not_catastrophic", bearOk);

        boolean debtOk = md.debtToEquity() == null || md.debtToEquity() <= cfg.dbl("/decision/max_debt_to_equity");
        boolean roeOk = md.roe() == null || md.roe() >= cfg.dbl("/decision/min_roe");
        checks.put("debt_ok", debtOk);
        checks.put("roe_ok", roeOk);

        boolean cashOk = true;
        TreeMap<String, Double> ni = md.history().get("net_income");
        TreeMap<String, Double> fcf = md.history().get("free_cash_flow");
        if (ni != null && fcf != null && !ni.isEmpty()) {
            String latest = ni.lastKey();
            double n = ni.get(latest);
            if (n > 0 && fcf.containsKey(latest)) {
                double conv = fcf.get(latest) / n;
                checks.put("fcf_conversion", conv);
                cashOk = conv >= cfg.dbl("/decision/min_fcf_conversion");
            }
        }
        checks.put("cash_conversion_ok", cashOk);

        String severity = skeptic.path("severity").asText("none");
        boolean skepticOk = !severity.equals(cfg.str("/decision/block_on_skeptic_severity"));
        checks.put("skeptic_severity", severity);
        checks.put("skeptic_ok", skepticOk);
        checks.put("accounting_red_flags", filings.path("accounting_red_flags").size());

        boolean qualityOk = debtOk && roeOk && cashOk;
        String rec;
        if (!skepticOk) rec = "AVOID";
        else if (mosOk && bearOk && qualityOk) rec = "INVEST (long term)";
        else if (qualityOk) rec = "WATCHLIST";
        else rec = "AVOID";
        return new Decision(rec, checks);
    }
}
