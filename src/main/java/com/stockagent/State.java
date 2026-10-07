package com.stockagent;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

/** What the monitor remembers between runs: one JSON file per ticker, plus a CSV history of every signal. */
final class State {
    private static final String CSV_HEADER = "timestamp,ticker,tier,price,bear,base,bull,recommendation,signal,status,reason";

    private State() {}

    private static Path file(String dir, String ticker) {
        return Path.of(dir, ticker.replace('.', '_') + ".json");
    }

    static ObjectNode load(String dir, String ticker) throws IOException {
        Path p = file(dir, ticker);
        if (!Files.exists(p)) return Json.MAPPER.createObjectNode().put("ticker", ticker);
        return (ObjectNode) Json.MAPPER.readTree(Files.readString(p, StandardCharsets.UTF_8));
    }

    static void save(String dir, String ticker, ObjectNode state) throws IOException {
        Files.createDirectories(Path.of(dir));
        Files.writeString(file(dir, ticker), Json.pretty(state), StandardCharsets.UTF_8);
    }

    /** Appends one row to signals.csv, the record used later to judge how good the signals were. */
    static void appendSignal(String dir, Object... cols) throws IOException {
        Files.createDirectories(Path.of(dir));
        Path p = Path.of(dir, "signals.csv");
        String row = Arrays.stream(cols)
                .map(c -> c == null ? "" : "\"" + String.valueOf(c).replace("\"", "\"\"") + "\"")
                .collect(Collectors.joining(","));
        String text = (Files.exists(p) ? "" : CSV_HEADER + System.lineSeparator()) + row + System.lineSeparator();
        Files.writeString(p, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
