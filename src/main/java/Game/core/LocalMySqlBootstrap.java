package Game.core;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LocalMySqlBootstrap {

    private LocalMySqlBootstrap() {
    }

    public static boolean isDebuggerAttached() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .anyMatch(arg -> arg.contains("jdwp"));
    }

    public static void ensureAvailable(String host, int port, boolean autoStartEnabled, String mysqlBinPath,
                                       String mysqlIniPath, int timeoutMs) {
        if (!autoStartEnabled || !isWindows() || !isLocalHost(host) || isPortOpen(host, port, 500)) {
            return;
        }

        final Path scriptPath = Path.of("scripts", "start-local-mysql3307.ps1").toAbsolutePath();
        if (!Files.isRegularFile(scriptPath)) {
            System.err.println("mysql auto-start script not found: " + scriptPath);
            return;
        }
        if (!Files.isRegularFile(Path.of(mysqlBinPath))) {
            System.err.println("mysql auto-start binary not found: " + mysqlBinPath);
            return;
        }
        if (!Files.isRegularFile(Path.of(mysqlIniPath))) {
            System.err.println("mysql auto-start ini not found: " + mysqlIniPath);
            return;
        }

        try {
            System.out.println("mysql auto-start: booting local MySQL on " + host + ":" + port);
            final Path logDir = Path.of("log_server");
            Files.createDirectories(logDir);
            final Path logFile = logDir.resolve("mysql-autostart.log");
            final ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe",
                    "-ExecutionPolicy", "Bypass",
                    "-File", scriptPath.toString(),
                    "-MySqlBin", mysqlBinPath,
                    "-MyIni", mysqlIniPath,
                    "-DbHost", host,
                    "-Port", Integer.toString(port)
            );
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
            final Process process = builder.start();
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("mysql auto-start script exited with code " + exitCode);
                return;
            }
            waitForPort(host, port, timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("mysql auto-start interrupted");
        } catch (IOException e) {
            System.err.println("mysql auto-start failed: " + e.getMessage());
        }
    }

    private static void waitForPort(String host, int port, int timeoutMs) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isPortOpen(host, port, 1000)) {
                System.out.println("mysql auto-start: local MySQL is ready on " + host + ":" + port);
                return;
            }
            Thread.sleep(500L);
        }
        System.err.println("mysql auto-start timed out waiting for " + host + ":" + port);
    }

    private static boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static boolean isLocalHost(String host) {
        return "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host) || "0.0.0.0".equals(host);
    }
}
