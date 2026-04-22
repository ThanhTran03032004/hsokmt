
package Game.activities;

import Game.core.Util;
import Game.client.Player;
import Game.core.Manager;
import Game.core.SQL;
import Game.core.Service;
import Game.io.Message;
import Game.io.Session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Game.map.*;

import static Game.map.LeaveItemMap.leave_vang;

import Game.template.ItemTemplate3;
import Game.template.MainObject;
import Game.template.Mob;
import Game.template.BoxItem;


public class ChiemThanhManager {

    public static final ConcurrentHashMap<String, List<String>> Clan_entrys = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Integer, Byte> player_HoiSinh = new ConcurrentHashMap<>();
    private static final List<String>[] subMaps = new ArrayList[4];
    private static int Vang;
    public static boolean isRegister;
    public static long timeAttack = 0;
    private static final Mob_in_map truChinh = new Mob_in_map();

    public static void request_livefromdie(Map map, Session conn, int type) throws IOException {
        if (type == 1) { // hsl
            Service.send_box_input_yesno(conn, 9, "Bạn muốn hồi sinh tại chỗ với " + Util.number_format(GetVangHoiSinh(conn.p)) + " vàng");
        } else if (type == 0) { // ve lang
            conn.p.isdie = false;
            conn.p.hp = conn.p.body.get_HpMax();
            conn.p.mp = conn.p.body.get_MpMax();
            if (!ChiemThanhManager.joinMap(conn.p)) {
                Vgo vgo = new Vgo();
                vgo.id_map_go = 1;
                vgo.x_new = (short) 528;
                vgo.y_new = (short) 480;
                conn.p.change_map(conn.p, vgo);
            }
            Service.usepotion(conn.p, 0, conn.p.body.get_HpMax());
            Service.usepotion(conn.p, 1, conn.p.body.get_MpMax());
        }
    }

    public static void ActionHoiSinh(Map map, Player p) throws IOException {
        if (!map.isMapChiemThanh()) return;
        int vang = 500;
        if (player_HoiSinh.containsKey(p.ID)) {
            vang = GetVangHoiSinh(p);
        } else {
            player_HoiSinh.put(p.ID, (byte) 1);
        }
        if (p.get_vang() >= vang) {
            p.isdie = false;
            p.hp = p.body.get_HpMax();
            p.mp = p.body.get_MpMax();
            p.update_vang(-vang, "Trừ %s vàng hồi sinh trong chiếm thành");
            UpdateVang(vang);
            if (player_HoiSinh.containsKey(p.ID)) {
                int count = player_HoiSinh.get(p.ID);
                player_HoiSinh.replace(p.ID, (byte) (count + 1));
            } else {
                player_HoiSinh.put(p.ID, (byte) 1);
            }
            Service.send_char_main_in4(p);
            for (Player p2 : map.players) {
                MapService.send_in4_other_char(map, p2, p);
            }
            // chest in4
            Service.send_combo(p.conn);
            Service.usepotion(p, 0, p.body.get_HpMax());
            Service.usepotion(p, 1, p.body.get_MpMax());
        } else {
            Service.send_notice_box(p.conn, "Không đủ " + Util.number_format(vang) + " vàng để thực hiện");
        }
    }

    public static int GetVangHoiSinh(Player p) {
        if (player_HoiSinh.containsKey(p.ID)) {
            int counths = player_HoiSinh.get(p.ID);
            if (counths < 50)
                return counths * 500;
            else
                return 50 * 500;
        } else return 500;
    }

    public static int GetVang() {
        return Vang;
    }

    public static synchronized void UpdateVang(int vangjoin) {
        if (Vang + vangjoin > 2_000_000_000) {
            Vang = 2_000_000_000;
        } else {
            Vang += vangjoin;
        }
    }

    public static void init() {
        try {
            for (short i = 83; i <= 86; i++) {
                String name = "Cửa đông";
                if (i == 84) {
                    name = "Cửa tây";
                } else if (i == 85) {
                    name = "Cửa nam";
                } else if (i == 86) {
                    name = "Cửa bắc";
                }
                List<Vgo> vgos = new ArrayList<>();
                vgos.add(getVgo(i));
                Map m = new Map(i, 0, name, (byte) 0, false, false, 60, 1, vgos, 0);
                Mob_in_map mob = new Mob_in_map();
                mob.template = Mob.entry.get(153);
                mob.ID = 1;
                mob.ishs = false;
                mob.isATK = false;

                mob.hp = mob.Set_hpMax(1_000_000);
                mob.x = 324;
                mob.y = 216;
                mob.map_id = (byte) i;
                mob.zone_id = 0;
                mob.level = 1;
                mob.time_refresh = 1000000;

                Mob_in_map mob1 = new Mob_in_map();
                mob1.template = Mob.entry.get(154);
                mob1.ID = 2;
                mob1.isATK = true;
                mob1.hp = mob1.Set_hpMax(5_000_000);
                mob1.x = 187;
                mob1.y = 164;
                mob1.map_id = (byte) i;
                mob1.zone_id = 0;
                mob1.level = 1;
                mob1.Set_Dame(10_000);
                Mob_in_map mob2 = new Mob_in_map();
                mob2.template = Mob.entry.get(154);
                mob2.ID = 3;
                mob2.isATK = true;
                mob2.hp = mob2.Set_hpMax(1_000_000);
                mob2.x = 451;
                mob2.y = 164;
                mob2.map_id = (byte) i;
                mob2.zone_id = 0;
                mob2.level = 1;
                mob2.Set_Dame(10_000);

                m.mobs = new Mob_in_map[]{mob, mob1, mob2};
                m.start_map();
                Map.entrys.add(new Map[]{m});
            }
            Map m = new Map((short) 87, 0, "Đấu trường", (byte) 0, false, false, 200, 1, new ArrayList<Vgo>(), 0);
            m.mobs = new Mob_in_map[5];
            for (int i = 0; i < 5; i++) {
                m.mobs[i] = new Mob_in_map();
                if (i == 4) {
                    m.mobs[i] = truChinh;
                    m.mobs[i].Set_Dame(100_000);
                } else {
                    m.mobs[i].Set_Dame(50_000);
                }
                m.mobs[i].template = Mob.entry.get(i < 4 ? 151 : 152);
                m.mobs[i].ID = i + 10;
                m.mobs[i].ishs = false;
                m.mobs[i].isATK = true;
                m.mobs[i].hp = m.mobs[i].Set_hpMax(i < 4 ? 200_000_000 : 700_000_000);
//                if (i < 4) {
//                    m.mobs[i].time_refresh = 1000000;
//                }
                m.mobs[i].time_refresh = 1000000;
                if (i == 0) {
                    m.mobs[i].x = 234;
                    m.mobs[i].y = 864;
                } else if (i == 1) {
                    m.mobs[i].x = 870;
                    m.mobs[i].y = 900;
                } else if (i == 2) {
                    m.mobs[i].x = 882;
                    m.mobs[i].y = 312;
                } else if (i == 3) {
                    m.mobs[i].x = 240;
                    m.mobs[i].y = 336;
                } else {
                    m.mobs[i].x = 613;
                    m.mobs[i].y = 630;
                }

                m.mobs[i].map_id = (byte) 87;
                m.mobs[i].zone_id = 0;
                m.mobs[i].level = 1;
            }
            m.start_map();
            Map.entrys.add(new Map[]{m});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ResetMap() {
        NamePlayerOwner = null;
        idIconClan = -1;
        NameClan = null;
        timeDa = 0;
        truChinh.isdie = false;
        truChinh.hp = truChinh.get_HpMax();
        for (Map[] map : Map.entrys) {
            if (map == null || map.length == 0 || !map[0].isMapChiemThanh()) continue;
            for (Map m : map) {
                for (Mob_in_map mob : m.mobs) {
                    if (mob == null) {
                        continue;
                    }
                    mob.isdie = false;
                    mob.hp = mob.get_HpMax();
                }
            }
        }
    }

    private static Vgo getVgo(int mapid) {
        Vgo v = new Vgo();
        v.id_map_go = 87;
        v.x_old = 324;
        v.y_old = 216;
        if (mapid == 83) {
            v.x_new = 610;
            v.y_new = 110;
        } else if (mapid == 84) {
            v.x_new = 1120;
            v.y_new = 576;
        } else if (mapid == 85) {
            v.x_new = 610;
            v.y_new = 1120;
        } else {
            v.x_new = 80;
            v.y_new = 576;
        }
        return v;
    }

    public static void StartRegister() {
        if (isRegister) {
            return;
        }
        ResetMap();
        Manager.ResetCThanh();
        isRegister = true;
        try {
            Manager.gI().chatKTGprocess("Đã đến thời gian đăng kí chiếm thành");
        } catch (Exception e) {
        }
    }

    public static void EndRegister() {
        if (!isRegister) {
            return;
        }
        isRegister = false;
        timeAttack = System.currentTimeMillis() + 1000 * 60 * 90;
        SetupMap();
        try {
            Manager.gI().chatKTGprocess("Đã đến thời gian chiếm thành, hãy cùng bang hội chiến đấu hết mình để thống trị thiên hạ");
        } catch (Exception e) {
        }
    }

    public static void SetupMap() {
        synchronized (subMaps) {
            int k = 0;
            for (java.util.Map.Entry<String, List<String>> entry : Clan_entrys.entrySet()) {
                if (subMaps[k] == null) {
                    subMaps[k] = new ArrayList<>();
                }
                String key = entry.getKey();
                subMaps[k].add(key);
                k = (k + 1) % 4;
            }
            for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                Session s = Session.client_entry.get(i);
                if (s.connected && s.get_in4 && s.p != null && s.p.map != null) {
                    try {
                        joinMap(s.p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void CloseMap() {
        for (Map[] map : Map.entrys) {
            if (map == null || map.length == 0 || !map[0].isMapChiemThanh()) continue;
            for (Map m : map) {
                for (int i = m.players.size() - 1; i >= 0; i--) {
                    Player p = m.players.get(i);
                    if (p == null) continue;
                    try {
                        Vgo v = new Vgo();
                        v.id_map_go = 1;
                        v.x_new = 350;
                        v.y_new = 350;
                        p.change_map(p, v);
                        Service.send_time_box(p, (byte) 0, null, null);
                    } catch (Exception e) {
                        try {
                            p.conn.close();
                        } catch (Exception ee) {
                        }
                    }
                }
            }
        }
    }

    public static boolean joinMap(Player p) throws IOException {
        if (subMaps == null || p == null || p.myclan == null || !Clan_entrys.containsKey(p.myclan.name_clan) || !Clan_entrys.get(p.myclan.name_clan).contains(p.name) || timeAttack < System.currentTimeMillis()) {
            return false;
        }
        for (int i = 0; i < subMaps.length; i++) {
            if (!subMaps[i].contains(p.myclan.name_clan)) {
                continue;
            }
            Vgo v = new Vgo();
            v.id_map_go = (byte) (i + 83);
            v.x_new = 300;
            v.y_new = 300;
            p.change_map(p, v);
            return true;
        }
        return false;
    }

    public static void ClanRegister(Player p) throws IOException {
        if (!isRegister) {
            if (timeAttack > System.currentTimeMillis()) {
                joinMap(p);
            } else {
                Service.send_notice_box(p.conn, "Bạn chỉ có thể đăng ký tham gia chiếm thành lúc 20h45-21h30 vào các ngày 2,4,6 trong tuần");
            }
        } else if (p.myclan == null) {
            Service.send_notice_box(p.conn, "Chức năng chỉ dành cho clan.");
        } else if (!p.myclan.mems.get(0).name.equals(p.name)) {
            Service.send_notice_box(p.conn, "Bạn không phải thủ lĩnh.");
        } else if (Clan_entrys.containsKey(p.myclan.name_clan)) {
            Service.send_notice_box(p.conn, "Clan của bạn đã có tên trong danh sách.");
        }
        else if (p.party == null || p.party.get_mems().size() < 1) {
            Service.send_notice_box(p.conn, "Cần tạo nhóm 5 thành viên trong clan có level từ 60 để tham gia.");
        } else if (p.myclan.get_vang() < 0) {
            Service.send_notice_box(p.conn, "Cần tối thiểu 10tr quỹ bang để đăng kí.");
        }
        else {
            List<String> nameP = new ArrayList<>();
            for (int i = 0; i < p.party.get_mems().size(); i++) {
                Player p2 = p.party.get_mems().get(i);
                if (p2 == null || p2.conn == null || !p2.conn.connected) {
                    Service.send_notice_box(p.conn, "Có lỗi xảy ra hãy tạo lại nhóm và thử lại.");
                    return;
                }
                if (p2.myclan == null || !p2.myclan.name_clan.equals(p.myclan.name_clan) || p2.level < 60) {
                    Service.send_notice_box(p.conn, "Cần tạo nhóm 5 thành viên trong clan có level từ 60 để tham gia.");
                    return;
                }
                nameP.add(p2.name);
            }
            p.myclan.update_vang(-10_000_000);
            UpdateVang(10_000_000);
            Clan_entrys.put(p.myclan.name_clan, nameP);
            Service.send_notice_box(p.conn, "Đăng ký thành công.");
        }
    }

    public static boolean isChangeMap(Map map) {
        if (map.map_id < 83 || map.map_id > 86) {
            return true;
        }
        for (Mob_in_map mob : map.mobs) {
            if (mob == null) {
                continue;
            }
            if (mob.template.mob_id == 153 && !mob.isdie) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDameTruChinh(Map map) {
        for (Mob_in_map mob : map.mobs) {
            if (mob == null) {
                continue;
            }
            if (mob.template.mob_id != 152 && !mob.isdie) {
                return false;
            }
        }
        return true;
    }

    private static String NamePlayerOwner;
    private static int idIconClan;
    private static String NameClan;
    private static long timeDa;

    public static synchronized void SetOwner(Player p) {
        if (timeAttack < System.currentTimeMillis() || p.map.map_id != 87) {
            return;
        }
        List<String> l = new ArrayList<>();
        for (Player p0 : p.map.players) {
            if (p.myclan.equals(p0.myclan) && !p0.isdie) {
                l.add(p0.name);
            }
        }
        timeDa = System.currentTimeMillis() + 1000 * 60 * 10;
        NamePlayerOwner = l.get(Util.random(l.size()));
        idIconClan = p.myclan.icon;
        NameClan = p.myclan.name_clan;

        System.out.println("Người giữ đá: " + NamePlayerOwner);
        for (Player p0 : p.map.players) {
            try {
                SenDataTime(p0.conn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void PlayerDie(Player p) {
        if (timeAttack < System.currentTimeMillis() || !p.name.equals(NamePlayerOwner) || p.map.map_id != 87) {
            return;
        }

        NamePlayerOwner = null;
        idIconClan = -1;
        NameClan = null;
        timeDa = 0;
        truChinh.isdie = false;
        truChinh.hp = truChinh.get_HpMax();
        for (Player p0 : p.map.players) {
            try {
                SenDataTime(p0.conn);
            } catch (IOException e) {
            }
        }
    }

    public static boolean isOwner(Player p) {
        return p.map.map_id == 87 && p.name != null && p.name.equals(NamePlayerOwner);
    }

    public static void SenDataTime(Session conn) throws IOException {
        long _time = System.currentTimeMillis();
        if (NamePlayerOwner == null || NamePlayerOwner.isEmpty()) {
            Message m = new Message(-104);
            m.writer().writeByte(0);
            m.writer().writeInt(-1);
            m.writer().writeUTF("");
            conn.addmsg(m);
            m.cleanup();
            m = new Message(-104);
            m.writer().writeByte(1);
            m.writer().writeByte(1);
            m.writer().writeShort((int) ((timeAttack - _time) / 1000));
            m.writer().writeUTF("Chiếm thành");
            conn.addmsg(m);
            m.cleanup();
        } else {
            Message m = new Message(-104);
            m.writer().writeByte(0);
            m.writer().writeInt(idIconClan);
            m.writer().writeUTF(NameClan);
            conn.addmsg(m);
            m.cleanup();
            m = new Message(-104);
            m.writer().writeByte(1);
            m.writer().writeByte(2);
            m.writer().writeShort((int) ((timeAttack - _time) / 1000));
            m.writer().writeUTF("Chiếm thành");
            m.writer().writeShort((int) ((timeDa - _time) / 1000));
            m.writer().writeUTF("Thời gian còn lại");
            conn.addmsg(m);
            m.cleanup();
        }
    }

    public static void EndChiemThanh() {
        System.out.println("event_daily.ChiemThanhManager.EndChiemThanh()");
        Manager.ResetCThanh();
        long time = System.currentTimeMillis();
        if (timeDa > 0 && timeDa < time && NameClan != null && !NameClan.isEmpty()) {
            Manager.nameClanThue = NameClan;
            Manager.setClanThue();
            List<String> ss = Clan_entrys.get(Manager.nameClanThue);
            if (ss != null) {
                Manager.PlayersWinCThanh.addAll(ss);
            }
        } else if (NameClan != null && !NameClan.isEmpty()) {
            Manager.nameClanThue = NameClan;
            Manager.setClanThue();
            List<String> ss = Clan_entrys.get(Manager.nameClanThue);
            if (ss != null) {
                Manager.PlayersWinCThanh.addAll(ss);
            }
        } else {
            Manager.nameClanThue = null;
        }
        if (Manager.ClanThue != null) {
            Manager.ClanThue.update_vang(GetVang());
            Vang = 0;
        }

        timeAttack = 0;
        Manager.thue = 5;
        CloseMap();
        try {
            Manager.gI().chatKTGprocess("Chiếm thành đã kết thúc " + (NameClan != null && !NameClan.isEmpty() ? " bang " + NameClan : " không bang nào") + " chiếm được thành");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            saveData();
        }
    }

    public static void update() {
        if (timeAttack == 0)
            return;
        long time = System.currentTimeMillis();
        if (timeDa > 0 && timeDa < time && NameClan != null && !NameClan.isEmpty()) {
            EndChiemThanh();
        } else if (timeAttack < time)
            EndChiemThanh();
    }

    public static void saveData() {
        if (Manager.ClanThue == null) return;
        try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement()) {
            st.execute("UPDATE `config_server` SET `chiem_thanh` = '" + Manager.ClanThue.ID + "' WHERE `id` = '" + 1 + "';");
            connection.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void Obj_Die(Map map, MainObject mainAtk, MainObject focus) throws IOException {
        if (!mainAtk.isPlayer() || !focus.isMob()) return;
        Player p = (Player) mainAtk;
        Mob_in_map mob = (Mob_in_map) focus;
        if (mob != null) {
            // roi do boss co dinh
            short[] id_item_leave3 = new short[]{};
            short[] id_item_leave4 = new short[]{};
            short[] id_item_leave7 = new short[]{};
            //short id_medal_material = -1;
            short sizeRandomMedal = 0;
            switch (mob.template.mob_id) {
                case 151: {
                    id_item_leave4 = new short[]{-1, -1, -1, -1, -1, -1, 54, 53, 18};
                    id_item_leave7 = new short[]{11, 13, 2, 3, 2, 3, 14};
                    if (Util.random(100) < 10)
                        id_item_leave3 = new short[]{(short) Util.random(4577, 4585)};
                    sizeRandomMedal = (short) (30);
                    break;
                }
                case 152: {
                    id_item_leave4 = new short[]{-1, -1, -1, -1, -1, -1, 54, 53, 18};
                    id_item_leave7 = new short[]{11, 13, 2, 3, 2, 3, 14};
                    if (Util.random(100) < 20)
                        id_item_leave3 = new short[]{(short) Util.random(4577, 4585)};
                    sizeRandomMedal = (short) (30);
                    break;
                }
            }
            for (short id : id_item_leave3) {
                ItemTemplate3 temp = ItemTemplate3.item.get(id);
                LeaveItemMap.leave_item_by_type3(map, id, 5, p, temp.getName(), mob.ID);
            }
            for (int i = 0; i < 3; i++) {
                for (short id : id_item_leave4) {
                    if (id == -1) {
                        leave_vang(map, mob, p);
                    } else {
                        LeaveItemMap.leave_item_by_type4(map, id, p, mob.ID, p.ID);
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                for (short id : id_item_leave7) {
                    LeaveItemMap.leave_item_by_type7(map, id, p, mob.ID, p.ID);
                }
            }
            for (int l = 0; l < sizeRandomMedal; l++) {
                LeaveItemMap.leave_item_by_type7(map, (short) Util.random(136, 146), p, mob.ID, p.ID);
            }
        }
    }
    
    public static boolean isOwnerClanBuffActive(Player p) {
    return p != null
        && p.map != null
        && p.map.map_id == 87
        && NameClan != null && !NameClan.isEmpty()
        && p.myclan != null
        && NameClan.equals(p.myclan.name_clan);
}


    public static void NhanQua(Player p) throws IOException {
        if (!Manager.PlayersWinCThanh.contains(p.name)) {
            Service.send_notice_box(p.conn, "Bạn không phải người tham gia chiếm thành, hoặc đã nhận quà rồi.");
            return;
        }
        List<BoxItem> ids = new ArrayList<>();
        String text = "Phần quà của bạn: ";

        if (Manager.PlayersWinCThanh.contains(p.name)) {
            int size = Util.random(5, 15);
            for (int i = 0; i < size; i++) {
                short id = (short) Util.random(126, 146);
                ids.add(new BoxItem(id, (short) 1, (byte) 7));
            }
            size = Util.random(5);
            for (int i = 0; i < size; i++) {
                ids.add(new BoxItem((short) Util.random(205, 208), (short) Util.random(5), (byte) 4));
            }
            size = Util.random(5);
            for (int i = 0; i < size; i++) {
                ids.add(new BoxItem((short) Util.random(8, 11), (short) Util.random(5, 20), (byte) 7));
            }
        }
        if (ids.isEmpty()) {
            Service.send_notice_box(p.conn, "Bạn không phải người tham gia chiếm thành, hoặc đã nhận quà rồi.");
        } else if (p.item.get_inventory_able() < ids.size()) {
            Service.send_notice_box(p.conn, "Không đủ " + ids.size() + " ô trống trong hành trang.");
        } else {
            p.point_activity += 1000;
            Manager.PlayersWinCThanh.remove(p.name);
            for (BoxItem it : ids) {
                if (it.catagory == 4 && it.id == -2) {
                    p.update_ngoc(it.quantity);
                } else if (it.catagory == 4 && it.id == -1) {
                    p.update_vang(it.quantity, "Nhận %s vàng từ quà top chiếm thành");
                } else {
                    p.item.add_item_inventory47(it.id, it.quantity, it.catagory);
                }
            }
            Service.Show_open_box_notice_item(p, text, ids);
        }
    }
}
