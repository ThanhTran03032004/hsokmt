package Game.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Game.core.Manager;
import Game.core.Service;
import Game.io.Session;
import Game.template.Item3;
import Game.template.Item47;
import Game.template.ItemTemplate3;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Noel extends EventManager {
    private static final String name = "Giáng sinh";
    public static final HashMap<String, Integer> list_nhankeo = new HashMap<>();
    public static final List<BXH_naukeo> list_bxh_naukeo = new ArrayList<>();
    public static final Set<String> list_bxh_naukeo_name = new HashSet<>();
    public static final List<BXH_naukeo> list_caythong = new ArrayList<>();

    public static void Menu(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 1) {
            switch (index) {
                case 0: {
                    Service.send_box_input_text(conn, 10, "Nhập số lượng", new String[]{"Số lượng :"});
                    break;
                }
                case 1: {
                    Service.send_notice_box(conn,
                            "Để đổi thành Hộp đồ chơi hoàn chỉnh theo công thức: 20.000 vàng + 50 Bức tượng rồng + 50 Kiếm đồ chơi + 50 Đôi giày nhỏ xíu + 50 Trang phục tí hon + 50 Mũ lính chì."
                                    + "\nĐể đổi thành Túi kẹo hoàn chỉnh theo công thức: 50.000 vàng + 5 Kẹo.");
                    break;
                }
                case 2: {
                    if (notCanRegister()) {
                        Service.send_notice_box(conn, "Không trong thời gian đăng ký!");
                        return;
                    }
                    if (conn.p.get_ngoc() < 5) {
                        Service.send_notice_box(conn, "Không đủ 5 ngọc");
                        return;
                    }
                    if (check(registerList, conn.p.name)) {
                        Service.send_notice_box(conn, "Đã đăng ký rồi, quên à!");
                        return;
                    }
                    conn.p.update_ngoc(-5);
                    Noel.add_material(conn.p.name, 0);
                    Service.send_notice_box(conn, "Đăng ký thành công, có thể góp nguyên liệu rồi");
                    break;
                }
                case 3: {
                    if (notCanRegister()) {
                        Service.send_notice_box(conn, "Không trong thời gian đăng ký");
                        return;
                    }
                    if (check(registerList, conn.p.name)) {
                        Service.send_box_input_text(conn, 11, "Nhập số lượng", new String[]{"Số lượng :"});
                    } else {
                        Service.send_notice_box(conn, "Chưa đăng ký nấu kẹo, hãy đăng ký!");
                    }
                    break;
                }
                case 4: {
                    int quant = Noel.get_keo(conn.p.name);
                    if (quant > 0) {
                        quant = Math.min(quant, 20);
                        if (Noel.list_bxh_naukeo_name.contains(conn.p.name)) {
                            quant += 20;
                        }
                        quant *= 3;
                        Item47 it = new Item47();
                        it.category = 4;
                        it.id = 162;
                        it.quantity = (short) quant;
                        conn.p.item.add_item_inventory47(4, it);
                        Service.send_notice_box(conn, "Nhận được " + quant + " kẹo");
                    } else {
                        Service.send_notice_box(conn, "Đã nhận rồi hoặc chưa tham gia!");
                    }
                    break;
                }
                case 5: {
                    Service.send_box_input_text(conn, 12, "Nhập số lượng", new String[]{"Số lượng :"});
                    break;
                }
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13: {
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Hành trang không đủ chỗ trống!");
                        return;
                    }
                    short[] id_receiv = new short[]{4626, 4761, 3610, 4636, 4709, 4710, 281, 3616};
                    short[] tuikeo_required = new short[]{120, 120, 60, 60, 30, 30, 15, 60};
                    short[] hopdochoi_required = new short[]{120, 120, 60, 60, 30, 30, 15, 60};
                    int[] ngoc_required = new int[]{360, 330, 60, 60, 60, 60, 15, 300};
                    if (tuikeo_required[index - 6] > conn.p.item.total_item_by_id(4, 157)) {
                        Service.send_notice_box(conn, "Không đủ " + tuikeo_required[index - 6] + " túi kẹo!");
                        return;
                    }
                    if (hopdochoi_required[index - 6] > conn.p.item.total_item_by_id(4, 158)) {
                        Service.send_notice_box(conn, "Không đủ " + hopdochoi_required[index - 6] + " hộp đồ chơi!");
                        return;
                    }
                    if (ngoc_required[index - 6] > conn.p.get_ngoc()) {
                        Service.send_notice_box(conn, "Không đủ " + ngoc_required[index - 6] + " ngọc!");
                        return;
                    }
                    if (index != 12) {
                        Item3 itbag = new Item3();
                        ItemTemplate3 it_temp = ItemTemplate3.item.get(id_receiv[index - 6]);
                        itbag.id = it_temp.getId();
                        itbag.name = it_temp.getName();
                        itbag.clazz = it_temp.getClazz();
                        itbag.type = it_temp.getType();
                        itbag.level = 10;
                        itbag.icon = it_temp.getIcon();
                        itbag.op = new ArrayList<>();
                        itbag.op.addAll(it_temp.getOp());
                        itbag.color = it_temp.getColor();
                        itbag.part = it_temp.getPart();
                        itbag.tier = 0;
                        itbag.islock = false;
                        itbag.time_use = 0;
                        conn.p.item.add_item_inventory3(itbag);
                        Service.send_notice_box(conn, "Nhận được " + itbag.name + ".");
                    } else {
                        Item47 itbag = new Item47();
                        itbag.id = id_receiv[index - 6];
                        itbag.quantity = (short) 20;
                        itbag.category = 4;
                        conn.p.item.add_item_inventory47(4, itbag);
                        Service.send_notice_box(conn, "Nhận được 20 xe trượt tuyết.");
                    }
                    conn.p.item.remove(4, 157, tuikeo_required[index - 6]);
                    conn.p.item.remove(4, 158, hopdochoi_required[index - 6]);
                    conn.p.update_ngoc(-ngoc_required[index - 6]);
                    break;
                }
                default: {
                    Service.send_notice_box(conn, "Đang được chuẩn bị");
                    break;
                }
            }
        }
    }

    public synchronized static void add_material(String name, int quant) {
//        if (quant == 0) {
//            EventManager.registerList.put(name, 0);
//        } else {
//            EventManager.registerList.put(name, (EventManager.registerList.get(name) + quant));
//        }
//        boolean check = false;
//        for (BXH_naukeo bxhNaukeo : list_bxh_naukeo) {
//            if (bxhNaukeo.name.equals(name)) {
//                bxhNaukeo.quant = EventManager.registerList.get(name);
//                bxhNaukeo.time = System.currentTimeMillis();
//                check = true;
//                break;
//            }
//        }
//        if (!check) {
//            list_bxh_naukeo.add(new BXH_naukeo(name, EventManager.registerList.get(name), System.currentTimeMillis()));
//        }
    }

    public synchronized static void add_caythong(String name, int quant) {
        boolean check = false;
        for (int i = 0; i < list_caythong.size(); i++) {
            if (list_caythong.get(i).name.equals(name)) {
                list_caythong.get(i).quant += quant;
                list_caythong.get(i).time = System.currentTimeMillis();
                check = true;
                break;
            }
        }
        if (!check) {
            list_caythong.add(new BXH_naukeo(name, quant, System.currentTimeMillis()));
        }
    }

    public synchronized static void finish() {
//        Noel.list_nhankeo.clear();
//        Noel.list_nhankeo.putAll(EventManager.registerList);
//        EventManager.registerList.clear();
//        Noel.list_bxh_naukeo_name.clear();
//        for (int i = 0; i < Noel.list_bxh_naukeo.size(); i++) {
//            if (i > 9) {
//                break;
//            }
//            Noel.list_bxh_naukeo_name.add(Noel.list_bxh_naukeo.get(i).name);
//        }
//        Noel.list_bxh_naukeo.clear();

    }

    public synchronized static int get_keo(String name) {
        int quant = 0;
        if (Noel.list_nhankeo.containsKey(name)) {
            quant += Noel.list_nhankeo.get(name);
            Noel.list_nhankeo.remove(name);
        }
        return quant;
    }

    public synchronized static int get_keo_now(String name) {
//        int quant = 0;
//        if (EventManager.registerList.containsKey(name)) {
//            quant += EventManager.registerList.get(name);
//        }
//        return quant;
        return 0;
    }

    public synchronized static String[] get_top_naukeo() {
        if (Noel.list_bxh_naukeo.isEmpty()) {
            return new String[]{"Chưa có thông tin"};
        }
        String[] top;
        if (Noel.list_bxh_naukeo.size() < 10) {
            top = new String[Noel.list_bxh_naukeo.size()];
        } else {
            top = new String[10];
        }
        for (int i = 0; i < top.length; i++) {
            top[i] = "Top " + (i + 1) + " : " + Noel.list_bxh_naukeo.get(i).name + " : " + Noel.list_bxh_naukeo.get(i).quant;
        }
        return top;
    }

    public synchronized static String[] get_top_caythong() {
        if (Noel.list_caythong.isEmpty()) {
            return new String[]{"Chưa có thông tin"};
        }
        String[] top;
        if (Noel.list_caythong.size() < 10) {
            top = new String[Noel.list_caythong.size()];
        } else {
            top = new String[10];
        }
        for (int i = 0; i < top.length; i++) {
            top[i] = "Top " + (i + 1) + " : " + Noel.list_caythong.get(i).name + " : " + Noel.list_caythong.get(i).quant + " lần";
        }
        return top;
    }

    public synchronized static void sort_bxh() {
        Collections.sort(Noel.list_bxh_naukeo, new Comparator<BXH_naukeo>() {
            @Override
            public int compare(BXH_naukeo o1, BXH_naukeo o2) {
                int compare = (o1.quant == o2.quant) ? 0 : ((o1.quant > o2.quant) ? -1 : 1);
                if (compare != 0) {
                    return compare;
                }
                return (o1.time > o2.time) ? 1 : -1;
            }
        });
        while (Noel.list_bxh_naukeo.size() > 10) {
            Noel.list_bxh_naukeo.remove(Noel.list_bxh_naukeo.size() - 1);
        }
        Collections.sort(Noel.list_caythong, new Comparator<BXH_naukeo>() {
            @Override
            public int compare(BXH_naukeo o1, BXH_naukeo o2) {
                int compare = (o1.quant == o2.quant) ? 0 : ((o1.quant > o2.quant) ? -1 : 1);
                if (compare != 0) {
                    return compare;
                }
                return (o1.time > o2.time) ? 1 : -1;
            }
        });
    }

    public static class BXH_naukeo {

        public String name;
        public int quant;
        public long time;

        public BXH_naukeo(String name2, int integer, long t) {
            name = name2;
            quant = integer;
            time = t;
        }
    }

    public static void LoadDB(JSONObject jsob) {
        long t_ = System.currentTimeMillis();
        synchronized (list_caythong) {
            Noel.list_caythong.clear();
            registerList.clear();
            Noel.list_bxh_naukeo.clear();
            JSONArray jsar_1 = (JSONArray) JSONValue.parse(jsob.get("list_caythong").toString());
            for (int i = 0; i < jsar_1.size(); i++) {
                JSONArray jsar_2 = (JSONArray) JSONValue.parse(jsar_1.get(i).toString());
                Noel.list_caythong.add(new Noel.BXH_naukeo(jsar_2.get(0).toString(), Integer.parseInt(jsar_2.get(1).toString()), t_));
            }
            jsar_1.clear();
            //
            jsar_1 = (JSONArray) JSONValue.parse(jsob.get("list_naukeo").toString());
//            for (int i = 0; i < jsar_1.size(); i++) {
//                JSONArray jsar_2 = (JSONArray) JSONValue.parse(jsar_1.get(i).toString());
//                EventManager.registerList.put(jsar_2.get(0).toString(), Integer.parseInt(jsar_2.get(1).toString()));
//                Noel.list_bxh_naukeo.add(new Noel.BXH_naukeo(jsar_2.get(0).toString(), Integer.parseInt(jsar_2.get(1).toString()), t_));
//            }
        }
    }

    public static JSONObject SaveData() {
        synchronized (list_caythong) {
            JSONArray jsar_1 = new JSONArray();
            for (int i = 0; i < Noel.list_caythong.size(); i++) {
                JSONArray jsar_2 = new JSONArray();
                jsar_2.add(Noel.list_caythong.get(i).name);
                jsar_2.add(Noel.list_caythong.get(i).quant);
                jsar_1.add(jsar_2);
            }
            JSONArray jsar_3 = new JSONArray();
//                for (Map.Entry<String, Integer> en : EventManager.registerList.entrySet()) {
//                    JSONArray jsar_2 = new JSONArray();
//                    jsar_2.add(en.getKey());
//                    jsar_2.add(en.getValue());
//                    jsar_3.add(jsar_2);
//                }
            //
            JSONObject jsob = new JSONObject();
            jsob.put("list_naukeo", jsar_3);
            jsob.put("list_caythong", jsar_1);
            return jsob;
        }
    }
}
