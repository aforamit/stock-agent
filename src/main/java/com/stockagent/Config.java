package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Thin wrapper over the YAML config. Access values with JSON pointers, e.g. cfg.dbl("/decision/min_roe"). */
final class Config {
    final JsonNode root;

    private Config(JsonNode root) {
        this.root = root;
    }

    /** Loads from a file path if it exists, otherwise from the bundled classpath config.yaml. */
    static Config load(String path) throws Exception {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        Path p = Path.of(path);
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                return new Config(yaml.readTree(in));
            }
        }
        try (InputStream in = Config.class.getResourceAsStream("/config.yaml")) {
            if (in == null) throw new IllegalStateException("config.yaml not found: " + path);
            return new Config(yaml.readTree(in));
        }
    }

    /** Risk profile name -> its decision thresholds, in config order. Falls back to a legacy "decision:" block. */
    Map<String, JsonNode> riskProfiles() {
        Map<String, JsonNode> out = new LinkedHashMap<>();
        root.path("risk_profiles").fields().forEachRemaining(e -> out.put(e.getKey(), e.getValue()));
        if (out.isEmpty()) out.put("default", root.path("decision"));
        return out;
    }

    /** The profile whose signal raises alerts: monitor.risk_profile, else the first one defined. */
    String activeProfile() {
        Map<String, JsonNode> profiles = riskProfiles();
        String p = root.at("/monitor/risk_profile").asText("");
        if (!p.isBlank() && !profiles.containsKey(p)) {
            throw new IllegalStateException("monitor.risk_profile '" + p + "' is not one of " + profiles.keySet());
        }
        return p.isBlank() ? profiles.keySet().iterator().next() : p;
    }

    JsonNode at(String ptr) {
        return root.at(ptr);
    }

    String str(String ptr) {
        return root.at(ptr).asText();
    }

    double dbl(String ptr) {
        return root.at(ptr).asDouble();
    }

    int intVal(String ptr, int dflt) {
        JsonNode n = root.at(ptr);
        return n.isMissingNode() || n.isNull() ? dflt : n.asInt();
    }

    boolean bool(String ptr, boolean dflt) {
        JsonNode n = root.at(ptr);
        return n.isMissingNode() || n.isNull() ? dflt : n.asBoolean();
    }
}
