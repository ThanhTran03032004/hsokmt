
package Game.event;

import Game.core.Manager;
import Game.core.Service;
import Game.core.Util;
import Game.io.Session;
import Game.map.Map;
import Game.template.Item3;
import Game.template.ItemTemplate3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class Event_2 {
    private static String name_event = "sự kiện hè";
    public static final CopyOnWriteArrayList<MobCay> entrys = new CopyOnWriteArrayList<>();
    public static long timeCreate;
    public static final List<BXH_DoiQua> list_MamNguQua = new ArrayList<>();

    public static MobCay getMob(int idx) {
        for (MobCay m : entrys) {
            if (m.index == idx)
                return m;
        }
        return null;
    }

    public static void addmob(MobCay mob) {
        synchronized (entrys) {
            if (!entrys.contains(mob)) {
                entrys.add(mob);
            }
        }
    }

    public static void removemob(MobCay mob) {
        try {
            synchronized (entrys) {
                mob.MobLeave();
                entrys.remove(mob);
            }
        } catch (Exception e) {
        }

    }

    public static void ClearMob() {
        long time = System.currentTimeMillis();
        synchronized (entrys) {
            for (MobCay mob : entrys) {
                try {
                    mob.MobLeave();
                } catch (Exception e) {
                }
            }
            entrys.clear();
        }
        if (timeCreate > 0 && (time - timeCreate) / 1000 / 60 > 44) {

        }
    }

    public static void ResetMob() throws IOException {
        long time = System.currentTimeMillis();
        timeCreate = time;
        short idx = 30000;
        synchronized (entrys) {
            for (Map[] map : Map.entrys) {
                if (map.length <= 3) continue;
                for (int i = 0; i < 4; i++) {
                    Map m = map[i];
                    if (i == 1 || m == null || m.ismaplang || m.showhs || m.typemap != 0 || Map.is_map_cant_save_site(m.map_id))
                        continue;
                    short leng = (short) Util.random(1, 4);
                    for (int j = 0; j < leng; j++) {
                        MobCay mob = new MobCay(m, idx);
                        addmob(mob);
                        idx++;
                    }
                }
            }
            Manager.gI().chatKTGprocess("Cây ngũ quả đã xuất hiện hãy nhanh chân lên nào");
        }
    }

    public static void Update() {
        try {
            long time = System.currentTimeMillis();
            if (time - timeCreate > 1000 * 60 * 28 && !entrys.isEmpty())
                ClearMob();
            if (time - timeCreate > 1000 * 60 * 30)
                ResetMob();
            for (MobCay mob : entrys) {
                mob.update();
            }

        } catch (Exception e) {
        }
    }

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

    public static void add_caythong(String name, int quant) {
        synchronized (list_MamNguQua) {
            for (int i = 0; i < list_MamNguQua.size(); i++) {
                if (list_MamNguQua.get(i).name.equals(name)) {
                    list_MamNguQua.get(i).quant += quant;
                    list_MamNguQua.get(i).time = System.currentTimeMillis();
                    return;
                }
            }
            list_MamNguQua.add(new BXH_DoiQua(name, quant, System.currentTimeMillis()));
        }
    }

    public static void LoadDB(JSONObject jsob) {
        synchronized (list_MamNguQua) {
            list_MamNguQua.clear();
            long t_ = System.currentTimeMillis();
            JSONArray jsar_1 = (JSONArray) JSONValue.parse(jsob.get("list_mam_ngu_qua").toString());
            for (int i = 0; i < jsar_1.size(); i++) {
                JSONArray jsar_2 = (JSONArray) JSONValue.parse(jsar_1.get(i).toString());
                list_MamNguQua.add(new BXH_DoiQua(jsar_2.get(0).toString(), Integer.parseInt(jsar_2.get(1).toString()), t_));
            }
            jsar_1.clear();
        }
    }

    public static JSONObject SaveData() {
        synchronized (list_MamNguQua) {
            JSONArray jsar_1 = new JSONArray();
            for (BXH_DoiQua bxhDoiQua : list_MamNguQua) {
                JSONArray jsar_2 = new JSONArray();
                jsar_2.add(bxhDoiQua.name);
                jsar_2.add(bxhDoiQua.quant);
                jsar_1.add(jsar_2);
            }
            //
            JSONObject jsob = new JSONObject();
            jsob.put("list_mam_ngu_qua", jsar_1);
            return jsob;
        }
    }

    public static void sort_bxh() {
        synchronized (list_MamNguQua) {
            Collections.sort(list_MamNguQua, new Comparator<BXH_DoiQua>() {
                @Override
                public int compare(BXH_DoiQua o1, BXH_DoiQua o2) {
                    int compare = (o1.quant == o2.quant) ? 0 : ((o1.quant > o2.quant) ? -1 : 1);
                    if (compare != 0) {
                        return compare;
                    }
                    return (o1.time > o2.time) ? 1 : -1;
                }
            });
        }
    }

    public static String[] get_top() {
        synchronized (list_MamNguQua) {
            if (list_MamNguQua.isEmpty()) {
                return new String[]{"Chưa có thông tin"};
            }
            String[] top;
            if (list_MamNguQua.size() < 10) {
                top = new String[list_MamNguQua.size()];
            } else {
                top = new String[10];
            }
            for (int i = 0; i < top.length; i++) {
                top[i] = "Top " + (i + 1) + " : " + list_MamNguQua.get(i).name + " : " + list_MamNguQua.get(i).quant + " lần";
            }
            return top;
        }
    }

    public static boolean isBuyItemSK(Session conn, int cat, int idbuy, int quant) throws IOException {
        List<Integer> it3 = new ArrayList<>(java.util.Arrays.asList(4714, 4715, 4769, 4770, 4771, 4772, 4716, 4717, 4718, 4719));
        //System.out.println("event.Event_2.isBuyItemSK()"+idbuy);
        if (cat == 3 && it3.contains(idbuy)) {
            if (conn.p.item.get_inventory_able() < 1) {
                Service.send_notice_box(conn, "Hàng trang đầy");
                return true;
            }
            if ((conn.p.get_ngoc() < 500 && idbuy == 4762) || (conn.p.get_ngoc() < 50 && idbuy != 4762)) {
                Service.send_notice_box(conn, "Không đủ ngọc");
                return true;
            }
            conn.p.update_ngoc(idbuy == 4762 ? -500 : -50);
            Item3 itInventory = new Item3();
            itInventory.id = (short) idbuy;
            itInventory.clazz = ItemTemplate3.item.get(idbuy).getClazz();
            itInventory.type = ItemTemplate3.item.get(idbuy).getType();
            itInventory.level = ItemTemplate3.item.get(idbuy).getLevel();
            itInventory.icon = ItemTemplate3.item.get(idbuy).getIcon();
            itInventory.color = ItemTemplate3.item.get(idbuy).getColor();
            itInventory.part = ItemTemplate3.item.get(idbuy).getPart();
            itInventory.islock = true;
            itInventory.name = ItemTemplate3.item.get(idbuy).getName();
            itInventory.tier = 0;
            itInventory.op = new ArrayList<>();
            if (idbuy != 4762)
                itInventory.op.addAll(ItemTemplate3.item.get(idbuy).getOp());
            itInventory.time_use = 0;
            itInventory.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 72 * (idbuy == 4762 ? 0 : 7);

            conn.p.item.add_item_inventory3(itInventory);
            Service.send_notice_box(conn, "Bạn đã mua thành công vật phẩm " + itInventory.name);
            return true;
        }
        return false;
    }
}
