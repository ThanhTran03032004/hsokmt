/*
 * Event Vu Lan: rơi "hoa sen trắng" (id 306) với tỉ lệ 10% khi Player hạ quái
 */
package Game.event;

import Game.client.Player;
import Game.map.Map;
import Game.map.LeaveItemMap;
import Game.template.MainObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Event_3 {
    private static String name_event = "sự kiện vu lan";

    // ====== Cấu hình rơi đồ ======
    public static boolean ENABLED = true;            // Bật/tắt event
    public static final short ITEM_ID = 306;         // Hoa sen trắng
    public static int DROP_RATE_PERCENT = 100;        // 10%

    // ====== BXH đổi quà ======
    public static final List<BXH_DoiQua> list_HoaDang = new ArrayList<>();

    /**
     * Gọi ở điểm quái chết. Nếu killer là Player, có 10% rơi item 306.
     */
    public static void LeaveItemMap(Map map, MainObject focus, MainObject objAtk) throws IOException {
        if (!ENABLED) return;
        if (objAtk == null || !objAtk.isPlayer()) return;

        // Tỉ lệ 10%: random 0..99 < 10
        if (ThreadLocalRandom.current().nextInt(100) < DROP_RATE_PERCENT) {
            Player p = (Player) objAtk;
            LeaveItemMap.leave_item_by_type4(map, ITEM_ID, p, focus.ID);
        }
    }

    // ====== BXH ======
    public static class BXH_DoiQua {
        public String name;
        public int quant;
        public long time;

        public BXH_DoiQua(String name2, int integer, long t) {
            name = name2;
            quant = integer;
            time = t;
        }
    }

    public static void add_DoiQua(String name, int quant) {
        synchronized (list_HoaDang) {
            for (int i = 0; i < list_HoaDang.size(); i++) {
                if (list_HoaDang.get(i).name.equals(name)) {
                    list_HoaDang.get(i).quant += quant;
                    list_HoaDang.get(i).time = System.currentTimeMillis();
                    return;
                }
            }
            list_HoaDang.add(new BXH_DoiQua(name, quant, System.currentTimeMillis()));
        }
    }

    public static void LoadDB(JSONObject jsob) {
        synchronized (list_HoaDang) {
            list_HoaDang.clear();
            long t_ = System.currentTimeMillis();
            Object arr = jsob.get("list_hoadang");
            if (arr == null) return;
            JSONArray jsar_1 = (JSONArray) JSONValue.parse(arr.toString());
            if (jsar_1 == null) return;

            for (int i = 0; i < jsar_1.size(); i++) {
                JSONArray jsar_2 = (JSONArray) JSONValue.parse(jsar_1.get(i).toString());
                if (jsar_2 == null || jsar_2.size() < 2) continue;
                list_HoaDang.add(new BXH_DoiQua(
                        jsar_2.get(0).toString(),
                        Integer.parseInt(jsar_2.get(1).toString()),
                        t_)); // time tie-break sẽ là thời điểm load
            }
        }
    }

    public static JSONObject SaveData() {
        synchronized (list_HoaDang) {
            JSONArray jsar_1 = new JSONArray();
            for (int i = 0; i < list_HoaDang.size(); i++) {
                JSONArray jsar_2 = new JSONArray();
                jsar_2.add(list_HoaDang.get(i).name);
                jsar_2.add(list_HoaDang.get(i).quant);
                // Nếu muốn lưu cả time, thêm: jsar_2.add(list_HoaDang.get(i).time);
                jsar_1.add(jsar_2);
            }
            JSONObject jsob = new JSONObject();
            jsob.put("list_hoadang", jsar_1);
            return jsob;
        }
    }

    public static void sort_bxh() {
        synchronized (list_HoaDang) {
            Collections.sort(list_HoaDang, new Comparator<BXH_DoiQua>() {
                @Override
                public int compare(BXH_DoiQua o1, BXH_DoiQua o2) {
                    int compare = Integer.compare(o2.quant, o1.quant); // nhiều hơn xếp trên
                    if (compare != 0) return compare;
                    return Long.compare(o1.time, o2.time); // ai đạt sớm hơn xếp trước
                }
            });
        }
    }

    public static String[] get_top() {
        synchronized (list_HoaDang) {
            sort_bxh();
            if (list_HoaDang.isEmpty()) {
                return new String[]{"Chưa có thông tin"};
            }
            int n = Math.min(list_HoaDang.size(), 10);
            String[] top = new String[n];
            for (int i = 0; i < n; i++) {
                top[i] = "Top " + (i + 1) + " : " + list_HoaDang.get(i).name + " : " + list_HoaDang.get(i).quant + " lần";
                top[i] = "Top " + (i + 1) + " : " + list_HoaDang.get(i).name;
            }
            return top;
        }
    }

    // Tuỳ chọn: Getter tên sự kiện
    public static String getEventName() {
        return name_event;
    }
}
