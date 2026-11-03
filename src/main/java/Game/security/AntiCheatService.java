package Game.security;

import Game.io.Session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class AntiCheatService {
    private static final AntiCheatService INSTANCE = new AntiCheatService();
    private static final Path LOG_PATH = Path.of("log", "anticheat-events.tsv");
    private final Object logLock = new Object();

    private AntiCheatService() {
    }

    public static AntiCheatService gI() {
        return INSTANCE;
    }

    public int record(Session session, ViolationType type, int score, String detail) {
        int total = session.addAntiCheatScore(Math.max(1, score));
        String player = session.p == null ? "" : session.p.name;
        String line = String.join("\t",
                Instant.now().toString(),
                clean(session.ip),
                clean(session.user),
                clean(player),
                clean(session.clientBuildId),
                type.name(),
                Integer.toString(score),
                Integer.toString(total),
                clean(detail)) + System.lineSeparator();
        synchronized (logLock) {
            try {
                Files.createDirectories(LOG_PATH.getParent());
                Files.writeString(LOG_PATH, line, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Cannot write anti-cheat log: " + e.getMessage());
            }
        }
        System.out.println("ANTICHEAT " + type + " user=" + session.user + " score=" + total + " " + clean(detail));
        return total;
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }
}
