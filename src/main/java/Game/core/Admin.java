package Game.core;

import Game.Helps.ItemStar;
import Game.client.Player;
import Game.io.Session;
import Game.template.*;
import Game.Helps.Medal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Admin {
    public static HashMap<String, Integer> topLevel = new HashMap<>();
    public static HashMap<String, Integer> topEvent = new HashMap<>();

    public static void randomMedal(Player p, byte color_, byte tier_, boolean isLock) {
        ItemTemplate3 temp = ItemTemplate3.item.get(Util.random(4587, 4591));
        Item3 itbag = new Item3();
        itbag.id = temp.getId();
        itbag.clazz = temp.getClazz();
        itbag.type = temp.getType();
        itbag.level = 1; // level required
        itbag.icon = temp.getIcon();
        itbag.color = color_;
        itbag.part = temp.getPart();
        itbag.islock = isLock;
        itbag.name = temp.getName();
        itbag.tier = 0;
        //
        List<Option> opnew = new ArrayList<>();
        byte typest = (byte) Util.random(0, 5);
        int _st;
        byte dongan;
        if (color_ == 0) {
            _st = Util.random(130, 140);
            dongan = (byte) Util.random(5, 7);
        } else if (color_ == 1) {
            _st = Util.random(130, 150);
            dongan = (byte) Util.random(5, 8);
        } else if (color_ == 2) {
            _st = Util.random(140, 160);
            dongan = (byte) Util.random(5, 8);
        } else if (color_ == 3) {
            _st = Util.random(150, 170);
            dongan = (byte) Util.random(5, 9);
        } else {
            _st = Util.random(160, 180);
            dongan = (byte) Util.random(7, 11);
        }
        // thêm dòng st gốc
        opnew.add(new Option(typest, _st, itbag.id));
        opnew.add(new Option(96, dongan, itbag.id));
        //
        itbag.op = new ArrayList<>();
        itbag.opMedal = Medal.CreateMedal(dongan, color_, itbag.id);
        itbag.op.addAll(opnew);
        itbag.time_use = 0;
        itbag.item_medal = new short[5];

        int material_type_1st = Util.random(0, 7);
        int material_type_2nd = Util.random(0, 7);
        while (material_type_1st == material_type_2nd) {
            material_type_2nd = Util.random(0, 7);
        }
        itbag.item_medal[0] = (short) (MaterialMedal.m_white[material_type_1st][Util.random(0, 10)] + 200);
        itbag.item_medal[1] = (short) (MaterialMedal.m_white[material_type_2nd][Util.random(0, 10)] + 200);
        itbag.item_medal[2] = (short) (MaterialMedal.m_blue[Util.random(0, 10)] + 200);
        itbag.item_medal[3] = (short) (MaterialMedal.m_yellow[Util.random(0, 10)] + 200);
        itbag.item_medal[4] = (short) (MaterialMedal.m_violet[Util.random(0, 10)] + 200);

        for (byte i = 0; i < tier_; i++) {
            itbag.tier = (byte) (i + 1);
            Medal.UpgradeMedal(itbag);
        }
        p.item.add_item_inventory3(itbag);
    }

    public static void randomTT(Session conn, byte color, byte type) throws IOException {
        short type_item = ItemStar.ConvertType(type, conn.p.clazz);
        short id_item = ItemStar.GetIDItem(type, conn.p.clazz);
        List<Option> ops = ItemStar.GetOpsItemStar(conn.p.clazz, (byte) type_item, 0);

        Item3 itbag = new Item3();
        itbag.id = id_item;
        itbag.name = ItemTemplate3.item.get(id_item).getName();
        itbag.clazz = ItemTemplate3.item.get(id_item).getClazz();
        itbag.type = ItemTemplate3.item.get(id_item).getType();
        itbag.level = 45;
        itbag.icon = ItemTemplate3.item.get(id_item).getIcon();
        itbag.op = new ArrayList<>();
        for (Option o : ops) {
            int pr = o.getParam(0);
            int pr1 = (int) (pr * color * 0.25);
            if ((o.id >= 58 && o.id <= 60) || (o.id >= 100 && o.id <= 107))
                itbag.op.add(new Option(o.id, pr, itbag.id));
            else if (o.id == 37 || o.id == 38) {
                itbag.op.add(new Option(o.id, 2, itbag.id));
            } else
                itbag.op.add(new Option(o.id, pr1, itbag.id));
        }
        int[] opAo = {-111, -110, -109, -108, -107};
        int[] opNon = {-102, -113, -105};
        int[] opVK = {-101, -113, -86, -84, -82, -80};
        int[] opNhan = {-89, -87, -104, -86, -84, -82, -80};
        int[] opDayChuyen = {-87, -105, -103, -91};
        int[] opGang = {-89, -103, -91};
        int[] opGiay = {-104, -103, -91};

        if (color == 4) {
            if (itbag.type == 0 || itbag.type == 1) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opAo[Util.nextInt(opAo.length)];
                    int opid2 = opAo[Util.nextInt(opAo.length)];
                    while (opid1 == opid2) {
                        opid1 = opAo[Util.nextInt(opAo.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opAo[Util.nextInt(opAo.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 2) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNon[Util.nextInt(opNon.length)];
                    int opid2 = opNon[Util.nextInt(opNon.length)];
                    while (opid1 == opid2) {
                        opid1 = opNon[Util.nextInt(opNon.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNon[Util.nextInt(opNon.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 3) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGang[Util.nextInt(opGang.length)];
                    int opid2 = opGang[Util.nextInt(opGang.length)];
                    while (opid1 == opid2) {
                        opid1 = opGang[Util.nextInt(opGang.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opGang[Util.nextInt(opGang.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 4) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNhan[Util.nextInt(opNhan.length)];
                    int opid2 = opNhan[Util.nextInt(opNhan.length)];
                    while (opid1 == opid2) {
                        opid1 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNhan[Util.nextInt(opNhan.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 5) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    int opid2 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    while (opid1 == opid2) {
                        opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 6) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGiay[Util.nextInt(opGiay.length)];
                    int opid2 = opGiay[Util.nextInt(opGiay.length)];
                    while (opid1 == opid2) {
                        opid1 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opGiay[Util.nextInt(opGiay.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type > 6) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opVK[Util.nextInt(opVK.length)];
                    int opid2 = opVK[Util.nextInt(opVK.length)];
                    while (opid1 == opid2) {
                        opid1 = opVK[Util.nextInt(opVK.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opVK[Util.nextInt(opVK.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            }
        } else if (color == 5) {
            if (itbag.type == 0 || itbag.type == 1) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opAo[Util.nextInt(opAo.length)];
                    int opid2 = opAo[Util.nextInt(opAo.length)];
                    int opid3 = opAo[Util.nextInt(opAo.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opAo[Util.nextInt(opAo.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opAo[Util.nextInt(opAo.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opAo[Util.nextInt(opAo.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opAo[Util.nextInt(opAo.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 2) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNon[Util.nextInt(opNon.length)];
                    int opid2 = opNon[Util.nextInt(opNon.length)];
                    int opid3 = opNon[Util.nextInt(opNon.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opNon[Util.nextInt(opNon.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opNon[Util.nextInt(opNon.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opNon[Util.nextInt(opNon.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNon[Util.nextInt(opNon.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 3) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGang[Util.nextInt(opGang.length)];
                    int opid2 = opGang[Util.nextInt(opGang.length)];
                    int opid3 = opGang[Util.nextInt(opGang.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opGang[Util.nextInt(opGang.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opGang[Util.nextInt(opGang.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opGang[Util.nextInt(opGang.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                } else {
                    int opid = opGang[Util.nextInt(opGang.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 4) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNhan[Util.nextInt(opNhan.length)];
                    int opid2 = opNhan[Util.nextInt(opNhan.length)];
                    int opid3 = opNhan[Util.nextInt(opNhan.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNhan[Util.nextInt(opNhan.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 5) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    int opid2 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    int opid3 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 6) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGiay[Util.nextInt(opGiay.length)];
                    int opid2 = opGiay[Util.nextInt(opGiay.length)];
                    int opid3 = opGiay[Util.nextInt(opGiay.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opGiay[Util.nextInt(opGiay.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type > 7) {
                int percent = Util.nextInt(0, 100);
                if (percent > 90) {
                    int opid1 = opVK[Util.nextInt(opVK.length)];
                    int opid2 = opVK[Util.nextInt(opVK.length)];
                    int opid3 = opVK[Util.nextInt(opVK.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opVK[Util.nextInt(opVK.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opVK[Util.nextInt(opVK.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opVK[Util.nextInt(opVK.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opVK[Util.nextInt(opVK.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            }
        }
        itbag.color = color;
        itbag.part = ItemTemplate3.item.get(id_item).getPart();
        itbag.tier = 0;
        itbag.time_use = 0;
        itbag.islock = true;
        conn.p.item.add_item_inventory3(itbag);
        conn.p.item.char_inventory(3);
    }
    public static void setTop() {
        topEvent();
    }
    public static void topEvent() {
        topEvent.put("", 1);
    }
    public static void setThanhTich() {
        for (Map.Entry<String, Integer> entry : Manager.gI().thanh_tich.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (value == 0) {
                Manager.gI().ty_phu.add(key);
            } else if (value == 1) {
                Manager.gI().trieu_phu.add(key);
            } else {
                Manager.gI().dai_gia.add(key);
            }
        }
    }
    public static void quatopLevel(Session conn) throws IOException {
        if (!Admin.topLevel.containsKey(conn.p.name)) {
            Service.send_notice_box(conn, "Không có tên");
            return;
        }
        if (conn.p.item.get_inventory_able() < 5) {
            Service.send_notice_box(conn, "Hành trang cần tối thiểu 5 ô trống");
            return;
        }
        switch (Admin.topLevel.get(conn.p.name)) {
            case 1: {
                conn.p.update_ngoc(70000);
                conn.p.update_vang(100000000L, "Nhận %s vàng từ quà top Level");

                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 100;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(4580);
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                temp3 = ItemTemplate3.item.get(4706);// Tóc
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 10, false);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
            case 2: {
                conn.p.update_ngoc(40000);
                conn.p.update_vang(100000000L, "Nhận %s vàng từ quà top Level");

                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 50;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(4580);
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                temp3 = ItemTemplate3.item.get(4706);// Tóc
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, false);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
            case 3: {
                conn.p.update_ngoc(20000);
                conn.p.update_vang(80000000L, "Nhận %s vàng từ quà top Level");

                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 40;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(4584);
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                temp3 = ItemTemplate3.item.get(4706);// Tóc
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, true);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
            case 4: {
                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 30;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(Util.random(4577, 4585));
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, true);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
        }
        Manager.gI().notifierBot.sendNotification(conn.p.name + " đã nhận quà top level");
    }

    public static void quatopEvent(Session conn) throws IOException {
        if (!Admin.topEvent.containsKey(conn.p.name)) {
            Service.send_notice_box(conn, "Không có tên");
            return;
        }
        if (conn.p.item.get_inventory_able() < 10) {
            Service.send_notice_box(conn, "Hành trang cần tối thiểu 10 ô trống");
            return;
        }
        switch (Admin.topEvent.get(conn.p.name)) {
            case 1: {
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4640); // cánh
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                int id = 4812;
                if (conn.p.clazz <= 1) {
                    id = 4813;
                }
                temp3 = ItemTemplate3.item.get(id);// Thời trang rồng
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                it.op.add(new Option(7, 7000));
                it.op.add(new Option(8, 7000));
                it.op.add(new Option(9, 7000));
                it.op.add(new Option(10, 7000));
                it.op.add(new Option(11, 7000));
                it.op.add(new Option(27, 1500));
                it.op.add(new Option(-128, 500));
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                temp3 = ItemTemplate3.item.get(4617);// trứng rồng lửa
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 10, false);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topEvent.remove(conn.p.name);
                break;
            }
            case 2: {
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4641); // cánh
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                int id = 4812;
                if (conn.p.clazz <= 1) {
                    id = 4813;
                }
                temp3 = ItemTemplate3.item.get(id);// Thời trang rồng
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                it.op.add(new Option(7, 5000));
                it.op.add(new Option(8, 5000));
                it.op.add(new Option(9, 5000));
                it.op.add(new Option(10, 5000));
                it.op.add(new Option(11, 5000));
                it.op.add(new Option(27, 1500));
                it.op.add(new Option(-128, 500));

                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                temp3 = ItemTemplate3.item.get(4617);// trứng rồng lửa
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 8, false);
                randomTT(conn, (byte) 5, (byte) 7);
                randomTT(conn, (byte) 5, (byte) 7);
                randomTT(conn, (byte) 5, (byte) 7);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topEvent.remove(conn.p.name);
                break;
            }
            case 3: {
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4641); // cánh
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();
                it.expiry_date = 6L * 30 * 24 * 60 * 60 * 1000 + System.currentTimeMillis();
                conn.p.item.add_item_inventory3(it);

                int id = 4812;
                if (conn.p.clazz <= 1) {
                    id = 4813;
                }
                temp3 = ItemTemplate3.item.get(id);// Thời trang rồng
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                it.op.add(new Option(7, 4000));
                it.op.add(new Option(8, 4000));
                it.op.add(new Option(9, 4000));
                it.op.add(new Option(10, 4000));
                it.op.add(new Option(11, 4000));
                it.op.add(new Option(27, 1500));
                it.op.add(new Option(-128, 500));

                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, false);
                randomTT(conn, (byte) 5, (byte) 5);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topEvent.remove(conn.p.name);
                break;
            }
        }
        Manager.gI().notifierBot.sendNotification(conn.p.name + " đã nhận quà top event");
    }
}
