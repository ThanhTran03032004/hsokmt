package Game.core;

import Game.Helps.*;
import Game.History.His_KMB;
import Game.client.Player;
import Game.io.Session;
import Game.map.Map;
import Game.map.MapService;
import Game.Helps.Medal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Game.io.Message;
import Game.template.Item3;
import Game.template.Item47;
import Game.template.ItemTemplate3;
import Game.template.ItemTemplate7;
import Game.template.MaterialMedal;
import Game.template.Option;
import Game.template.Part_fashion;
import Game.template.Player_store;

public class GameSrc {

    public static byte[] percent = new byte[]{95, 90, 80, 70, 60, 50, 45, 35, 25, 15, 13, 5, 4, 3, 2, 1};
    public static short[] wing_upgrade_material_long_khuc_xuong = new short[]{10, 12, 15, 19, 24, 30, 37, 45, 54, 64,
        75, 87, 100, 114, 129, 145, 162, 180, 199, 219, 240, 262, 285, 309, 334, 360, 387, 415, 444, 474};
    public static short[] wing_upgrade_material_kim_loai = new short[]{20, 22, 25, 29, 34, 40, 47, 55, 64, 74, 85, 97,
        110, 124, 139, 155, 172, 190, 209, 229, 250, 272, 295, 319, 344, 370, 397, 425, 454, 484};
    public static short[] wing_upgrade_material_da_cuong_hoa = new short[]{50, 54, 60, 68, 78, 90, 104, 120, 138, 158,
        180, 204, 230, 258, 288, 320, 354, 390, 428, 468, 510, 554, 600, 648, 698, 750, 804, 860, 918, 978};
    public static int[] wing_upgrade_material_gold = new int[]{50000, 52000, 55000, 59000, 64000, 70000, 77000, 85000,
        94000, 104000, 115000, 127000, 140000, 154000, 169000, 185000, 202000, 220000, 239000, 259000, 280000, 302000,
        325000, 349000, 374000, 400000, 427000, 455000, 484000, 514000};
    public static int[] wing_upgrade_material_time = new int[]{2, 4, 6, 8, 12, 15, 20, 30, 45, 60, 100, 130, 150,
        180, 220, 242, 286, 322, 340, 380, 422, 456, 502, 550, 600, 652, 706, 742, 770, 830};

    public static void rebuild_item(Session conn, Message m2) throws IOException {
        byte type = m2.reader().readByte();
        short id = m2.reader().readShort();
        byte tem = m2.reader().readByte(); // type item insert
        // System.out.println(type);
        // System.out.println(id);
        // System.out.println(tem);
        if (tem == 7 && conn.p.id_item_rebuild == -1) {
            Service.send_notice_nobox_white(conn, "Hãy bỏ trang bị vào trước");
            return;
        }
        if (tem == 7) {
            Item3 selectedItem = getInventory3Item(conn, conn.p.id_item_rebuild);
            if (!hasValidRebuildTier(selectedItem)) {
                conn.p.id_item_rebuild = -1;
                conn.p.is_use_mayman = false;
                conn.p.id_use_mayman = -1;
                Service.send_notice_box(conn, "Hãy chọn lại trang bị muốn cường hóa");
                return;
            }
            if (id != 13 && id != 14 && id != 4) {
                Service.send_notice_box(conn, "Chỉ nên dùng cỏ 4 lá hoặc đá 3 màu");
                return;
            }
            Message m = new Message(67);
            m.writer().writeByte(type);
            int tier = selectedItem.tier;
            switch (type) {
                case 0: {
                    m.writer().writeShort(id);
                    conn.p.id_use_mayman = id;
                    m.writer().writeByte(7);
                    m.writer().writeUTF(GameSrc.percent[tier] + "% + 30%");
                    conn.p.is_use_mayman = true;
                    break;
                }
                case 1: {
                    m.writer().writeByte(7);
                    m.writer().writeUTF(GameSrc.percent[tier] + "%");
                    conn.p.is_use_mayman = false;
                    conn.p.id_use_mayman = -1;
                    break;
                }
                default: {
                    Service.send_notice_box(conn, "Chưa có chức năng");
                    return;
                }
            }
            conn.addmsg(m);
            m.cleanup();
        } else {
            Message m = new Message(67);
            Message m12 = null;
            boolean next = false;
            if (conn.ac_admin > 3 && Manager.BuffAdminMaterial) {
                next = true;
            }
            if (type == 2 && !hasValidRebuildTier(getInventory3Item(conn, conn.p.id_item_rebuild))) {
                conn.p.id_item_rebuild = -1;
                Service.send_notice_box(conn, "Hãy chọn lại trang bị muốn cường hóa");
                return;
            }
            if (type == 2 && conn.p.time_speed_rebuild > System.currentTimeMillis()) {
                Service.send_notice_nobox_white(conn, "Thao tác cường hóa quá nhanh");
                return;
            }
            if (type == 2 && !next && !checkmaterial(conn, conn.p.id_item_rebuild, tem)) {
                m.writer().writeByte(3);
                m.writer().writeUTF("Không đủ nguyên liệu");
            } else {
                m.writer().writeByte(type);
                switch (type) {
                    case 0: {
                        Item3 it = getInventory3Item(conn, id);
                        if (!hasValidRebuildTier(it) || !check_item_can_rebuild(it.type)) {
                            Service.send_notice_box(conn, "Trang bị không phù hợp");
                            return;
                        }
                        if ((it.id >= 4587 && it.id <= 4590) || Part_fashion.fashions.contains(it.id)) {
                            Service.send_notice_box(conn, "Trang bị không phù hợp");
                            return;
                        }
                        m.writer().writeShort(id);
                        conn.p.id_item_rebuild = id;
                        m.writer().writeByte(3);
                        m.writer().writeUTF(GameSrc.percent[it.tier] + "%");
                        // remove item may man
                        Message m1212 = new Message(67);
                        m1212.writer().writeByte(1);
                        m1212.writer().writeByte(7);
                        m1212.writer().writeUTF(GameSrc.percent[it.tier] + "%");
                        conn.addmsg(m1212);
                        m1212.cleanup();
                        conn.p.is_use_mayman = false;
                        conn.p.id_use_mayman = -1;
                        //
                        break;
                    }
                    case 1: {
                        m.writer().writeByte(3);
                        m.writer().writeUTF("HSO"); // 0,01%
                        conn.p.id_item_rebuild = -1;
                        break;
                    }
                    case 2: {
                        conn.p.time_speed_rebuild = System.currentTimeMillis() + 4500L;
                        Item3 it_upgrade = getInventory3Item(conn, conn.p.id_item_rebuild);
                        if (Part_fashion.fashions.contains(it_upgrade.id)) {
                            Service.send_notice_box(conn, "Trang bị không phù hợp");
                            return;
                        }
                        if (it_upgrade.tier > 14) {
                            return;
                        }
                        boolean percent;
                        int get_percent = GameSrc.percent[it_upgrade.tier];
                        if (it_upgrade.tier < 7) {
                            get_percent += 30;
                        }
                        if (conn.ac_admin > 3 && Manager.BuffAdmin) {
                            percent = true;
                        } else if (conn.p.is_use_mayman && (conn.p.id_use_mayman == 14 || conn.p.id_use_mayman == 13)) {
                            percent = (get_percent + 15) > Util.random(0, 100); //cũ là 120
                        } else {
                            percent = get_percent > Util.random(0, 100);
                        }
                        if (percent) {
                            conn.p.item.inventory3[conn.p.id_item_rebuild].tier++;
                            if (it_upgrade.tier >= 9 && conn.p.item.inventory3[conn.p.id_item_rebuild].type == 5) {
                                for (int i = 0; i < conn.p.item.inventory3[conn.p.id_item_rebuild].op.size(); i++) {
                                    if (conn.p.item.inventory3[conn.p.id_item_rebuild].op.get(i).id == 37
                                            || conn.p.item.inventory3[conn.p.id_item_rebuild].op.get(i).id == 38) {
                                        conn.p.item.inventory3[conn.p.id_item_rebuild].op.get(i).setParam(3);
                                    }
                                }
                            }
                        } else {
                            if (conn.p.is_use_mayman && (conn.p.id_use_mayman == 14 || conn.p.id_use_mayman == 13)) {
                                if (conn.p.id_use_mayman == 13) {
                                    if (it_upgrade.tier > 6 && it_upgrade.tier != 11) {
                                        it_upgrade.tier -= 1;
                                    }
                                }
                            } else {
                                if (it_upgrade.tier >= 11) {
                                    it_upgrade.tier = 11;
                                } else if (it_upgrade.tier >= 6) {
                                    it_upgrade.tier = 6;
                                } else if (it_upgrade.tier >= 1) {
                                    it_upgrade.tier = 1;
                                }
                            }
                        }
                        if (conn.p.is_use_mayman
                                && (conn.p.id_use_mayman == 14 || conn.p.id_use_mayman == 13 || conn.p.id_use_mayman == 4)) {
                            conn.p.item.remove(7, conn.p.id_use_mayman, 1);
                            conn.p.item.char_inventory(7);
                            if (conn.p.item.total_item_by_id(7, conn.p.id_use_mayman) < 1) {
                                conn.p.is_use_mayman = false;
                                conn.p.id_use_mayman = -1;
                            }
                        }
                        conn.p.item.char_inventory(4);
                        conn.p.item.char_inventory(7);
                        conn.p.item.char_inventory(3);
                        String per = GameSrc.percent[it_upgrade.tier] + "%";
                        if (tem == 0) { // vang
                            if (!percent) {
                                m.writer().writeByte(4);
                                m.writer().writeUTF("Ta rất tiếc, quá trình cường hóa đã thất bại.");
                            } else {
                                m.writer().writeByte(3);
                                m.writer().writeUTF("Mi đã cường hóa thành công " + it_upgrade.name + "!");
                            }
                        } else if (tem == 1) { // ngoc
                            if (!percent) {
                                m.writer().writeByte(4);
                                m.writer().writeUTF("Ta rất tiếc, quá trình cường hóa đã thất bại.");
                            } else {
                                m.writer().writeByte(3);
                                m.writer().writeUTF("Mi đã cường hóa thành công " + it_upgrade.name + "!");
                            }
                        }
                        if (it_upgrade.tier < 15) {
                            m12 = new Message(67);
                            m12.writer().writeByte(0);
                            m12.writer().writeShort(conn.p.id_item_rebuild);
                            m12.writer().writeByte(3);
                            m12.writer().writeUTF(per);
                        }
                        break;
                    }
                }
            }
            conn.addmsg(m);
            m.cleanup();
            if (m12 != null) {
                conn.addmsg(m12);
                m12.cleanup();
            }
        }
    }

    private static Item3 getInventory3Item(Session conn, int index) {
        if (conn == null || conn.p == null || conn.p.item == null || conn.p.item.inventory3 == null
                || index < 0 || index >= conn.p.item.inventory3.length) {
            return null;
        }
        return conn.p.item.inventory3[index];
    }

    private static boolean hasValidRebuildTier(Item3 item) {
        return item != null && item.tier >= 0 && item.tier < percent.length;
    }

    private static boolean check_item_can_rebuild(byte type) {
        switch (type) {
            case 0: // coat
            case 1: // pant
            case 2: // crown
            case 3: // grove
            case 4: // ring
            case 5: // chain
            case 6: // shoes
            // case 7: // wing
            //case 15: // fashion
            case 8:
            case 9:
            case 10:
            case 16:
            //case 21:
            //case 22:
            //case 23:
            //case 24:
            //case 25:
            //case 26:
            case 11: {
                return true;
            }
        }
        return false;
    }

    private static boolean checkmaterial(Session conn, short id, byte tem) throws IOException {
        Item3 item = getInventory3Item(conn, id);
        if (!hasValidRebuildTier(item) || (tem != 0 && tem != 1)) {
            return false;
        }

        int goldCost;
        int gemCost;
        short[] materialIds;
        if (item.tier <= 2) {
            goldCost = 5_000;
            gemCost = 5;
            materialIds = new short[]{0, 1};
        } else if (item.tier <= 5) {
            goldCost = 6_000;
            gemCost = 7;
            materialIds = new short[]{0, 1};
        } else if (item.tier <= 8) {
            goldCost = 8_000;
            gemCost = 10;
            materialIds = new short[]{0, 1, 2};
        } else if (item.tier <= 10) {
            goldCost = 10_000;
            gemCost = 15;
            materialIds = new short[]{0, 1, 2};
        } else {
            goldCost = 10_000;
            gemCost = 20;
            materialIds = new short[]{0, 1, 2, 3};
        }

        if ((tem == 0 && conn.p.get_vang() < goldCost)
                || (tem == 1 && conn.p.get_ngoc() < gemCost)) {
            return false;
        }
        for (short materialId : materialIds) {
            if (conn.p.item.total_item_by_id(7, materialId) < 1) {
                return false;
            }
        }

        if (tem == 0) {
            conn.p.update_vang(-goldCost, "Trừ %s vàng nâng cấp trang bị");
        } else {
            conn.p.update_ngoc(-gemCost);
        }
        for (short materialId : materialIds) {
            conn.p.item.remove(7, materialId, 1);
        }
        updatematerial(conn, 7, materialIds);
        return true;
    }

    private static void updatematerial(Session conn, int b, short[] id) throws IOException {
        conn.p.item.char_inventory(7);
        for (short materialId : id) {
            Message m = new Message(16);
            m.writer().writeByte(1);
            m.writer().writeByte(b);
            m.writer().writeLong(conn.p.get_vang());
            m.writer().writeInt(conn.p.get_ngoc());
            m.writer().writeByte(b);
            m.writer().writeShort(materialId);
            m.writer().writeShort(conn.p.item.total_item_by_id(b, materialId));
            m.writer().writeByte(1);
            m.writer().writeByte(0);
            conn.addmsg(m);
            m.cleanup();
        }
    }

    public synchronized static void trade_process(Session conn, Message m2) throws IOException {
        if (1 == 1) {
            Service.send_notice_box(conn, "Chức năng bảo trì");
            return;
        }
        byte type = m2.reader().readByte();
//        switch (type) {
//            case 0: {
//                Player p0 = Map.get_player_by_name(m2.reader().readUTF());
//                if (p0 != null) {
//                    if (p0.list_item_trade == null) {
//                        Message m = new Message(36);
//                        m.writer().writeByte(0);
//                        m.writer().writeUTF(conn.p.name);
//                        p0.conn.addmsg(m);
//                        m.cleanup();
//                    } else {
//                        Service.send_notice_box(conn, "Đối phương đang có giao dịch");
//                    }
//                } else {
//                    Service.send_notice_box(conn, "Xảy ra lỗi");
//                }
//                break;
//            }
//            case 1: {
//                Message m = new Message(36);
//                m.writer().writeByte(1);
//                Player p0 = Map.get_player_by_name(m2.reader().readUTF());
//                if (p0 == null) {
//                    return;
//                }
//                m.writer().writeUTF(p0.name);
//                conn.p.name_trade = p0.name;
//                p0.name_trade = conn.p.name;
//                conn.addmsg(m);
//                m.cleanup();
//                //
//                m = new Message(36);
//                m.writer().writeByte(1);
//                m.writer().writeUTF(conn.p.name);
//                p0.conn.addmsg(m);
//                m.cleanup();
//                p0.list_item_trade = new short[9];
//                conn.p.list_item_trade = new short[9];
//                for (int i = 0; i < conn.p.list_item_trade.length; i++) {
//                    conn.p.list_item_trade[i] = -1;
//                    p0.list_item_trade[i] = -1;
//                }
//                break;
//            }
//            case 2: {
//                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                if (p0 == null) {
//                    return;
//                }
//                int index_item_trade = -1;
//                for (int i = 0; i < conn.p.list_item_trade.length; i++) {
//                    if (conn.p.list_item_trade[i] == -1) {
//                        index_item_trade = i;
//                        break;
//                    }
//                }
//                if (index_item_trade == -1) {
//                    Service.send_notice_box(conn, "Không thể thêm đồ");
//                    return;
//                }
//                byte typeit = m2.reader().readByte();
//                short idit = m2.reader().readShort();
//                switch (typeit) {
//                    case 3: {
//                        conn.p.list_item_trade[index_item_trade] = idit;
//                        Message m = new Message(36);
//                        m.writer().writeByte(2);
//                        m.writer().writeByte(1); // index
//                        m.writer().writeByte(typeit);
//                        m.writer().writeShort(-1);
//                        Item3 it3 = conn.p.item.inventory3[idit];
//                        m.writer().writeShort(it3.icon);
//                        m.writer().writeUTF(it3.name);
//                        m.writer().writeByte(it3.color);
//                        m.writer().writeByte(conn.p.clazz);
//                        m.writer().writeShort(it3.level);
//                        m.writer().writeByte(it3.tier); // tier
//                        m.writer().writeByte(it3.op.size());
//                        for (int i = 0; i < it3.op.size(); i++) {
//                            m.writer().writeByte(it3.op.get(i).id);
//                            m.writer().writeInt(it3.op.get(i).getParam(it3.tier));
//                        }
//                        p0.conn.addmsg(m);
//                        m.cleanup();
//                        break;
//                    }
//                }
//                break;
//            }
//            case 3: {
//                byte typeit = m2.reader().readByte();
//                short idit = m2.reader().readShort();
//                switch (typeit) {
//                    case 3: {
//                        for (Short itbuffer : conn.p.list_item_trade) {
//                            if (itbuffer > -1) {
//                                Message m = new Message(36);
//                                m.writer().writeByte(3);
//                                m.writer().writeByte(1); // index
//                                m.writer().writeByte(typeit);
//                                m.writer().writeShort(-1);
//                                Item3 it3 = conn.p.item.inventory3[itbuffer];
//                                m.writer().writeShort(it3.icon);
//                                m.writer().writeUTF(it3.name);
//                                m.writer().writeByte(it3.color);
//                                m.writer().writeByte(conn.p.clazz);
//                                m.writer().writeShort(it3.level);
//                                m.writer().writeByte(it3.tier); // tier
//                                m.writer().writeByte(it3.op.size());
//                                for (int i = 0; i < it3.op.size(); i++) {
//                                    m.writer().writeByte(it3.op.get(i).id);
//                                    m.writer().writeInt(it3.op.get(i).getParam(it3.tier));
//                                }
//                                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                                if (p0 == null) {
//                                    return;
//                                }
//                                p0.conn.addmsg(m);
//                                m.cleanup();
//                            }
//                        }
//                        for (int i = 0; i < conn.p.list_item_trade.length; i++) {
//                            if (conn.p.list_item_trade[i] == idit) {
//                                conn.p.list_item_trade[i] = -1;
//                                break;
//                            }
//                        }
//                        for (Short itbuffer : conn.p.list_item_trade) {
//                            if (itbuffer > -1) {
//                                Message m = new Message(36);
//                                m.writer().writeByte(2);
//                                m.writer().writeByte(1); // index
//                                m.writer().writeByte(typeit);
//                                m.writer().writeShort(-1);
//                                Item3 it3 = conn.p.item.inventory3[itbuffer];
//                                m.writer().writeShort(it3.icon);
//                                m.writer().writeUTF(it3.name);
//                                m.writer().writeByte(it3.color);
//                                m.writer().writeByte(conn.p.clazz);
//                                m.writer().writeShort(it3.level);
//                                m.writer().writeByte(it3.tier); // tier
//                                m.writer().writeByte(it3.op.size());
//                                for (int i = 0; i < it3.op.size(); i++) {
//                                    m.writer().writeByte(it3.op.get(i).id);
//                                    m.writer().writeInt(it3.op.get(i).getParam(it3.tier));
//                                }
//                                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                                if (p0 == null) {
//                                    return;
//                                }
//                                p0.conn.addmsg(m);
//                                m.cleanup();
//                            }
//                        }
//                        break;
//                    }
//                }
//                break;
//            }
//            case 4: {
//                conn.p.lock_trade = true;
//                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                if (p0 == null) {
//                    return;
//                }
//                Service.send_notice_nobox_white(p0.conn, (conn.p.name + " khóa giao dịch"));
//                if (conn.p.lock_trade && p0.lock_trade) {
//                    Message m = new Message(36);
//                    m.writer().writeByte(4);
//                    m.writer().writeByte(2);
//                    p0.conn.addmsg(m);
//                    conn.addmsg(m);
//                    m.cleanup();
//                }
//                break;
//            }
//            case 5: {
//                conn.p.accept_trade = true;
//                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                if (p0 == null) {
//                    return;
//                }
//                Service.send_notice_nobox_white(p0.conn, (conn.p.name + " xác nhận giao dịch"));
//                if (conn.p.accept_trade && p0.accept_trade) {
//                    //
//                    if (conn.p.money_trade > 0) {
//                        if (conn.p.get_vang() < conn.p.money_trade) {
//                            Service.send_notice_box(conn, "Không đủ vàng");
//                            return;
//                        }
//                        conn.p.update_vang(-conn.p.money_trade);
//                        p0.update_vang(conn.p.money_trade);
//                        Log.gI().add_log(p0.name,
//                                "giao dịch với " + conn.p.name + " nhận " + Util.number_format(conn.p.money_trade) + " vàng");
//                        conn.p.money_trade = 0;
//                    }
//                    if (p0.money_trade > 0) {
//                        if (p0.get_vang() < p0.money_trade) {
//                            Service.send_notice_box(p0.conn, "Không đủ vàng");
//                            return;
//                        }
//                        conn.p.update_vang(p0.money_trade);
//                        p0.update_vang(-p0.money_trade);
//                        Log.gI().add_log(conn.p.name,
//                                "giao dịch với " + p0.name + " nhận " + Util.number_format(p0.money_trade) + " vàng");
//                        p0.money_trade = 0;
//                    }
//                    // trade item p1
//                    List<Item3> itembag3buffer = new ArrayList<>();
//                    for (short itbuffer : conn.p.list_item_trade) {
//                        if (itbuffer > -1) {
//                            itembag3buffer.add(conn.p.item.inventory3[itbuffer]);
//                        }
//                    }
//                    for (Item3 it : itembag3buffer) {
//                        for (int i = 0; i < p0.item.inventory3.length; i++) {
//                            if (p0.item.inventory3[i] == null) {
//                                p0.item.inventory3[i] = it;
//                                break;
//                            }
//                        }
//                    }
//                    for (short itbuffer : conn.p.list_item_trade) {
//                        if (itbuffer > -1) {
//                            conn.p.item.inventory3[itbuffer] = null;
//                        }
//                    }
//                    // trade item p2
//                    itembag3buffer.clear();
//                    itembag3buffer = new ArrayList<>();
//                    for (short itbuffer : p0.list_item_trade) {
//                        if (itbuffer > -1) {
//                            itembag3buffer.add(p0.item.inventory3[itbuffer]);
//                        }
//                    }
//                    for (Item3 it : itembag3buffer) {
//                        for (int i = 0; i < conn.p.item.inventory3.length; i++) {
//                            if (conn.p.item.inventory3[i] == null) {
//                                conn.p.item.inventory3[i] = it;
//                                break;
//                            }
//                        }
//                    }
//                    for (short itbuffer : p0.list_item_trade) {
//                        if (itbuffer > -1) {
//                            p0.item.inventory3[itbuffer] = null;
//                        }
//                    }
//                    p0.item.char_inventory(4);
//                    conn.p.item.char_inventory(4);
//                    p0.item.char_inventory(7);
//                    conn.p.item.char_inventory(7);
//                    p0.item.char_inventory(3);
//                    conn.p.item.char_inventory(3);
//                    //
//                    Message m = new Message(36);
//                    m.writer().writeByte(5);
//                    m.writer().writeByte(2);
//                    p0.conn.addmsg(m);
//                    conn.addmsg(m);
//                    m.cleanup();
//                    //
//                    conn.p.name_trade = "";
//                    p0.name_trade = "";
//                    conn.p.lock_trade = false;
//                    p0.lock_trade = false;
//                    conn.p.money_trade = 0;
//                    p0.money_trade = 0;
//                    conn.p.accept_trade = false;
//                    p0.accept_trade = false;
//                    conn.p.list_item_trade = null;
//                    p0.list_item_trade = null;
//                } else {
//                    Message m = new Message(36);
//                    m.writer().writeByte(5);
//                    m.writer().writeByte(1);
//                    p0.conn.addmsg(m);
//                    m.cleanup();
//                    m = new Message(36);
//                    m.writer().writeByte(5);
//                    m.writer().writeByte(0);
//                    conn.addmsg(m);
//                    m.cleanup();
//                }
//                break;
//            }
//            case 6: {
//                Message m = new Message(36);
//                m.writer().writeByte(6);
//                conn.addmsg(m);
//                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                if (p0 == null) {
//                    return;
//                }
//                p0.conn.addmsg(m);
//                m.cleanup();
//                conn.p.name_trade = "";
//                p0.name_trade = "";
//                conn.p.lock_trade = false;
//                p0.lock_trade = false;
//                conn.p.money_trade = 0;
//                p0.money_trade = 0;
//                conn.p.accept_trade = false;
//                p0.accept_trade = false;
//                conn.p.list_item_trade = null;
//                p0.list_item_trade = null;
//                break;
//            }
//            case 7: {
//                int money = m2.reader().readInt();
//                if (money < 0) {
//                    money = 0;
//                }
//                if (money > conn.p.get_vang()) {
//                    money = (int) conn.p.get_vang();
//                }
//                if (money > 2_000_000_000) {
//                    money = 2_000_000_000;
//                }
//                conn.p.money_trade = money;
//                Message m = new Message(36);
//                m.writer().writeByte(7);
//                m.writer().writeInt(money);
//                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                if (p0 == null) {
//                    return;
//                }
//                p0.conn.addmsg(m);
//                m.cleanup();
//                break;
//            }
//            case 9: {
//                String text = m2.reader().readUTF();
//                Message m = new Message(36);
//                m.writer().writeByte(9);
//                m.writer().writeUTF(text);
//                Player p0 = Map.get_player_by_name(conn.p.name_trade);
//                if (p0 == null) {
//                    return;
//                }
//                p0.conn.addmsg(m);
//                m.cleanup();
//                break;
//            }
//        }
    }

    public static void replace_item_process(Player p, Message m2) throws IOException {
        byte type = m2.reader().readByte();
        short id = m2.reader().readShort();
        if (type == 0) {
            if (p.item.inventory3[id] != null && !check_item_can_rebuild(p.item.inventory3[id].type)) {
                Service.send_notice_box(p.conn, "Không phù hợp");
                return;
            }
            if (p.item.inventory3[id] != null && Part_fashion.fashions.contains(p.item.inventory3[id].id)) {
                Service.send_notice_box(p.conn, "Trang bị không phù hợp");
                return;
            }
            if (p.item_replace == -1) {
                if (p.item.inventory3[id] != null && CheckItem.isMeDay(p.item.inventory3[id].id)) {
                    Service.send_notice_box(p.conn, "Trang bị không phù hợp");
                    return;
                }
                if (p.item.inventory3[id] != null && p.item.inventory3[id].tier < 6) {
                    Service.send_notice_box(p.conn, "Bỏ vật phẩm trên +6 vào trước");
                    return;
                }
                if (p.item.inventory3[id] != null) {
                    if (p.item.inventory3[id].tier <= 10 && p.item.total_item_by_id(7, 346) < 1) {
                        Service.send_notice_box(p.conn, "Không đủ đá thạch anh cấp 1.");
                        return;
                    } else if (p.item.inventory3[id].tier > 10 && p.item.inventory3[id].tier <= 13 && p.item.total_item_by_id(7, 347) < 1) {
                        Service.send_notice_box(p.conn, "Không đủ đá thạch anh cấp 2.");
                        return;
                    } else if (p.item.inventory3[id].tier > 13 && p.item.inventory3[id].tier <= 15 && p.item.total_item_by_id(7, 348) < 1) {
                        Service.send_notice_box(p.conn, "Không đủ đá thạch anh cấp 3.");
                        return;
                    }
                }
                p.item_replace = id;
            } else if (p.item_replace2 == -1) {
                if (p.item.inventory3[id].tier > 0) {
                    Service.send_notice_box(p.conn, "Chỉ có thể bỏ vật phẩm +0 vào");
                    return;
                }
                if (p.item.inventory3[p.item_replace].type != p.item.inventory3[id].type) {
                    Service.send_notice_box(p.conn, "Chỉ có thể chuyển hóa trang bị cùng loại");
                    return;
                }
                if (p.item.inventory3[p.item_replace].level > p.item.inventory3[id].level) {
                    Service.send_notice_box(p.conn, "Chỉ có thể chuyển hóa trang bị level cao hơn");
                    return;
                }
                p.item_replace2 = id;
            } else {
                Service.send_notice_box(p.conn, "Lỗi, hãy thử lại");
                return;
            }
            Message m = new Message(73);
            m.writer().writeByte(0);
            m.writer().writeShort(id);
            if (p.item_replace2 == -1) {
                m.writer().writeByte(1);
            } else {
                m.writer().writeByte(0);
            }
            p.conn.addmsg(m);
            m.cleanup();
        } else if (type == 1) {
            if (p.item.inventory3[p.item_replace].tier < p.item.inventory3[p.item_replace2].tier) {
                return;
            }
            if (!p.item.inventory3[p.item_replace2].islock) {
                p.item.inventory3[p.item_replace2].islock = true;
                p.item.inventory3[p.item_replace2].name
                        = ItemTemplate3.item.get(p.item.inventory3[p.item_replace2].id).getName() + " [Khóa]";
                p.item.inventory3[p.item_replace2].UpdateName();
                p.item.char_inventory(4);
                p.item.char_inventory(7);
                p.item.char_inventory(3);
            }
            Service.send_box_input_yesno(p.conn, 122,
                    "Bạn có chắc chắn muốn chuyển hóa?");
        }
    }

    private static int get_ngoc_medal_upgrade(byte tier) {
        int[] result = new int[]{200, 400, 450, 500, 550, 600, 700, 800, 900, 3000, 3500, 4000, 4500, 5000, 6000};
        return result[tier];
    }

    private static int get_quant_material_medal_upgrade(byte tier) {
        return (2 + tier);
    }

    public static void Create_Medal(Session conn, Message m2) {
        try {
            byte type = m2.reader().readByte();
            short id = -1;
            byte tem = -1;
            try {
                id = m2.reader().readShort();
                tem = m2.reader().readByte();
            } catch (IOException e) {
            }
            if (id != -1 && tem == 7) {
                byte type_item = ItemTemplate7.item.get(id).getType();
                if (type_item != 54) {
                    Service.send_notice_box(conn, "Vật phẩm không hợp lệ");
                    return;
                }
            }
            switch (type) {
                case 0: { // fusion
                    if (tem == 7) {
                        if (id >= 246 && id < 346) {
                            Service.send_notice_box(conn, "Cấp vật phẩm đã đạt tối đa!");
                            return;
                        }
                        int quant = conn.p.item.total_item_by_id(7, id);
                        if (quant < 5) {
                            Service.send_notice_box(conn, "Chưa đủ nguyên liệu để hợp thành tối thiểu 1 vật phẩm!");
                        } else {
                            Message m = new Message(-105);
                            m.writer().writeByte(0);
                            m.writer().writeByte(5);
                            m.writer().writeShort(id);
                            conn.addmsg(m);
                            m.cleanup();
                        }
                    } else if (tem == 3) {
                        Item3 it_temp = conn.p.item.inventory3[id];
                        if (it_temp != null && it_temp.tier < 15
                                && (it_temp.id == 4587 || it_temp.id == 4588 || it_temp.id == 4589 || it_temp.id == 4590)) {
                            Message m_send = new Message(-105);
                            m_send.writer().writeByte(4);
                            m_send.writer().writeByte(5);
                            for (int i = 0; i < 5; i++) {
                                m_send.writer().writeShort(it_temp.item_medal[i]);
                                if (conn.version >= 270) {
                                    m_send.writer().writeShort(get_quant_material_medal_upgrade(it_temp.tier));
                                } else {
                                    m_send.writer().writeByte(get_quant_material_medal_upgrade(it_temp.tier));
                                }
                            }
                            conn.addmsg(m_send);
                            m_send.cleanup();
                        } else {
                            Service.send_notice_box(conn, "Lỗi hãy thử lại!");
                        }
                    }
                    break;
                }
                case 2: {
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Hành trang đầy!");
                        return;
                    }
                    if (id >= 246 && id < 346) {
                        Service.send_notice_box(conn, "Cấp vật phẩm đã đạt tối đa!");
                        return;
                    }
                    String name = ItemTemplate7.item.get(id).getName();
                    Service.send_box_input_text(conn, 7, name, new String[]{"Số lượng"});
                    if (conn.p.fusion_material_medal_id == -1) {
                        conn.p.fusion_material_medal_id = id;
                    }
                    break;
                }
                case 3: { // create
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Hành trang đầy!");
                        return;
                    }
                    if (System.currentTimeMillis() < conn.p.time_speed_rebuild) {
                        Service.send_notice_box(conn, "Thao tác quá nhanh");
                        return;
                    }
                    if (conn.p.id_medal_is_created != -1) {
                        if (Manager.BuffAdminMaterial && conn.ac_admin > 3) {
                        } else if (conn.p.item.total_item_by_id(7,
                                conn.p.medal_create_material[5 * conn.p.id_medal_is_created]) < 1) {
                            Service.send_notice_box(conn, "Chưa đủ nguyên liệu!");
                            return;
                        } else if (conn.p.item.total_item_by_id(7,
                                conn.p.medal_create_material[1 + 5 * conn.p.id_medal_is_created]) < 1) {
                            Service.send_notice_box(conn, "Chưa đủ nguyên liệu!");
                            return;
                        } else if (conn.p.item.total_item_by_id(7,
                                conn.p.medal_create_material[2 + 5 * conn.p.id_medal_is_created]) < 1) {
                            Service.send_notice_box(conn, "Chưa đủ nguyên liệu!");
                            return;
                        } else if (conn.p.item.total_item_by_id(7,
                                conn.p.medal_create_material[3 + 5 * conn.p.id_medal_is_created]) < 1) {
                            Service.send_notice_box(conn, "Chưa đủ nguyên liệu!");
                            return;
                        } else if (conn.p.item.total_item_by_id(7,
                                conn.p.medal_create_material[4 + 5 * conn.p.id_medal_is_created]) < 1) {
                            Service.send_notice_box(conn, "Chưa đủ nguyên liệu!");
                            return;
                        }

                        int bound1 = 5 * conn.p.id_medal_is_created;

                        Message m = new Message(-105);
                        m.writer().writeByte(3);
                        m.writer().writeByte(3);
                        ItemTemplate3 temp = ItemTemplate3.item.get(conn.p.id_medal_is_created + 4587);
                        byte ran_ = (byte) Util.random(0, 101);
                        byte color_ = 0;
                        if (conn.ac_admin > 3 && Manager.BuffAdmin) {
                            color_ = 4;
                        } else if (ran_ <= 4) {
                            color_ = 4;
                        } else if (ran_ <= 30) {
                            color_ = 3;
                        } else if (ran_ <= 50) {
                            color_ = 2;
                        } else if (ran_ <= 75) {
                            color_ = 1;
                        }

                        conn.p.time_speed_rebuild = System.currentTimeMillis() + 2000;
                        m.writer().writeUTF("Chúc mừng bạn nhận được " + temp.getName());
                        m.writer().writeByte(3);
                        m.writer().writeUTF(temp.getName());
                        m.writer().writeByte(temp.getClazz());
                        m.writer().writeShort(temp.getId());
                        m.writer().writeByte(temp.getType());
                        m.writer().writeShort(temp.getIcon());
                        m.writer().writeByte(0); // tier
                        m.writer().writeShort(1); // level required
                        m.writer().writeByte(color_); // color
                        m.writer().writeByte(0); // can sell
                        m.writer().writeByte(0); // can trade
                        m.writer().writeByte(temp.getOp().size());
                        for (int i = 0; i < temp.getOp().size(); i++) {
                            m.writer().writeByte(temp.getOp().get(i).id);
                            m.writer().writeInt(temp.getOp().get(i).getParam(0));
                        }
                        m.writer().writeInt(0); // time use
                        m.writer().writeByte(0);
                        m.writer().writeByte(0);
                        m.writer().writeByte(0);
                        conn.addmsg(m);
                        m.cleanup();
                        //
                        Item3 itbag = new Item3();
                        itbag.id = temp.getId();
                        itbag.clazz = temp.getClazz();
                        itbag.type = temp.getType();
                        itbag.level = 1; // level required
                        itbag.icon = temp.getIcon();
                        itbag.color = color_;
                        itbag.part = temp.getPart();
                        itbag.islock = false;
                        itbag.name = temp.getName();
                        itbag.tier = 0;
                        //
                        List<Option> opnew = new ArrayList<>();
                        //Option op = new Option(1, 1,itbag.id);
                        byte typest = (byte) Util.random(0, 5);
                        int _st = Util.random(300, 500);
                        //op.id = 96;
                        byte dongan = 0;
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
                        for (int i = 0; i < 5; i++) {
                            itbag.item_medal[i] = conn.p.medal_create_material[bound1 + i];
                            conn.p.item.remove(7, conn.p.medal_create_material[bound1 + i], 1);
                        }
                        conn.p.item.add_item_inventory3(itbag);
                        conn.p.item.char_inventory(4);
                        conn.p.item.char_inventory(7);
                        conn.p.item.char_inventory(3);
                        conn.p.id_medal_is_created = -1;

                        // change material
                        int material_type_1st = Util.random(0, 7);
                        int material_type_2nd = Util.random(0, 7);
                        while (material_type_1st == material_type_2nd) {
                            material_type_2nd = Util.random(0, 7);
                        }

                        conn.p.medal_create_material[bound1] = (short) (MaterialMedal.m_white[material_type_1st][Util.random(0, 10)] + 200);
                        conn.p.medal_create_material[bound1 + 1] = (short) (MaterialMedal.m_white[material_type_2nd][Util.random(0, 10)] + 200);
                        conn.p.medal_create_material[bound1 + 2] = (short) (MaterialMedal.m_blue[Util.random(0, 10)] + 200);
                        conn.p.medal_create_material[bound1 + 3] = (short) (MaterialMedal.m_yellow[Util.random(0, 10)] + 200);
                        conn.p.medal_create_material[bound1 + 4] = (short) (MaterialMedal.m_violet[Util.random(0, 10)] + 200);
                    }
                    break;
                }
                case 4: {
                    if (id >= conn.p.item.inventory3.length) {
                        return;
                    }
                    Item3 it_temp = conn.p.item.inventory3[id];
                    if (it_temp != null && it_temp.id >= 4587 && it_temp.id <= 4590 && it_temp.tier < 15) {
                        conn.p.id_Upgrade_Medal_Star = id;
                        if (conn.p.item.total_item_by_id(7, 349) < 1 && conn.p.item.total_item_by_id(7, 350) < 1
                                && conn.p.item.total_item_by_id(7, 351) < 1) {
                            UpgradeMedal(conn, (byte) 0);
                        } else {
                            MenuController.send_menu_select(conn, 111, new String[]{"Không", "Đá krypton cấp 1",
                                "Đá krypton cấp 2", "Đá krypton cấp 3"}, (byte) 0);
                        }
                    } else {
                        Service.send_notice_box(conn, "Trang bị không phù hợp hoặc đã đạt cấp tối đa!");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static short[] Ratio_Upgrade_Medal = new short[]{11000, 11000, 11000, 11000, 11000, 11000, 11000, 1300, 300, 100, 100, 100, 100, 100, 100, 100};

    public static void UpgradeMedal(Session conn, byte index) {
        try {
            if (index > 3) {
                return;
            }
            int id = conn.p.id_Upgrade_Medal_Star;
            if (id < 0) {
                Service.send_notice_box(conn, "Hãy chọn lại vật phẩm muốn nâng cấp!");
                return;
            }
            if (System.currentTimeMillis() < conn.p.time_speed_rebuild) {
                Service.send_notice_box(conn, "Thao tác quá nhanh");
                return;
            }
            if (id >= conn.p.item.inventory3.length) {
                return;
            }
            Item3 it_temp = conn.p.item.inventory3[id];
            if (it_temp != null && it_temp.id >= 4587 && it_temp.id <= 4590 && it_temp.tier < 15) {
                for (int i = 0; i < 5; i++) {
                    short id_item_upgr = it_temp.item_medal[i];
                    int quant_item_upgr = get_quant_material_medal_upgrade(it_temp.tier);
                    if (conn.p.item.total_item_by_id(7, id_item_upgr) < quant_item_upgr && !(Manager.BuffAdminMaterial && conn.ac_admin > 3)) {
                        Service.send_notice_box(conn, "Chưa đủ " + ItemTemplate7.item.get(id_item_upgr).getName());
                        return;
                    }
                }
                int ngoc_req = get_ngoc_medal_upgrade(it_temp.tier);
                if (conn.p.get_ngoc() < ngoc_req) {
                    Service.send_notice_box(conn, "Chưa đủ " + ngoc_req + " ngọc");
                    return;
                }
                if (index > 0 && conn.p.item.total_item_by_id(7, 348 + index) < 1) {
                    Service.send_notice_box(conn, "Bạn không đủ Đá krypton cấp " + index);
                    return;
                }
                conn.p.update_ngoc(-ngoc_req);
                for (int i = 0; i < 5; i++) {
                    short id_item_upgr = it_temp.item_medal[i];
                    int quant_item_upgr = get_quant_material_medal_upgrade(it_temp.tier);
                    conn.p.item.remove(7, id_item_upgr, quant_item_upgr);
                }
                conn.p.time_speed_rebuild = System.currentTimeMillis() + 2000;
                if (index > 0) {
                    conn.p.item.remove(7, 348 + index, 1);
                }

                float[] plus_rate = new float[]{(float) 0, (float) 0.01, (float) 0.02, (float) 0.03};
                boolean suc = conn.ac_admin > 3
                        || (Ratio_Upgrade_Medal[it_temp.tier] + Ratio_Upgrade_Medal[it_temp.tier] * plus_rate[index]) > Util.random(11000);

                if (suc) {
                    it_temp.tier++;
                    it_temp = Medal.UpgradeMedal(it_temp);
                    // change material
                    int material_type_1st = Util.random(0, 7);
                    int material_type_2nd = Util.random(0, 7);
                    while (material_type_1st == material_type_2nd) {
                        material_type_2nd = Util.random(0, 7);
                    }
                    if (it_temp.tier > 6) {
                        it_temp.item_medal[0] = (short) (MaterialMedal.m_white[material_type_1st][Util.random(0, 10)] + 200);
                        it_temp.item_medal[1] = (short) (MaterialMedal.m_white[material_type_2nd][Util.random(0, 10)] + 200);
                        it_temp.item_medal[2] = (short) (MaterialMedal.m_blue[Util.random(0, 10)] + 200);
                        it_temp.item_medal[3] = (short) (MaterialMedal.m_yellow[Util.random(0, 10)] + 200);
                        it_temp.item_medal[4] = (short) (MaterialMedal.m_violet[Util.random(0, 10)] + 200);
                    }
                    conn.p.item.inventory3[id] = it_temp;
                }
                //
                Message m = new Message(-105);
                m.writer().writeByte(3);
                if (suc) {
                    m.writer().writeByte(3);
                } else {
                    m.writer().writeByte(4);
                }
                ItemTemplate3 temp = ItemTemplate3.item.get(it_temp.id);
                if (suc) {
                    m.writer().writeUTF("Thành công!");
                } else {
                    m.writer().writeUTF("Thất bại!");
                }
                m.writer().writeByte(3);
                m.writer().writeUTF(it_temp.name);
                m.writer().writeByte(temp.getClazz());
                m.writer().writeShort(temp.getId());
                m.writer().writeByte(temp.getType());
                m.writer().writeShort(temp.getIcon());
                m.writer().writeByte(it_temp.tier); // tier
                m.writer().writeShort(1); // level required
                m.writer().writeByte(it_temp.color); // color
                m.writer().writeByte(0); // can sell
                m.writer().writeByte(0); // can trade
                m.writer().writeByte(it_temp.op.size());
                for (int i = 0; i < it_temp.op.size(); i++) {
                    m.writer().writeByte(it_temp.op.get(i).id);
                    if (it_temp.op.get(i).id == 96) {
                        m.writer().writeInt(it_temp.op.get(i).getParam(0));
                    } else {
                        m.writer().writeInt(it_temp.op.get(i).getParam(it_temp.tier));
                    }
                }
                m.writer().writeInt(0); // time use
                m.writer().writeByte(0);
                m.writer().writeByte(0);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();
                //
                conn.p.item.char_inventory(4);
                conn.p.item.char_inventory(7);
                conn.p.item.char_inventory(3);
                //
                if (it_temp.tier < 15) {
                    m = new Message(-105);
                    m.writer().writeByte(5);
                    if (suc) {
                        m.writer().writeByte(3);
                    } else {
                        m.writer().writeByte(4);
                    }
                    if (suc) {
                        m.writer().writeUTF("Thành công!");
                    } else {
                        m.writer().writeUTF("Thất bại!");
                    }
                    m.writer().writeShort(id);
                    conn.addmsg(m);
                    m.cleanup();
                }
                //
            } else {
                Service.send_notice_box(conn, "Lỗi hãy thử lại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ChangeCS_Medal(Session conn, int actions) throws IOException {
        if (System.currentTimeMillis() < conn.p.time_speed_rebuild) {
            Service.send_notice_box(conn, "Thao tác quá nhanh");
            return;
        }
        if (conn.p.item.wear == null || conn.p.item.wear.length < 13 || conn.p.item.wear[12] == null || !CheckItem.isMeDay(conn.p.item.wear[12].id) || (actions != 94 && actions != 98)) {
            Service.send_notice_box(conn, "Không tìm thấy vật phẩm!");
            return;
        }
        conn.p.time_speed_rebuild = System.currentTimeMillis() + 500;
        if (conn.p.get_ngoc() < 500) {          
            Service.send_notice_box(conn, "Không đủ 500 ngọc để thực hiện!");
            return;
        }
        conn.p.update_ngoc(-500);
        Item3 it_temp = conn.p.item.wear[12];        
         
        if (actions == 94) {
            if (it_temp.op == null || it_temp.op.size() <= 0) {
                Service.send_notice_box(conn, "Lỗi trang bị");
                return;
            }
            for (int i = 0; i < it_temp.op.size(); i++) {
                Option op = it_temp.op.get(i);
                if (op != null && op.id >= 0 && op.id <= 4) {
                    int _st = 0;
                    byte color_ = it_temp.color;
                    if (color_ == 0) {
                        _st = Util.random(130, 140);
                    } else if (color_ == 1) {
                        _st = Util.random(130, 150);
                    } else if (color_ == 2) {
                        _st = Util.random(140, 160);
                    } else if (color_ == 3) {
                        _st = Util.random(150, 170);
                    } else if (color_ == 4) {
                        _st = Util.random(160, 180);
                    }
                    it_temp.op.set(i, new Option(Util.random(0, 5), _st, it_temp.id));
                    Service.send_wear(conn.p);
                    Service.send_notice_box(conn, "Thành công");
                    return;
                }
            }
            Service.send_notice_box(conn, "Không tìm thấy chỉ số phù hợp!");
        }
        if (actions == 98) {
            if (it_temp.op == null || it_temp.op.isEmpty()) {
                Service.send_notice_box(conn, "Trang bị lỗi!");
                return;
            }
            byte lastid = -1;
            int lastpr = -1;
            List<Integer> id_PTST = new ArrayList<>(java.util.Arrays.asList(7, 8, 9, 10, 11));
            List<Integer> ops = new ArrayList<>();
            for (int i = 0; i < it_temp.op.size(); i++) {
                Option op = it_temp.op.get(i);
                if (op == null || !(op.id >= 7 && op.id <= 11)) {
                    continue;
                }
                //int ran = Util.random(0, 101);
                Integer id_add = Util.random(id_PTST, ops);
                if (id_add == null) {
                    continue;
                }
                int param_add = 0;
                if (id_add >= 7 && id_add <= 11) {
                    if (it_temp.color == 0) {
                        param_add = Util.randomNext(114, 160, lastpr);
                    } else if (it_temp.color == 1) {
                        param_add = Util.randomNext(160, 207, lastpr);
                    } else if (it_temp.color == 2) {
                        param_add = Util.randomNext(207, 254, lastpr);
                    } else if (it_temp.color == 3) {
                        param_add = Util.randomNext(253, 300, lastpr);
                    } else if (it_temp.color == 4) {
                        param_add = Util.randomNext(300, 347, lastpr);
                    }
                }
                ops.add(id_add);
                it_temp.op.set(i, new Option(id_add, param_add, it_temp.id));
            }
            Service.send_wear(conn.p);
            Service.send_notice_box(conn, "Thành công");
        }
    }

    public static void Wings_Process(Session conn, Message m2) throws IOException {
        // Service.send_notice_box(conn, "Đang phát triển");
        byte type = m2.reader().readByte();
        int wing = m2.reader().readInt();
        short id = m2.reader().readShort();
        // System.out.println(type);
        // System.out.println(wing);
        // System.out.println(id);
        if (type == 1) { // create
            if (conn.p.item.get_inventory_able() < 1) {
                Service.send_notice_box(conn, "Hành trang không đủ chỗ!");
                return;
            }
            if (conn.p.item.total_item_by_id(7, 8) < 80) {
                Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(8).getName() + "!");
                return;
            } else if (conn.p.item.total_item_by_id(7, 9) < 60) {
                Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(9).getName() + "!");
                return;
            } else if (conn.p.item.total_item_by_id(7, 10) < 40) {
                Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(10).getName() + "!");
                return;
            } else if (conn.p.item.total_item_by_id(7, 11) < 20) {
                Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(11).getName() + "!");
                return;
            } else if (conn.p.item.total_item_by_id(7, 0) < 100) {
                Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(0).getName() + "!");
                return;
            } else if (conn.p.item.total_item_by_id(7, 3) < 20) {
                Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(3).getName() + "!");
                return;
            }
            if (conn.p.get_vang() < 3_000_000) {
                Service.send_notice_box(conn, "Không đủ vàng!");
                return;
            }
            //
            conn.p.update_vang(-3_000_000, "Trừ %s vàng tạo cánh");
            conn.p.item.remove(7, 8, 80);
            conn.p.item.remove(7, 9, 60);
            conn.p.item.remove(7, 10, 40);
            conn.p.item.remove(7, 11, 20);
            conn.p.item.remove(7, 0, 100);
            conn.p.item.remove(7, 3, 20);
            conn.p.item.char_inventory(7);
            //
            ItemTemplate3 temp = ItemTemplate3.item.get(wing);
            Item3 itbag = new Item3();
            itbag.id = (short) wing;
            itbag.name = temp.getName();
            itbag.clazz = temp.getClazz();
            itbag.type = temp.getType();
            itbag.level = 60;
            itbag.icon = temp.getIcon();
            //
            itbag.op = new ArrayList<>();
            List<Option> op_new = new ArrayList<>();
            switch (wing) {
                case 2880, 2915: { // canh chien than
                    op_new.add(new Option(25, Util.random(1, 7), itbag.id));
                    break;
                }
                case 2887, 2894: { // canh rong
                    op_new.add(new Option(23, Util.random(1, 6), itbag.id));
                    break;
                }// canh quai thu
                case 2901, 2922: { // canh yeu tinh
                    op_new.add(new Option(24, Util.random(1, 7), itbag.id));
                    break;
                }
                case 2908, 2929: { // canh phuong hoang
                    op_new.add(new Option(26, Util.random(1, 6), itbag.id));
                    break;
                }
            }
            op_new.add(new Option(41,
                    ((3 > Util.random(0, 150)) ? 4 : ((10 > Util.random(0, 150)) ? 3 : ((45 > Util.random(0, 120)) ? 2 : 1)))
                    * 100, itbag.id));
            op_new.add(new Option(42,
                    ((3 > Util.random(0, 150)) ? 8 : ((10 > Util.random(0, 150)) ? 7 : ((45 > Util.random(0, 120)) ? 6 : 5)))
                    * 1000, itbag.id));
            //
            itbag.op.addAll(op_new);
            //
            itbag.color = 5;
            itbag.part = temp.getPart();
            itbag.tier = 0;
            itbag.islock = false;
            itbag.time_use = 0;
            conn.p.item.add_item_inventory3(itbag);
            conn.p.item.char_inventory(4);
            conn.p.item.char_inventory(7);
            conn.p.item.char_inventory(3);
            for (int i = (conn.p.item.inventory3.length - 1); i >= 0; i--) {
                if (conn.p.item.inventory3[i] != null && conn.p.item.inventory3[i].id == wing) {
                    Message m = new Message(77);
                    m.writer().writeByte(5);
                    m.writer().writeUTF("Chúc mừng bạn nhận được " + temp.getName());
                    m.writer().writeShort(i);
                    conn.addmsg(m);
                    m.cleanup();
                    break;
                }
            }
            Log.gI().add_log(conn.p.name, "Tạo cánh " + temp.getName());
        } else if (type == 2) {
            if (!conn.p.is_create_wing) {
                Item3 it = conn.p.item.inventory3[id];
                if (it != null) {
                    if (it.tier > 29) {
                        Service.send_notice_box(conn, "Đã nâng cấp tối đa!");
                        return;
                    }
                    if (it.time_use > 0) {
                        long time_ = it.time_use - System.currentTimeMillis();
                        time_ /= 60_000;
                        Service.send_notice_nobox_white(conn, "Sử dụng sau " + ((time_ > 0) ? time_ : 1) + "p nữa");
                        return;
                    }
                    Message m = new Message(77);
                    m.writer().writeByte(2);
                    m.writer().writeShort(id);
                    conn.addmsg(m);
                    m.cleanup();
                    //
                    m = new Message(77);
                    m.writer().writeByte(0);
                    m.writer().writeInt(it.id);
                    m.writer().writeUTF((it.name + " +" + (it.tier + 1)));
                    m.writer().writeInt(GameSrc.wing_upgrade_material_gold[it.tier]);
                    m.writer().writeShort(60);
                    m.writer().writeInt(GameSrc.wing_upgrade_material_time[it.tier] * 60);
                    m.writer().writeByte(6);
                    m.writer().writeShort(8);
                    m.writer().writeShort(GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    m.writer().writeShort(9);
                    m.writer().writeShort(GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    m.writer().writeShort(10);
                    m.writer().writeShort(GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    m.writer().writeShort(11);
                    m.writer().writeShort(GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    m.writer().writeShort(0);
                    m.writer().writeShort(GameSrc.wing_upgrade_material_da_cuong_hoa[it.tier]);
                    m.writer().writeShort(3);
                    m.writer().writeShort(GameSrc.wing_upgrade_material_kim_loai[it.tier]);
                    conn.addmsg(m);
                    m.cleanup();
                }
            }
        } else if (type == 3) {
            if (!conn.p.is_create_wing) {
                Item3 it = conn.p.item.inventory3[id];
                if (it != null) {
                    if (it.time_use != 0) {
                        return;
                    }
                    // check material
                    if (conn.p.item.total_item_by_id(7, 8) < GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]) {
                        Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(8).getName() + "!");
                        return;
                    } else if (conn.p.item.total_item_by_id(7, 9) < GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]) {
                        Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(9).getName() + "!");
                        return;
                    } else if (conn.p.item.total_item_by_id(7,
                            10) < GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]) {
                        Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(10).getName() + "!");
                        return;
                    } else if (conn.p.item.total_item_by_id(7,
                            11) < GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]) {
                        Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(11).getName() + "!");
                        return;
                    } else if (conn.p.item.total_item_by_id(7, 0) < GameSrc.wing_upgrade_material_da_cuong_hoa[it.tier]) {
                        Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(0).getName() + "!");
                        return;
                    } else if (conn.p.item.total_item_by_id(7, 3) < GameSrc.wing_upgrade_material_kim_loai[it.tier]) {
                        Service.send_notice_box(conn, "Không đủ " + ItemTemplate7.item.get(3).getName() + "!");
                        return;
                    }
                    if (conn.p.get_vang() < GameSrc.wing_upgrade_material_gold[it.tier]) {
                        Service.send_notice_box(conn, "Không đủ vàng!");
                        return;
                    }
                    //
                    conn.p.update_vang(-GameSrc.wing_upgrade_material_gold[it.tier], "Trừ %s vàng nâng cấp cánh");
                    conn.p.item.remove(7, 8, GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    conn.p.item.remove(7, 9, GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    conn.p.item.remove(7, 10, GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    conn.p.item.remove(7, 11, GameSrc.wing_upgrade_material_long_khuc_xuong[it.tier]);
                    conn.p.item.remove(7, 0, GameSrc.wing_upgrade_material_da_cuong_hoa[it.tier]);
                    conn.p.item.remove(7, 3, GameSrc.wing_upgrade_material_kim_loai[it.tier]);
                    conn.p.item.char_inventory(7);
                    //
                    it.tier++;
                    Option op_new;
                    switch (it.tier) {
                        case 5: {
                            it.op.add(new Option(14, Util.random(70, 100), it.id));
                            break;
                        }
                        case 10: {
                            it.part++;
                            op_new = new Option(1, 0, it.id);
                            switch (it.id) {
                                case 2894, 2908, 2929, 2880: {
                                    op_new.id = 7;
                                    break;
                                }
                                case 2887: {
                                    op_new.id = 9;
                                    break;
                                }
                                case 2901: {
                                    op_new.id = 11;
                                    break;
                                }
                                case 2915: {
                                    op_new.id = 8;
                                    break;
                                }
                                case 2922: {
                                    op_new.id = 10;
                                    break;
                                }
                            }
                            op_new.setParam(Util.random(1, 4) * 100);
                            it.op.add(op_new);
                            break;
                        }
                        case 15: {
                            op_new = new Option(1, 0, it.id);
                            switch (it.id) {
                                case 2894:
                                case 2929:
                                case 2908:
                                case 2887: {
                                    op_new.id = 28;
                                    break;
                                }
                                case 2901:
                                case 2922:
                                case 2880:
                                case 2915: {
                                    op_new.id = 27;
                                    break;
                                }
                            }
                            op_new.setParam(Util.random(1, 4) * 100);
                            it.op.add(op_new);
                            break;
                        }
                        case 20: {
                            it.part++;
                            it.op.add(new Option(15, Util.random(1, 4) * 100, it.id));
                            break;
                        }
                        case 25: {
                            op_new = new Option(1, 0, it.id);
                            switch (it.id) {
                                case 2894:
                                case 2887: {
                                    op_new.id = 33;
                                    break;
                                }
                                case 2929:
                                case 2908: {
                                    op_new.id = 36;
                                    break;
                                }
                                case 2901:
                                case 2922: {
                                    op_new.id = 34;
                                    break;
                                }
                                case 2880:
                                case 2915: {
                                    op_new.id = 35;
                                    break;
                                }
                            }
                            op_new.setParam(Util.random(1, 3) * 100);
                            it.op.add(op_new);
                            break;
                        }
                        case 30: {
                            it.part++;
                            op_new = new Option(1, 1, it.id);
                            switch (it.id) {
                                case 2894:
                                case 2929:
                                case 2908:
                                case 2887: {
                                    op_new.id = 37;
                                    break;
                                }
                                case 2901:
                                case 2922:
                                case 2880:
                                case 2915: {
                                    op_new.id = 38;
                                    break;
                                }
                            }
                            op_new.setParam(1);
                            it.op.add(op_new);
                            break;
                        }
                    }
                    it.time_use = System.currentTimeMillis()
                            + (((long) GameSrc.wing_upgrade_material_time[it.tier - 1]) * 3_600_000L);
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(7);
                    conn.p.item.char_inventory(3);
                    Message m = new Message(77);
                    m.writer().writeByte(5);
                    m.writer().writeUTF(("Đã nâng cấp thành công " + it.name + " lên +" + it.tier));
                    m.writer().writeShort(id);
                    conn.addmsg(m);
                    m.cleanup();
                }
            } else {
                Service.send_notice_box(conn, "Bạn đang tính tạo cánh mới, quên à?");
            }
        }
    }

    public static void player_store(Session conn, Message m2) throws IOException {
        if (conn.status != 0) {
            Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt, hãy kích hoạt");
            return;
        }
        if (conn.p.map.map_id != 82) {
            return;
        }
        byte cmd = m2.reader().readByte();
        switch (cmd) {
            case 0: { // bán
                if (conn.p.item.total_item_by_id(4, 135) > 0) {
                    int size = m2.reader().readByte();
                    if (size > 12) {
                        Service.send_notice_box(conn, "Tối đa 12 món!");
                        return;
                    }
                    conn.p.my_store.clear();
                    for (int i = 0; i < size; i++) {
                        Player_store p_store = new Player_store();
                        p_store.it_id = m2.reader().readShort();
                        p_store.it_price = m2.reader().readInt();
                        if (p_store.it_price > 2_000_000_000 || p_store.it_price < 10_000) {
                            Service.send_notice_box(conn, "Giá tiền không phù hợp!");
                            return;
                        }
                        p_store.it_quant = m2.reader().readShort();
                        p_store.it_type = m2.reader().readByte();
                        if (p_store.it_quant <= 0 || p_store.it_quant > 32_000) {
                            Service.send_notice_box(conn, "Số lượng không hợp lệ");
                            return;
                        }
                        if (p_store.it_type == 3) {
                            if (conn.p.item.inventory3[p_store.it_id] == null
                                    || (conn.p.item.inventory3[p_store.it_id] != null && conn.p.item.inventory3[p_store.it_id].islock)) {
                                Service.send_notice_box(conn, "Không thể bán vật phẩm khóa!");
                                return;
                            }
                            if (conn.p.item.inventory3[p_store.it_id] != null && conn.p.item.inventory3[p_store.it_id].expiry_date > 0) {
                                Service.send_notice_box(conn, "Không thể bán vật phẩm có hạn sử dụng!");
                                return;
                            }
                        } else if (p_store.it_type == 4 || p_store.it_type == 7) {
                            if (conn.p.item.total_item_by_id(p_store.it_type, p_store.it_id) < p_store.it_quant) {
                                Service.send_notice_box(conn, "Số lượng không hợp lệ");
                                return;
                            }
                            if (p_store.it_type == 4 && (CheckItem.item4CanTrade(p_store.it_id) || p_store.it_id == 135 || p_store.it_id == 52 || p_store.it_id == 56 || p_store.it_id == 143 || p_store.it_id == 226 || (p_store.it_id >= 48 && p_store.it_id <= 51))) {
                                Service.send_notice_box(conn, "Đồ bán không hợp lệ");
                                return;
                            }
                            if (p_store.it_type == 7 && ItemTemplate7.item.get(p_store.it_id).getTrade() == 1 || (p_store.it_id >= 8 && p_store.it_id <= 11)) {
                                Service.send_notice_box(conn, "Đồ bán không hợp lệ");
                                return;
                            }
                        }
                        conn.p.my_store.add(p_store);
                    }
                    if (!conn.p.my_store_name.isEmpty()) {
                        Service.send_notice_box(conn, "Đang bán rồi!");
                        return;
                    }
                    for (int i = 0; i < conn.p.map.players.size(); i++) {
                        Player p0 = conn.p.map.players.get(i);
                        if (p0 != null && !p0.my_store_name.isEmpty() && p0.ID != conn.p.ID
                                && (Math.abs(p0.x - conn.p.x) < 75) && (Math.abs(p0.y - conn.p.y) < 75)) {
                            Service.send_notice_box(conn, "Quá gần một gian hàng khác, hãy chọn chỗ khác!");
                            return;
                        }
                    }
                    conn.p.my_store_name = m2.reader().readUTF();
                    if (conn.p.my_store_name.isEmpty()) {
                        conn.p.my_store_name = "Cửa hàng " + conn.p.name;
                    }
                    Message ms = new Message(-102);
                    ms.writer().writeByte(1);
                    ms.writer().writeShort(conn.p.ID);
                    ms.writer().writeUTF(conn.p.my_store_name);
                    MapService.send_msg_player_inside(conn.p.map, conn.p, ms, true);
                    ms.cleanup();
                    conn.p.item.remove(4, 135, 1);
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(7);
                    conn.p.item.char_inventory(3);
                    Service.send_box_input_yesno(conn, 97, "Bạn có muốn bán riêng vật phẩm cho ai không?");
                    //Service.send_box_input_text(conn, 20, "Bán riêng cho nhân vật", new String[]{"Tên nhân vật"});
                } else {
                    Service.send_notice_box(conn, "Không đủ vé!");
                }
                break;
            }
            case 4: {
                Message ms = new Message(-102);
                ms.writer().writeByte(2);
                ms.writer().writeShort(conn.p.ID);
                MapService.send_msg_player_inside(conn.p.map, conn.p, ms, true);
                ms.cleanup();
                conn.p.my_store_name = "";
                conn.p.Store_Sell_ToPL = "";
                Service.send_notice_box(conn, "Hủy bán thành công!");
                break;
            }
            case 1: {//xem cửa hàng
                short id_p = m2.reader().readShort();
                Player p0 = findStorePlayerById(id_p);
                if (p0 != null && !p0.my_store_name.isEmpty()) {
                    GameSrc.update_store_player_to_other(p0, conn);
                }
                break;
            }
            case 2: {// mua
                short iditem = m2.reader().readShort();
                short idChar = m2.reader().readShort();
                byte idType = m2.reader().readByte();
                Player p0 = findStorePlayerById(idChar);
                if (p0 != null && !p0.my_store_name.isEmpty()) {
                    if (p0.Store_Sell_ToPL != null && !p0.Store_Sell_ToPL.isEmpty() && !p0.Store_Sell_ToPL.equals(conn.p.name)) {
                        Service.send_notice_box(conn, "Người bán không muốn bán vật phẩm cho bạn!");
                        return;
                    }
                    His_KMB hist = new His_KMB(p0.name, conn.p.name, p0.get_vang(), conn.p.get_vang());

                    for (int i = 0; i < p0.my_store.size(); i++) {
                        if (p0.my_store.get(i).it_type == 3 && p0.item.inventory3[p0.my_store.get(i).it_id] != null
                                && p0.item.inventory3[p0.my_store.get(i).it_id].id == iditem) {
                            long vang_trade = p0.my_store.get(i).it_price;
                            if (conn.p.get_vang() < vang_trade) {
                                Service.send_notice_box(conn, "Không đủ " + vang_trade + " vàng!");
                                return;
                            }
                            if (conn.p.item.get_inventory_able() < 1) {
                                Service.send_notice_box(conn, "Hành trang đầy!");
                                return;
                            }
                            Item3 itTrade = p0.item.inventory3[p0.my_store.get(i).it_id];
                            conn.p.update_vang(-vang_trade, "Trừ %s vàng mua vật phẩm trong khu mua bán của " + p0.name);
                            long thue = (vang_trade / 100) * Manager.thue;
                            if (Manager.ClanThue != null) {
                                Manager.ClanThue.update_vang(thue);
                            }
                            vang_trade -= thue;

                            p0.update_vang(vang_trade, "Nhận %s vàng bán vật phẩm trong khu mua bán cho " + conn.p.name);

                            hist.tem3 = itTrade;
                            hist.UpdateGold(p0.get_vang(), conn.p.get_vang());
                            hist.Flus();

                            conn.p.item.add_item_inventory3(itTrade);
                            p0.item.inventory3[p0.my_store.get(i).it_id] = null;
                            p0.item.char_inventory(4);
                            p0.item.char_inventory(7);
                            p0.item.char_inventory(3);
                            conn.p.item.char_inventory(4);
                            conn.p.item.char_inventory(7);
                            conn.p.item.char_inventory(3);
                            p0.my_store.remove(p0.my_store.get(i));
                            GameSrc.update_store_player(p0);
                            GameSrc.update_store_player_to_other(p0, conn);
                            Service.send_notice_box(conn, "Mua thành công");
                            Service.send_notice_box(p0.conn, "Bán thành công, nhận được " + vang_trade + "vàng");
                            p0.flush();
                            conn.p.flush();
                            break;
                        } else if (p0.my_store.get(i).it_type == 4 || p0.my_store.get(i).it_type == 7) {
                            if (p0.my_store.get(i).it_id == iditem && p0.my_store.get(i).it_type == idType) {
                                if (p0.item.total_item_by_id(idType, iditem) >= p0.my_store.get(i).it_quant) {
                                    int vang_trade = p0.my_store.get(i).it_price;
                                    if (conn.p.get_vang() < vang_trade) {
                                        Service.send_notice_box(conn, "Không đủ " + vang_trade + " vàng!");
                                        return;
                                    }
                                    if (conn.p.item.total_item_by_id(idType, iditem) + p0.my_store.get(i).it_quant > 30_000
                                            || (conn.p.item.total_item_by_id(idType, iditem) == 0 && conn.p.item.get_inventory_able() < 1)) {
                                        Service.send_notice_box(conn, "Hành trang đầy!");
                                        return;
                                    }
                                    conn.p.update_vang(-vang_trade, "Trừ %s vàng mua vật phẩm trong khu mua bán của " + p0.name);
                                    long thue = (vang_trade / 100) * Manager.thue;
                                    if (Manager.ClanThue != null) {
                                        Manager.ClanThue.update_vang(thue);
                                    }
                                    //                                Manager.ClanThue.update_vang((vang_trade * Manager.thue) / 100);
                                    vang_trade -= thue;
                                    p0.update_vang(vang_trade, "Nhận %s vàng bán vật phẩm trong khu mua bán cho " + conn.p.name);
                                    Item47 it_b_add = new Item47();
                                    it_b_add.category = idType;
                                    it_b_add.id = iditem;
                                    it_b_add.quantity = p0.my_store.get(i).it_quant;

                                    hist.tem47 = it_b_add;
                                    hist.UpdateGold(p0.get_vang(), conn.p.get_vang());
                                    hist.Flus();

                                    conn.p.item.add_item_inventory47(idType, it_b_add);
                                    p0.item.remove(idType, iditem, p0.my_store.get(i).it_quant);
                                    p0.item.char_inventory(4);
                                    p0.item.char_inventory(7);
                                    p0.item.char_inventory(3);
                                    conn.p.item.char_inventory(4);
                                    conn.p.item.char_inventory(7);
                                    conn.p.item.char_inventory(3);
                                    p0.my_store.remove(p0.my_store.get(i));
                                    GameSrc.update_store_player(p0);
                                    GameSrc.update_store_player_to_other(p0, conn);
                                    Service.send_notice_box(conn, "Mua thành công");
                                    Service.send_notice_box(p0.conn, "Bán thành công, nhận được " + vang_trade + "vàng");
                                    p0.flush();
                                    conn.p.flush();
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    private static Player findStorePlayerById(int id) {
        Player seller = Map.get_player_by_id(id);
        if (seller == null) {
            seller = AdminToolServer.gI().findFakeBotById(id);
        }
        return seller;
    }

    private static void update_store_player_to_other(Player p0, Session conn) throws IOException {
        Message m = new Message(23);
        m.writer().writeUTF("Cửa hàng " + p0.name);
        m.writer().writeByte(17);
        m.writer().writeShort(p0.my_store.size());
        for (int i = 0; i < p0.my_store.size(); i++) {
            Player_store temp = p0.my_store.get(i);
            m.writer().writeByte(temp.it_type);
            switch (temp.it_type) {
                case 3: {
                    Item3 it_b = p0.item.inventory3[temp.it_id];
                    if (it_b != null) {
                        m.writer().writeShort(it_b.id);
                        m.writer().writeUTF(it_b.name);
                        m.writer().writeByte(it_b.clazz);
                        m.writer().writeByte(it_b.type);
                        m.writer().writeShort(it_b.icon);
                        m.writer().writeByte(it_b.tier);
                        m.writer().writeShort(it_b.level);
                        m.writer().writeByte(it_b.color);
                        m.writer().writeByte(it_b.op.size() + 1);
                        for (int j = 0; j < it_b.op.size(); j++) {
                            m.writer().writeByte(it_b.op.get(j).id);
                            m.writer().writeInt(it_b.op.get(j).getParam(it_b.tier));
                        }
                        m.writer().writeByte(70);
                        m.writer().writeInt(temp.it_price);
                        m.writer().writeByte(0);
                    } else {
                        Service.send_notice_box(conn, "Cửa hàng lỗi, hãy thử lại sau!");
                        return;
                    }
                    break;
                }
                case 4: {
                    m.writer().writeShort(temp.it_id);
                    m.writer().writeShort(temp.it_quant);
                    m.writer().writeLong(temp.it_price);
                    break;
                }
                case 7: {
                    m.writer().writeShort(temp.it_id);
                    m.writer().writeShort(temp.it_quant);
                    m.writer().writeByte(1); // can sell
                    m.writer().writeByte(1); // can trade
                    m.writer().writeLong(temp.it_price);
                    break;
                }
            }
        }
        m.writer().writeShort(p0.ID);
        conn.addmsg(m);
        m.cleanup();
    }

    private static void update_store_player(Player p0) throws IOException {
        Message m = new Message(-102);
        m.writer().writeByte(3);
        m.writer().writeByte(p0.my_store.size());
        for (int i = 0; i < p0.my_store.size(); i++) {
            Player_store temp = p0.my_store.get(i);
            m.writer().writeByte(temp.it_type);
            switch (temp.it_type) {
                case 3: {
                    Item3 it_in_bag = p0.item.inventory3[temp.it_id];
                    if (it_in_bag != null) {
                        m.writer().writeShort(it_in_bag.id);
                        m.writer().writeUTF(it_in_bag.name);
                        m.writer().writeByte(it_in_bag.clazz);
                        m.writer().writeByte(it_in_bag.type);
                        m.writer().writeShort(it_in_bag.icon);
                        m.writer().writeByte(it_in_bag.tier);
                        m.writer().writeShort(it_in_bag.level);
                        m.writer().writeByte(it_in_bag.color);
                        m.writer().writeByte(it_in_bag.op.size() + 1);
                        for (int j = 0; j < it_in_bag.op.size(); j++) {
                            m.writer().writeByte(it_in_bag.op.get(j).id);
                            m.writer().writeInt(it_in_bag.op.get(j).getParam(it_in_bag.tier));
                        }
                        m.writer().writeByte(70);
                        m.writer().writeInt(temp.it_price);
                        m.writer().writeByte(0);
                    } else {
                        return;
                    }
                    break;
                }
                case 4: {
                    m.writer().writeShort(temp.it_id);
                    m.writer().writeShort(temp.it_quant);
                    m.writer().writeInt(temp.it_price);
                    break;
                }
                case 7: {
                    m.writer().writeShort(temp.it_id);
                    m.writer().writeShort(temp.it_quant);
                    m.writer().writeByte(1); // can sell
                    m.writer().writeByte(1); // can trade
                    m.writer().writeLong(temp.it_price);
                    break;
                }
            }
        }
        p0.conn.addmsg(m);
        m.cleanup();
    }

    public static void Hop_Ngoc_Kham(Player p, Message m2) throws IOException {
        byte type = m2.reader().readByte();
        short id_item = m2.reader().readShort();

//        System.out.println("core.GameSrc.Hop_Ngoc_Kham()");
//        System.out.println(type);
//        System.out.println(id_item);
        switch (type) {
            case 0: {
                if (id_item >= p.item.inventory3.length) {
                    Service.send_notice_box(p.conn, "Trang bị không phù hợp!");
                    return;
                }
                Item3 it_check = p.item.inventory3[id_item];
                if (it_check == null || !check_item_can_rebuild(it_check.type) || Part_fashion.fashions.contains(it_check.id)) {
                    Service.send_notice_box(p.conn, "Trang bị không phù hợp!");
                    return;
                }
                short id_g1 = m2.reader().readShort();
                short id_g2 = m2.reader().readShort();
                short id_g3 = m2.reader().readShort();
                if (id_g2 != -1 || id_g3 != -1) {
                    Service.send_notice_box(p.conn, "Mỗi lần hãy khảm 1 viên ngọc thôi!");
                    return;
                }
                if (id_g1 == -1 || p.item.total_item_by_id(7, id_g1) < 1) {
                    Service.send_notice_box(p.conn, "Lỗi hãy thử lại!");
                    return;
                }
                if (!Kham_Item.CheckNgocTinhLuyen(id_g1)) {
                    Service.send_notice_box(p.conn, "Chỉ có thể khảm ngọc tinh luyện!");
                    return;
                }
                Item3 it3 = p.item.inventory3[id_item];
                if (it3 == null) {
                    break;
                }
                if (!GameSrc.check_item_kham_ngoc_type(id_g1, it3)) {
                    Service.send_notice_box(p.conn, "Không thể khảm ngọc này lên vật phẩm này!");
                    return;
                }
                short[] ngoc_in4 = p.item.check_kham_ngoc(it3);
//                if (!Helps.Kham_Item.CheckNgocTinhluyen(id_g1, ngoc_in4)) {
//                    Service.send_notice_box(p.conn, "Không thể khảm cùng lúc ngọc tinh luyện và chưa tinh luyện!");
//                    return;
//                }
                int index_ngoc_kham_vao = -1;
                for (int i = 0; i < ngoc_in4.length; i++) {
                    if (ngoc_in4[i] == -1) {
                        index_ngoc_kham_vao = i;
                        break;
                    }
                }
                if (index_ngoc_kham_vao == -1) {
                    Service.send_notice_box(p.conn, "Vật phẩm chưa có lỗ đục hoặc không có lỗ dư!");
                    return;
                }
                int vang_total = (GameSrc.get_vang_hopngoc(id_g1) / 50_000) * 1_000_000;
                if (p.get_vang() < vang_total) {
                    Service.send_notice_box(p.conn, "Không đủ " + (vang_total) + " vàng!");
                    return;
                }
                Message m = new Message(-100);
                if (10 > Util.random(100) || (p.conn.ac_admin > 3 && Manager.BuffAdmin)) {
                    if (!Kham_Item.KhamNgoc(id_g1, it3)) {
                        Service.send_notice_box(p.conn, "Không thể khảm!");
                        return;
                    }
                    for (int i = 0; i < it3.op.size(); i++) {
                        if (it3.op.get(i).id == (index_ngoc_kham_vao + 58)) {
                            it3.op.get(i).setParam(id_g1);
                            break;
                        }
                    }
                    m.writer().writeByte(3);
                    m.writer().writeUTF("Thành công, chúc mừng");
                } else {
                    m.writer().writeByte(4);
                    m.writer().writeUTF("Chúc con may mắn lần sau!");
                }
                m.writer().writeShort(it3.id);
                m.writer().writeByte(3);
                p.conn.addmsg(m);
                m.cleanup();
                //
                p.update_vang(-vang_total, "Trừ %s vàng khảm ngọc");
                p.item.remove(7, id_g1, 1);
                p.item.char_inventory(4);
                p.item.char_inventory(7);
                p.item.char_inventory(3);

                break;
            }
            case 1: {
                if (p.item.get_inventory_able() < 1) {
                    Service.send_notice_box(p.conn, "Hành trang đầy!");
                } else if (GameSrc.get_vang_hopngoc(id_item) <= 200_000) {
                    p.id_hop_ngoc = id_item;
                    Service.send_box_input_text(p.conn, 15, "Nhập số lượng", new String[]{"Nhập số lượng"});
                } else {
                    Service.send_notice_box(p.conn, "Đã hợp tối đa!");
                }
                break;
            }
            case 2: {
                short id_g1 = m2.reader().readShort();
                Item3 it3 = p.item.inventory3[id_item];
                if (it3 == null || !check_item_can_rebuild(it3.type) || Part_fashion.fashions.contains(it3.id)) {
                    Service.send_notice_box(p.conn, "Trang bị không phù hợp!");
                    return;
                }
                if (it3 != null) {
                    short[] ngoc_in4 = p.item.check_kham_ngoc(it3);
                    if (ngoc_in4[0] != -2 && ngoc_in4[1] != -2 && ngoc_in4[2] != -2) {
                        Service.send_notice_box(p.conn, "Không thể đục thêm với vật phẩm này!");
                        return;
                    }
                    int index_ngoc_kham_vao = -1;
                    for (int i = 0; i < ngoc_in4.length; i++) {
                        if (ngoc_in4[i] == -2) {
                            index_ngoc_kham_vao = i;
                            break;
                        }
                    }
                    if (index_ngoc_kham_vao == -1) {
                        Service.send_notice_box(p.conn, "Lỗi hãy thử lại!");
                        return;
                    }
                    if (p.get_vang() < ((index_ngoc_kham_vao + 1) * 5_000_000)) {
                        Service.send_notice_box(p.conn, "Không đủ " + ((index_ngoc_kham_vao + 1) * 5_000_000) + " vàng!");
                        return;
                    }
                    if (p.item.total_item_by_id(7, id_g1) < 1) {
                        Service.send_notice_box(p.conn, "Không thể đủ nguyên liệu đục!");
                        return;
                    }
                    boolean suc = false;
                    if (id_g1 == 33) {
                        suc = 35 > Util.random(100 + index_ngoc_kham_vao * 35);
                    } else if (id_g1 == 44) {
                        suc = 55 > Util.random(100 + index_ngoc_kham_vao * 35);
                    } else if (id_g1 == 45) {
                        suc = 65 > Util.random(100 + index_ngoc_kham_vao * 35);
                    } else {
                        Service.send_notice_box(p.conn, "Nguyên liệu đục không hợp lệ");
                        return;
                    }
                    p.update_vang(-((index_ngoc_kham_vao + 1) * 2_000_000), "Trừ %s vàng đục lỗ");
                    p.item.remove(7, id_g1, 1);
                    if (p.conn.ac_admin > 3 && Manager.BuffAdmin) {
                        suc = true;
                    }
                    Message m = new Message(-100);
                    if (suc) {
                        it3.op.add(new Option((58 + index_ngoc_kham_vao), -1, it3.id));
                        m.writer().writeByte(3);
                        m.writer().writeUTF("Thành công, chúc mừng");
                    } else {
                        m.writer().writeByte(4);
                        m.writer().writeUTF("Chúc con may mắn lần sau!!");
                    }
                    m.writer().writeShort(it3.id);
                    m.writer().writeByte(3);
                    p.conn.addmsg(m);
                    m.cleanup();
                    p.item.char_inventory(4);
                    p.item.char_inventory(7);
                    p.item.char_inventory(3);
                }
                break;
            }
        }
    }

    private static boolean check_item_kham_ngoc_type(short id_g1, Item3 it3) {
        boolean check = false;
        if ((it3.type >= 8 && it3.type <= 11) && id_g1 >= 352 && id_g1 <= 361) {
            check = true;
        }
        if ((it3.type == 0 || it3.type == 1 || it3.type == 2 || it3.type == 3 || it3.type == 6) && id_g1 >= 362
                && id_g1 <= 371) {
            check = true;
        }
        if ((it3.type == 4 || it3.type == 5) && id_g1 >= 372 && id_g1 <= 381) {
            check = true;
        }
        //
        if (!check) {
            id_g1 -= 30;
            if ((it3.type >= 8 && it3.type <= 11) && id_g1 >= 352 && id_g1 <= 361) {
                check = true;
            }
            if ((it3.type == 0 || it3.type == 1 || it3.type == 2 || it3.type == 3 || it3.type == 6) && id_g1 >= 362
                    && id_g1 <= 371) {
                check = true;
            }
            if ((it3.type == 4 || it3.type == 5) && id_g1 >= 372 && id_g1 <= 381) {
                check = true;
            }
        }
        return check;
    }

    public static int get_vang_hopngoc(int id) {
        if (id >= 23 && id <= 43) {
            return (((id - 23) % 5) + 1) * 200_000;
        } else {
            return (((id - 352) % 5) + 1) * 200_000;
        }
    }

    public static short[] Ratio_UpgradeItemStar = new short[]{4800, 3300, 2800, 2300, 1800, 1300, 1000, 700, 400};

    public static void ActionsItemStar(Session conn, Message m) throws IOException {
        if (conn.p.time_speed_rebuild > System.currentTimeMillis()) {
            Service.send_notice_box(conn, "Thao tác quá nhanh");
            return;
        }
        conn.p.time_speed_rebuild = System.currentTimeMillis() + 2000;
        byte type = m.reader().readByte();
        short id = -1;
        byte tem = -1;
        try {
            id = m.reader().readShort();
            tem = m.reader().readByte();
        } catch (IOException e) {
        }
        switch (type) {
            case 0: {
                if (tem != 3) {
                    Service.send_notice_box(conn, "Trang bị không phù hợp!");
                    return;
                }
                if (id >= conn.p.item.inventory3.length) {
                    return;
                }
                Item3 temp = conn.p.item.inventory3[id];
                if (temp == null) {
                    return;
                }
                if (!(temp.id >= 4656 && temp.id <= 4675)) {
                    Service.send_notice_box(conn, "Trang bị không phù hợp!");
                    return;
                }
                if (temp.tierStar < 9) {
                    conn.p.TypeItemStarCreate = ItemStar.ConvertType(temp.type);
                    Message m_send = new Message(-105);
                    m_send.writer().writeByte(4);
                    m_send.writer().writeByte(5);
                    if (conn.p.MaterialItemStar == null || conn.p.MaterialItemStar.length < 5) {
                        conn.p.SetMaterialItemStar();
                    }
                    for (int i = conn.p.TypeItemStarCreate * 5; i < conn.p.TypeItemStarCreate * 5 + 5; i++) {
                        m_send.writer().writeShort(conn.p.MaterialItemStar[i]);
                        if (conn.version > 270) {
                            m_send.writer().writeShort((temp.tierStar + 1));
                        } else {
                            m_send.writer().writeByte((temp.tierStar + 1));
                        }
                    }
                    conn.addmsg(m_send);
                    m_send.cleanup();
                } else {
                    Service.send_notice_box(conn, "Trang bị đã nâng tối đa");
                }
                conn.p.time_speed_rebuild = 0;
                break;
            }
            case 3://tạo trang bị tinh tú
            {
                if (conn.p != null) {
                    try {
                        createItemStarBatch(conn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                for (int i = conn.p.TypeItemStarCreate * 5; i < conn.p.TypeItemStarCreate * 5 + 5; i++) {
                    if (conn.p.item.total_item_by_id(7, conn.p.MaterialItemStar[i]) < 1 && (conn.ac_admin < 4 || !Manager.BuffAdminMaterial)) {
                        Service.send_notice_box(conn, "Thiếu nguyên liệu!");
                        return;
                    }
                }
                for (int i = conn.p.TypeItemStarCreate * 5; i < conn.p.TypeItemStarCreate * 5 + 5; i++) {
                    conn.p.item.remove(7, conn.p.MaterialItemStar[i], 1);
                }
                short type_item = ItemStar.ConvertType(conn.p.TypeItemStarCreate, conn.p.ClazzItemStar);
                short id_item = ItemStar.GetIDItem(conn.p.TypeItemStarCreate, conn.p.ClazzItemStar);
                if (type_item == -1 || id_item == -1) {
                    Service.send_notice_box(conn, "Lỗi hãy thử lại sau!");
                    return;
                }
                List<Option> ops = ItemStar.GetOpsItemStar(conn.p.ClazzItemStar, (byte) type_item, 0);
                if (ops != null && !ops.isEmpty()) {
                    conn.p.ChangeMaterialItemStar(conn.p.TypeItemStarCreate);
                    int ran = Util.random(100);
                    byte color = 0;
                    if ((conn.ac_admin > 4 && Manager.BuffAdmin) || ran >= 95) {
                        color = 5;
                    } else if (ran >= 85)//8%
                    {
                        color = 4;
                    } else if (ran >= 75)//15%
                    {
                        color = 3;
                    } else if (ran >= 40)//19%
                    {
                        color = 2;
                    } else if (ran >= 29)//24%
                    {
                        color = 1;
                    }
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
                        if ((o.id >= 58 && o.id <= 60) || (o.id >= 100 && o.id <= 107)) {
                            itbag.op.add(new Option(o.id, pr, itbag.id));
                        } else if (o.id == 37 || o.id == 38) {
                            itbag.op.add(new Option(o.id, 2, itbag.id));
                        } else {
                            itbag.op.add(new Option(o.id, pr1, itbag.id));
                        }
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
                    if (color >= 3) {
                        itbag.op.add(Option.createOpItemStar(129, itbag.id));
                        itbag.op.add(Option.createOpItemStar(130, itbag.id));
                    }
                    itbag.color = color;
                    itbag.part = ItemTemplate3.item.get(id_item).getPart();
                    itbag.tier = 0;
                    itbag.time_use = 0;
                    itbag.islock = false;
                    conn.p.item.add_item_inventory3(itbag);
                    conn.p.item.char_inventory(3);
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(7);
                    conn.p.ResetCreateItemStar();
                    m.cleanup();
                    m = new Message(-105);
                    m.writer().writeByte(3);
                    m.writer().writeByte(3);
                    m.writer().writeUTF("Chúc mừng bạn nhận được " + itbag.name);
                    m.writer().writeByte(3);
                    m.writer().writeUTF(itbag.name);
                    m.writer().writeByte(itbag.clazz);
                    m.writer().writeShort(itbag.id);
                    m.writer().writeByte(itbag.type);
                    m.writer().writeShort(itbag.icon);
                    m.writer().writeByte(0); // tier
                    m.writer().writeShort(1); // level required
                    m.writer().writeByte(itbag.color); // color
                    m.writer().writeByte(0); // can sell
                    m.writer().writeByte(0); // can trade
                    m.writer().writeByte(itbag.op.size());
                    for (int i = 0; i < itbag.op.size(); i++) {
                        byte ido = itbag.op.get(i).id;
                        if (ido < 0) {
                            ido = 0;
                        }
                        m.writer().writeByte(ido);
                        m.writer().writeInt(itbag.op.get(i).getParam(0));
                    }
                    m.writer().writeInt(0); // time use
                    m.writer().writeByte(itbag.islock ? 1 : 0);
                    m.writer().writeByte(0);
                    m.writer().writeByte(0);
                    conn.addmsg(m);
                    m.cleanup();
                } else {
                    Service.send_notice_box(conn, "không tìm thấy chỉ số trang bị phù hợp!");
                    return;
                }
                break;
            }
            case 4: {//nâng cấp tbtt
                if (id >= conn.p.item.inventory3.length || id < 0) {
                    return;
                }
                Item3 temp = conn.p.item.inventory3[id];
                if (temp != null && temp.id >= 4656 && temp.id <= 4675 && temp.tierStar < 9) {
                    if (temp.getParamOption((byte) 129) == 0) {
                        Service.send_notice_box(conn, "Trang bị đã hết độ bền không thể nâng");
                        return;
                    }
                    conn.p.id_Upgrade_Medal_Star = id;
                    MenuController.send_menu_select(conn, 111, new String[]{"Không = " + (Ratio_UpgradeItemStar[temp.tierStar] / 100) + "%",
                        "Đá hỏa tinh = " + ((Ratio_UpgradeItemStar[temp.tierStar] + 500) / 100) + "%"}, (byte) 1);
                } else {
                    Service.send_notice_box(conn, "Trang bị không phù hợp hoặc đã đạt cấp tối đa!");
                    return;
                }
                break;
            }
        }
    }

    private static void createItemStarBatch(Session conn) throws IOException {
        int createCount = (conn.ac_admin > 3 && conn.p.CountItemStarCreate > 1) ? conn.p.CountItemStarCreate : 1;
        if (conn.p.item.get_inventory_able() < createCount) {
            Service.send_notice_box(conn, "Can trong " + createCount + " o hanh trang");
            return;
        }
        for (int i = conn.p.TypeItemStarCreate * 5; i < conn.p.TypeItemStarCreate * 5 + 5; i++) {
            if (conn.p.item.total_item_by_id(7, conn.p.MaterialItemStar[i]) < createCount
                    && (conn.ac_admin < 4 || !Manager.BuffAdminMaterial)) {
                Service.send_notice_box(conn, "Thieu nguyen lieu!");
                return;
            }
        }
        short typeItem = ItemStar.ConvertType(conn.p.TypeItemStarCreate, conn.p.ClazzItemStar);
        short idItem = ItemStar.GetIDItem(conn.p.TypeItemStarCreate, conn.p.ClazzItemStar);
        if (typeItem == -1 || idItem == -1) {
            Service.send_notice_box(conn, "Loi hay thu lai sau!");
            return;
        }
        for (int i = conn.p.TypeItemStarCreate * 5; i < conn.p.TypeItemStarCreate * 5 + 5; i++) {
            conn.p.item.remove(7, conn.p.MaterialItemStar[i], createCount);
        }
        Item3 lastItem = null;
        Set<String> batchSignatures = new HashSet<>();
        for (int i = 0; i < createCount; i++) {
            Item3 rolledItem = null;
            for (int attempt = 0; attempt < 20; attempt++) {
                Item3 tempItem = createItemStarReward(conn, (byte) typeItem, idItem);
                if (tempItem == null) {
                    break;
                }
                rolledItem = tempItem;
                String signature = buildItemStarSignature(tempItem);
                if (createCount == 1 || batchSignatures.add(signature)) {
                    break;
                }
            }
            lastItem = rolledItem;
            if (lastItem == null) {
                Service.send_notice_box(conn, "Loi tao vat pham, hay thu lai sau!");
                return;
            }
            if (createCount > 1) {
                batchSignatures.add(buildItemStarSignature(lastItem));
            }
            conn.p.item.add_item_inventory3(lastItem);
        }
        conn.p.ChangeMaterialItemStar(conn.p.TypeItemStarCreate);
        conn.p.item.char_inventory(3);
        conn.p.item.char_inventory(4);
        conn.p.item.char_inventory(7);
        conn.p.ResetCreateItemStar();
        if (lastItem != null) {
            sendItemStarCreateMessage(conn, lastItem, createCount);
        }
    }

    private static Item3 createItemStarReward(Session conn, byte typeItem, short idItem) {
        ItemTemplate3 template = ItemTemplate3.item.get(idItem);
        if (template == null) {
            return null;
        }
        List<Option> ops = ItemStar.GetOpsItemStar(conn.p.ClazzItemStar, typeItem, 0);
        if (ops == null || ops.isEmpty()) {
            return null;
        }
        int ran = Util.random(100);
        byte color = 0;
        if ((conn.ac_admin > 4 && Manager.BuffAdmin) || ran >= 95) {
            color = 5;
        } else if (ran >= 85) {
            color = 4;
        } else if (ran >= 75) {
            color = 3;
        } else if (ran >= 40) {
            color = 2;
        } else if (ran >= 29) {
            color = 1;
        }
        Item3 itbag = new Item3();
        itbag.id = idItem;
        itbag.name = template.getName();
        itbag.clazz = template.getClazz();
        itbag.type = template.getType();
        itbag.level = 45;
        itbag.icon = template.getIcon();
        itbag.op = new ArrayList<>();
        for (Option o : ops) {
            int pr = o.getParam(0);
            int pr1 = (int) (pr * color * 0.25);
            if ((o.id >= 58 && o.id <= 60) || (o.id >= 100 && o.id <= 107)) {
                itbag.op.add(new Option(o.id, pr, itbag.id));
            } else if (o.id == 37 || o.id == 38) {
                itbag.op.add(new Option(o.id, 2, itbag.id));
            } else {
                itbag.op.add(new Option(o.id, pr1, itbag.id));
            }
        }
        addItemStarBonusOptions(itbag, color);
        if (color >= 3) {
            itbag.op.add(Option.createOpItemStar(129, itbag.id));
            itbag.op.add(Option.createOpItemStar(130, itbag.id));
        }
        itbag.color = color;
        itbag.part = template.getPart();
        itbag.tier = 0;
        itbag.time_use = 0;
        itbag.islock = false;
        return itbag;
    }

    private static void addItemStarBonusOptions(Item3 itbag, byte color) {
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
    }

    private static String buildItemStarSignature(Item3 item) {
        StringBuilder builder = new StringBuilder();
        builder.append(item.id).append('|').append(item.color).append('|');
        for (Option option : item.op) {
            if (option == null) {
                continue;
            }
            builder.append(option.id).append(':').append(option.getParam(0)).append(';');
        }
        return builder.toString();
    }

    private static void sendItemStarCreateMessage(Session conn, Item3 itbag, int createCount) throws IOException {
        Message m = new Message(-105);
        m.writer().writeByte(3);
        m.writer().writeByte(3);
        if (createCount > 1) {
            m.writer().writeUTF("Da tao thanh cong " + createCount + " mon, vat pham hien thi la mon cuoi cung");
        } else {
            m.writer().writeUTF("Chuc mung ban nhan duoc " + itbag.name);
        }
        m.writer().writeByte(3);
        m.writer().writeUTF(itbag.name);
        m.writer().writeByte(itbag.clazz);
        m.writer().writeShort(itbag.id);
        m.writer().writeByte(itbag.type);
        m.writer().writeShort(itbag.icon);
        m.writer().writeByte(0);
        m.writer().writeShort(1);
        m.writer().writeByte(itbag.color);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeByte(itbag.op.size());
        for (int i = 0; i < itbag.op.size(); i++) {
            byte ido = itbag.op.get(i).id;
            if (ido < 0) {
                ido = 0;
            }
            m.writer().writeByte(ido);
            m.writer().writeInt(itbag.op.get(i).getParam(0));
        }
        m.writer().writeInt(0);
        m.writer().writeByte(itbag.islock ? 1 : 0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        conn.addmsg(m);
        m.cleanup();
    }

    public static void UpgradeItemStar(Session conn, byte index) throws IOException {
        try {
            int id = conn.p.id_Upgrade_Medal_Star;
            if (id >= conn.p.item.inventory3.length || id < 0) {
                return;
            }
            Item3 temp = conn.p.item.inventory3[id];
            int sl = temp.tierStar + 1;
            if (temp == null || !(temp.id >= 4656 && temp.id <= 4675)) {
                Service.send_notice_box(conn, "Trang bị không phù hợp!");
                return;
            }
            for (int i = conn.p.TypeItemStarCreate * 5; i < conn.p.TypeItemStarCreate * 5 + 5; i++) {
                if (conn.p.item.total_item_by_id(7, conn.p.MaterialItemStar[i]) < sl 
                        && (conn.ac_admin < 4 || !Manager.BuffAdminMaterial)) {
                    Service.send_notice_box(conn, "Thiếu nguyên liệu!");
                    return;
                }
            }
            
            if (conn.p.get_vang() < 5000000) {
                Service.send_notice_box(conn, "Không đủ 5.000.000 vàng");
                return;
            }

            if (temp.tierStar >= Ratio_UpgradeItemStar.length) {
                Service.send_notice_box(conn, "Trang bị đã đạt cấp tối đa!");
                return;
            }
            if (index == 1 && conn.p.item.total_item_by_id(7, 471) < 1) {
                Service.send_notice_box(conn, "Bạn không đủ đá hỏa tinh!");
                return;
            }
            boolean suc = (Ratio_UpgradeItemStar[temp.tierStar] + (index == 1 ? 500 : 0) + temp.getParamOption((byte) 130))
                    > Util.random(11000) || (conn.ac_admin > 3 && Manager.BuffAdmin);
            if (suc) {
                List<Option> ops = ItemStar.GetOpsItemStarUpgrade(temp.clazz, temp.type, temp.id, temp.tierStar + 1, temp.op);
                if (ops == null || ops.isEmpty()) {
                    Service.send_notice_box(conn, "Lỗi không tìm thấy chỉ số, hãy chụp lại chỉ số và báo ngay cho ad \"Nhắn riêng\"");
                    return;
                }

                temp.tierStar++;
                temp.level = ItemStar.GetLevelItemStar(temp.tierStar);
                temp.op.clear();
                temp.UpdateName();
                for (Option o : ops) {
                    int pr = o.getParam(0);
                    if ((o.id >= 58 && o.id <= 60) || (o.id >= 100 && o.id <= 107)) {
                        temp.op.add(new Option(o.id, pr, temp.id));
                    } else if (o.id == 37 || o.id == 38) {
                        temp.op.add(new Option(o.id, 2, temp.id));
                    } else if (o.id == -115 || o.id == -103 || o.id == -88 || o.id == -87) {
                        temp.op.add(new Option(o.id, pr - 3000, temp.id));
                    } else if (o.id < -70) {
                        int pr1 = (int) (pr + (int) temp.color * 5.067);
                        int pr2 = (int) (pr + (int) temp.color * 5.767);
                        temp.op.add(new Option(o.id, Util.random(pr1, pr2), temp.id));
                    } else {
                        int pr1 = (int) (pr * temp.color * 0.25);
                        temp.op.add(new Option(o.id, pr1, temp.id));
                    }
                }
            } else {
                if (index == 0) {
                    for (Option o : temp.op) {
                        if (o.id == (byte) 129) {
                            o.param -= 100;
                        }
                    }
                }
            }
            // xóa nl
            for (int i = conn.p.TypeItemStarCreate * 5; i < conn.p.TypeItemStarCreate * 5 + 5; i++) {
                conn.p.item.remove(7, conn.p.MaterialItemStar[i], sl);
            }
            conn.p.update_vang(-5000000, "Nâng cấp trang bị tinh tú " + temp.name);
            
            if (index == 1) {
                conn.p.item.remove(7, 471, 1);
            }
            if (suc && (temp.tierStar + 1) % 3 == 0) {
                conn.p.ChangeMaterialItemStar(conn.p.TypeItemStarCreate);
            }
            conn.p.item.char_inventory(4);
            conn.p.item.char_inventory(7);
            conn.p.item.char_inventory(3);
            Message m = new Message(-105);
            m.writer().writeByte(3);
            if (suc) {
                m.writer().writeByte(3);
                m.writer().writeUTF("Thành công!");
            } else {
                m.writer().writeByte(4);
                m.writer().writeUTF("Thất bại!");
            }
            m.writer().writeByte(3);
            m.writer().writeUTF(temp.name);
            m.writer().writeByte(temp.clazz);
            m.writer().writeShort(temp.id);
            m.writer().writeByte(temp.type);
            m.writer().writeShort(temp.icon);
            m.writer().writeByte(temp.tier); // tier
            m.writer().writeShort(1); // level required
            m.writer().writeByte(temp.color); // color
            m.writer().writeByte(0); // can sell
            m.writer().writeByte(0); // can trade
            m.writer().writeByte(temp.op.size());
            for (int i = 0; i < temp.op.size(); i++) {
                m.writer().writeByte(temp.op.get(i).id);
                m.writer().writeInt(temp.op.get(i).getParam(0));

            }
            m.writer().writeInt(0); // time use
            m.writer().writeByte(0);
            m.writer().writeByte(0);
            m.writer().writeByte(0);
            conn.addmsg(m);
            m.cleanup();
            if (temp.tierStar < 9) {
                m = new Message(-105);
                m.writer().writeByte(5);
                if (suc) {
                    m.writer().writeByte(3);
                    m.writer().writeUTF("Thành công, xin chúc mừng");
                } else {
                    m.writer().writeByte(4);
                    m.writer().writeUTF("Thất bại rồi");
                }
                m.writer().writeShort(id);
                conn.addmsg(m);
                m.cleanup();
            }
        } catch (Exception eee) {
            eee.printStackTrace();
        }
    }

    public static final int[] quantity_update_armor = new int[]{1000, 2000, 4000, 8000, 300, 600, 900, 1200, 1500, 2000};
    public static final short[] material_update_armor = new short[]{481, 481, 481, 481, 485, 485, 485, 489, 489, 489};
    public static final short[] gems_update_armor = new short[]{1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000};
    public static short[] ratio_upgrade_armor = new short[]{11000, 10000, 9000, 8000, 5000, 4000, 3500, 3000, 2500, 2000};
    public static byte[] option1 = new byte[]{(byte) 171, (byte) 173, (byte) 175, 99, 91, 92, 93, 94, 27, 28, 29, 30};
    public static byte[] option2 = new byte[]{(byte) 185, (byte) 186, 89, 38, 37, 23, 24, 25, 26};

    public static void ActionsItemArmor(Session conn, Message m) {
        try {
            if (conn.p.time_speed_rebuild > System.currentTimeMillis()) {
                Service.send_notice_box(conn, "Thao tác quá nhanh.");
                return;
            }
            conn.p.time_speed_rebuild = System.currentTimeMillis() + 2000;

            byte type = m.reader().readByte();
            short id = -1;
            byte cat = -1;
            if (type != 3) {
                id = m.reader().readShort();
                cat = m.reader().readByte();
            } else {
                if (conn.p.type_armor_create == -1 || conn.p.id_armor_create == -1) {
                    return;
                }
            }
            switch (type) {
                case 0: {
                    if (cat != 3) {
                        Service.send_notice_box(conn, "Trang bị không phù hợp!");
                        return;
                    }
                    if (id >= conn.p.item.inventory3.length) {
                        return;
                    }
                    Item3 item = conn.p.item.inventory3[id];
                    if (item != null) {
                        if (item.type == 28) {
                            if (item.tier < 10) {
                                Message m_send = new Message(-105);
                                m_send.writer().writeByte(4);
                                m_send.writer().writeByte(1);
                                short id_material = (short) (material_update_armor[item.tier] + item.id - 4784);
                                m_send.writer().writeShort(id_material);
                                if (conn.version >= 270) {
                                    m_send.writer().writeShort(quantity_update_armor[item.tier]);
                                } else {
                                    m_send.writer().writeByte(quantity_update_armor[item.tier]);
                                }
                                conn.addmsg(m_send);
                                m_send.cleanup();
                            } else {
                                Service.send_notice_box(conn, "Trang bị đã nâng tối đa");
                            }
                        } else {
                            Service.send_notice_box(conn, "Vật phẩm không phù hợp");
                        }
                    } else {
                        Service.send_notice_box(conn, "Vui lòng chọn giáp siêu nhân để nâng cấp");
                    }
                    break;
                }
                case 3: {
                    int id_material = 481 + conn.p.id_armor_create;
                    if (conn.p.item.total_item_by_id(7, id_material) < 500) {
                        conn.p.type_armor_create = -1;
                        conn.p.id_armor_create = -1;
                        conn.p.isCreateArmor = false;
                        Service.send_notice_box(conn, "Không đủ nguyên liệu");
                        return;
                    }
                    if (conn.p.get_vang() < 5000000) {
                        conn.p.type_armor_create = -1;
                        conn.p.id_armor_create = -1;
                        conn.p.isCreateArmor = false;
                        Service.send_notice_box(conn, "Không đủ 5,000,000 vàng");
                        return;
                    }
                    conn.p.update_vang(-5000000, "Tạo giáp siêu nhân");
                    conn.p.item.remove(7, id_material, 500);

                    Item3 itbag = new Item3();
                    short iditem = (short) (4784 + conn.p.id_armor_create);
                    ItemTemplate3 temp = ItemTemplate3.item.get(iditem);
                    itbag.id = iditem;
                    itbag.name = temp.getName();
                    itbag.clazz = temp.getClazz();
                    itbag.type = temp.getType();
                    itbag.level = temp.getLevel();
                    itbag.icon = temp.getIcon();
                    itbag.op = new ArrayList<>();
                    itbag.op.add(new Option(15, 100, itbag.id));
                    itbag.op.add(new Option(17 + conn.p.type_armor_create, 100, itbag.id));
                    itbag.color = temp.getColor();
                    itbag.part = temp.getPart();
                    itbag.tier = 0;
                    itbag.islock = false;
                    itbag.time_use = 0;
                    itbag.expiry_date = 0;
                    conn.p.item.add_item_inventory3(itbag);
                    conn.p.item.char_inventory(7);
                    conn.p.type_armor_create = -1;
                    conn.p.id_armor_create = -1;
                    conn.p.isCreateArmor = false;

                    m.cleanup();
                    m = new Message(-105);
                    m.writer().writeByte(3);
                    m.writer().writeByte(3);
                    m.writer().writeUTF("Chúc mừng bạn đã nhận được " + temp.getName());
                    m.writer().writeByte(3);
                    m.writer().writeUTF(temp.getName());
                    m.writer().writeByte(temp.getClazz());
                    m.writer().writeShort(temp.getId());
                    m.writer().writeByte(temp.getType());
                    m.writer().writeShort(temp.getIcon());
                    m.writer().writeByte(0); // tier
                    m.writer().writeShort(1); // level required
                    m.writer().writeByte(temp.getColor()); // color
                    m.writer().writeByte(0); // can sell
                    m.writer().writeByte(0); // can trade
                    m.writer().writeByte(temp.getOp().size());
                    for (int i = 0; i < temp.getOp().size(); i++) {
                        m.writer().writeByte(temp.getOp().get(i).id);
                        m.writer().writeInt(temp.getOp().get(i).getParam(0));
                    }
                    m.writer().writeInt(0); // time use
                    m.writer().writeByte(0);
                    m.writer().writeByte(0);
                    m.writer().writeByte(0);
                    conn.addmsg(m);
                    m.cleanup();

                    break;
                }
                case 4: {
                    if (cat != 3) {
                        return;
                    }
                    if (id >= conn.p.item.inventory3.length) {
                        return;
                    }
                    Item3 item = conn.p.item.inventory3[id];
                    if (item != null && item.type == 28 && item.tier < 10) {
                        short id_material = (short) (material_update_armor[item.tier] + item.id - 4784);
                        System.out.println(id_material);
                        int quantity = quantity_update_armor[item.tier];
                        int gems = gems_update_armor[item.tier];

                        if (conn.p.get_ngoc() < gems) {
                            Service.send_notice_box(conn, "Không đủ " + gems + " ngọc");
                            return;
                        }
                        if (conn.p.item.total_item_by_id(7, id_material) >= quantity) {
                            boolean suc = ratio_upgrade_armor[item.tier] > Util.random(11000);
                            conn.p.update_ngoc(-gems);
                            if (suc) {
                                item.tier++;
                                for (int i = 0; i < item.op.size(); i++) {
                                    Option op = item.op.get(i);
                                    if (op.id < 0 || op.id == 99) {
                                        op.param += Util.random(80, 100);
                                    } else if (91 <= op.id && op.id <= 94 || op.id == 15 || (op.id >= 17 && op.id <= 20)) {
                                        op.param += Util.random(80, 100);
                                    } else if (27 <= op.id && op.id <= 30) {
                                        op.param += Util.random(80, 100);
                                    } else if (23 <= op.id && op.id <= 26) {
                                        op.param += 1;
                                    } else if (37 <= op.id && op.id <= 38) {
                                        op.param += 1;
                                    }
                                }
                                if (item.tier == 5) {
                                    byte id_op = option1[Util.random(option1.length - 1)];
                                    int param = 1;
                                    if (id_op < 0 || id_op == 99) {
                                        param = 100;
                                    } else if (91 <= id_op && id_op <= 94) {
                                        param = Util.random(3, 6) * 100;
                                    } else {
                                        param = 5 * 100;
                                    }
                                    item.op.add(new Option(id_op, param, item.id));
                                } else if (item.tier == 9) {
                                    byte id_op = option2[Util.random(option2.length - 1)];
                                    int param = 1;
                                    if (id_op < 0) {
                                        param = 300;
                                    } else if (id_op == 89) {
                                        param = 100;
                                    }
                                    item.op.add(new Option(id_op, param, item.id));
                                } else if (item.tier == 6 || item.tier == 7 || item.tier == 8 || item.tier == 10) {
                                    if (Util.random(10) < 3) {
                                        byte id_op = option1[Util.random(option1.length - 1)];
                                        while (item.hasOp(id_op)) {
                                            id_op = option1[Util.random(option1.length - 1)];
                                        }
                                        int param = 1;
                                        if (id_op < 0 || id_op == 99) {
                                            param = 100;
                                        } else if (91 <= id_op && id_op <= 94) {
                                            param = Util.random(3, 6) * 100;
                                        } else {
                                            param = 5 * 100;
                                        }
                                        item.op.add(new Option(id_op, param, item.id));
                                    }
                                }
                                conn.p.item.remove(7, id_material, quantity);
                                conn.p.item.inventory3[id] = item;
                            } else {
                                if (item.tier < 4) {
                                    conn.p.item.remove(7, id_material, quantity);
                                } else if (item.tier < 8) {
                                    conn.p.item.remove(7, id_material, (quantity / 2));
                                } else if (item.tier < 10) {
                                    conn.p.item.remove(7, id_material, (quantity / 3));
                                }
                            }

                            m.cleanup();
                            m = new Message(-105);
                            m.writer().writeByte(3);
                            if (suc) {
                                m.writer().writeByte(3);
                            } else {
                                m.writer().writeByte(4);
                            }
                            ItemTemplate3 temp = ItemTemplate3.item.get(item.id);
                            if (suc) {
                                m.writer().writeUTF("Thành công!");
                            } else {
                                m.writer().writeUTF("Thất bại!");
                            }
                            m.writer().writeByte(3);
                            m.writer().writeUTF(item.name);
                            m.writer().writeByte(temp.getClazz());
                            m.writer().writeShort(temp.getId());
                            m.writer().writeByte(temp.getType());
                            m.writer().writeShort(temp.getIcon());
                            m.writer().writeByte(item.tier); // tier
                            m.writer().writeShort(1); // level required
                            m.writer().writeByte(item.color); // color
                            m.writer().writeByte(0); // can sell
                            m.writer().writeByte(0); // can trade
                            m.writer().writeByte(item.op.size());
                            for (int i = 0; i < item.op.size(); i++) {
                                m.writer().writeByte(item.op.get(i).id);
                                m.writer().writeInt(item.op.get(i).getParam(item.tier));
                            }
                            m.writer().writeInt(0); // time use
                            m.writer().writeByte(0);
                            m.writer().writeByte(0);
                            m.writer().writeByte(0);
                            conn.addmsg(m);
                            m.cleanup();
                            //
                            conn.p.item.char_inventory(4);
                            conn.p.item.char_inventory(7);
                            conn.p.item.char_inventory(3);

                            m = new Message(-105);
                            m.writer().writeByte(5);
                            if (suc) {
                                m.writer().writeByte(3);
                            } else {
                                m.writer().writeByte(4);
                            }
                            if (suc) {
                                m.writer().writeUTF("Thành công!");
                            } else {
                                m.writer().writeUTF("Thất bại!");
                            }
                            m.writer().writeShort(id);
                            conn.addmsg(m);
                            m.cleanup();

                        } else {
                            Service.send_notice_box(conn, "Không đủ nguyên liệu");
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
