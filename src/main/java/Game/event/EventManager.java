package Game.event;

import Game.core.Manager;
import Game.core.MenuController;
import Game.core.Service;
import Game.io.Session;
import Game.template.ItemTemplate4;
import Game.core.SQL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

// === NEW: import Vu Lan event ===
import Game.event.Event_3;

public class EventManager {
    public String name;
    public static ArrayList<PlayerRegister> registerList = new ArrayList<>();
    public static int time = 0;
    public static EventManager eventManager;

    // Chỉ dùng cho Tết/Noel (cơ chế 4 nguyên liệu). Vu Lan không dùng mảng này.
    public static final short[][] item_drop = new short[][]{
            new short[]{29, 30, 89, 28}, // Tết (LunarNewYear)
            new short[]{118, 119, 120, 121, 122, 153, 154, 155, 156}, // Noel
            new short[]{}, // Vu Lan: không dùng cơ chế 4 nguyên liệu
            new short[]{306}
    };

    // =========================
    //         START
    // =========================
    public synchronized void start() {
        try {
            if (Manager.gI().event == 0) {
                LunarNewYear.list_nhan_banh.clear();
                Manager.gI().chatKTGprocess("Bắt đầu nấu bánh, các bạn có thể tăng tốc nấu");
                time = 60;
            } else if (Manager.gI().event == 1) {
                Noel.list_nhankeo.clear();
                Manager.gI().chatKTGprocess("Bắt đầu nấu kẹo, các bạn có thể tăng tốc nấu");
                time = 60;
            } else if (Manager.gI().event == 2) { // === NEW: Vu Lan
                // Vu Lan (Event_3) không cần reset danh sách trong EventManager
                // Danh sách thành tích nằm ở Event_3.list_HoaDang
                Manager.gI().chatKTGprocess("Bắt đầu sự kiện Vu Lan, hãy thu thập và đổi Hoa đăng!");
                time = 60; // bạn có thể chỉnh theo nhu cầu
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    //       PROCESS MENU
    // =========================
    public static void processMenu(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 0) {
            LunarNewYear.Menu(conn, index);
        } else if (Manager.gI().event == 1) {
            Noel.Menu(conn, index);
        } else if (Manager.gI().event == 2) { // === NEW: Vu Lan
            // Vu Lan hiện chỉ cần hiển thị BXH đổi quà (Hoa đăng)
            String[] top = Event_3.get_top();
            MenuController.send_menu_select(conn, (short)-66, top, (byte)-1);
        }
    }

    // =========================
    //         UPDATE
    // =========================
    public synchronized static void update(int t) throws IOException {
        if (time != 0) {
            time -= t;
            System.out.println("Thời gian sự kiện: " + time);
            if (time <= 0) {
                time = 0;
                finish();
            }
        }
    }

    // =========================
    //         FINISH
    // =========================
    public synchronized static void finish() throws IOException {
        if (Manager.gI().event == 0 || Manager.gI().event == 1) {
            // Cơ chế Tết/Noel: chấm theo 4 nguyên liệu → total = min(material)/10
            for (PlayerRegister playerRegister : registerList) {
                playerRegister.setTotal();
            }
            registerList.sort(Comparator.comparingInt(playerRegister -> -playerRegister.total));
        }

        if (Manager.gI().event == 0) {
            Manager.gI().chatKTGprocess("Thời gian nấu bánh đã xong");
            LunarNewYear.finish();
        } else if (Manager.gI().event == 1) {
            Manager.gI().chatKTGprocess("Thời gian nấu kẹo đã xong");
            Noel.finish();
        } else if (Manager.gI().event == 2) { // === NEW: Vu Lan
            // Vu Lan: sắp xếp BXH theo số lần đổi quà, cùng số lần thì ưu tiên thời gian sớm
            Event_3.sort_bxh();
            Manager.gI().chatKTGprocess("Sự kiện Vu Lan đã kết thúc. Kiểm tra BXH đổi Hoa đăng!");
            // Nếu bạn muốn trao thưởng tự động theo TOP tại đây, hãy gọi hàm thưởng tại đây.
            // Ví dụ: RewardVuLan.giveTopRewards(Event_3.list_HoaDang);
        }
    }

    // =========================
    //        SEND INFO
    // =========================
    public static void send_info(Session conn) throws IOException {
        if (Manager.gI().event == 0 || Manager.gI().event == 1) {
            EventManager.PlayerRegister player = EventManager.getPlayer(EventManager.registerList, conn.p.name);
            if (player != null) {
                StringBuilder sb = new StringBuilder();
                short[] drop = item_drop[Manager.gI().event];
                for (int i = 0; i < drop.length && i < player.material.length; i++) {
                    ItemTemplate4 it = ItemTemplate4.item.get(drop[i]);
                    sb.append(it.getName()).append(" ").append(player.material[i]).append("\n");
                }
                Service.send_notice_box(conn, sb.toString());
            } else {
                Service.send_notice_box(conn, "Bạn chưa tham gia sự kiện.");
            }
        } else if (Manager.gI().event == 2) { // === NEW: Vu Lan
            // Vu Lan: hiển thị nhanh TOP hiện tại
            String[] top = Event_3.get_top();
            MenuController.send_menu_select(conn, (short)-66, top, (byte)-1);
        }
    }

    // =========================
    //         TOP EVENT
    // =========================
    public static void top_event(Session conn) throws IOException {
        if (Manager.gI().event == 2) {
            // Vu Lan: top theo Event_3 (BXH đổi quà)
            String[] top = Event_3.get_top();
            MenuController.send_menu_select(conn, (short)-66, top, (byte)-1);
            return;
        }

        // Tết/Noel: vẫn dùng point_event trong DB
        try (Connection connection = SQL.gI().getConnection();
             PreparedStatement psSelect = connection.prepareStatement(
                     "SELECT `name`,`point_event` FROM `player` WHERE `point_event` > 0 ORDER BY `point_event` DESC LIMIT 10;")) {
            try (ResultSet rs = psSelect.executeQuery()) {
                List<String> txt = new ArrayList<>();
                boolean inTop = false;
                int index = 0;
                while (rs.next()) {
                    String top = "Hạng " + ++index + ": ";
                    String name = rs.getString("name");
                    if (name.equals(conn.p.name) || conn.ac_admin > 3) {
                        name += " " + rs.getString("point_event");
                        inTop = true;
                    }
                    txt.add(top + name);
                }
                if (!inTop) {
                    txt.add("..." + conn.p.name + " " + conn.p.point_event);
                }
                if (!txt.isEmpty()) {
                    String[] myArray = txt.toArray(new String[0]);
                    MenuController.send_menu_select(conn, (short)-66, myArray, (byte)-1);
                } else {
                    Service.send_notice_box(conn, "Chưa có ai trong top");
                }
            } catch (SQLException ee) {
                ee.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================
    //    ĐIỀU KIỆN ĐĂNG KÝ
    // =========================
    public synchronized static boolean notCanRegister() {
        // Khóa từ 16:30 đến hết 17:59
        if (Manager.hour == 17) {
            return true;
        }
        return Manager.hour == 16 && Manager.minute >= 30;
    }

    // =========================
    //      TIỆN ÍCH CHUNG
    // =========================
    public synchronized static boolean check(ArrayList<PlayerRegister> list, String name) {
        for (PlayerRegister p : list) {
            if (p.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static PlayerRegister getPlayer(ArrayList<PlayerRegister> list, String name) {
        for (PlayerRegister p : list) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        return null;
    }

    // =========================
    //      SAVE/LOAD (CŨ)
    //  — Cho Tết/Noel với 4 nguyên liệu
    // =========================
    public static JSONArray save(ArrayList<PlayerRegister> list) {
        synchronized (list) {
            JSONArray jsar = new JSONArray();
            for (PlayerRegister playerRegister : list) {
                JSONArray jsar_1 = new JSONArray();
                jsar_1.add(playerRegister.name);
                for (short quant : playerRegister.material) {
                    jsar_1.add(quant);
                }
                jsar_1.add(playerRegister.total);
                jsar_1.add(playerRegister.rank);
                jsar.add(jsar_1);
            }
            return jsar;
        }
    }

    public synchronized static void loadDatabase(JSONObject jsob) {
        // Giữ nguyên hành vi cũ cho Tết/Noel
        registerList = new ArrayList<>();
        JSONArray jsar = (JSONArray) JSONValue.parse(jsob.get("register").toString());
        for (Object o : jsar) {
            JSONArray jsar_1 = (JSONArray) o;

            String name = (String) jsar_1.get(0);

            short[] material = new short[4];
            for (int i = 0; i < 4; i++) {
                material[i] = ((Long) jsar_1.get(i + 1)).shortValue();
            }

            short total = ((Long) jsar_1.get(5)).shortValue();
            int rank = ((Long) jsar_1.get(6)).intValue();

            PlayerRegister playerRegister = new PlayerRegister(name);
            playerRegister.material = material;
            playerRegister.total = total;
            playerRegister.rank = rank;
            registerList.add(playerRegister);
        }
        jsar.clear();

        LunarNewYear.list_nhan_banh = new ArrayList<>();
        jsar = (JSONArray) JSONValue.parse(jsob.get("receive").toString());
        for (Object o : jsar) {
            JSONArray jsar_1 = (JSONArray) o;

            String name = (String) jsar_1.get(0);

            short[] material = new short[4];
            for (int i = 0; i < 4; i++) {
                material[i] = ((Long) jsar_1.get(i + 1)).shortValue();
            }

            short total = ((Long) jsar_1.get(5)).shortValue();
            int rank = ((Long) jsar_1.get(6)).intValue();

            PlayerRegister playerRegister = new PlayerRegister(name);
            playerRegister.material = material;
            playerRegister.total = total;
            playerRegister.rank = rank;
            LunarNewYear.list_nhan_banh.add(playerRegister);
        }
        jsar.clear();

        // === NEW: nếu blob DB có dữ liệu Vu Lan (list_hoadang) thì nạp cho Event_3
        loadDatabase_VuLan(jsob);
    }

    // =========================
    //    SAVE/LOAD Vu Lan (NEW)
    // =========================

    // Lưu phần dữ liệu Vu Lan (không phá cấu trúc cũ).
    // Trả về JSONObject chỉ chứa key "list_hoadang"
    public static JSONObject save_VuLan() {
        // Ủy quyền cho Event_3
        return Event_3.SaveData();
    }

    // Nạp phần dữ liệu Vu Lan nếu có
    public static void loadDatabase_VuLan(JSONObject jsob) {
        if (jsob == null) return;
        try {
            Object hoadang = jsob.get("list_hoadang");
            if (hoadang != null) {
                // Tạo 1 JSONObject tạm để tái sử dụng API của Event_3
                JSONObject wrap = new JSONObject();
                wrap.put("list_hoadang", hoadang);
                Event_3.LoadDB(wrap);
            }
        } catch (Exception e) {
            // Nếu không có key hoặc format khác, bỏ qua an toàn
            e.printStackTrace();
        }
    }

    // =========================
    //     PLAYER REGISTER (cũ)
    // =========================
    public static class PlayerRegister {
        public String name;
        public short[] material; // CHỈ dùng cho Tết/Noel
        public short total;
        public int rank;

        public PlayerRegister(String name) {
            this.name = name;
            this.material = new short[4];
            this.total = 0;
            this.rank = -1;
        }

        public void setTotal() {
            int minValue = material[0];
            for (int i = 1; i < material.length; i++) {
                if (material[i] < minValue) {
                    minValue = material[i];
                }
            }
            this.total = (short) (minValue / 10);
        }
    }
}
