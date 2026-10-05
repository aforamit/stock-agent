package com.stockagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
