package Game.map;

import Game.Quest.QuestService;
import Game.activities.Maze;
import Game.core.Util;
import Game.activities.ChiemThanhManager;
import Game.ai.MobAi;
import Game.ai.NhanBan;
import Game.ai.Player_Nhan_Ban;

import java.io.IOException;
import java.util.ArrayList;

import Game.client.Clan;
import Game.client.Pet;
import Game.client.Player;
import Game.client.Squire;
import Game.core.AdminToolServer;
import Game.core.Manager;
import Game.core.MenuController;
import Game.core.Service;
import Game.event.Event_2;
import Game.event.MobCay;
import Game.activities.ChienTruong;
import Game.io.Message;
import Game.io.Session;

import java.util.List;

import Game.template.EffTemplate;
import Game.template.Eff_TextFire;
import Game.template.Item3;
import Game.template.LvSkill;
import Game.template.MainObject;
import Game.template.MobDungeon;
import Game.template.Mob_MoTaiNguyen;
import Game.template.Pet_di_buon;
import Game.template.Pet_di_buon_manager;
import Game.template.StrucEff;

public class MapService {

    public static void enter(Map map, Player p) {
        if (!map.players.contains(p)) {
            map.players.add(p);
        }
        // Đệ tử
        if (p.isLiveSquire) {
            Squire.squireEnterMap(p);
        }
        try {
            if (map.zone_id == 5 && !map.isMapLoiDai() && !map.isMapChiemThanh()) {
                MapService.change_flag(map, p, -1);
            }
            map.send_map_data(p);
            // Reset và gửi mob ngay sau map data để client nhận mob trước các packet khác
            p.other_player_inside.clear();
            p.other_mob_inside.clear();
            p.other_mob_inside_update.clear();
            load_player_in_map(map, p);
            p.map.send_horse(p);
//            p.send_eff_loi_dai(95, 32000);
            Service.send_char_main_in4(p);
            Service.send_combo(p.conn);
            Service.send_point_pk(p);
            Service.send_health(p);
            Service.send_wear(p);
            p.change_new_date();

            QuestService.sendQuestList(p.conn);
            QuestService.sendQuestFinish(p.conn);
            QuestService.sendQuestDoing(p.conn);

            List<Integer> defaul = new ArrayList<>(java.util.Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
            List<Integer> pks = new ArrayList<>();
            if (Map.is_map_chiem_mo(map, true) && Manager.gI().chiem_mo.isRunning()) {
                int typepk = -1;
                for (Player pl : map.players) {
                    if (pl.ID == p.ID || pl.typepk == -1 || pl.is_nhanban) {
                        continue;
                    }
                    if (p.myclan != null && pl.myclan != null) {
                        if (pl.myclan.name_clan.equals(p.myclan.name_clan)) {
                            typepk = pl.typepk;
                            break;
                        }
                    }
                    pks.add((int) pl.typepk);
                }
                if (typepk == -1) {
                    Integer t = Util.random(defaul, pks);
                    if (t != null) {
                        typepk = t;
                    } else {
                        typepk = Util.random(1, 10);
                    }
                }
                Message m = new Message(42);
                m.writer().writeShort(p.ID);
                m.writer().writeByte(typepk);
                p.typepk = (byte) typepk;
                MapService.send_msg_player_inside(map, p, m, true);
                m.cleanup();
            }
            // }
            if (p.party != null) {
                p.party.sendin4();
            }
            if (map.map_id == 102 && map.kingCupMap != null) {
                short[] time = new short[]{(short) ((map.kingCupMap.timeWar - System.currentTimeMillis()) / 1000)};
                try {
                    if (map.kingCupMap.players_attack.contains(p)) {
                        map.kingCupMap.sendTile();
                    } else {
                        Service.send_time_box(p, (byte) 1, time, new String[]{"Lôi đài"});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (map.isMapChiemThanh()) {
                ChiemThanhManager.SenDataTime(p.conn);
            } else if (map.isMapChienTruong()) {
                if (map.Arena != null && map.Arena.timeBienHinh > System.currentTimeMillis()
                        && ((p.typepk == 2 && map.map_id != 55) || (p.typepk == 1 && map.map_id != 59)
                        || (p.typepk == 4 && map.map_id != 57) || (p.typepk == 3 && map.map_id != 53))) {
                    p.id_henshin = 102;
                } else {
                    p.id_henshin = -1;
                }
                for (int i = 0; i < map.players.size(); i++) {
                    Player p01 = map.players.get(i);
                    MapService.send_in4_other_char(map, p01, p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void leave(Map map, Player p) {
        if (map.players.contains(p)) {
            map.players.remove(p);
        }
        // Đệ tử
        if (p.isLiveSquire) {
            Squire.squireLeaveMap(p);
        }
        try {
            if (map.map_id == 87) {
                ChiemThanhManager.PlayerDie(p);
            } else if (map.kingCupMap != null) {
                Service.send_time_box(p, (byte) 0, new short[0], new String[0]);
                if (map.kingCupMap.players_attack.contains(p)) {
                    p.typepk = -1;
                }
                Message m = new Message(8);
                m.writer().writeShort(p.ID);
                send_msg_player_inside(map, p, m, false);
                m.cleanup();
            } else if (map.isMapLangPhuSuong()) {
                Service.send_time_box(p, (byte) 0, new short[0], new String[0]);
                Message m = new Message(8);
                m.writer().writeShort(p.ID);
                send_msg_player_inside(map, p, m, false);
                m.cleanup();
            } else {
                Message m = new Message(8);
                m.writer().writeShort(p.ID);
                send_msg_player_inside(map, p, m, false);
                m.cleanup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load_player_in_map(Map map, Player p) {
        try {
            Message m = new Message(4);
            m.writer().writeByte(0);
            m.writer().writeShort(0);
            m.writer().writeShort(p.ID);
            m.writer().writeShort(p.x);
            m.writer().writeShort(p.y);
            m.writer().writeByte(-1);
            if (map.map_id != 50) {
                MapService.update_inside_player(map, m, p);
                AdminToolServer.gI().sendFakeBotPresence(p);
            }
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send_msg_player_inside(Map map, MainObject mainObj, Message m, boolean included) {
        for (int i = 0; i < map.players.size(); i++) {
            Player p0 = map.players.get(i);
            if (AdminToolServer.gI().isFakeBotPlayer(p0)) {
                continue;
            }
            if (p0 != null && !p0.isSquire && ((Math.abs(p0.x - mainObj.x) < 1500 && Math.abs(p0.y - mainObj.y) < 1500)
                    || Map.is_map__load_board_player(map.map_id)) && (included || (mainObj.ID != p0.ID))) {
                p0.conn.addmsg(m);
            }
        }
    }

    public static void update_in4_2_other_inside(Map map, Player p) throws IOException {
        long _time = System.currentTimeMillis();
        if (p.get_EffMe_Kham(StrucEff.TangHinh) != null) {
            return;
        }
        for (int i = 0; i < map.players.size(); i++) {
            Player p0 = map.players.get(i);
            if (AdminToolServer.gI().isFakeBotPlayer(p0)) {
                continue;
            }
            if (p0.ID != p.ID && !p0.isSquire && ((Math.abs(p0.x - p.x) < 1500 && Math.abs(p0.y - p.y) < 1500)
                    || Map.is_map__load_board_player(map.map_id))) {
                MapService.send_in4_other_char(map, p0, p);
            }
        }
    }

    public static void send_move(Map map, Player p, Message m2) throws IOException {
        short mx = m2.reader().readShort();
        short my = m2.reader().readShort();
        boolean changeee = false;
        if (p.is_changemap && (!map.isMapChiemThanh() || ChiemThanhManager.isChangeMap(map))) {
            for (Vgo vgo : map.vgos) {
                if (Math.abs(vgo.x_old - p.x) < 40 && Math.abs(vgo.y_old - p.y) < 40) {
                    boolean ch = true;
                    if (Map.is_map_chien_truong(map.map_id)) {
                        switch (map.map_id) {
                            case 54: {
                                if (vgo.id_map_go == 53 && p.typepk != 5) {
                                    ch = false;
                                }
                                break;
                            }
                            case 56: {
                                if (vgo.id_map_go == 55 && p.typepk != 2) {
                                    ch = false;
                                }
                                break;
                            }
                            case 58: {
                                if (vgo.id_map_go == 57 && p.typepk != 4) {
                                    ch = false;
                                }
                                break;
                            }
                            case 60: {
                                if (vgo.id_map_go == 59 && p.typepk != 1) {
                                    ch = false;
                                }
                                break;
                            }
                        }
                    }
                    if (ch) {
                        p.change_map(p, vgo);
                    }
                    return;
                }
            }
        }
        if (p.is_changemap && map.isMapMaze()) {
            Vgo vgo = Maze.getVgo(p);
            if (vgo != null) {
                p.change_map(p, vgo);
                return;
            }
        }
        long _time = System.currentTimeMillis();
        if (Math.abs(p.x - mx) > (300) || Math.abs(p.y - my) > 300) {
            Message m = new Message(4);
            m.writer().writeByte(0);
            m.writer().writeShort(0);
            m.writer().writeShort(p.ID);
            m.writer().writeShort(p.x);
            m.writer().writeShort(p.y);
            m.writer().writeByte(-1);
            p.conn.addmsg(m);
            m.cleanup();
            return;
        }
        p.time_move = _time;
        short x_old = p.x;
        short y_old = p.y;

        p.x = mx;
        p.y = my;

        if (p.is_changemap && p.timeCantChangeMap < _time && (map.map_id < 83 || map.map_id > 86)) {
            for (Vgo vgo : map.vgos) {
                if (Math.abs(vgo.x_old - p.x) < 40 && Math.abs(vgo.y_old - p.y) < 40) {
                    Map[] map_go = Map.get_map_by_id(vgo.id_map_go);
                    if (p.level < map_go[0].request_level) {
                        Message m = new Message(4);
                        m.writer().writeByte(0);
                        m.writer().writeShort(0);
                        m.writer().writeShort(p.ID);
                        m.writer().writeShort(x_old);
                        m.writer().writeShort(y_old);
                        m.writer().writeByte(-1);
                        p.conn.addmsg(m);
                        m.cleanup();
                        Service.send_notice_nobox_white(p.conn, "Yêu cầu cấp độ " + map_go[0].request_level);
                        return;
                    }
                    p.change_map(p, vgo);
                    return;
                }
            }
        } else if (!(Math.abs(p.x_old - p.x) < 45 && Math.abs(p.y_old - p.y) < 45)) {
            p.is_changemap = true;
        }
        //
        if (map.map_id != 50) {
            Message m = new Message(4);
            m.writer().writeByte(0);
            m.writer().writeShort(0);
            m.writer().writeShort(p.ID);
            m.writer().writeShort(p.x);
            m.writer().writeShort(p.y);
            m.writer().writeByte(-1);
            //
            MapService.update_inside_player(map, m, p);
            AdminToolServer.gI().sendFakeBotPresenceOnMove(p);
            //
            m.cleanup();
        }
        if (p.pet_di_buon != null && p.pet_di_buon.id_map == p.map.map_id && p.map.zone_id == 5) {
            if (p.pet_di_buon.time_move < System.currentTimeMillis()) {
                p.pet_di_buon.time_move = System.currentTimeMillis() + 1000L;
                if (Math.abs(p.pet_di_buon.x - p.x) < (85 * p.pet_di_buon.speed)
                        && Math.abs(p.pet_di_buon.y - p.y) < (85 * p.pet_di_buon.speed)) {
                    p.pet_di_buon.x = p.x;
                    p.pet_di_buon.y = p.y;
                    if (p.pet_di_buon.speed != 1 && p.pet_di_buon.time_skill < System.currentTimeMillis()) {
                        p.pet_di_buon.speed = 1;
                        //
                        Message mm = new Message(7);
                        mm.writer().writeShort(p.pet_di_buon.ID);
                        mm.writer().writeByte(p.pet_di_buon.level);
                        mm.writer().writeShort(p.pet_di_buon.x);
                        mm.writer().writeShort(p.pet_di_buon.y);
                        mm.writer().writeInt(p.pet_di_buon.hp);
                        mm.writer().writeInt(p.pet_di_buon.get_HpMax());
                        mm.writer().writeByte(0);
                        mm.writer().writeInt(-1);
                        mm.writer().writeShort(-1);
                        mm.writer().writeByte(1);
                        mm.writer().writeByte(p.pet_di_buon.speed);
                        mm.writer().writeByte(0);
                        mm.writer().writeUTF(p.pet_di_buon.name);
                        mm.writer().writeLong(-1);
                        mm.writer().writeByte(4);
                        for (int i = 0; i < map.players.size(); i++) {
                            Player p0 = map.players.get(i);
                            if (p0 != null) {
                                p0.conn.addmsg(mm);
                            }
                        }
                        mm.cleanup();
                    }
                }
                Message m22 = new Message(4);
                m22.writer().writeByte(1);
                m22.writer().writeShort(131);
                m22.writer().writeShort(p.pet_di_buon.ID);
                m22.writer().writeShort(p.pet_di_buon.x);
                m22.writer().writeShort(p.pet_di_buon.y);
                m22.writer().writeByte(-1);
                for (int i = 0; i < map.players.size(); i++) {
                    Player p0 = map.players.get(i);
                    if (p0 != null) {
                        p0.conn.addmsg(m22);
                    }
                }
                m22.cleanup();
            }
        }
    }

    public static void update_inside_player(Map map, Message m, Player p) throws IOException {
        Message m4 = new Message(4);
        for (MobCay temp : map.mobEvens) {
            if ((Math.abs(temp.x - p.x) < 300 && Math.abs(temp.y - p.y) < 300) || Map.is_map__load_board_player(map.map_id)) {

                if (!p.other_mob_inside.containsKey((int) temp.index)) {
                    p.other_mob_inside.put((int) temp.index, true);
                }
                if (!p.other_mob_inside_update.containsKey((int) temp.index)) {
                    p.other_mob_inside_update.put((int) temp.index, false);
                }
                if (p.other_mob_inside.get((int) temp.index)) {
                    temp.SendMob(p.conn);
                    p.other_mob_inside.replace((int) temp.index, true, false);
                }
            }
        }
        for (Mob_in_map temp : map.bossInMaps) {
            if (temp.isdie) {
                continue;
            }
            if ((Math.abs(temp.x - p.x) < 200 && Math.abs(temp.y - p.y) < 200)
                    || Map.is_map__load_board_player(map.map_id)) {
                if (!temp.list_fight.contains(p)
                        && ((temp.template.mob_id == 151 || temp.template.mob_id == 152 || temp.template.mob_id == 154) || temp.isBoss()
                        || ((Math.abs(temp.x - p.x) < 50) && (Math.abs(temp.y - p.y) < 50)))) {
                    temp.list_fight.add(p);
                }
                if (!p.other_mob_inside.containsKey(temp.ID)) {
                    p.other_mob_inside.put(temp.ID, true);
                }
                if (!p.other_mob_inside_update.containsKey(temp.ID)) {
                    p.other_mob_inside_update.put(temp.ID, false);
                }
                if (p.other_mob_inside.get(temp.ID)) {
                    m4.writer().writeByte(1);
                    m4.writer().writeShort(temp.template.mob_id);
                    m4.writer().writeShort(temp.ID);
                    m4.writer().writeShort(temp.x);
                    m4.writer().writeShort(temp.y);
                    m4.writer().writeByte(-1);
                    p.other_mob_inside.replace(temp.ID, true, false);
                } else if (p.other_mob_inside_update.get(temp.ID)) {
                    //
                    map.BossIn4(p.conn, temp.ID);
                    p.other_mob_inside_update.replace(temp.ID, true, false);
                }
            } else if (p.other_mob_inside_update.containsKey(temp.ID) && !p.other_mob_inside_update.get(temp.ID)) {
                p.other_mob_inside_update.replace(temp.ID, false, true);
            }
        }
        for (Mob_in_map temp : map.mobs) {
            if (temp == null) {
                continue;
            }
            if (temp.isdie && !(temp.template.mob_id >= 89 && temp.template.mob_id <= 92)) {
                continue;
            }
            if ((Math.abs(temp.x - p.x) < 200 && Math.abs(temp.y - p.y) < 200)
                    || Map.is_map__load_board_player(map.map_id)) {
                if (!temp.list_fight.contains(p)
                        && ((temp.template.mob_id == 151 || temp.template.mob_id == 152 || temp.template.mob_id == 154) || temp.isBoss()
                        || temp.template.isBossEvent() || ((Math.abs(temp.x - p.x) < 50) && (Math.abs(temp.y - p.y) < 50)))) {
                    temp.list_fight.add(p);
                }
                if (!p.other_mob_inside.containsKey(temp.ID)) {
                    p.other_mob_inside.put(temp.ID, true);
                }
                if (!p.other_mob_inside_update.containsKey(temp.ID)) {
                    p.other_mob_inside_update.put(temp.ID, false);
                }
                if (p.other_mob_inside.get(temp.ID)) {
                    m4.writer().writeByte(1);
                    m4.writer().writeShort(temp.template.mob_id);
                    m4.writer().writeShort(temp.ID);
                    m4.writer().writeShort(temp.x);
                    m4.writer().writeShort(temp.y);
                    m4.writer().writeByte(-1);
                    p.other_mob_inside.replace(temp.ID, true, false);
                } else if (p.other_mob_inside_update.get(temp.ID)) {
                    //
                    Service.mob_in4(p, temp.ID);
                    p.other_mob_inside_update.replace(temp.ID, true, false);
                }
            } else if (p.other_mob_inside_update.containsKey(temp.ID) && !p.other_mob_inside_update.get(temp.ID)) {
                p.other_mob_inside_update.replace(temp.ID, false, true);
            }
        }
        //
        long _time = System.currentTimeMillis();
        boolean isth = p.get_EffMe_Kham(StrucEff.TangHinh) != null;
        for (int i = 0; i < map.players.size() && !isth; i++) {
            Player p0 = map.players.get(i);
            if (p0.ID == p.ID) {
                continue;
            }
            if (AdminToolServer.gI().isFakeBotPlayer(p0)) {
                continue;
            }
            if (p0.isSquire) {
                continue;
            }
            if (!p.other_player_inside.containsKey(p0.ID)) {
                p.other_player_inside.put(p0.ID, true);
            }
            p0.conn.addmsg(m);
            if (p.other_player_inside.get(p0.ID) && p0.get_EffMe_Kham(StrucEff.TangHinh) == null) {
                m4.writer().writeByte(0);
                m4.writer().writeShort(0);
                m4.writer().writeShort(p0.ID);
                m4.writer().writeShort(p0.x);
                m4.writer().writeShort(p0.y);
                m4.writer().writeByte(-1);
                //
                p.other_player_inside.replace(p0.ID, true, false);
            }
            if (p0.get_EffMe_Kham(StrucEff.TangHinh) != null && p.other_player_inside.containsKey(p0.ID)) {
                Message m3 = new Message(8);
                m3.writer().writeShort(p0.ID);
                p.conn.addmsg(m3);
                m3.cleanup();
                p.other_player_inside.remove(p0.ID);
            }
        }
        if (m4.writer().size() > 0) {
            p.conn.addmsg(m4);
        }
        m4.cleanup();
    }

    public static Mob_in_map get_mob_by_index(Map map, int n) {
        if (map != null) {
            for (Mob_in_map m : map.mobs) {
                if (m != null && m.ID == n) {
                    return m;
                }
            }
        }
        return null;
    }

    public static void mob_fire(Map map, Mob_in_map mob, Player p_target, int dame) throws IOException {
        if (mob.template.mob_id >= 89 && mob.template.mob_id <= 92) {
            return;
        }
        Message m = new Message(10);
        m.writer().writeByte(1);
        m.writer().writeShort(mob.ID);
        m.writer().writeInt(mob.hp);
        m.writer().writeByte(0);
        m.writer().writeByte(1);
        m.writer().writeShort(p_target.ID);
        m.writer().writeInt(dame); // dame mob
        m.writer().writeInt(p_target.hp);
        m.writer().writeByte(2); // id skill mob
        m.writer().writeByte(0);
        MapService.send_msg_player_inside(map, p_target, m, true);
        m.cleanup();
        // boss skill
        int percent_stun = 0;
        int time_stun = 0;
        if (mob.isBoss() && mob.level > 70) {
            int sizelv = p_target.level - mob.level;
            if (mob.template.mob_id == 174 || mob.level >= 120) {
                sizelv = 0;
            } else if (Map.is_map_cant_save_site(mob.map_id)) {
                sizelv = 0;
            }
            switch (sizelv) {
                case 5:
                case 4:
                case 3:
                case 2:
                case 1:
                case 0: {
                    percent_stun = 15;
                    time_stun = 2;
                    break;
                }
                default: {
                    dame *= 50;
                    percent_stun = 50;
                    time_stun = 5;
                    break;
                }
            }
        }
        if (p_target.get_EffMe_Kham(StrucEff.NgocKhaiHoan) != null) {
            return;
        }
        if (mob.isBoss() && (percent_stun > Util.random(0, 100))) {
            if (!p_target.isStunes(false)) {
                MapService.add_eff_stun(map, mob, p_target, time_stun, Util.random(4, 8));
            }
        }
    }

    public static void add_eff_stun(Map map, MainObject mainObj, MainObject focus, int time, int type)
            throws IOException {
        if (focus == null) {
            return;
        }
        Message m = new Message(75);
        m.writer().writeByte(type);
        m.writer().writeByte(focus.get_TypeObj());
        m.writer().writeShort(focus.ID);
        m.writer().writeShort(time);
        m.writer().writeByte(0);
        m.writer().writeShort(mainObj.ID);
        MapService.send_msg_player_inside(map, focus, m, true);
        m.cleanup();
        //
        int time_ = 1000 * time;
        switch (type) {
            case 7: {
                focus.add_EffDefault(-124, 1000, time_);
                break;
            }
            case 4: {
                focus.add_EffDefault(-123, 1000, time_);
                break;
            }
            case 5: {
                focus.add_EffDefault(-122, 1000, time_);
                break;
            }
            case 6: {
                focus.add_EffDefault(-121, 1000, time_);
                break;
            }
        }
    }

    public static void change_flag(Map map, Player p_target, int type) throws IOException {
        if ((map.zone_id == 5 && type != -1 && p_target.item.wear[11] != null && p_target.item.wear[11].id != 3593)
                || map.isMapLoiDai() || map.isMapChiemThanh()) {
            Service.send_notice_box(p_target.conn, "Không thể thực hiện!");
            return;
        }
        if (Map.is_map_chien_truong(map.map_id)) {
            return;
        }
        if (map.map_id != 52 && map.zone_id == 5) {
            if (p_target.item.wear[11] != null && p_target.item.wear[11].id != 3593) {
                type = 11;
            } else if (p_target.item.wear[11] != null && p_target.item.wear[11].id != 3599) {
                type = 12;
            }
        }
        if (p_target.isLiveSquire) {
            p_target.squire.typepk = (byte) type;
            
            Message msg = new Message(42);
            msg.writer().writeShort(p_target.squire.ID);
            msg.writer().writeByte(type);
            MapService.send_msg_player_inside(map, p_target.squire, msg, true);
            msg.cleanup();
        }
        Message m = new Message(42);
        m.writer().writeShort(p_target.ID);
        m.writer().writeByte(type);
        p_target.typepk = (byte) type;
        MapService.send_msg_player_inside(map, p_target, m, true);
        m.cleanup();
    }

    @SuppressWarnings("unused")
    public static void buff_skill(Map map, Session conn, Message m2) throws IOException {
        byte type = m2.reader().readByte();
        byte tem = m2.reader().readByte();
        byte size_buff = m2.reader().readByte();
        MapService.add_eff_skill(map, conn.p, null, type);
        for (int i = 0; i < size_buff; i++) {
            int id = Short.toUnsignedInt(m2.reader().readShort());
        }
    }

    public static void add_eff_skill(Map map, Player p, Player p2, byte index_skill) throws IOException {
        int sk_point = p.body.get_skill_point(index_skill);
        if (sk_point < 1) {
            return;
        }
        int time_buff = p.skills[index_skill].mLvSkill[sk_point - 1].timeBuff;
        int range = p.skills[index_skill].mLvSkill[sk_point - 1].range_lan;
        int n_target = p.skills[index_skill].mLvSkill[sk_point - 1].nTarget - 1;
        switch (p.clazz) {
            case 0: {
                if (index_skill == 18) {
                    p.add_EffDefault(23, 1000, 60_000);
                    Message m = new Message(75);
                    m.writer().writeByte(12);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    m.writer().writeShort(60);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    p.conn.addmsg(m);
                    m.cleanup();
                    MapService.send_eff_other(p.map, p, 23);
                    Service.send_char_main_in4(p);
                } else if (index_skill == 13) {
                    byte[] id_sk = new byte[]{15, 35};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 14) {
                    byte[] id_sk = new byte[]{33, 9, 7};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1]), p.get_pramskill_byid(index_skill, id_sk[2])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 17 && p2 != null) {
                    MapService.add_eff_stun(map, p, p2, 5, 7);
                }
                break;
            }
            case 1: {
                if (index_skill == 18) {
                    p.add_EffDefault(24, 1000, 60_000);
                    Message m = new Message(75);
                    m.writer().writeByte(12);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    m.writer().writeShort(60);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    p.conn.addmsg(m);
                    m.cleanup();
                    MapService.send_eff_other(map, p, 24);
                    Service.send_char_main_in4(p);
                } else if (index_skill == 13) {
                    byte[] id_sk = new byte[]{15, 34};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 14) {
                    byte[] id_sk = new byte[]{33, 11, 7};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1]), p.get_pramskill_byid(index_skill, id_sk[2])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 17 && p2 != null) {
                    MapService.add_eff_stun(map, p, p2, 10, 4);
                }
                break;
            }
            case 2: {
                if (index_skill == 18) {
                    p.add_EffDefault(52, 1000, 60_000);
                    Message m = new Message(75);
                    m.writer().writeByte(12);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    m.writer().writeShort(60);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    p.conn.addmsg(m);
                    m.cleanup();
                    MapService.send_eff_other(map, p, 52);
                } else if (index_skill == 13) {
                    byte[] id_sk = new byte[]{15, 35};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 14) {
                    byte[] id_sk = new byte[]{36, 8, 7};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1]), p.get_pramskill_byid(index_skill, id_sk[2])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 17 && p2 != null) {
                    MapService.add_eff_stun(map, p, p2, 10, 5);
                }
                break;
            }
            case 3: {
                if (index_skill == 18) {
                    p.add_EffDefault(53, 1000, 60_000);
                    Message m = new Message(75);
                    m.writer().writeByte(12);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    m.writer().writeShort(60);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    p.conn.addmsg(m);
                    m.cleanup();
                    MapService.send_eff_other(map, p, 53);
                } else if (index_skill == 13) {
                    byte[] id_sk = new byte[]{15, 34};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 14) {
                    byte[] id_sk = new byte[]{36, 10, 7};
                    int[] param_sk = new int[]{p.get_pramskill_byid(index_skill, id_sk[0]),
                        p.get_pramskill_byid(index_skill, id_sk[1]), p.get_pramskill_byid(index_skill, id_sk[2])};
                    for (int i = 0; i < p.map.players.size(); i++) {
                        if (n_target < 1) {
                            continue;
                        }
                        Player p0 = p.map.players.get(i);
                        if (p0 != null && p0.ID != p.ID && !p0.isdie && Math.abs(p0.x - p.x) < range
                                && Math.abs(p0.y - p.y) < range && p0.typepk != 0 && p.typepk == p0.typepk) {
                            for (int j = 0; j < id_sk.length; j++) {
                                p0.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                            }
                            MapService.add_eff_skill_msg(map, p, p0, index_skill, time_buff, id_sk, param_sk);
                            n_target--;
                        }
                    }
                    for (int j = 0; j < id_sk.length; j++) {
                        p.add_EffDefault(id_sk[j], param_sk[j], time_buff);
                    }
                    MapService.add_eff_skill_msg(map, p, p, index_skill, time_buff, id_sk, param_sk);
                } else if (index_skill == 17 && p2 != null) {
                    MapService.add_eff_stun(map, p, p2, 10, 6);
                }
                break;
            }
        }
    }

    private static void add_eff_skill_msg(Map map, Player p, Player p0, byte index_skill, int time_buff, byte[] id_sk,
            int[] param_sk) throws IOException {
        int index_skill2 = 0;
        switch (p.clazz) {
            case 0: {
                index_skill2 = index_skill;
                break;
            }
            case 1: {
                if (index_skill == 13) {
                    index_skill2 = 30;
                } else {
                    index_skill2 = 31;
                }
                break;
            }
            case 2: {
                index_skill2 = index_skill;
                break;
            }
            case 3: {
                if (index_skill == 13) {
                    index_skill2 = 30;
                } else {
                    index_skill2 = 31;
                }
                break;
            }
        }
        Message m = new Message(40);
        m.writer().writeByte(1);
        m.writer().writeByte(1);
        m.writer().writeShort(p.ID);
        m.writer().writeByte(index_skill);
        m.writer().writeInt(time_buff);
        m.writer().writeShort(p0.ID);
        m.writer().writeByte(0);
        m.writer().writeByte(index_skill2);
        if (index_skill == 13) {
            int index = -1;
            m.writer().writeByte(id_sk.length + 1);
            for (int i = 0; i < id_sk.length; i++) {
                m.writer().writeByte(id_sk[i]);
                m.writer().writeInt(param_sk[i]);
                if (id_sk[i] == 15) {
                    index = i;
                }
            }
            int param;
            if (index == -1) {
                param = 0;
            } else {
                param = param_sk[index];
            }
            m.writer().writeByte(14);
            m.writer().writeInt(p0.body.get_DefBase() * (param / 100) / 100);
        } else if (index_skill == 14) {
            int index = -1;
            int index1 = -1;
            int index2 = -1;
            int index3 = -1;
            int index4 = -1;
            m.writer().writeByte(id_sk.length + 5);
            for (int i = 0; i < id_sk.length; i++) {
                m.writer().writeByte(id_sk[i]);
                m.writer().writeInt(param_sk[i]);
                if (id_sk[i] == 7) {
                    index = i;
                }
                if (id_sk[i] == 8) {
                    index1 = i;
                }
                if (id_sk[i] == 9) {
                    index2 = i;
                }
                if (id_sk[i] == 10) {
                    index3 = i;
                }
                if (id_sk[i] == 11) {
                    index4 = i;
                }
            }
            int pr0, pr1, pr2, pr3, pr4;
            if (index == -1) {
                pr0 = 0;
            } else {
                pr0 = param_sk[index];
            }
            if (index1 == -1) {
                pr1 = 0;
            } else {
                pr1 = param_sk[index1];
            }
            if (index2 == -1) {
                pr2 = 0;
            } else {
                pr2 = param_sk[index2];
            }
            if (index3 == -1) {
                pr3 = 0;
            } else {
                pr3 = param_sk[index3];
            }
            if (index4 == -1) {
                pr4 = 0;
            } else {
                pr4 = param_sk[index4];
            }
            m.writer().writeByte(0);
            m.writer().writeInt((p0.body.get_DameProp(0) * (pr0 / 100)) / 100);
            m.writer().writeByte(1);
            m.writer().writeInt((p0.body.get_DameProp(1) * (pr1 / 100)) / 100);
            m.writer().writeByte(2);
            m.writer().writeInt((p0.body.get_DameProp(2) * (pr2 / 100)) / 100);
            m.writer().writeByte(3);
            m.writer().writeInt((p0.body.get_DameProp(3) * (pr3 / 100)) / 100);
            m.writer().writeByte(4);
            m.writer().writeInt((p0.body.get_DameProp(4) * (pr4 / 100)) / 100);
        } else {
            m.writer().writeByte(0);
        }
        p0.conn.addmsg(m);
        m.cleanup();
        if (p0.ID != p.ID) {
            m = new Message(40);
            m.writer().writeByte(1);
            m.writer().writeByte(1);
            m.writer().writeShort(p.ID);
            m.writer().writeByte(index_skill);
            m.writer().writeInt(time_buff);
            m.writer().writeShort(p0.ID);
            m.writer().writeByte(0);
            m.writer().writeByte(index_skill2);
            m.writer().writeByte(0);
            p.conn.addmsg(m);
            m.cleanup();
        }
        //
        m = new Message(40);
        m.writer().writeByte(0);
        m.writer().writeByte(1);
        m.writer().writeShort(p.ID);
        m.writer().writeByte(index_skill);
        m.writer().writeInt(time_buff);
        m.writer().writeShort(p0.ID);
        m.writer().writeByte(0);
        m.writer().writeByte(index_skill2);
        if (index_skill == 13) {
            int index = -1;
            m.writer().writeByte(id_sk.length + 1);
            for (int i = 0; i < id_sk.length; i++) {
                m.writer().writeByte(id_sk[i]);
                m.writer().writeInt(param_sk[i]);
                if (id_sk[i] == 15) {
                    index = i;
                }
            }
            int param;
            if (index == -1) {
                param = 0;
            } else {
                param = param_sk[index];
            }
            m.writer().writeByte(14);
            m.writer().writeInt(p0.body.get_DefBase() * (param / 100) / 100);
        } else if (index_skill == 14) {
            int index = -1;
            int index1 = -1;
            int index2 = -1;
            int index3 = -1;
            int index4 = -1;
            m.writer().writeByte(id_sk.length + 5);
            for (int i = 0; i < id_sk.length; i++) {
                m.writer().writeByte(id_sk[i]);
                m.writer().writeInt(param_sk[i]);
                if (id_sk[i] == 7) {
                    index = i;
                }
                if (id_sk[i] == 8) {
                    index1 = i;
                }
                if (id_sk[i] == 9) {
                    index2 = i;
                }
                if (id_sk[i] == 10) {
                    index3 = i;
                }
                if (id_sk[i] == 11) {
                    index4 = i;
                }
            }
            int pr0, pr1, pr2, pr3, pr4;
            if (index == -1) {
                pr0 = 0;
            } else {
                pr0 = param_sk[index];
            }
            if (index1 == -1) {
                pr1 = 0;
            } else {
                pr1 = param_sk[index1];
            }
            if (index2 == -1) {
                pr2 = 0;
            } else {
                pr2 = param_sk[index2];
            }
            if (index3 == -1) {
                pr3 = 0;
            } else {
                pr3 = param_sk[index3];
            }
            if (index4 == -1) {
                pr4 = 0;
            } else {
                pr4 = param_sk[index4];
            }
            m.writer().writeByte(0);
            m.writer().writeInt((p0.body.get_DameProp(0) * (pr0 / 100)) / 100);
            m.writer().writeByte(1);
            m.writer().writeInt((p0.body.get_DameProp(1) * (pr1 / 100)) / 100);
            m.writer().writeByte(2);
            m.writer().writeInt((p0.body.get_DameProp(2) * (pr2 / 100)) / 100);
            m.writer().writeByte(3);
            m.writer().writeInt((p0.body.get_DameProp(3) * (pr3 / 100)) / 100);
            m.writer().writeByte(4);
            m.writer().writeInt((p0.body.get_DameProp(4) * (pr4 / 100)) / 100);
        } else {
            m.writer().writeByte(0);
        }
        p0.conn.addmsg(m);
        m.cleanup();
    }

    private static void send_eff_other(Map map, Player p, int id) throws IOException {
        EffTemplate temp = p.get_EffDefault(id);
        if (temp != null) {
            switch (id) {
                case -121: {
                    Message m = new Message(75);
                    m.writer().writeByte(6);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    long time_exist = temp.time - System.currentTimeMillis();
                    if (time_exist < 1000) {
                        return;
                    }
                    m.writer().writeShort((short) (time_exist / 1000));
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    MapService.send_msg_player_inside(map, p, m, false);
                    m.cleanup();
                    break;
                }
                case -122: {
                    Message m = new Message(75);
                    m.writer().writeByte(5);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    long time_exist = temp.time - System.currentTimeMillis();
                    if (time_exist < 1000) {
                        return;
                    }
                    m.writer().writeShort((short) (time_exist / 1000));
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    MapService.send_msg_player_inside(map, p, m, false);
                    m.cleanup();
                    break;
                }
                case -123: {
                    Message m = new Message(75);
                    m.writer().writeByte(4);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    long time_exist = temp.time - System.currentTimeMillis();
                    if (time_exist < 1000) {
                        return;
                    }
                    m.writer().writeShort((short) (time_exist / 1000));
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    MapService.send_msg_player_inside(map, p, m, false);
                    m.cleanup();
                    break;
                }
                case -124: {
                    Message m = new Message(75);
                    m.writer().writeByte(7);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    long time_exist = temp.time - System.currentTimeMillis();
                    if (time_exist < 1000) {
                        return;
                    }
                    m.writer().writeShort((short) (time_exist / 1000));
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    MapService.send_msg_player_inside(map, p, m, false);
                    m.cleanup();
                    break;
                }
                case 23:
                case 24:
                case 52:
                case 53: {
                    Message m = new Message(75);
                    m.writer().writeByte(12);
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    long time_exist = temp.time - System.currentTimeMillis();
                    if (time_exist < 1000) {
                        return;
                    }
                    m.writer().writeShort((short) (time_exist / 1000));
                    m.writer().writeByte(0);
                    m.writer().writeShort(p.ID);
                    MapService.send_msg_player_inside(map, p, m, false);
                    m.cleanup();
                    break;
                }
            }
        }
    }

    public static void send_chat(Map map, Session conn, Message m2) throws IOException {
        String chat = m2.reader().readUTF();
        if (conn.ac_admin > 3 && chat.equals("lenhadmin")) {
            Message m = new Message(7);
            m.writer().writeShort(30109);
            m.writer().writeShort(40);
            m.writer().writeShort(conn.p.x);
            m.writer().writeShort(conn.p.y);
            m.writer().writeInt(1000);
            m.writer().writeInt(1000);
            m.writer().writeByte(0);
            m.writer().writeInt(1);
            m.writer().writeShort(-1);
            m.writer().writeByte(1);
            m.writer().writeByte(1);
            m.writer().writeByte(0);
            m.writer().writeLong(-1);
            m.writer().writeByte(0);
            conn.addmsg(m);
            m.cleanup();
            MenuController.send_menu_select(conn, 126, new String[]{"Bảo trì", "Cộng vàng x1.000.000.000",
                "Cộng ngọc x1.000.000", "Update data", "Lấy item", "Up level", "Set Xp", "Khóa mõm", "Gỡ khóa mõm", "Khóa vòng quay",
                "Khóa GD", "Khóa KMB", "Ấp trứng nhanh",
                "Buff Admin", "Buff Nguyên liệu", "Mở chiếm mỏ", "Đóng chiếm mỏ", "Reset mob events",
                (ChiemThanhManager.isRegister ? "Đóng" : "Mở") + " đăng kí chiếm thành", "Mở đăng kí chiến trường", "Dịch map", "loadconfig",
                (Manager.logErrorLogin ? "tắt" : "bật") + " log bug", "disconnect client", "check bug", "fix bug"});
        } else if (conn.ac_admin > 10 && chat.equals("xem")) {
            int num = 0;
            int count = 0;
            for (Map[] mm : Map.entrys) {
                for (Map map0 : mm) {
                    if (map0.mobEvens != null) {
                        for (int i = 0; i < map0.mobEvens.size(); i++) {
                            count++;
                        }
                    }
                }
            }
            for (Map[] maps : Map.entrys) {
                for (Map map_ : maps) {
                    num += map_.players.size();
                }
            }
            Service.send_notice_box(conn,
                    "Vị Trí " + conn.p.x + " - " + conn.p.y + "\n Map id : " + map.map_id + "\n Zone : " + map.zone_id
                    + "\n Số Người kết nối : " + Session.client_entry.size() + "\n Số Người online : " + num
                    + "\nmob event: " + Event_2.entrys.size() + " / " + count);
        } else if (chat.equals("thoatket") && !conn.p.isRobber() && !conn.p.isKnight() && !conn.p.isTrader()) {
            conn.p.veLang();
        } else if (chat.equals("vip") && conn.ac_admin > 3) {
            List<MainObject> objects = new ArrayList<>();
            objects.add(conn.p);
            Service.send_eff_auto(conn, objects, 137);
        } else if (chat.equals("vip2") && conn.ac_admin > 3) {
            List<MainObject> objects = new ArrayList<>();
            objects.add(conn.p);
            Service.send_eff_auto(conn, objects, 138);
        } else {
            SendChat(map, conn.p, chat, false);
        }
    }

    public static void SendChat(Map map, Player p, String chat, boolean include) throws IOException {
        Message m = new Message(27);
        m.writer().writeShort(p.ID);
        m.writer().writeByte(0);
        m.writer().writeUTF(chat);
        MapService.send_msg_player_inside(map, p, m, include);
        m.cleanup();
    }

    public static void send_in4_other_char(Map map, Player p, Player p0) throws IOException {
        int dem = 0;
        for (int i = 0; i < 11; i++) {
            if (i != 0 && i != 1 && i != 6 && i != 7 && i != 10) {
                continue;
            }
            if (p0.item.wear[i] != null) {
                dem++;
            }
        }
        Message m = new Message(5);
        m.writer().writeShort(p0.ID);
        m.writer().writeUTF(p0.name);
        m.writer().writeShort(p0.x);
        m.writer().writeShort(p0.y);
        m.writer().writeByte(p0.clazz);
        m.writer().writeByte(-1);
        m.writer().writeByte(p0.head);
        m.writer().writeByte(p0.eye);
        m.writer().writeByte(p0.hair);
        m.writer().writeShort(p0.level);
        m.writer().writeInt(p0.hp);
        m.writer().writeInt(p0.body.get_HpMax());
        m.writer().writeByte(p0.typepk);
        m.writer().writeShort(p0.hieuchien);
        m.writer().writeByte(dem);
        //
        for (int i = 0; i < p0.item.wear.length; i++) {
            if (i != 0 && i != 1 && i != 6 && i != 7 && i != 10) {
                continue;
            }
            Item3 temp = p0.item.wear[i];
            if (temp != null) {
                m.writer().writeByte(temp.type);

                if (i == 10 && p0.item.wear[14] != null && (p0.item.wear[14].id >= 4638 && p0.item.wear[14].id <= 4648)) {
                    m.writer().writeByte(p0.item.wear[14].part);
                } else {
                    m.writer().writeByte(temp.part);
                }
                m.writer().writeByte(3);
                m.writer().writeShort(-1);
                m.writer().writeShort(-1);
                m.writer().writeShort(-1);
                m.writer().writeShort(-1); // eff
            }
        }
        //
        if (p0.myclan != null) {
            m.writer().writeShort(p0.myclan.icon);
            m.writer().writeInt(Clan.get_id_clan(p0.myclan));
            m.writer().writeUTF(p0.myclan.name_clan_shorted);
            m.writer().writeByte(p0.myclan.get_mem_type(p0.name));
        } else {
            m.writer().writeShort(-1); // clan
        }
        Pet temp = p0.get_pet_follow();
        if (temp != null) {
            m.writer().writeByte(temp.type); // type
            m.writer().writeByte(temp.icon); // icon
            m.writer().writeByte(temp.nframe); // nframe
        } else {
            m.writer().writeByte(-1); // pet
        }
        m.writer().writeByte(p0.fashion.length);
        for (int i = 0; i < p0.fashion.length; i++) {
            if (p.conn.version >= 280) {
                m.writer().writeShort(p0.fashion[i]);
            } else {
                m.writer().writeByte(p0.fashion[i]);
            }
        }
        //
        m.writer().writeShort(p0.id_henshin);
        m.writer().writeByte(p0.type_use_horse);
        m.writer().writeBoolean(false);
        m.writer().writeByte(1);
        if (map.isMapChiemThanh() && p.myclan != null && p0.myclan != null && !p.myclan.equals(p0.myclan)) {
            m.writer().writeByte(1);
        } else {
            m.writer().writeByte(0);
        }
        m.writer().writeShort(Service.get_id_mat_na(p0)); // mat na
        m.writer().writeByte(1); // paint mat na trc sau
        m.writer().writeShort(Service.get_id_phiphong(p0)); // phi phong
        m.writer().writeShort(Service.get_id_weapon(p0)); // weapon
        m.writer().writeShort(p0.id_horse);
        m.writer().writeShort(Service.get_id_hair(p0)); // hair
        m.writer().writeShort(Service.get_id_wing(p0)); // wing
        m.writer().writeShort(Service.get_id_danhhieu(p0)); // phi phong
        m.writer().writeShort(-1); // body
        m.writer().writeShort(-1); // leg
        m.writer().writeShort(-1); // bienhinh
        p.conn.addmsg(m);
        m.cleanup();
        if (p.ID != p0.ID && !p0.my_store_name.isEmpty()) {
            m = new Message(-102);
            m.writer().writeByte(1);
            m.writer().writeShort(p0.ID);
            m.writer().writeUTF(p0.my_store_name);
            p.conn.addmsg(m);
            m.cleanup();
        }
    }

    public static void request_livefromdie(Map map, Session conn, Message m) throws IOException {
        if (map.isMapLoiDai()) {
            return;
        }
        byte type = m.reader().readByte();
        if (map.isMapChiemThanh()) {
            ChiemThanhManager.request_livefromdie(map, conn, type);
        } else {
            if (type == 1) { // hsl
                Service.send_box_input_yesno(conn, 9, "Hồi sinh tại chỗ mất 5 ngọc, bạn có muốn thực hiện?");
            } else if (type == 0) { // ve lang
                conn.p.isdie = false;
                conn.p.hp = conn.p.body.get_HpMax();
                conn.p.mp = conn.p.body.get_MpMax();
                Vgo vgo = new Vgo();
                if (Map.is_map_chien_truong(map.map_id)) {
                    switch (conn.p.typepk) {
                        case 1: {
                            vgo.id_map_go = 59;
                            vgo.x_new = 240;
                            vgo.y_new = 224;
                            break;
                        }
                        case 2: {
                            vgo.id_map_go = 55;
                            vgo.x_new = 224;
                            vgo.y_new = 256;
                            break;
                        }
                        case 4: {
                            vgo.id_map_go = 57;
                            vgo.x_new = 264;
                            vgo.y_new = 272;
                            break;
                        }
                        case 5: {
                            vgo.id_map_go = 53;
                            vgo.x_new = 276;
                            vgo.y_new = 246;
                            break;
                        }
                    }
                } else {
                    vgo.id_map_go = 1;
                    vgo.x_new = (short) 528;
                    vgo.y_new = (short) 480;
                }
                conn.p.change_map(conn.p, vgo);
                Service.usepotion(conn.p, 0, conn.p.body.get_HpMax());
                Service.usepotion(conn.p, 1, conn.p.body.get_MpMax());
            }
        }
    }

    public static Player get_player_by_id(Map map, int n2) {
        for (Player p0 : map.players) {
            if (p0.ID == n2) {
                return p0;
            }
        }
        return null;
    }

    public static Player get_player_by_id(int n2) {
        for (Map[] maps : Map.entrys) {
            for (Map map : maps) {
                for (Player p0 : map.players) {
                    if (p0.ID == n2) {
                        return p0;
                    }
                }
            }
        }
        return null;
    }

    public static void Player_Die(Map map, MainObject p, MainObject Obj, boolean include) throws IOException {
        Message m = new Message(41);
        m.writer().writeShort(p.ID);
        m.writer().writeShort(Obj.ID);
        m.writer().writeShort(Obj.typepk); // point pk
        m.writer().writeByte(Obj.get_TypeObj()); // type main object
        if (!include) {
            ((Player) p).conn.addmsg(m);
        } else {
            MapService.send_msg_player_inside(map, Obj, m, true);
        }
        m.cleanup();
    }

    public static void MainObj_Die(Map map, Session conn, MainObject mob, boolean include) throws IOException {
        Message m2 = null;
        if (mob.get_TypeObj() == 0 || mob.isMobDiBuon()) {
            m2 = new Message(8);
            m2.writer().writeShort(mob.ID);
        } else {
            m2 = new Message(17);
            m2.writer().writeShort(conn != null ? conn.p.ID : mob.ID);
            m2.writer().writeShort(mob.ID);
        }
        if (!include && conn != null) {
            conn.addmsg(m2);
        } else {
            MapService.send_msg_player_inside(map, mob, m2, true);
        }
        m2.cleanup();
    }

    public static void MainObj_Fire_Player(Map map, Player pTaget, MainObject mainAttack, int indexskill, int dame, List<Eff_TextFire> ListFire) throws IOException {
        Message m = new Message(6);
        m.writer().writeShort(mainAttack.ID);
        m.writer().writeByte(indexskill);
        m.writer().writeByte(1);
        m.writer().writeShort(pTaget.ID);
        m.writer().writeInt(dame); // dame
        m.writer().writeInt(pTaget.hp); // hp after
        m.writer().writeByte(ListFire.size());
        for (int i = 0; i < ListFire.size(); i++) {
            Eff_TextFire ef = ListFire.get(i);
            if (ef == null) {
                continue;
            }
            m.writer().writeByte(ef.type); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
            m.writer().writeInt(ef.dame); // par
        }
        m.writer().writeInt(mainAttack.hp);
        m.writer().writeInt(mainAttack.mp);
        m.writer().writeByte(11);
        m.writer().writeInt(0);
        MapService.send_msg_player_inside(map, pTaget, m, true);
        m.cleanup();
    }

    public static void Fire_Mob(Map map, Session conn, int indexskill, int idPTaget,
            int dame, int hpPtaget, List<Eff_TextFire> ListFire, int mobid, Eff_TextFire dame2) throws IOException {
        Message m = new Message(9);
        m.writer().writeShort(conn.p.ID);
        m.writer().writeByte(indexskill);
        m.writer().writeByte(1);
        m.writer().writeShort(idPTaget);
        m.writer().writeInt(dame); // dame
        m.writer().writeInt(hpPtaget); // hp mob after
        if (ListFire == null || ListFire.isEmpty()) {
            m.writer().writeByte(1);
            m.writer().writeByte(0); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
            m.writer().writeInt(dame);
        } else {
            m.writer().writeByte(ListFire.size());
            for (Eff_TextFire ef : ListFire) {
                if (ef == null) {
                    m.writer().writeByte(0); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
                    m.writer().writeInt(dame);
                } else {
                    m.writer().writeByte(ef.type); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
                    m.writer().writeInt(ef.dame); // par
                }
            }
        }
        m.writer().writeInt(conn.p.hp);
        m.writer().writeInt(conn.p.mp);
        m.writer().writeByte(dame2.type); //10 : st ánh sáng, 11: st bóng tối, 12: st chuẩn
        m.writer().writeInt(dame2.dame);
        MapService.send_msg_player_inside(map, conn.p, m, true);
        m.cleanup();
    }

    public static void Fire_Player(Map map, Session conn, int indexskill, int idPTaget, int dame, int hpPtaget, List<Eff_TextFire> ListFire, Eff_TextFire dame2) throws IOException {
        if (Map.is_map_chien_truong(map.map_id)) {
            conn.p.update_point_arena(1);
        }
        Message m = new Message(6);
        m.writer().writeShort(conn.p.ID);
        m.writer().writeByte(indexskill);
        m.writer().writeByte(1);
        m.writer().writeShort(idPTaget);
        m.writer().writeInt(dame); // dame
        m.writer().writeInt(hpPtaget); // hp after
        m.writer().writeByte(ListFire.size());
        for (int i = 0; i < ListFire.size(); i++) {
            Eff_TextFire ef = ListFire.get(i);
            if (ef == null) {
                continue;
            }
            m.writer().writeByte(ef.type); // 1: xuyen giap, 2:hut hp, 3: hut mp, 4: chi mang, 5: phan don
            m.writer().writeInt(ef.dame); // par
        }
        m.writer().writeInt(conn.p.hp);
        m.writer().writeInt(conn.p.mp);
        m.writer().writeByte(dame2.type);
        m.writer().writeInt(dame2.dame);
        MapService.send_msg_player_inside(map, conn.p, m, true);
        m.cleanup();
    }

    public static void use_skill(Map map, Session conn, Message m, int type_atk) throws IOException {
        try {
            long time_ = System.currentTimeMillis();
            byte index_skill = m.reader().readByte();
            int n = m.reader().readByte();
            int ObjAtk = 0;
            short n3 = 0;

            int sk_point1 = conn.p.skill_point[index_skill];
            if (sk_point1 < 1) {
                return;
            }
            if (conn.p.item.wear[0] == null) {
                Service.send_notice_nobox_white(conn, "Chưa có vũ khí");
                return;
            }
            LvSkill _skill = conn.p.skills[index_skill].mLvSkill[sk_point1 - 1];
            while (sk_point1 > 1 && _skill.LvRe > conn.p.level) {
                sk_point1--;
                _skill = conn.p.skills[index_skill].mLvSkill[sk_point1 - 1];
            }
            if (_skill.LvRe > conn.p.level) {
                Service.send_notice_nobox_white(conn, "Level không đủ " + _skill.LvRe);
                return;
            }
            int sk_pointPlus = conn.p.get_skill_point_plus(index_skill);
            if (sk_point1 + sk_pointPlus <= 15) {
                _skill = conn.p.skills[index_skill].mLvSkill[(sk_point1 + sk_pointPlus) - 1];
            } else {
                _skill = conn.p.skills[index_skill].mLvSkill[14];
            }
            if (conn.p.mp - _skill.mpLost < 0) {
                Service.send_notice_nobox_white(conn, "Không đủ mp");
                return;
            }
            if (conn.p.isStunes(true)) {
                return;
            }
            if (conn.p.time_delay_skill[index_skill] > time_) {
                if (++conn.p.enough_time_disconnect > 5) {
                    conn.close();
                }
                return;
            }
            // bắt đầu tính dame
            conn.p.mp -= _skill.mpLost;
            n = (_skill.nTarget < n) ? _skill.nTarget : n;
            conn.p.time_delay_skill[index_skill] = (long) (time_ + _skill.delay * 0.97);
            conn.p.enough_time_disconnect = 0;
            byte type = 0;
            if (index_skill == 2 || index_skill == 4 || index_skill == 6 || index_skill == 8 || index_skill == 19 || index_skill == 20) {
                type = index_skill == 20 && (conn.p.clazz == 2 || conn.p.clazz == 1)
                        || index_skill == 19 && (conn.p.clazz == 0 || conn.p.clazz == 3) ? (byte) 0 : 1;
            }
            if (index_skill == 0) {
                type = 2;
            }
            List<Integer> ListATK = new ArrayList<>();
            if (type_atk == 0) {
                for (int i = 0; i < n; ++i) {
                    ObjAtk = Short.toUnsignedInt(m.reader().readShort());
                    Mob_in_map mob_target = MapService.get_mob_by_index(map, ObjAtk);
                    if (mob_target == null) {
                        mob_target = map.GetBoss(ObjAtk);
                    }
                    if (map.zone_id == 5 && !map.isMapChiemThanh() && !map.isMapLoiDai()) {
                        Pet_di_buon pet_di_buon = Pet_di_buon_manager.check(ObjAtk);
                        if (pet_di_buon != null) {
                            if (!pet_di_buon.equals(conn.p.pet_di_buon)) {
                                MainObject.MainAttack(map, conn.p, pet_di_buon, index_skill, _skill, type);
                            }
                        }
                    } else if (mob_target != null) {
                        MainObject.MainAttack(map, conn.p, mob_target, index_skill, _skill, type);
                    } else if (ObjAtk > 10000 && ObjAtk < 11000) {//mob boss
                        Message m2 = new Message(17);
                        m2.writer().writeShort(-1);
                        m2.writer().writeShort(ObjAtk);
                        conn.addmsg(m2);
                        m2.cleanup();
                    } else if (conn.p.map.map_id == 48) {
                        Dungeon d = DungeonManager.get_list(conn.p.name);
                        if (d != null) {
                            MobDungeon mod_target_dungeon = d.get_mob(ObjAtk);
                            if (mod_target_dungeon != null) {
                                MainObject.MainAttack(map, conn.p, mod_target_dungeon, index_skill, _skill, type);
                            }
                        }
                    } else if (Map.is_map_chiem_mo(conn.p.map, true) && conn.p.myclan != null) {
                        Mob_MoTaiNguyen temp_mob = conn.p.myclan.get_mo_tai_nguyen(conn.p.map.map_id);
                        if (temp_mob == null) {
                            temp_mob = Manager.gI().chiem_mo.get_mob_in_map(map);
                            MainObject.MainAttack(map, conn.p, temp_mob, index_skill, _skill, type);
                        } else {
                            MenuController.send_menu_select(conn, -129, new String[]{"Triệu hồi (" + (temp_mob.nhanBans.size() + 1) + ")", "Nâng cấp"});
                        }
                    }
                    if (mob_target != null && mob_target.isBoss()) {
                        ListATK.add(mob_target.ID);
                    }
                }
            } else if (type_atk == 1) {
                for (int i = 0; i < n; ++i) {
                    n3 = m.reader().readShort();
                    int n2 = n3;
                    Player p_target = null;
                    if ((p_target = MapService.get_player_by_id(map, n2)) != null && !p_target.isSquire) {
                        // đánh người chơi
                        MainObject.MainAttack(map, conn.p, p_target, index_skill, _skill, type);
                        ListATK.add(p_target.ID);
                    } else if (Map.is_map_chiem_mo(conn.p.map, true) && conn.p.myclan != null) {
                        // đánh nhân bản
                        Mob_MoTaiNguyen temp_mob = conn.p.myclan.get_mo_tai_nguyen(conn.p.map.map_id);
                        if (temp_mob == null) {
                            temp_mob = Manager.gI().chiem_mo.get_mob_in_map(conn.p.map);
                            if (temp_mob.nhanBans != null) {
                                NhanBan nhanBan = temp_mob.getNhanban(n2);
                                if (nhanBan != null) {
                                    ListATK.add(temp_mob.nhanban.ID);
                                    MainObject.MainAttack(map, conn.p, nhanBan, index_skill, _skill, type);
                                }
                            }
                        }
                    } else {
                        if (n3 >= -1000 && n3 < 0) {
                            if (map.isMapChienTruong()) {
                                for (Player_Nhan_Ban ai : ChienTruong.gI().list_ai) {
                                    if (!ai.isdie && ai.map.equals(conn.p.map) && ai.id == n3) {
                                        Player_Nhan_Ban.player_attack(conn, map, ai, index_skill);
                                        ai.target = conn.p.ID;
                                    }
                                }
                            } else {
                                for (MobAi ai : map.Ai_entrys) {
                                    if (ai != null && ai.ID == n3) {
                                        try {
                                            MainObject.MainAttack(map, conn.p, ai, index_skill, _skill, type);
                                            ListATK.add(ai.ID);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            Player_Nhan_Ban.atk(map, conn.p, n2, index_skill, (int) 3000);
                        }
                    }
                }
            }
            if (!ListATK.isEmpty()) {
                int prKham = 0;
                if ((prKham = conn.p.total_item_param(100)) > 0) {
                    if (ListATK.contains(conn.p.kham.idAtk_HN)) {
                        conn.p.kham.CountAtk_HN++;
                    } else {
                        conn.p.kham.idAtk_HN = ListATK.get(Util.random(ListATK.size()));
                        conn.p.kham.CountAtk_HN = 1;
                    }
                    if (prKham > 0 && conn.p.kham.CountAtk_HN >= prKham) {
                        conn.p.kham.idAtk_HN = 0;
                        conn.p.kham.CountAtk_HN = 0;
                        conn.p.add_EffMe_Kham(StrucEff.NgocHonNguyen, 0, System.currentTimeMillis() + 3000);
                        Eff_special_skill.send_eff_kham(conn.p, StrucEff.NgocHonNguyen, 3000);
                    }
                }
            }
            // Đệ tử
            if (conn.p.squire != null && conn.p.isLiveSquire) {
                conn.p.squire.use_skill(map, conn, n, ObjAtk, n3, type_atk);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void Fire_Mob_DiBuon(Map map, Session conn, Pet_di_buon pet_di_buon, int index_skill, int dameBase) throws IOException {
        if (dameBase < 0) {
            dameBase = 0;
        }
        if (pet_di_buon != null) {
            pet_di_buon.hp -= dameBase;
            if (pet_di_buon.hp <= 0) {
                pet_di_buon.hp = 0;
                Message mout = new Message(8);
                mout.writer().writeShort(pet_di_buon.ID);
                for (int i1 = 0; i1 < map.players.size(); i1++) {
                    Player p0 = map.players.get(i1);
                    if (p0 != null) {
                        p0.conn.addmsg(mout);
                    }
                }
                mout.cleanup();
                Pet_di_buon_manager.remove(pet_di_buon.name);
                pet_di_buon.p.pet_di_buon = null;
                for (int j = 0; j < pet_di_buon.item.size(); j++) {
                    ItemMap it_leave = new ItemMap();
                    it_leave.id_item = (short) pet_di_buon.item.get(j);
                    it_leave.color = (byte) 0;
                    it_leave.quantity = 1;
                    it_leave.category = 3;
                    it_leave.idmaster = (short) pet_di_buon.p.ID;
                    it_leave.op = new ArrayList<>();
                    it_leave.time_exist = System.currentTimeMillis() + 60_000L;
                    it_leave.time_pick = System.currentTimeMillis() + 1_500L;
                    map.add_item_map_leave(map, conn.p, it_leave, pet_di_buon.ID);
                }
            }
            Message m_atk = new Message(9);
            m_atk.writer().writeShort(conn.p.ID);
            m_atk.writer().writeByte(index_skill);
            m_atk.writer().writeByte(1);
            m_atk.writer().writeShort(pet_di_buon.ID);
            m_atk.writer().writeInt((int) dameBase); // dame
            m_atk.writer().writeInt(pet_di_buon.hp); // hp mob after
            m_atk.writer().writeByte(0);
            m_atk.writer().writeInt(conn.p.hp);
            m_atk.writer().writeInt(conn.p.mp);
            m_atk.writer().writeByte(11); // 1: green, 5: small white 9: big white, 10: st dien, 11: st bang
            m_atk.writer().writeInt(0); // dame plus
            MapService.send_msg_player_inside(map, conn.p, m_atk, true);
            m_atk.cleanup();
            pet_di_buon.update_all(conn.p);
        }
    }

    public static void use_item_arena(Map map, Player p, short id) throws IOException {
        if (Map.is_map_chien_truong(map.map_id)) {
            if (p.time_use_item_arena[id - 57] < System.currentTimeMillis()) {
                if (map.time_use_item_arena[id - 57] < System.currentTimeMillis()) {
                    boolean ch = false;
                    switch (map.map_id) {
                        case 54: {
                            if (p.typepk == 5) {
                                ch = true;
                            }
                            break;
                        }
                        case 56: {
                            if (p.typepk == 2) {
                                ch = true;
                            }
                            break;
                        }
                        case 58: {
                            if (p.typepk == 4) {
                                ch = true;
                            }
                            break;
                        }
                        case 60: {
                            if (p.typepk == 1) {
                                ch = true;
                            }
                            break;
                        }
                    }
                    if (ch) {
                        switch (id) {
                            case 57: {
                                map.Arena.timeCnNha = System.currentTimeMillis() + 180_000L;
                                break;
                            }
                            case 58: {
                                map.Arena.timeBienHinh = System.currentTimeMillis() + 180_000L;
                                for (int i2 = 0; i2 < map.players.size(); i2++) {
                                    Player p0 = map.players.get(i2);
                                    if (p0.typepk != p.typepk) {
                                        p0.id_henshin = 102;
                                        for (int i = 0; i < map.players.size(); i++) {
                                            Player p01 = map.players.get(i);
                                            MapService.send_in4_other_char(map, p01, p0);
                                        }
                                    }
                                }
                                break;
                            }
                            case 59: {
                                map.Arena.timeCnLinh = System.currentTimeMillis() + 180_000L;
                                break;
                            }
                        }
                        p.item.remove(4, id, 1);
                        p.update_point_arena(10);
                        map.time_use_item_arena[id - 57] = System.currentTimeMillis() + 240_000L;
                        p.time_use_item_arena[id - 57] = System.currentTimeMillis() + 240_000L;
                    }
                } else {
                    Service.send_notice_box(p.conn,
                            "Sử dụng sau " + (map.time_use_item_arena[id - 57] - System.currentTimeMillis()) / 1000 + " s");
                }
            } else {
                Service.send_notice_box(p.conn,
                        "Sử dụng sau " + (p.time_use_item_arena[id - 57] - System.currentTimeMillis()) / 1000 + " s");
            }
        }
    }
}
