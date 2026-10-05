package com.stockagent;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Plain data carriers. */
final class Models {
    private Models() {}

    record Doc(String name, String url, String text, String error) {
        static Doc ok(String name, String url, String text) {
            return new Doc(name, url, text, null);
        }

        static Doc failed(String name, String url, String error) {
            return new Doc(name, url, "", error);
        }
    }

    /** history: metric name -> (period end date -> value), sorted by date. */
    record MarketData(
            String ticker, String company, String currency, Double price, Double shares, Double marketCap,
            Double totalDebt, Double totalCash, Double trailingPe, Double forwardPe, Double evToEbitda,
            Double priceToBook, Double roe, Double debtToEquity, String sector, String industry,
            Map<String, TreeMap<String, Double>> history) {}

    record Collected(String market, MarketData marketData, List<Doc> documents) {}

    record Decision(String recommendation, com.fasterxml.jackson.databind.node.ObjectNode checks) {}
}
