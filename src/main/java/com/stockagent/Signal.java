package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a stored analysis plus today's price into BUY / SELL / HOLD / WATCH / AVOID. Pure code, no LLM,
 * so the cheap check can re-evaluate it as often as needed.
 */
final class Signal {
    record Result(String signal, String reason) {}

    private Signal() {}

    static boolean isAction(String signal) {
        return "BUY".equals(signal) || "SELL".equals(signal);
    }

    /** @param previous the last confirmed signal (may be null); used to widen the band so a price on the line does not flip daily */
    static Result evaluate(Config cfg, JsonNode analysis, double price, boolean held, String previous) {
        JsonNode v = analysis.path("valuation"), c = analysis.path("checks");
        if (!v.path("reliable").asBoolean(false)) {
            return new Result(held ? "HOLD" : "AVOID", "no reliable valuation: " + v.path("notes"));
        }
        double base = v.path("base").asDouble();
        double bear = v.path("bear").isNumber() ? v.path("bear").asDouble() : base;
        double mos = cfg.dbl("/decision/margin_of_safety");
        double band = cfg.at("/monitor/hysteresis").asDouble(0.05);
        double buyBelow = base * (1 - mos + ("BUY".equals(previous) ? band : 0));
        double sellAbove = base * (1 + cfg.at("/monitor/sell_above_base").asDouble(0.10) - ("SELL".equals(previous) ? band : 0));
        String levels = String.format("price %.2f, buy below %.2f, base value %.2f, sell above %.2f", price, buyBelow, base, sellAbove);

        List<String> broken = new ArrayList<>();
        if (!c.path("skeptic_ok").asBoolean(true)) broken.add("skeptic severity " + c.path("skeptic_severity").asText());
        if (!c.path("debt_ok").asBoolean(true)) broken.add("debt too high");
        if (!c.path("roe_ok").asBoolean(true)) broken.add("ROE too low");
        if (!c.path("cash_conversion_ok").asBoolean(true)) broken.add("weak cash conversion");
        if (!broken.isEmpty()) {
            String why = "thesis checks failing (" + String.join(", ", broken) + "); " + levels;
            if (held && cfg.bool("/monitor/sell_on_thesis_break", true)) return new Result("SELL", why);
            return new Result(held ? "HOLD" : "AVOID", why);
        }

        if (held && price >= sellAbove) return new Result("SELL", "price above value; " + levels);
        if (price <= buyBelow) {
            if (bear >= price * (1 - cfg.dbl("/decision/max_bear_downside"))) {
                return new Result("BUY", "margin of safety met; " + levels);
            }
            return new Result(held ? "HOLD" : "WATCH", String.format("cheap but bear value %.2f is too far below; %s", bear, levels));
        }
        return new Result(held ? "HOLD" : "WATCH", levels);
    }
}
