package Game.security;

import Game.core.Manager;
import Game.io.Message;
import Game.io.Session;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class ClientAttestationService {
    public static final byte COMMAND = -109;
    private static final long MAX_CLOCK_SKEW_MS = 120_000L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ClientAttestationService() {
    }

    public static void sendChallenge(Session session) throws IOException {
        byte[] nonce = new byte[32];
        SECURE_RANDOM.nextBytes(nonce);
        session.attestationNonce = nonce;
        session.attestationIssuedAt = System.currentTimeMillis();

        Message challenge = new Message(COMMAND);
        challenge.writer().writeByte(0);
        challenge.writer().writeInt(Manager.gI().unityProtocol);
        challenge.writer().writeUTF(Base64.getEncoder().encodeToString(nonce));
        challenge.writer().writeLong(session.attestationIssuedAt);
        session.addmsg(challenge);
    }

    public static void processResponse(Session session, Message message) throws IOException {
        int operation = message.reader().readUnsignedByte();
        if (operation == 2) {
            if (session.getSessionState() != SessionState.KEY_EXCHANGED
                    || Manager.gI().unityBuildSecrets.isEmpty()) {
                reject(session, "Unity challenge requested while no builds are configured");
                return;
            }
            sendChallenge(session);
            return;
        }
        if (operation != 1 || session.attestationNonce == null) {
            reject(session, "attestation response without an active challenge");
            return;
        }

        int protocol = message.reader().readInt();
        String buildId = bounded(message.reader().readUTF(), 64, "build id");
        String platform = bounded(message.reader().readUTF(), 32, "platform");
        String packageName = bounded(message.reader().readUTF(), 128, "package name");
        String manifestHash = bounded(message.reader().readUTF(), 128, "manifest hash");
        long clientTimestamp = message.reader().readLong();
        String suppliedMac = bounded(message.reader().readUTF(), 128, "signature");
        long now = System.currentTimeMillis();

        String secret = Manager.gI().unityBuildSecrets.get(buildId);
        boolean metadataValid = protocol == Manager.gI().unityProtocol
                && Manager.gI().unityPackages.contains(packageName)
                && secret != null
                && !secret.isBlank()
                && now - session.attestationIssuedAt <= MAX_CLOCK_SKEW_MS
                && Math.abs(now - clientTimestamp) <= MAX_CLOCK_SKEW_MS;

        String nonce = Base64.getEncoder().encodeToString(session.attestationNonce);
        String canonical = protocol + "|" + buildId + "|" + platform + "|" + packageName + "|"
                + manifestHash + "|" + clientTimestamp + "|" + nonce;
        byte[] expected = metadataValid ? hmac(secret, canonical) : new byte[32];
        byte[] supplied = decodeHex(suppliedMac);

        session.attestationNonce = null;
        if (!metadataValid || supplied == null || !MessageDigest.isEqual(expected, supplied)) {
            reject(session, "invalid Unity build attestation: " + buildId + "/" + packageName);
            return;
        }

        session.clientBuildId = buildId;
        session.clientPlatform = platform;
        session.clientPackageName = packageName;
        session.unityAttested = true;
        session.setSessionState(SessionState.ATTESTED);
    }

    private static byte[] hmac(String configuredSecret, String canonical) throws IOException {
        try {
            byte[] key = configuredSecret.startsWith("b64:")
                    ? Base64.getDecoder().decode(configuredSecret.substring(4))
                    : configuredSecret.getBytes(StandardCharsets.UTF_8);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IOException("Cannot verify Unity client signature", e);
        }
    }

    private static byte[] decodeHex(String value) {
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String bounded(String value, int maxLength, String field) throws IOException {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty() || result.length() > maxLength) {
            throw new IOException("Invalid " + field);
        }
        return result;
    }

    private static void reject(Session session, String reason) throws IOException {
        AntiCheatService.gI().record(session, ViolationType.CLIENT_ATTESTATION, 10, reason);
        session.sendLoginNotice("Phien ban game khong hop le. Hay cap nhat ban Unity moi nhat.");
        session.close();
    }
}
