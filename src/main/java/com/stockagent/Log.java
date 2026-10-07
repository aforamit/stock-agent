package com.stockagent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Run log: console messages plus the full LLM prompts and replies, one timestamped file per run. */
final class Log {
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter LINE_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static BufferedWriter out;
    private static Path file;

    private Log() {}

    /** Opens <dir>/<name>_<timestamp>.log. Until this is called, messages go to the console only. */
    static synchronized void init(String dir, String name) throws IOException {
        Path d = Path.of(dir);
        Files.createDirectories(d);
        file = d.resolve(name.replace('.', '_') + "_" + LocalDateTime.now().format(FILE_TS) + ".log");
        out = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
    }

    static synchronized Path file() {
        return file;
    }

    /** Console and log file. */
    static void info(String msg) {
        System.out.println(msg);
        write("INFO", msg.strip());
    }

    /** A BUY/SELL signal change: console, run log, and alerts.log (one file across all runs). */
    static void alert(String msg) {
        System.out.println("*** ALERT " + msg);
        write("ALERT", msg);
        Path dir;
        synchronized (Log.class) {
            dir = file == null ? null : file.getParent();
        }
        if (dir == null) return;
        try {
            Files.writeString(dir.resolve("alerts.log"),
                    LocalDateTime.now().format(LINE_TS) + " " + msg + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Alert write failed: " + e.getMessage());
        }
    }

    /** Log file only: for long content such as prompts and model replies. */
    static void detail(String title, String body) {
        write("DEBUG", title + "\n" + body);
    }

    static void error(String msg, Throwable t) {
        StringWriter sw = new StringWriter();
        if (t != null) t.printStackTrace(new PrintWriter(sw));
        write("ERROR", msg + (t == null ? "" : "\n" + sw));
    }

    private static synchronized void write(String level, String msg) {
        if (out == null) return;
        try {
            out.write(LocalDateTime.now().format(LINE_TS) + " [" + level + "] " + msg);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            System.err.println("Log write failed: " + e.getMessage());
        }
    }
}
