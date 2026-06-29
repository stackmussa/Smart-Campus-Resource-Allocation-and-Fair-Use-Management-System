package com.scrafms.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Loads schema.sql from the classpath into SQL Server.
 * Tries JDBC TCP first; if that fails due to Browser/TCP being unavailable,
 * falls back to spawning sqlcmd.exe via ProcessBuilder (pure Java).
 */
public class SchemaLoader {

    public static void main(String[] args) {
        System.out.println("=== SCRAFMS SchemaLoader ===");
        try {
            String sql = readSqlFile();
            List<String> batches = splitOnGo(sql);
            System.out.println("Found " + batches.size() + " SQL batches.");

            boolean loaded = tryJdbc(batches);
            if (!loaded) {
                System.out.println("\n[INFO] JDBC unavailable (TCP/Browser not running), trying sqlcmd fallback...");
                loaded = trySqlCmd(sql);
            }

            if (loaded) {
                System.out.println("\n[SUCCESS] Schema loaded successfully.");
            } else {
                System.err.println("\n[FAILURE] All connection methods failed.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("\n[FAILURE] " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // -----------------------------------------------------------------------
    // JDBC path
    // -----------------------------------------------------------------------

    private static boolean tryJdbc(List<String> batches) {
        Properties props = loadDbProperties();
        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            System.out.println("[JDBC] Connected.");
            int n = 0;
            for (String batch : batches) {
                String t = batch.trim();
                if (t.isEmpty()) continue;
                n++;
                try {
                    stmt.execute(t);
                    System.out.println("  [OK] Batch " + n);
                } catch (SQLException e) {
                    System.err.println("  [ERR] Batch " + n + ": " + e.getMessage());
                    System.err.println("        SQL: " + t.substring(0, Math.min(100, t.length())).replace("\n", " "));
                    throw e;
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("[JDBC] Failed: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // sqlcmd fallback  (connects via named pipes — works even when TCP is off)
    // -----------------------------------------------------------------------

    private static boolean trySqlCmd(String sql) throws IOException, InterruptedException {
        Properties props = loadDbProperties();

        // Extract server/instance and database from the JDBC URL
        String url      = props.getProperty("db.url");
        String server   = extractServer(url);
        String database = extractDatabase(url);
        String user     = props.getProperty("db.user");
        String pass     = props.getProperty("db.password");

        // Locate sqlcmd.exe
        String sqlcmdExe = findSqlCmd();
        if (sqlcmdExe == null) {
            System.out.println("[SQLCMD] sqlcmd.exe not found on PATH or default locations.");
            return false;
        }
        System.out.println("[SQLCMD] Using: " + sqlcmdExe);
        System.out.println("[SQLCMD] Server: " + server);

        ProcessBuilder pb = new ProcessBuilder(
            sqlcmdExe,
            "-S", server,
            "-U", user,
            "-P", pass,
            "-C",          // trust server certificate
            "-b",          // exit on error
            "-i", "NUL"   // we pipe SQL via stdin, but need a dummy -i; override below
        );

        // Actually pipe via stdin – simpler than a temp file
        ProcessBuilder pb2 = new ProcessBuilder(
            sqlcmdExe,
            "-S", server,
            "-U", user,
            "-P", pass,
            "-d", database,
            "-C",
            "-b"
            // no -i: reads from stdin
        );
        pb2.redirectErrorStream(true);
        Process proc = pb2.start();

        // Write SQL to stdin
        try (OutputStream os = proc.getOutputStream()) {
            os.write(sql.getBytes(StandardCharsets.UTF_8));
        }

        // Read combined stdout+stderr
        StringBuilder output = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("  [SQLCMD] " + line);
                output.append(line).append("\n");
            }
        }

        int exitCode = proc.waitFor();
        if (exitCode == 0) {
            return true;
        }
        System.out.println("[SQLCMD] exited with code " + exitCode);
        return false;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String extractServer(String jdbcUrl) {
        // jdbc:sqlserver://HOST\\INSTANCE;...  →  HOST\INSTANCE
        String s = jdbcUrl.replace("jdbc:sqlserver://", "");
        int semi = s.indexOf(';');
        if (semi >= 0) s = s.substring(0, semi);
        // unescape double backslash
        return s.replace("\\\\", "\\");
    }

    private static String extractDatabase(String jdbcUrl) {
        // databaseName=scrafms_db  or  database=scrafms_db
        for (String token : jdbcUrl.split(";")) {
            String t = token.trim();
            if (t.toLowerCase().startsWith("databasename=") || t.toLowerCase().startsWith("database=")) {
                int eq = t.indexOf('=');
                if (eq >= 0) return t.substring(eq + 1).trim();
            }
        }
        return "scrafms_db";
    }

    private static String findSqlCmd() {
        // Common install locations
        String[] candidates = {
            "sqlcmd",
            "C:\\Program Files\\Microsoft SQL Server\\Client SDK\\ODBC\\170\\Tools\\Binn\\sqlcmd.exe",
            "C:\\Program Files\\Microsoft SQL Server\\Client SDK\\ODBC\\180\\Tools\\Binn\\sqlcmd.exe",
            "C:\\Program Files (x86)\\Microsoft SQL Server\\Client SDK\\ODBC\\170\\Tools\\Binn\\sqlcmd.exe",
            "C:\\Program Files (x86)\\Microsoft SQL Server\\Client SDK\\ODBC\\180\\Tools\\Binn\\sqlcmd.exe"
        };
        for (String c : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(c, "-?");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                p.getInputStream().transferTo(OutputStream.nullOutputStream());
                p.waitFor();
                return c;
            } catch (Exception ignore) {}
        }
        return null;
    }

    private static Properties loadDbProperties() {
        Properties props = new Properties();
        try (InputStream in = SchemaLoader.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new RuntimeException("db.properties not found");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
        return props;
    }

    private static String readSqlFile() throws IOException {
        try (InputStream in = SchemaLoader.class.getClassLoader().getResourceAsStream("sql/schema.sql")) {
            if (in == null) throw new IOException("sql/schema.sql not found on classpath");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        }
    }

    public static void loadSchema() throws Exception {
        String sql = readSqlFile();
        List<String> batches = splitOnGo(sql);
        if (!tryJdbc(batches)) {
            if (!trySqlCmd(sql)) {
                throw new RuntimeException("All connection methods exhausted. Schema not loaded.");
            }
        }
    }

    private static List<String> splitOnGo(String sql) {
        List<String> batches = new ArrayList<>();
        String[] parts = sql.split("(?im)^\\s*GO\\s*$");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) batches.add(trimmed);
        }
        return batches;
    }
}
