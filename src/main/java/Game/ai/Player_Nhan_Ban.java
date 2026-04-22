package Game.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Game.core.Util;
import Game.io.Session;
import Game.map.ItemMap;
import Game.map.MapService;
import Game.client.Player;
import Game.activities.ChienTruong;
import Game.io.Message;
import Game.map.Map;
import Game.template.EffTemplate;
import Game.template.ItemTemplate7;
import Game.template.Point;

public class Player_Nhan_Ban {

    public static short[][] LOCATION = new short[][]{ //
            new short[]{318, 528, 516, 612}, // 2
            new short[]{416, 552, 120, 200}, // 3
            new short[]{424, 552, 304, 416}, // 4
            new short[]{235, 411, 493, 573} // 5
    };
    public int id;
    public short x, y;
    public short x_default, y_default;
    public Map map;
    public boolean isdie;
    public int hp, hp_max;
    public long time_move;
    public int target;
    public int village;
    public int dame;
    public long time_change_target;
    public long time_refresh;
    public int typePk;
    public short henshin;
    public String name;

    public static List<Player_Nhan_Ban> init() {
        int size2 = Map.get_map_by_id(56)[0].maxzone;
        int size3 = Map.get_map_by_id(60)[0].maxzone;
        int size4 = Map.get_map_by_id(58)[0].maxzone;
        int size5 = Map.get_map_by_id(54)[0].maxzone;
        int size6 = Map.get_map_by_id(61)[0].maxzone;

        int i = -2;
        List<Player_Nhan_Ban> result = new ArrayList<>();

        int size_linh_canh = 10;
        for (int j = 0; j < size2; j++) {
            for (int j2 = 0; j2 < size_linh_canh; j2++) { // 10 linh canh
                add_linh_canh(result, "Lính canh", i--, 56, j, 2, 2, (short) -1);
            }
        }
        for (int j = 0; j < size3; j++) {
            for (int j2 = 0; j2 < size_linh_canh; j2++) { // 10 linh canh
                add_linh_canh(result, "Lính canh", i--, 60, j, 3, 1, (short) -1);
            }
        }
        for (int j = 0; j < size4; j++) {
            for (int j2 = 0; j2 < size_linh_canh; j2++) { // 10 linh canh
                add_linh_canh(result, "Lính canh", i--, 58, j, 4, 4, (short) -1);
            }
        }
        for (int j = 0; j < size5; j++) {
            for (int j2 = 0; j2 < size_linh_canh; j2++) { // 10 linh canh
                add_linh_canh(result, "Lính canh", i--, 54, j, 5, 5, (short) -1);
            }
        }
        for (int j = 0; j < size6; j++) {
            // 4 tím, 1 đỏ, 5 vàng, 2 xanh
            add_linh_canh(result, "Rắn lưng gù", i--, 61, j, 2, 4, (short) 94);
            add_linh_canh(result, "Thần chết", i--, 61, j, 2, 4, (short) 96);
            add_linh_canh(result, "Hiệp sỹ xương trắng", i--, 61, j, 3, 1, (short) 93);
            add_linh_canh(result, "Thằn lằn mập", i--, 61, j, 3, 1, (short) 95);
            add_linh_canh(result, "Sói đô", i--, 61, j, 4, 5, (short) 97);
            add_linh_canh(result, "Rồng bụng bự", i--, 61, j, 4, 5, (short) 98);
            add_linh_canh(result, "Thiết chuỳ hộ vệ", i--, 61, j, 5, 2, (short) 99);
            add_linh_canh(result, "Thiết kích hộ vệ", i--, 61, j, 5, 2, (short) 100);
        }
        return result;
    }

    private static void add_linh_canh(List<Player_Nhan_Ban> result, String name, int i, int map, int zone, int village, int typePk, short henshin) {
        Player_Nhan_Ban temp = new Player_Nhan_Ban();
        temp.id = i;
        temp.name = name;
        if (map == 61) {
            if (typePk == 1) {
                temp.x_default = temp.x = 112;
                temp.y_default = temp.y = 88;
            } else if (typePk == 2) {
                temp.x_default = temp.x = 96;
                temp.y_default = temp.y = 944;
            } else if (typePk == 4) {
                temp.x_default = temp.x = 1008;
                temp.y_default = temp.y = 96;
            } else if (typePk == 5) {
                temp.x_default = temp.x = 976;
                temp.y_default = temp.y = 968;
            }
        } else {
            temp.x = 432;
            temp.y = 520;
        }
        temp.map = Map.get_map_by_id(map)[zone];
        temp.isdie = false;
        temp.hp_max = Util.random(200000, 300000);
        temp.hp = temp.hp_max;
        temp.target = -1;
        temp.village = village;
        temp.dame = 30000;
        temp.typePk = typePk;
        temp.henshin = henshin;
        result.add(temp);
    }

    public static void update(Map map) throws IOException {
        for (int i = 0; i < ChienTruong.gI().list_ai.size(); i++) {
            Player_Nhan_Ban temp = ChienTruong.gI().list_ai.get(i);
            if (temp.target == -1) {
                for (int j = 0; j < map.players.size(); j++) {
                    Player p_target = map.players.get(j);
                    if (p_target != null && p_target.typepk != temp.typePk && Math.abs(p_target.x - temp.x) < 150
                            && Math.abs(p_target.y - temp.y) < 150 && temp.time_change_target < System.currentTimeMillis()) {
                        temp.target = p_target.ID;
                        temp.time_change_target = System.currentTimeMillis() + 2000L;
                    }
                }
            }
            if (!temp.isdie && temp.map.equals(map) && temp.time_move < System.currentTimeMillis()) {
                temp.time_move = System.currentTimeMillis() + Util.random(2000, 5000);
                //
                Player p0 = Map.get_player_by_id(temp.target);
                if (p0 != null && temp.typePk != p0.typepk) { // atk
                    if (!p0.isdie && p0.map.equals(temp.map)) {
                        if (Math.abs(p0.x - temp.x) < 150 && Math.abs(p0.y - temp.y) < 150) {
                            temp.x = (short) (p0.x + Util.random(-30, 30));
                            temp.y = (short) (p0.y + Util.random(-30, 30));
                            move(map, temp);
                        }
                        atk(map, temp, p0);
                    } else {
                        temp.target = -1;
                    }
                } else {
                    if (map.map_id != 61) {
                        temp.x = (short) Util.random(Player_Nhan_Ban.LOCATION[temp.village - 2][0],
                                Player_Nhan_Ban.LOCATION[temp.village - 2][1]);
                        temp.y = (short) Util.random(Player_Nhan_Ban.LOCATION[temp.village - 2][2],
                                Player_Nhan_Ban.LOCATION[temp.village - 2][3]);
                    } else {
                        Point point = Point.getPoint(temp.x_default, temp.y_default, 150);
                        temp.x = (short) point.x;
                        temp.y = (short) point.y;
                    }
                    move(map, temp);
                }
            }
            if (temp.time_refresh < System.currentTimeMillis()) {
                if (temp.isdie) {
                    temp.hp = temp.hp_max;
                    temp.isdie = false;
                } else {
                    temp.hp += 1000;
                    if (temp.hp > temp.hp_max) {
                        temp.hp = temp.hp_max;
                    }
                }
                temp.time_refresh = System.currentTimeMillis() + 60_000L;
            }
        }
    }

    private static void atk(Map map, Player_Nhan_Ban temp, Player p0) throws IOException {
        int dame = (temp.dame * Util.random(90, 100)) / 100;
        int[] skills = new int[]{0, 2, 4, 6, 8};
        Message m = new Message(6);
        m.writer().writeShort(temp.id);
        m.writer().writeByte(skills[Util.random(skills.length)]); // indexskill
        m.writer().writeByte(1);
        m.writer().writeShort(p0.ID);
        boolean crit = 10 > Util.random(0, 120);
        int rc_dame = p0.body.get_PhanDame();
        EffTemplate ef = p0.get_eff(35);
        if (ef != null) {
            rc_dame += ef.param;
        }
        boolean react_dame = rc_dame > Util.random(0, 15000);
        if (crit) {
            dame *= 2;
        }
        int miss = p0.body.get_Miss(false);
        ef = p0.get_eff(34);
        if (ef != null) {
            miss += ef.param;
        }
        if (miss > Util.random(0, 15_000)) {
            dame = 0;
            react_dame = false;
            crit = false;
        }
        p0.hp -= dame;
        if (p0.hp <= 0) {
            p0.hp = 0;
            if (!p0.isdie) {
                p0.dame_affect_special_sk = 0;
                p0.hp = 0;
                p0.isdie = true;
                Message m2 = new Message(41);
                m2.writer().writeShort(p0.ID);
                m2.writer().writeShort(temp.id);
                m2.writer().writeShort(-1); // point pk
                m2.writer().writeByte(1); // type main object
                MapService.send_msg_player_inside(map, p0, m2, true);
                m2.cleanup();
            }
        }
        //
        m.writer().writeInt(dame); // dame
        m.writer().writeInt(p0.hp); // hp after
        //
        if (dame > 0 && crit) {
            if (react_dame) {
                m.writer().writeByte(2); // size color show
                //
                m.writer().writeByte(4); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
                m.writer().writeInt((int) dame); // par
                //
                m.writer().writeByte(5);
                m.writer().writeInt((int) dame);
            } else {
                m.writer().writeByte(1); // size color show
                //
                m.writer().writeByte(4); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
                m.writer().writeInt((int) dame); // par
                //
            }
        } else {
            if (react_dame) {
                m.writer().writeByte(2);
                m.writer().writeByte(0);
                m.writer().writeInt((int) dame);
                m.writer().writeByte(5);
                m.writer().writeInt(dame);
            } else {
                m.writer().writeByte(0);
            }
        }
        if (react_dame && dame > 0) {
            temp.hp -= dame;
            if (temp.hp <= 0) {
                temp.hp = 0;
                temp.isdie = true;
                Message m3 = new Message(8);
                m3.writer().writeShort(temp.id);
                for (int i = 0; i < map.players.size(); i++) {
                    Player pp = map.players.get(i);
                    pp.conn.addmsg(m);
                }
                m3.cleanup();
            }
        }
        m.writer().writeInt(temp.hp);
        m.writer().writeInt(0);
        m.writer().writeByte(11);
        m.writer().writeInt(0);
        for (int i = 0; i < map.players.size(); i++) {
            Player pp = map.players.get(i);
            pp.conn.addmsg(m);
        }
        m.cleanup();
    }

    private static void move(Map map, Player_Nhan_Ban temp) throws IOException {
        Message m22 = new Message(4);
        m22.writer().writeByte(0);
        m22.writer().writeShort(0);
        m22.writer().writeShort(temp.id);
        m22.writer().writeShort(temp.x);
        m22.writer().writeShort(temp.y);
        m22.writer().writeByte(-1);
        for (int i = 0; i < map.players.size(); i++) {
            Player p0 = map.players.get(i);
            p0.conn.addmsg(m22);
        }
        m22.cleanup();
    }

    public static void atk(Map map, Player p, int n2, int indexskill, int dame) throws IOException {

        if ((p.typepk == 2 && map.map_id == 56) || (p.typepk == 1 && map.map_id == 60)
                || (p.typepk == 4 && map.map_id == 58) || (p.typepk == 5 && map.map_id == 54)) {
            return;
        }
        short id = (short) n2;
        for (int i = 0; i < ChienTruong.gI().list_ai.size(); i++) {
            Player_Nhan_Ban temp = ChienTruong.gI().list_ai.get(i);
            if (temp.id == id) {
                Message m = new Message(6);
                m.writer().writeShort(p.ID);
                m.writer().writeByte(indexskill);
                m.writer().writeByte(1);
                m.writer().writeShort(temp.id);
                // if (indexskill == 17) {
                // MapService.add_eff_skill(map, p, p0, indexskill);
                // }
                EffTemplate ef = null;
                int cr = p.body.getCrit();
                ef = p.get_eff(33);
                if (ef != null) {
                    cr += ef.param;
                }
                boolean crit = cr > Util.random(0, 15000);
                if (crit) {
                    dame *= 2;
                }
                if (20 > Util.random(0, 120)) {
                    dame = 0;
                    crit = false;
                }
                if (dame < 0) {
                    dame = 0;
                }
                if (dame > 2_000_000_000) {
                    dame = 2_000_000_000;
                }
                temp.hp -= dame;
                if (temp.hp <= 0) {
                    temp.hp = 0;
                    temp.isdie = true;
                    //
                    Message m2 = new Message(41);
                    m2.writer().writeShort(temp.id);
                    m2.writer().writeShort(p.ID);
                    m2.writer().writeShort(-1); // point pk
                    m2.writer().writeByte(1); // type main object
                    for (int i2 = 0; i2 < map.players.size(); i2++) {
                        Player pp = map.players.get(i2);
                        pp.conn.addmsg(m2);
                    }
                    m2.cleanup();
                    //
                    Message m3 = new Message(8);
                    m3.writer().writeShort(temp.id);
                    for (int i2 = 0; i2 < map.players.size(); i2++) {
                        Player pp = map.players.get(i2);
                        pp.conn.addmsg(m3);
                    }
                    m3.cleanup();
                    //
                    p.update_point_arena(2);
                }
                //
                m.writer().writeInt((int) dame); // dame
                m.writer().writeInt(temp.hp); // hp after
                //
                int dame_react = 0;
                //
                if (dame > 0 && crit) {
                    m.writer().writeByte(1); // size color show
                    m.writer().writeByte(4); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
                    m.writer().writeInt((int) dame); // par
                } else {
                    m.writer().writeByte(0);
                }
                m.writer().writeInt(p.hp);
                m.writer().writeInt(p.mp);
                m.writer().writeByte(11);
                m.writer().writeInt(0);
                MapService.send_msg_player_inside(map, p, m, true);
                m.cleanup();
                break;
            }
        }
    }

    public static void player_attack(Session conn, Map map, Player_Nhan_Ban nhanBan, byte index_skill) throws IOException {
        if (conn.p.typepk == nhanBan.typePk || nhanBan.isdie) return;
        Message m_p_2_nb = new Message(6);
        m_p_2_nb.writer().writeShort(conn.p.ID);
        m_p_2_nb.writer().writeByte(index_skill);
        m_p_2_nb.writer().writeByte(1);
        m_p_2_nb.writer().writeShort(nhanBan.id);
        int dame = Util.random(nhanBan.hp_max / 10, nhanBan.hp_max / 8);
        boolean crit = conn.p.body.getCrit() > Util.random(0, 10000);
        if (crit) {
            dame *= 2;
        }
        if (map.map_id != 61 && map.Arena.timeBienHinh > System.currentTimeMillis()) {
            dame = 1;
        }
        nhanBan.hp -= dame;
        if (nhanBan.hp <= 0) {
            nhanBan.hp = 0;
            conn.p.update_point_arena(2);
            nhanBan.isdie = true;
            Message m_out = new Message(8);
            m_out.writer().writeShort(nhanBan.id);
            for (int j = 0; j < map.players.size(); j++) {
                map.players.get(j).conn.addmsg(m_out);
            }
            m_out.cleanup();
            if (Util.random(100) < 50) {
                leave_material(map, (short) Util.random(126, 130), conn.p, nhanBan.id);
            }
        }
        //
        m_p_2_nb.writer().writeInt(dame); // dame
        m_p_2_nb.writer().writeInt((nhanBan != null) ? nhanBan.hp : 0); // hp after
        //
        if (dame > 0 && crit) {
            m_p_2_nb.writer().writeByte(1); // size color show
            m_p_2_nb.writer().writeByte(4); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
            m_p_2_nb.writer().writeInt(dame); // par
        } else {
            m_p_2_nb.writer().writeByte(0);
        }
        m_p_2_nb.writer().writeInt(conn.p.hp);
        m_p_2_nb.writer().writeInt(conn.p.mp); // mp nhan ban
        m_p_2_nb.writer().writeByte(11);
        m_p_2_nb.writer().writeInt(0);
        MapService.send_msg_player_inside(conn.p.map, conn.p, m_p_2_nb, true);
        m_p_2_nb.cleanup();
    }

    public static void leave_material(Map map, short id_it, Player p_master, int index) throws IOException {
        if (p_master != null && !p_master.isDropMaterialMedal && id_it >= 46 && id_it <= 345) {
            return;
        }
        int index_item_map = map.get_item_map_index_able();
        if (index_item_map > -1) {
            //
            map.item_map[index_item_map] = new ItemMap();
            map.item_map[index_item_map].id_item = id_it;
            if (ItemTemplate7.item.get(map.item_map[index_item_map].id_item).getColor() == 21) {
                map.item_map[index_item_map].color = 1;
            } else {
                map.item_map[index_item_map].color = 0;
            }
            map.item_map[index_item_map].quantity = 1;
            map.item_map[index_item_map].category = 7;
            map.item_map[index_item_map].setIdmaster((short) p_master.ID);
            map.item_map[index_item_map].time_exist = System.currentTimeMillis() + 60_000L;
            map.item_map[index_item_map].time_pick = System.currentTimeMillis() + 1_500L;
            // add in4 game scr
            Message mi = new Message(19);
            mi.writer().writeByte(7);
            mi.writer().writeShort(-1); // id mob die
            mi.writer().writeShort(ItemTemplate7.item.get(map.item_map[index_item_map].id_item).getIcon());
            mi.writer().writeShort(index_item_map); //
            mi.writer().writeUTF(ItemTemplate7.item.get(map.item_map[index_item_map].id_item).getName());
            mi.writer().writeByte(map.item_map[index_item_map].color); // color
            mi.writer().writeShort(index); // id player die
            MapService.send_msg_player_inside(map, p_master, mi, true);
            mi.cleanup();
        }
    }
}
