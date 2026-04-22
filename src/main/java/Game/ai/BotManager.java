package Game.ai;

import Game.core.Manager;
import Game.core.ServerManager;
import Game.core.Util;
import Game.core.SQL;
import Game.map.Map; // dùng cho bản đồ (Game.map.Map)
import Game.io.Session; // giữ import Session nếu bạn cần dùng nơi khác

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
// KHÔNG import java.util.Map để tránh trùng tên với Game.map.Map
// KHÔNG import java.util.LinkedHashMap để giảm rủi ro — sẽ dùng fully-qualified
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class BotManager extends TelegramLongPollingBot {

    // --- CẤU HÌNH ---
    private final String botToken = System.getenv().getOrDefault("HESO_BOT_TOKEN",
            "7685475112:AAEgA7YO5v0HSfDapeI8hcCkM5ju3EHxkmI");
    private final String chatId   = System.getenv().getOrDefault("HESO_ADMIN_CHAT_ID",
            "5154548822"); // chỉ ID này mới có quyền

    // Tệp backup do backuptool.bat tạo
    private final String BACKUP_SQL = "backup.sql";
    private Timer backupTimer;
    private boolean backupScheduled = false;

    // Các mốc nạp (điều chỉnh theo hệ quy chiếu của bạn)
    private static final long[] MOC_NAP = new long[] { 200L, 500L, 1_000_000L, 2_000_000L, 3_000_000L, 5_000_000L };

    // ƯU TIÊN item7 trước như yêu cầu
    private static final List<ItemTableDef> ITEM_TABLES = Arrays.asList(
        new ItemTableDef("item7", "id", "name"),
        new ItemTableDef("item5", "id", "name"),
        new ItemTableDef("item4", "id", "name"),
        new ItemTableDef("item3", "id", "name")
    );

    private static class ItemTableDef {
        final String table, idCol, nameCol;
        ItemTableDef(String t, String id, String name) {
            this.table = t; this.idCol = id; this.nameCol = name;
        }
    }

    @Override
    public String getBotUsername() { return "backuphso_bot"; }

    @Override
    public String getBotToken() { return botToken; }

    // ================== TIỆN ÍCH GỬI TIN ==================
    public void sendNotification(String message) {
        try {
            SendMessage m = new SendMessage();
            m.setChatId(chatId);
            m.setText(message);
            execute(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotificationOther(String message, String toChatId) {
        try {
            SendMessage m = new SendMessage();
            m.setChatId(toChatId);
            m.setText(message);
            execute(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================== XỬ LÝ LỆNH ==================
    public void processCommand(String command) {
        // Chuẩn hoá: trim và gom nhiều space về 1 space
        command = command == null ? "" : command.trim().replaceAll("\\s+", " ");
        if (command.isEmpty()) {
            sendNotification("Lệnh trống. Gõ /help để xem hướng dẫn.");
            return;
        }

        final String cmdLower = command.toLowerCase(Locale.ROOT);

        try {
            if (cmdLower.equals("/help")) {
                sendNotification(
                        "📋 LỆNH HỖ TRỢ:\n" +
                        "• ban <user> / ban_user <user>     → Khoá tài khoản (lock=1)\n" +
                        "• unban <user> / unban_user <user> → Mở khoá tài khoản (lock=0)\n" +
                        "• nap <user> <amount> <tigia>      → coin+=amount*tigia; tiennap/tongnap/tongthang/diemnap+=amount; báo mốc nạp\n" +
                        "• finditem <tên> / item <tên>      → Tìm ID vật phẩm trong item3/item4/item5/item7\n" +
                        "• /buildcode <block>               → Sinh mã từ block 'Tên x Số lượng' (tự nhận diện nếu dán trực tiếp)\n" +
                        "• /memory                          → RAM server & số người online\n" +
                        "• /accept                          → Bật/tắt cho phép truy cập\n" +
                        "• /backup                          → Gửi tệp backup hiện tại ngay\n" +
                        "• /startbackup                     → Bật gửi backup 30 phút/lần\n" +
                        "• /stopbackup                      → Tắt gửi backup định kỳ\n\n" +
                        "💡 Có thể dán trực tiếp block:\nNgọc x 15,000\\nRương đỏ x 100\\n... để bot tự sinh mã."
                );
                return;
            }

            // -------- BAN (lock = 1)
            if (cmdLower.startsWith("ban ") || cmdLower.startsWith("ban_user ")) {
                String[] s = command.split(" ", 2);
                if (s.length == 2) {
                    String user = s[1].trim();
                    if (user.startsWith("@")) user = user.substring(1);
                    final String sql = "UPDATE `account` SET `lock` = 1 WHERE `user` = ? LIMIT 1";
                    try (Connection conn = SQL.gI().getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, user);
                        int n = ps.executeUpdate();
                        if (n > 0) {
                            sendNotification("✅ Đã BAN tài khoản: " + user + " (lock=1)");
                        } else {
                            sendNotification("⚠️ Không tìm thấy user: " + user);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        sendNotification("❌ Lỗi ban: " + e.getMessage());
                    }
                } else {
                    sendNotification("❌ Lỗi cú pháp. Ví dụ: ban username");
                }
                return;
            }

            // -------- UNBAN (lock = 0)
            if (cmdLower.startsWith("unban ") || cmdLower.startsWith("unban_user ")) {
                String[] s = command.split(" ", 2);
                if (s.length == 2) {
                    String user = s[1].trim();
                    if (user.startsWith("@")) user = user.substring(1);
                    final String sql = "UPDATE `account` SET `lock` = 0 WHERE `user` = ? LIMIT 1";
                    try (Connection conn = SQL.gI().getConnection();
                         PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, user);
                        int n = ps.executeUpdate();
                        if (n > 0) {
                            sendNotification("✅ Đã UNBAN tài khoản: " + user + " (lock=0)");
                        } else {
                            sendNotification("⚠️ Không tìm thấy user: " + user);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        sendNotification("❌ Lỗi unban: " + e.getMessage());
                    }
                } else {
                    sendNotification("❌ Lỗi cú pháp. Ví dụ: unban username");
                }
                return;
            }

            // -------- NẠP + MỐC NẠP
            if (cmdLower.startsWith("nap ")) {
                String[] s = command.split(" ");
                if (s.length == 4) {
                    String user = s[1];
                    int amount = Integer.parseInt(s[2]);
                    int tigia  = Integer.parseInt(s[3]);

                    long newTongNap = topUp(user, amount, tigia);
                    if (newTongNap >= 0) {
                        long addCoin = (long) amount * tigia;
                        sendNotification("💰 Đã nạp cho " + user + " " + Util.number_format(addCoin) +
                                " coin. (tiennap/tongnap/tongthang/diemnap đều +=" + amount + ")");
                        sendMilestoneNotice(user, newTongNap);
                    } else {
                        sendNotification("⚠️ Nạp thất bại (kiểm tra user / DB).");
                    }
                } else {
                    sendNotification("❌ Lỗi cú pháp. Ví dụ: nap username 100 1000");
                }
                return;
            }

            // -------- TÌM VẬT PHẨM (lệnh tường minh)
            if (cmdLower.startsWith("finditem ") || cmdLower.startsWith("item ")) {
                String keyword = command.split(" ", 2)[1].trim();
                handleFindItem(keyword);
                return;
            }

            // -------- BUILD CODE TỪ BLOCK DẠNG "Tên x Số lượng"
            if (cmdLower.startsWith("/buildcode")) {
                String payload = command.replaceFirst("(?i)^/buildcode\\s*", "");
                if (payload.isEmpty()) {
                    sendNotification("❗ Dán block danh sách theo mẫu:\nTên x Số lượng\nVí dụ:\nNgọc x 15,000\nRương đỏ x 100");
                } else {
                    handleBuildItemCode(payload);
                }
                return;
            }

            // -------- MEMORY
            if (cmdLower.equals("/memory")) {
                int num = 0;
                for (Map[] maps : Map.entrys) {
                    for (Game.map.Map m : maps) {
                        num += m.players.size();
                    }
                }
                sendNotification("🧠 RAM server: " + ServerManager.getMemory() + "%\n" +
                                 "👥 Online: " + num);
                return;
            }

            // -------- ACCEPT
            if (cmdLower.equals("/accept")) {
                Manager.gI().isServerAdmin = !Manager.gI().isServerAdmin;
                sendNotification("🔓 Trạng thái truy cập: " +
                        (Manager.gI().isServerAdmin ? "CHO PHÉP" : "KHÔNG CHO PHÉP"));
                return;
            }

            // -------- BACKUP
            if (cmdLower.equals("/backup")) {
                sendFileNow();
                return;
            }
            if (cmdLower.equals("/startbackup")) {
                startBackupSchedule();
                sendNotification("⏱️ Đã bật gửi backup 30 phút/lần.");
                return;
            }
            if (cmdLower.equals("/stopbackup")) {
                stopBackupSchedule();
                sendNotification("⛔ Đã tắt gửi backup định kỳ.");
                return;
            }

            // -------- KHÔNG PHẢI LỆNH: tự nhận diện block quà hoặc tìm item theo keyword
            if (!command.startsWith("/")) {
                String[] lines = command.split("\\r?\\n");
                int matched = 0;
                for (String ln : lines) {
                    if (ln != null && LINE_RE.matcher(ln.trim()).find()) matched++;
                }
                if (matched >= 2) {
                    handleBuildItemCode(command);
                    return;
                }
                handleFindItem(command);
                return;
            }

            sendNotification("❓ Lệnh không hợp lệ. Gõ /help để xem danh sách lệnh.");

        } catch (NumberFormatException nf) {
            sendNotification("❌ Lỗi số học (amount/tigia không hợp lệ): " + nf.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendNotification("❌ Lỗi xử lý lệnh: " + e.getMessage());
        }
    }

    // ================== TELEGRAM UPDATE ==================
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
                return;
            }
            long fromId = update.getMessage().getFrom().getId();
            String text = update.getMessage().getText();

            // Chỉ cho phép admin
            if (fromId != Long.parseLong(this.chatId)) {
                sendNotificationOther("🚫 Bạn không phải admin của server. ID của bạn: " + fromId,
                        String.valueOf(fromId));
                return;
            }

            processCommand(text);

        } catch (Exception e) {
            e.printStackTrace();
            sendNotification("❌ Lỗi onUpdateReceived: " + e.getMessage());
        }
    }

    // ================== NẠP & MỐC ==================
    public long topUp(String name, int amount, int tigia) {
        String updateSql = "UPDATE `account` " +
                "SET `coin` = `coin` + ?, " +
                "    `tiennap` = `tiennap` + ?, " +
                "    `tongnap` = `tongnap` + ?, " +
                "    `tongthang` = `tongthang` + ?, " +
                "    `diemnap` = `diemnap` + ? " +
                "WHERE `user` = ?";

        String selectSql = "SELECT `tongnap` FROM `account` WHERE `user` = ? LIMIT 1";

        try (Connection conn = SQL.gI().getConnection()) {
            conn.setAutoCommit(false);

            long addCoin = (long) amount * tigia;

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setLong(1, addCoin);
                ps.setInt(2, amount);
                ps.setInt(3, amount);
                ps.setInt(4, amount);
                ps.setInt(5, amount);
                ps.setString(6, name == null ? "" : name.trim());

                int n = ps.executeUpdate();
                if (n <= 0) {
                    conn.rollback();
                    return -1;
                }
            }

            long tongnapNow = -1;
            try (PreparedStatement sel = conn.prepareStatement(selectSql)) {
                sel.setString(1, name == null ? "" : name.trim());
                try (ResultSet rs = sel.executeQuery()) {
                    if (rs.next()) {
                        tongnapNow = rs.getLong(1);
                    }
                }
            }

            conn.commit();
            return tongnapNow;

        } catch (SQLException e) {
            e.printStackTrace();
            sendNotification("❌ Lỗi nạp: " + e.getMessage());
            return -1;
        }
    }

    private void sendMilestoneNotice(String user, long tongnapNow) {
        int idxReached = -1;
        for (int i = 0; i < MOC_NAP.length; i++) {
            if (tongnapNow >= MOC_NAP[i]) idxReached = i;
            else break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📈 Tổng nạp hiện tại của ").append(user)
          .append(": ").append(Util.number_format(tongnapNow)).append("\n");

        if (idxReached >= 0) {
            long mocVal = MOC_NAP[idxReached];
            int mocSo = idxReached + 1;
            sb.append("🎯 Trạng thái mốc: ĐÃ ĐỦ **Mốc ").append(mocSo)
              .append(" (≥ ").append(Util.number_format(mocVal)).append(")**\n");
        } else {
            sb.append("🎯 Trạng thái mốc: Chưa đạt mốc nào.\n");
        }

        if (idxReached < MOC_NAP.length - 1) {
            long nextVal = MOC_NAP[idxReached + 1];
            long remain = Math.max(0, nextVal - tongnapNow);
            sb.append("⏳ Còn thiếu ").append(Util.number_format(remain))
              .append(" để đạt **Mốc ").append(idxReached + 2)
              .append(" (≥ ").append(Util.number_format(nextVal)).append(")**.");
        } else {
            sb.append("🏁 Bạn đã đạt mốc cao nhất hiện có. Tuyệt vời!");
        }

        sendNotification(sb.toString());
    }

    // ================== TÌM VẬT PHẨM (KEYWORD) ==================
    private void handleFindItem(String keyword) {
        keyword = keyword == null ? "" : keyword.trim();
        if (keyword.isEmpty()) {
            sendNotification("❌ Vui lòng nhập tên vật phẩm. Ví dụ: item Trứng Đại Bàng");
            return;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("🔎 Kết quả tìm: \"").append(keyword).append("\"\n");

        int totalFound = 0;
        try (Connection conn = SQL.gI().getConnection()) {
            for (ItemTableDef def : ITEM_TABLES) {
                List<String> rows = findItemsInTable(conn, def, keyword, 20);
                if (!rows.isEmpty()) {
                    totalFound += rows.size();
                    msg.append("\n— Bảng ").append(def.table).append(":\n");
                    for (String r : rows) {
                        msg.append("   ").append(r).append("\n");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendNotification("❌ Lỗi truy vấn vật phẩm: " + e.getMessage());
            return;
        }

        if (totalFound == 0) {
            msg.append("\n⚠️ Không thấy kết quả trong item3/item4/item5/item7.");
        } else {
            msg.append("\n✅ Tổng kết quả: ").append(totalFound);
        }

        sendNotification(msg.toString());
    }

    private List<String> findItemsInTable(Connection conn, ItemTableDef def, String keyword, int limit) throws SQLException {
        String sql = "SELECT " + backtick(def.idCol) + ", " + backtick(def.nameCol) +
                     " FROM " + backtick(def.table) +
                     " WHERE " + backtick(def.nameCol) + " LIKE ? " +
                     " LIMIT " + limit;

        List<String> out = new ArrayList<String>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong(1);
                    String name = rs.getString(2);
                    out.add("[" + def.table + "] id=" + id + " | name=" + (name == null ? "" : name));
                }
            }
        }
        return out;
    }

    private String backtick(String id) {
        return "`" + id.replace("`", "") + "`";
    }

    // ================== BUILD CODE TỪ BLOCK QUÀ ==================
    // Nhận dạng dòng "Tên ... x 123" (hoặc "×")
    private static final Pattern LINE_RE = Pattern.compile(
            "^(.*?)\\s*[x×]\\s*([0-9][0-9\\.,]*)\\s*$"
    );

    // Ưu tiên khớp tên chính xác, fallback LIKE
    private OptionalLong findItemIdBest(Connection conn, ItemTableDef def, String name) throws SQLException {
        // exact match
        String exactSql = "SELECT " + backtick(def.idCol) +
                " FROM " + backtick(def.table) +
                " WHERE " + backtick(def.nameCol) + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(exactSql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return OptionalLong.of(rs.getLong(1));
            }
        }
        // fallback LIKE
        String likeSql = "SELECT " + backtick(def.idCol) +
                " FROM " + backtick(def.table) +
                " WHERE " + backtick(def.nameCol) + " LIKE ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(likeSql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return OptionalLong.of(rs.getLong(1));
            }
        }
        return OptionalLong.empty();
    }

    // "15,000" -> 15000 ; "20.000.000" -> 20000000
    private long parseQuantity(String s) {
        if (s == null) return 0L;
        String cleaned = s.replace(".", "").replace(",", "").trim();
        if (cleaned.isEmpty()) return 0L;
        return Long.parseLong(cleaned);
    }

    private String cleanName(String raw) {
        String s = raw == null ? "" : raw.trim();
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    private void handleBuildItemCode(String blockText) {
        // Tách dòng, bỏ rỗng
        String[] lines = blockText.split("\\r?\\n");
        List<String> validLines = new ArrayList<String>();
        for (String ln : lines) {
            if (ln != null && !ln.trim().isEmpty()) validLines.add(ln.trim());
        }
        if (validLines.isEmpty()) {
            sendNotification("❌ Không có dòng hợp lệ.");
            return;
        }

        // Parse từng dòng -> (name, qty)
        class Req { String name; long qty; }
        List<Req> reqs = new ArrayList<Req>();
        for (String ln : validLines) {
            Matcher m = LINE_RE.matcher(ln);
            if (!m.find()) continue;
            String name = cleanName(m.group(1));
            long qty = parseQuantity(m.group(2));
            if (!name.isEmpty() && qty > 0) {
                Req r = new Req(); r.name = name; r.qty = qty; reqs.add(r);
            }
        }
        if (reqs.isEmpty()) {
            sendNotification("❌ Không nhận diện được dòng theo mẫu \"Tên x Số lượng\".");
            return;
        }

        // Tìm ID theo từng bảng (ưu tiên ITEM_TABLES đã set)
        List<long[]> combined = new ArrayList<long[]>(); // [id, qty]
        java.util.Map<String, List<long[]>> perTable =
                new java.util.LinkedHashMap<String, List<long[]>>();
        for (ItemTableDef def : ITEM_TABLES) {
            perTable.put(def.table, new ArrayList<long[]>());
        }

        List<String> notFound = new ArrayList<String>();

        try (Connection conn = SQL.gI().getConnection()) {
            for (Req r : reqs) {
                boolean matched = false;
                for (ItemTableDef def : ITEM_TABLES) {
                    OptionalLong idOpt = findItemIdBest(conn, def, r.name);
                    if (idOpt.isPresent()) {
                        long id = idOpt.getAsLong();
                        combined.add(new long[]{id, r.qty});
                        perTable.get(def.table).add(new long[]{id, r.qty});
                        matched = true;
                        break;
                    }
                }
                if (!matched) notFound.add(r.name + " x " + r.qty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendNotification("❌ Lỗi DB khi tạo mã: " + e.getMessage());
            return;
        }

        // Tạo chuỗi mã
        String combinedCode = toCodeString(combined);
        StringBuilder sb = new StringBuilder();
        sb.append("✅ ĐÃ TẠO MÃ VẬT PHẨM\n\n");
        sb.append("• Combined: ").append(combinedCode.isEmpty() ? "[]" : combinedCode).append("\n");

        for (ItemTableDef def : ITEM_TABLES) {
            List<long[]> arr = perTable.get(def.table);
            String code = toCodeString(arr);
            if (arr != null && !arr.isEmpty()) {
                sb.append("• ").append(def.table).append(": ").append(code).append("\n");
            }
        }

        if (!notFound.isEmpty()) {
            sb.append("\n⚠️ Không tìm thấy trong DB:\n");
            for (String s : notFound) sb.append("   - ").append(s).append("\n");
        }

        sendNotification(sb.toString());
    }

    private String toCodeString(List<long[]> pairs) {
        if (pairs == null || pairs.isEmpty()) return "[]";
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i = 0; i < pairs.size(); i++) {
            long[] p = pairs.get(i);
            b.append("[").append(p[0]).append(",").append(p[1]).append("]");
            if (i < pairs.size() - 1) b.append(",");
        }
        b.append("]");
        return b.toString();
    }

    // ================== BACKUP/GỬI FILE ==================
    private void sendFileNow() {
        try {
            Path p = Paths.get(BACKUP_SQL);
            if (!Files.exists(p)) {
                sendNotification("⚠️ Không tìm thấy " + BACKUP_SQL + ". Hãy chạy backuptool trước.");
                return;
            }

            // Nén gzip tạm thời
            File gz = gzipToTemp(p);
            String niceName = buildBackupName(gz.getName().endsWith(".gz") ? "backup.sql.gz" : "backup.sql");

            InputFile input = new InputFile(gz, niceName);
            SendDocument doc = new SendDocument();
            doc.setChatId(chatId);
            doc.setDocument(input);
            execute(doc);

            // Xoá file tạm
            try { gz.delete(); } catch (Exception ignored) {}

            sendNotification("📦 Đã gửi backup: " + niceName);

        } catch (TelegramApiException e) {
            sendNotification("❌ Telegram lỗi khi gửi file: " + e.getMessage());
        } catch (IOException e) {
            Logger.getLogger(BotManager.class.getName()).log(Level.SEVERE, null, e);
            sendNotification("❌ IO lỗi khi đọc/nén backup: " + e.getMessage());
        }
    }

    private File gzipToTemp(Path source) throws IOException {
        File tmp = File.createTempFile("backup_", ".sql.gz");
        try (InputStream in = Files.newInputStream(source);
             OutputStream out = new GZIPOutputStream(new FileOutputStream(tmp))) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
            }
        }
        return tmp;
    }

    private String buildBackupName(String baseName) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return ts + "_" + baseName;
    }

    // ================== LỊCH BACKUP ==================
    public void runBot() {
        try {
            Runtime.getRuntime().exec("cmd /c start backuptool.bat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        startBackupSchedule();
    }

    private void startBackupSchedule() {
        if (backupScheduled) return;
        backupTimer = new Timer("backup-sender", true);
        backupTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendFileNow();
            }
        }, 0, 30 * 60 * 1000L); // 30 phút
        backupScheduled = true;
    }

    private void stopBackupSchedule() {
        if (!backupScheduled) return;
        try {
            backupTimer.cancel();
        } catch (Exception ignored) {}
        backupScheduled = false;
    }
}
