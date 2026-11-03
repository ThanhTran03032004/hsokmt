package Game.security;

import java.util.HashMap;
import java.util.Map;

public final class PacketRateLimiter {
    private static final long WINDOW_MS = 1_000L;
    private final Map<Byte, Counter> commandCounters = new HashMap<>();
    private long globalWindowStartedAt = System.currentTimeMillis();
    private int globalCount;
    private long resourceWindowStartedAt = System.currentTimeMillis();
    private int resourceCount;

    public synchronized boolean allow(byte command, boolean authenticated, int globalLimit) {
        long now = System.currentTimeMillis();
        boolean resourceRequest = authenticated && isResourceRequest(command);
        if (resourceRequest) {
            if (now - resourceWindowStartedAt >= WINDOW_MS) {
                resourceWindowStartedAt = now;
                resourceCount = 0;
            }
            if (++resourceCount > Math.max(globalLimit, 600)) {
                return false;
            }
        } else {
            if (now - globalWindowStartedAt >= WINDOW_MS) {
                globalWindowStartedAt = now;
                globalCount = 0;
            }
            int effectiveGlobalLimit = authenticated ? globalLimit : Math.min(globalLimit, 30);
            if (++globalCount > effectiveGlobalLimit) {
                return false;
            }
        }

        int commandLimit = commandLimit(command);
        Counter counter = commandCounters.computeIfAbsent(command, ignored -> new Counter(now));
        if (now - counter.windowStartedAt >= WINDOW_MS) {
            counter.windowStartedAt = now;
            counter.count = 0;
        }
        return ++counter.count <= commandLimit;
    }

    private int commandLimit(byte command) {
        return switch (command) {
            case 1, 13, 14, -109 -> 5;
            case 61 -> 2;
            case 4 -> 35;
            case 5, 6 -> 25;
            case 7 -> 200;
            case -51 -> 512;
            case -52 -> 256;
            case 53 -> 8;
            default -> 60;
        };
    }

    private boolean isResourceRequest(byte command) {
        return command == -51 || command == -52 || command == 7;
    }

    private static final class Counter {
        private long windowStartedAt;
        private int count;

        private Counter(long windowStartedAt) {
            this.windowStartedAt = windowStartedAt;
        }
    }
}
