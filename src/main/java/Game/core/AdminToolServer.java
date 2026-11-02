package Game.core;

import Game.activities.ChiemThanhManager;
import Game.activities.ChienTruong;
import Game.client.Player;
import Game.io.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class AdminToolServer {
    private static AdminToolServer instance;
    private static final ObjectMapper JSON = new ObjectMapper();
    private final FakePlayerBotManager fakeBots = FakePlayerBotManager.gI();
    private HttpServer server;
    private boolean started;

    public static AdminToolServer gI() {
        if (instance == null) {
            instance = new AdminToolServer();
        }
        return instance;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        try {
            Manager manager = Manager.gI();
            InetSocketAddress bind = new InetSocketAddress(manager.admin_tool_bind, manager.admin_tool_port);
            server = HttpServer.create(bind, 0);
            server.createContext("/admin", this::handleAdminPage);
            server.createContext("/api/status", exchange -> handleApi(exchange, this::status));
            server.createContext("/api/connections", exchange -> handleApi(exchange, this::connections));
            server.createContext("/api/giftcode", exchange -> handleApi(exchange, this::createGiftCode));
            server.createContext("/api/bots", exchange -> handleApi(exchange, this::setBots));
            server.createContext("/api/maintenance", exchange -> handleApi(exchange, this::maintenance));
            server.createContext("/api/debug", exchange -> handleApi(exchange, this::debug));
            server.createContext("/api/battlefield/open", exchange -> handleApi(exchange, this::openBattlefield));
            server.createContext("/api/battlefield/start", exchange -> handleApi(exchange, this::startBattlefield));
            server.createContext("/api/castle/open", exchange -> handleApi(exchange, this::openCastle));
            server.createContext("/api/ban-player", exchange -> handleApi(exchange, this::banPlayer));
            server.createContext("/api/login-limit", exchange -> handleApi(exchange, this::setLoginLimit));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            started = true;
            System.out.println("Admin tool: http://" + manager.admin_tool_bind + ":" + manager.admin_tool_port + "/admin");
        } catch (IOException e) {
            System.err.println("Admin tool start failed: " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        started = false;
    }

    public int getFakeBotCount() {
        return fakeBots.getCount();
    }

    public void sendFakeBotPresence(Player viewer) {
        fakeBots.sendPresence(viewer);
    }

    public void sendFakeBotPresenceOnMove(Player viewer) {
        fakeBots.sendPresenceOnMove(viewer);
    }

    public boolean sendFakeBotInfo(Player viewer, int id) throws IOException {
        return fakeBots.sendInfo(viewer, id);
    }

    public boolean isFakeBotPlayer(Player player) {
        return fakeBots.isFakeBot(player);
    }

    public Player findFakeBotByName(String name) {
        return fakeBots.findBotPlayer(name);
    }

    public Player findFakeBotById(int id) {
        return fakeBots.findBotPlayer(id);
    }

    private void handleAdminPage(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method not allowed");
            return;
        }
        byte[] data = adminHtml().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private void handleApi(HttpExchange exchange, ApiHandler handler) throws IOException {
        try {
            if (!isAuthorized(exchange)) {
                sendJson(exchange, 401, error("bad token"));
                return;
            }
            Map<String, Object> body = readJsonBody(exchange);
            sendJson(exchange, 200, handler.handle(exchange, body));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, error(e.getMessage()));
        }
    }

    private boolean isAuthorized(HttpExchange exchange) {
        String expected = Manager.gI().admin_tool_token;
        if (expected == null || expected.isBlank()) {
            return false;
        }
        String token = exchange.getRequestHeaders().getFirst("X-Admin-Token");
        if (token == null || token.isBlank()) {
            token = query(exchange).get("token");
        }
        return expected.equals(token);
    }

    private Map<String, Object> status(HttpExchange exchange, Map<String, Object> body) {
        Map<String, Object> result = ok();
        result.put("maintenance", Manager.gI().isServerAdmin);
        result.put("debug", Manager.gI().debug);
        result.put("logErrorLogin", Manager.logErrorLogin);
        result.put("loginLimitPerIp", Manager.gI().login_limit_per_ip);
        result.put("sessions", Session.client_entry.size());
        result.put("onlinePlayers", SessionManager.countOnlinePlayers());
        result.put("fakeBots", getFakeBotCount());
        result.put("shownOnline", SessionManager.countOnlinePlayers() + getFakeBotCount());
        result.put("botMapId", fakeBots.getMapId());
        result.put("botZoneId", fakeBots.getZoneId());
        result.put("botX", fakeBots.getBaseX());
        result.put("botY", fakeBots.getBaseY());
        result.put("botAutoDistribute", fakeBots.isAutoDistribute());
        result.put("botFarmers", fakeBots.getFarmerCount());
        result.put("botSellers", fakeBots.getSellerCount());
        result.put("botVip", fakeBots.getVipCount());
        result.put("botBossHunters", fakeBots.getBossHunterCount());
        result.put("battlefieldRunning", ChienTruong.running);
        result.put("battlefieldStatus", ChienTruong.gI().getStatus());
        result.put("castleRegister", ChiemThanhManager.isRegister);
        result.put("castleAttackSeconds", Math.max(0L, (ChiemThanhManager.timeAttack - System.currentTimeMillis()) / 1000L));
        result.put("connectionsByIp", SessionManager.countConnectionsByIp());
        return result;
    }

    private Map<String, Object> connections(HttpExchange exchange, Map<String, Object> body) {
        return status(exchange, body);
    }

    private Map<String, Object> createGiftCode(HttpExchange exchange, Map<String, Object> body) throws SQLException, IOException {
        requirePost(exchange);
        String code = stringValue(body, "code", "").trim().toLowerCase(Locale.ROOT);
        if (!code.matches("[a-zA-Z0-9_\\-]{3,40}")) {
            throw new IllegalArgumentException("code must be 3-40 chars: a-z, 0-9, _, -");
        }

        int emptyBox = intValue(body, "emptyBox", 0);
        int limit = intValue(body, "limit", 1);
        int date = intValue(body, "date", 0);
        int level = intValue(body, "level", 1);
        int needActive = intValue(body, "needActive", 1);
        long vang = longValue(body, "vang", 0L);
        int ngoc = intValue(body, "ngoc", 0);
        String giftFor = stringValue(body, "giftFor", "");
        String item3 = normalizeGiftItems(body.get("item3"));
        String item4 = normalizeGiftItems(body.get("item4"));
        String item7 = normalizeGiftItems(body.get("item7"));

        String sql = "INSERT INTO `giftcode` (`giftname`,`empty_box`,`limit`,`date`,`gift_for`,`level`,`needActive`,`item3`,`item4`,`item7`,`vang`,`ngoc`) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = SQL.gI().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, emptyBox);
            ps.setInt(3, limit);
            ps.setInt(4, date);
            ps.setString(5, giftFor);
            ps.setInt(6, level);
            ps.setInt(7, needActive);
            ps.setString(8, item3);
            ps.setString(9, item4);
            ps.setString(10, item7);
            ps.setLong(11, vang);
            ps.setInt(12, ngoc);
            ps.executeUpdate();
            connection.commit();
        }

        Map<String, Object> result = ok();
        result.put("code", code);
        return result;
    }

    private Map<String, Object> setBots(HttpExchange exchange, Map<String, Object> body) {
        requirePost(exchange);
        boolean enabled = boolValue(body, "enabled", true);
        int count = Math.max(0, Math.min(5000, intValue(body, "count", fakeBots.getCount())));
        String prefix = stringValue(body, "prefix", fakeBots.getPrefix()).trim();
        int mapId = intValue(body, "mapId", fakeBots.getMapId());
        int zoneId = intValue(body, "zoneId", fakeBots.getZoneId());
        int x = intValue(body, "x", fakeBots.getBaseX());
        int y = intValue(body, "y", fakeBots.getBaseY());
        boolean autoDistribute = boolValue(body, "autoDistribute", fakeBots.isAutoDistribute());
        if (!autoDistribute) {
            Game.map.Map[] maps = Game.map.Map.get_map_by_id(mapId);
            if (maps == null) {
                throw new IllegalArgumentException("mapId not found");
            }
            if (zoneId < 0 || zoneId >= maps.length) {
                throw new IllegalArgumentException("zoneId out of range: 0-" + (maps.length - 1));
            }
        }
        fakeBots.configure(enabled, count, prefix, mapId, zoneId, x, y, autoDistribute);
        Map<String, Object> result = ok();
        result.put("fakeBots", getFakeBotCount());
        result.put("prefix", fakeBots.getPrefix());
        result.put("mapId", fakeBots.getMapId());
        result.put("zoneId", fakeBots.getZoneId());
        result.put("x", fakeBots.getBaseX());
        result.put("y", fakeBots.getBaseY());
        result.put("autoDistribute", fakeBots.isAutoDistribute());
        result.put("farmers", fakeBots.getFarmerCount());
        result.put("sellers", fakeBots.getSellerCount());
        result.put("vip", fakeBots.getVipCount());
        result.put("bossHunters", fakeBots.getBossHunterCount());
        return result;
    }

    private Map<String, Object> maintenance(HttpExchange exchange, Map<String, Object> body) {
        requirePost(exchange);
        boolean enabled = boolValue(body, "enabled", !Manager.gI().isServerAdmin);
        boolean kick = boolValue(body, "kick", false);
        Manager.gI().isServerAdmin = enabled;
        int kicked = enabled && kick ? kickNonAdminSessions() : 0;
        Map<String, Object> result = ok();
        result.put("maintenance", enabled);
        result.put("kicked", kicked);
        return result;
    }

    private Map<String, Object> debug(HttpExchange exchange, Map<String, Object> body) {
        requirePost(exchange);
        Manager.gI().debug = boolValue(body, "debug", Manager.gI().debug);
        Manager.logErrorLogin = boolValue(body, "logErrorLogin", Manager.logErrorLogin);
        Map<String, Object> result = ok();
        result.put("debug", Manager.gI().debug);
        result.put("logErrorLogin", Manager.logErrorLogin);
        return result;
    }

    private Map<String, Object> openBattlefield(HttpExchange exchange, Map<String, Object> body) throws IOException {
        requirePost(exchange);
        ChienTruong.running = true;
        ChienTruong.gI().open_register();
        Map<String, Object> result = ok();
        result.put("battlefieldRunning", ChienTruong.running);
        result.put("battlefieldStatus", ChienTruong.gI().getStatus());
        return result;
    }

    private Map<String, Object> startBattlefield(HttpExchange exchange, Map<String, Object> body) throws IOException {
        requirePost(exchange);
        ChienTruong.gI().start_now();
        Map<String, Object> result = ok();
        result.put("battlefieldRunning", ChienTruong.running);
        result.put("battlefieldStatus", ChienTruong.gI().getStatus());
        return result;
    }

    private Map<String, Object> openCastle(HttpExchange exchange, Map<String, Object> body) {
        requirePost(exchange);
        String mode = stringValue(body, "mode", "register").toLowerCase(Locale.ROOT);
        if ("attack".equals(mode)) {
            if (!ChiemThanhManager.isRegister) {
                ChiemThanhManager.StartRegister();
            }
            ChiemThanhManager.EndRegister();
        } else if ("close".equals(mode)) {
            if (ChiemThanhManager.timeAttack > System.currentTimeMillis()) {
                ChiemThanhManager.EndChiemThanh();
            } else {
                ChiemThanhManager.isRegister = false;
                Manager.ResetCThanh();
            }
        } else {
            ChiemThanhManager.StartRegister();
        }
        Map<String, Object> result = ok();
        result.put("castleRegister", ChiemThanhManager.isRegister);
        result.put("castleAttackSeconds", Math.max(0L, (ChiemThanhManager.timeAttack - System.currentTimeMillis()) / 1000L));
        return result;
    }

    private Map<String, Object> banPlayer(HttpExchange exchange, Map<String, Object> body) throws SQLException {
        requirePost(exchange);
        int playerId = intValue(body, "playerId", -1);
        if (playerId < 0) {
            throw new IllegalArgumentException("playerId is required");
        }
        boolean ban = boolValue(body, "ban", true);
        boolean kick = boolValue(body, "kick", true);
        String playerName;
        try (Connection connection = SQL.gI().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT `name` FROM `player` WHERE `id` = ? LIMIT 1")) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("player not found");
                }
                playerName = rs.getString("name");
            }
        }

        int updated;
        try (Connection connection = SQL.gI().getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE `account` SET `lock` = ? WHERE `char` LIKE ?")) {
            ps.setInt(1, ban ? 1 : 0);
            ps.setString(2, "%\"" + playerName + "\"%");
            updated = ps.executeUpdate();
            connection.commit();
        }
        int kicked = kick ? kickPlayer(playerId, playerName) : 0;
        Map<String, Object> result = ok();
        result.put("playerId", playerId);
        result.put("playerName", playerName);
        result.put("accountsUpdated", updated);
        result.put("kicked", kicked);
        result.put("banned", ban);
        return result;
    }

    private Map<String, Object> setLoginLimit(HttpExchange exchange, Map<String, Object> body) throws IOException {
        requirePost(exchange);
        int limit = intValue(body, "limit", Manager.gI().login_limit_per_ip);
        if (limit < 0 || limit > 1000) {
            throw new IllegalArgumentException("limit must be 0-1000");
        }
        Manager.gI().login_limit_per_ip = limit;
        updateConfigValue("login_limit_per_ip", String.valueOf(limit));
        Map<String, Object> result = ok();
        result.put("loginLimitPerIp", limit);
        return result;
    }

    private int kickNonAdminSessions() {
        int kicked = 0;
        List<Session> copy;
        synchronized (Session.client_entry) {
            copy = new ArrayList<>(Session.client_entry);
        }
        for (Session session : copy) {
            if (session == null || session.ac_admin > 0) {
                continue;
            }
            try {
                session.close();
                kicked++;
            } catch (IOException ignored) {
            }
        }
        return kicked;
    }

    private int kickPlayer(int playerId, String playerName) {
        int kicked = 0;
        List<Session> copy;
        synchronized (Session.client_entry) {
            copy = new ArrayList<>(Session.client_entry);
        }
        for (Session session : copy) {
            if (session == null || session.p == null) {
                continue;
            }
            if (session.p.ID == playerId || playerName.equalsIgnoreCase(session.p.name)) {
                try {
                    session.close();
                    kicked++;
                } catch (IOException ignored) {
                }
            }
        }
        return kicked;
    }

    private void updateConfigValue(String key, String value) throws IOException {
        Path path = Path.of("hso.conf");
        List<String> lines = Files.exists(path)
                ? Files.readAllLines(path, StandardCharsets.UTF_8)
                : new ArrayList<>();
        boolean replaced = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.startsWith("#") && line.startsWith(key + ":")) {
                lines.set(i, key + ": " + value);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lines.add(key + ": " + value);
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private Map<String, Object> readJsonBody(HttpExchange exchange) throws IOException {
        if (!hasBody(exchange)) {
            return new LinkedHashMap<>();
        }
        byte[] raw = exchange.getRequestBody().readAllBytes();
        if (raw.length == 0) {
            return new LinkedHashMap<>();
        }
        return JSON.readValue(raw, new TypeReference<LinkedHashMap<String, Object>>() {});
    }

    private boolean hasBody(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method);
    }

    private void requirePost(HttpExchange exchange) {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            throw new IllegalArgumentException("POST required");
        }
    }

    private String normalizeGiftItems(Object value) throws IOException {
        if (value == null) {
            return "[]";
        }
        if (value instanceof List) {
            return JSON.writeValueAsString(value);
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return "[]";
        }
        JSON.readTree(text);
        return text;
    }

    private static Map<String, Object> ok() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        return result;
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", false);
        result.put("error", message == null ? "unknown error" : message);
        return result;
    }

    private void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] data = JSON.writeValueAsBytes(value);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(data);
        }
    }

    private Map<String, String> query(HttpExchange exchange) {
        Map<String, String> result = new LinkedHashMap<>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) {
            return result;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String value = idx >= 0 ? pair.substring(idx + 1) : "";
            result.put(urlDecode(key), urlDecode(value));
        }
        return result;
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String stringValue(Map<String, Object> body, String key, String defaultValue) {
        Object value = body.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private int intValue(Map<String, Object> body, String key, int defaultValue) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value).replace("_", ""));
    }

    private long longValue(Map<String, Object> body, String key, long defaultValue) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value).replace("_", ""));
    }

    private boolean boolValue(Map<String, Object> body, String key, boolean defaultValue) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = String.valueOf(value).toLowerCase(Locale.ROOT);
        return "true".equals(text) || "1".equals(text) || "yes".equals(text) || "on".equals(text);
    }

    private String adminHtml() {
        String defaultToken;
        try {
            defaultToken = JSON.writeValueAsString(Manager.gI().admin_tool_token);
        } catch (Exception e) {
            defaultToken = "\"\"";
        }
        return """
                <!doctype html>
                <html lang="vi">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>HSO Admin Tool</title>
                    <style>
                        :root { --bg:#111827; --panel:#1f2937; --line:#374151; --text:#f9fafb; --muted:#9ca3af; --ok:#22c55e; --bad:#ef4444; --accent:#38bdf8; }
                        * { box-sizing:border-box; }
                        body { margin:0; font-family:Verdana, sans-serif; background:linear-gradient(135deg,#0f172a,#1f2937); color:var(--text); }
                        main { width:min(1180px, calc(100% - 24px)); margin:24px auto; }
                        h1 { margin:0 0 16px; }
                        .grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(260px,1fr)); gap:14px; }
                        .card { background:rgba(31,41,55,.92); border:1px solid var(--line); border-radius:14px; padding:16px; box-shadow:0 16px 40px rgba(0,0,0,.22); }
                        label { display:block; margin:9px 0 4px; color:var(--muted); font-size:13px; }
                        input, textarea, select { width:100%; border:1px solid var(--line); border-radius:9px; padding:10px; background:#0b1220; color:var(--text); }
                        textarea { min-height:70px; font-family:Consolas, monospace; }
                        button { border:0; border-radius:9px; padding:10px 12px; margin:8px 6px 0 0; cursor:pointer; background:var(--accent); color:#061018; font-weight:700; }
                        button.danger { background:var(--bad); color:white; }
                        button.ok { background:var(--ok); color:#04140a; }
                        .checkrow { display:flex; align-items:center; gap:8px; margin-top:10px; color:var(--muted); font-size:13px; }
                        .checkrow input { width:auto; }
                        pre { white-space:pre-wrap; word-break:break-word; background:#050b14; border:1px solid var(--line); border-radius:12px; padding:12px; min-height:90px; }
                        .row { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
                        .stat { color:var(--muted); line-height:1.7; }
                    </style>
                </head>
                <body>
                <main>
                    <h1>HSO Admin Tool</h1>
                    <div class="card">
                        <label>Admin token</label>
                        <input id="token" placeholder="admin_tool_token trong hso.conf">
                        <button onclick="saveToken()">Save token</button>
                        <button onclick="status()">Refresh status</button>
                    </div>
                    <div class="grid">
                        <section class="card">
                            <h3>Status</h3>
                            <div id="stats" class="stat">Chua load.</div>
                        </section>
                        <section class="card">
                            <h3>Giftcode</h3>
                            <label>Code</label><input id="gc_code" placeholder="tanthu2026">
                            <div class="row"><div><label>Limit</label><input id="gc_limit" value="1"></div><div><label>Empty box</label><input id="gc_empty" value="0"></div></div>
                            <div class="row"><div><label>Vang</label><input id="gc_vang" value="0"></div><div><label>Ngoc</label><input id="gc_ngoc" value="0"></div></div>
                            <div class="row"><div><label>Level</label><input id="gc_level" value="1"></div><div><label>Date item3</label><input id="gc_date" value="0"></div></div>
                            <label>Gift for user CSV</label><input id="gc_for" placeholder="bo trong = ai cung nhan">
                            <label>Item3 JSON [[id,expiry]]</label><textarea id="gc_item3">[]</textarea>
                            <label>Item4 JSON [[id,quantity]]</label><textarea id="gc_item4">[]</textarea>
                            <label>Item7 JSON [[id,quantity]]</label><textarea id="gc_item7">[]</textarea>
                            <button class="ok" onclick="giftcode()">Create giftcode</button>
                        </section>
                        <section class="card">
                            <h3>Bot player that</h3>
                            <label>So luong</label><input id="bot_count" value="100">
                            <label>Prefix (de Bot = ten nguoi that)</label><input id="bot_prefix" value="Bot">
                            <label class="checkrow"><input id="bot_auto" type="checkbox" checked> Phan bo nhu nguoi that: farm map quai, cho, vong xoay VIP</label>
                            <div class="row"><div><label>Map ID</label><input id="bot_map" value="1"></div><div><label>Zone</label><input id="bot_zone" value="0"></div></div>
                            <div class="row"><div><label>X</label><input id="bot_x" value="432"></div><div><label>Y</label><input id="bot_y" value="354"></div></div>
                            <div class="stat">Khi bat phan bo tu dong, Map/Zone/X/Y chi dung lam fallback. Bot mac full trang bi nhu player that. Bot cho chiem khoang 5% va treo do tim/cam gia 10-500tr. Bot VIP chiem khoang 2% va dung tai Cang Dong Soi canh NPC Cuop Bien de quay. Bot boss/farm phan bo deu o cac map up quai, khong dung o map 1.</div>
                            <button class="ok" onclick="bots(true)">Bat bot</button>
                            <button class="danger" onclick="bots(false)">Tat bot</button>
                        </section>
                        <section class="card">
                            <h3>Bao tri / Debug</h3>
                            <button class="danger" onclick="maintenance(true,false)">Bat bao tri</button>
                            <button class="danger" onclick="maintenance(true,true)">Bat + kick non-admin</button>
                            <button class="ok" onclick="maintenance(false,false)">Tat bao tri</button>
                            <button onclick="debug(true)">Bat debug</button>
                            <button onclick="debug(false)">Tat debug</button>
                        </section>
                        <section class="card">
                            <h3>Su kien</h3>
                            <button class="ok" onclick="battlefield()">Mo dang ky chien truong</button>
                            <button class="danger" onclick="startBattlefield()">Bat dau chien truong ngay</button>
                            <button class="ok" onclick="castle('register')">Mo dang ky chiem thanh</button>
                            <button class="ok" onclick="castle('attack')">Mo chien chiem thanh</button>
                            <button class="danger" onclick="castle('close')">Dong chiem thanh</button>
                        </section>
                        <section class="card">
                            <h3>Ban player</h3>
                            <label>Player ID</label><input id="ban_id" placeholder="id nhan vat">
                            <button class="danger" onclick="ban(true)">Ban + kick</button>
                            <button class="ok" onclick="ban(false)">Unban</button>
                        </section>
                        <section class="card">
                            <h3>Login limit</h3>
                            <label>Nick/IP, 0 = tat limit</label><input id="login_limit" value="3">
                            <button onclick="loginLimit()">Save limit</button>
                        </section>
                    </div>
                    <h3>Output</h3>
                    <pre id="out"></pre>
                </main>
                <script>
                    const serverToken = __ADMIN_TOKEN__;
                    const tokenInput = document.getElementById('token');
                    tokenInput.value = localStorage.getItem('hsoAdminToken') || serverToken || '';
                    function saveToken(){ localStorage.setItem('hsoAdminToken', tokenInput.value); }
                    async function api(path, body) {
                        try {
                            saveToken();
                            const opt = { headers:{'X-Admin-Token': tokenInput.value} };
                            if (body !== undefined) { opt.method='POST'; opt.headers['Content-Type']='application/json'; opt.body=JSON.stringify(body); }
                            const res = await fetch(path, opt);
                            const json = await res.json();
                            document.getElementById('out').textContent = `HTTP ${res.status}\\n` + JSON.stringify(json, null, 2);
                            await status(false);
                            return json;
                        } catch (e) {
                            document.getElementById('out').textContent = 'Request failed: ' + e;
                        }
                    }
                    async function status(print=true){
                        try {
                            saveToken();
                            const res = await fetch('/api/status', {headers:{'X-Admin-Token': tokenInput.value}});
                            const j = await res.json();
                            if(print) document.getElementById('out').textContent = `HTTP ${res.status}\\n` + JSON.stringify(j, null, 2);
                            if(j.ok) {
                                document.getElementById('stats').innerHTML =
                                    `Sessions: ${j.sessions}<br>Online real: ${j.onlinePlayers}<br>Bot players: ${j.fakeBots}<br>Shown online: ${j.shownOnline}<br>Bot auto: ${j.botAutoDistribute}<br>Farm/Cho/VIP/Boss: ${j.botFarmers}/${j.botSellers}/${j.botVip}/${j.botBossHunters}<br>Bot fallback map/zone: ${j.botMapId}/${j.botZoneId} (${j.botX},${j.botY})<br>Maintenance: ${j.maintenance}<br>Debug: ${j.debug}<br>Login limit/IP: ${j.loginLimitPerIp}`;
                            } else {
                                document.getElementById('stats').innerHTML = 'Token sai hoac API loi: ' + (j.error || res.status);
                            }
                        } catch (e) {
                            document.getElementById('stats').innerHTML = 'Khong ket noi duoc admin API. Server game da restart chua?';
                            if(print) document.getElementById('out').textContent = 'Request failed: ' + e;
                        }
                    }
                    function giftcode(){ api('/api/giftcode', {code:v('gc_code'), limit:n('gc_limit'), emptyBox:n('gc_empty'), vang:n('gc_vang'), ngoc:n('gc_ngoc'), level:n('gc_level'), date:n('gc_date'), giftFor:v('gc_for'), item3:v('gc_item3'), item4:v('gc_item4'), item7:v('gc_item7')}); }
                    function bots(enabled){ api('/api/bots', {enabled, count:n('bot_count'), prefix:v('bot_prefix'), autoDistribute:document.getElementById('bot_auto').checked, mapId:n('bot_map'), zoneId:n('bot_zone'), x:n('bot_x'), y:n('bot_y')}); }
                    function maintenance(enabled,kick){ api('/api/maintenance', {enabled,kick}); }
                    function debug(enabled){ api('/api/debug', {debug:enabled, logErrorLogin:enabled}); }
                    function battlefield(){ api('/api/battlefield/open', {}); }
                    function startBattlefield(){ api('/api/battlefield/start', {}); }
                    function castle(mode){ api('/api/castle/open', {mode}); }
                    function ban(ban){ api('/api/ban-player', {playerId:n('ban_id'), ban, kick:true}); }
                    function loginLimit(){ api('/api/login-limit', {limit:n('login_limit')}); }
                    function v(id){ return document.getElementById(id).value; }
                    function n(id){ return Number(v(id) || 0); }
                    status(false);
                </script>
                </body>
                </html>
                """.replace("__ADMIN_TOKEN__", defaultToken);
    }

    private interface ApiHandler {
        Map<String, Object> handle(HttpExchange exchange, Map<String, Object> body) throws Exception;
    }

}
