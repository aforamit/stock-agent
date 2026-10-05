package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/** Shared JSON helpers. */
final class Json {
    static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private Json() {}

    /** Pulls the first JSON object out of a model reply (tolerates fences and surrounding prose). */
    static JsonNode extract(String text) throws Exception {
        String t = text.replaceAll("```(?:json)?", "");
        int s = t.indexOf('{'), e = t.lastIndexOf('}');
        if (s < 0 || e < s) throw new IllegalArgumentException("No JSON found in model output");
        return MAPPER.readTree(t.substring(s, e + 1));
    }

    static String pretty(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception ex) {
            return String.valueOf(o);
        }
    }
}
