package Game.activities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Game.Boss.BossServer;
import Game.ai.Player_Nhan_Ban;
import Game.client.Player;
import Game.core.*;
import Game.map.Map;
import Game.map.MapService;
import Game.map.Vgo;
import Game.template.MainObject;
import Game.template.Part_player;
import Game.io.Message;
import Game.map.LeaveItemMap;

import static Game.map.LeaveItemMap.leave_vang;

import Game.map.Mob_in_map;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import Game.template.ItemTemplate3;
import Game.template.MemberBattlefields;

public class ChienTruong {
    public static ChienTruong instance;
    public static boolean running = false;
    private HashMap<String, MemberBattlefields> list;
    private List<MemberBattlefields> BXH;
    @Getter
    private int status; // 0 sleep, 1 : register, 2 : start
    private long time;
    public int[] info_house;
    public List<Player_Nhan_Ban> list_ai;
    public List<Mob_in_map> boss;
    public HashMap<String, MemberBattlefields> list_win;
    public byte type_house_win;
    public byte count_boss;

    public void setTime(int i) {
        this.time = i;
    }

    public static ChienTruong gI() {
        if (instance == null) {
            instance = new ChienTruong();
            instance.init();
        }
        return instance;
    }

    private void init() {
        this.list = new HashMap<>();
        this.list_win = new HashMap<>();
        this.BXH = new ArrayList<>();
        this.status = 0;
        this.time = 0;
        list_ai = Player_Nhan_Ban.init();
        this.boss = new ArrayList<>();
        type_house_win = 4;
        count_boss = 0;
        //
    }

    public synchronized void update() {
        try {
            if (this.getStatus() == 1 && this.time < System.currentTimeMillis()) { // Đang trong thời gian đăng ký
                this.start();
            } else if (this.status == 2) {
                if ((this.time - System.currentTimeMillis()) / 1000 / 60 == 55 && count_boss == 0) {
                    create_boss();
                } else if ((this.time - System.currentTimeMillis()) / 1000 / 60 == 50 && count_boss == 1) {
                    create_boss();
                }
                if (this.time <= System.currentTimeMillis()) {
                    this.finish();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void create_boss() {
        try {
            count_boss++;
            BossServer.refresh_boss_battlefield();
            Manager.gI().chatKTGprocess(" Xà nữ xuất hiện tại chiến trường.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        if (this.status == 1) {
            Manager.gI().chatKTGprocess("Chiến trường bắt đầu");
            this.BXH.clear();
            this.status = 2;
            this.time = 60 * 60 * 1000 + System.currentTimeMillis();
            int init_house = this.list.size() / 40;
            init_house = init_house > 0 ? init_house : 5;
            init_house = 3;
            this.info_house = new int[]{init_house, init_house, init_house, init_house};
            //
            List<MemberBattlefields> list = new ArrayList<>();
            for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
                list.add(en.getValue());
            }
            Collections.shuffle(list);
            for (int i = 0; i < list.size(); i++) {
                list.get(i).village = ((i % 4) + 2);
            }
            list.clear();
            //
            byte[] id_map = new byte[]{54, 56, 58, 60, 61};
            for (int i = 0; i < id_map.length; i++) {
                Map[] mapp = Map.get_map_by_id(id_map[i]);
                for (Map map : mapp) {
                    map.maxzone = (byte) init_house;
                    if (map.map_id == 61) continue;
                    for (Mob_in_map mobb : map.mobs) {
                        if (mobb == null) {
                            continue;
                        }
                        mobb.isdie = false;
                    }
                }
            }
            for (Player_Nhan_Ban temp : this.list_ai) {
                temp.isdie = false;
                temp.hp = temp.hp_max;
            }
            //
            for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
                Player p0 = Map.get_player_by_name(en.getKey());
                if (p0 != null) {
                    Vgo vgo = new Vgo();
                    switch (en.getValue().village) {
                        case 2: { // lang gio
                            vgo.id_map_go = 55;
                            vgo.x_new = 224;
                            vgo.y_new = 256;
                            MapService.change_flag(p0.map, p0, 2);
                            break;
                        }
                        case 3: { // lang lua
                            vgo.id_map_go = 59;
                            vgo.x_new = 240;
                            vgo.y_new = 224;
                            MapService.change_flag(p0.map, p0, 1);
                            break;
                        }
                        case 4: { // lang set
                            vgo.id_map_go = 57;
                            vgo.x_new = 264;
                            vgo.y_new = 272;
                            MapService.change_flag(p0.map, p0, 4);
                            break;
                        }
                        default: { // 5 lang anh sang
                            vgo.id_map_go = 53;
                            vgo.x_new = 276;
                            vgo.y_new = 246;
                            MapService.change_flag(p0.map, p0, 5);
                            break;
                        }
                    }
                    p0.change_map(p0, vgo);
                }
            }
        }
    }

    public synchronized void update_house_die(short id) throws IOException {
        switch (id) {
            case 89: { // nha set type 4
                if (this.info_house[2] > 0) {
                    this.info_house[2]--;
                }
                break;
            }
            case 90: { // nha gio type 2
                if (this.info_house[0] > 0) {
                    this.info_house[0]--;
                }
                break;
            }
            case 91: { // nha anh sang type 5
                if (this.info_house[3] > 0) {
                    this.info_house[3]--;
                }
                break;
            }
            case 92: { // nha lua type 3
                if (this.info_house[1] > 0) {
                    this.info_house[1]--;
                }
                break;
            }
        }
        for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
            Player p0 = Map.get_player_by_name(en.getKey());
            if (p0 != null) {
                ChienTruong.gI().send_info(p0);
            }
        }
        int dem = 4;
        int type_house = -1;
        for (int i = 0; i < info_house.length; i++) {
            if (info_house[i] <= 0) {
                finish_house(i + 2);
                dem--;
            } else {
                type_house = i;
            }
        }
        if (dem <= 1) {
            this.time = 3 * 60 * 1000 + System.currentTimeMillis();
            this.type_house_win = (byte) type_house;
            this.status = 2;
            for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
                Player p0 = Map.get_player_by_name(en.getKey());
                if (p0 != null && Map.is_map_chien_truong(p0.map.map_id)) {
                    list_win.put(en.getKey(), en.getValue());
                    p0.update_point_arena(100);
                    ChienTruong.gI().send_info(p0);
                    this.time = 0;
                    this.update_time(p0);
                }
            }
        }
    }

    private void finish_house(int i) {
        try {
            List<String> list_remove = new ArrayList<>();
            for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
                if (en.getValue().village == i) {
                    list_remove.add(en.getKey());
                    Player p0 = Map.get_player_by_name(en.getKey());
                    if (p0 != null) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 1;
                        vgo.x_new = 432;
                        vgo.y_new = 354;
                        p0.change_map(p0, vgo);
                        MapService.change_flag(p0.map, p0, -1);
                    }
                }
            }
            list_remove.forEach(l -> {
                this.list.remove(l);
            });
            for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
                Player p0 = Map.get_player_by_name(en.getKey());
                if (p0 != null && Map.is_map_chien_truong(p0.map.map_id)) {
                    ChienTruong.gI().send_info(p0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finish() {
        try {
            if (this.status == 2) {
                this.status = 3;
                running = false;
                String name_win = "";
                switch (this.type_house_win) {
                    case 0 -> name_win = "gió";
                    case 1 -> name_win = "lửa";
                    case 2 -> name_win = "sét";
                    case 3 -> name_win = "ánh sáng";
                    default -> {
                    }
                }
                //
                for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
                    Player p0 = Map.get_player_by_name(en.getKey());
                    if (p0 != null) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 1;
                        vgo.x_new = 432;
                        vgo.y_new = 354;
                        p0.change_map(p0, vgo);
                        MapService.change_flag(p0.map, p0, -1);
                    }
                }
                for (Mob_in_map mobInMap : boss) {
                    mobInMap.level = 10;
                    mobInMap.hp = 0;
                    mobInMap.is_boss_active = false;
                    mobInMap.isdie = true;
                }
                if (type_house_win != 4) {
                    Manager.gI().chatKTGprocess("Chiến trường kết thúc làng " + name_win + " giành chiến thắng");
                    for (Entry<String, MemberBattlefields> en : this.list_win.entrySet()) {
                        this.BXH.add(en.getValue());
                    }
                    this.BXH.sort(new Comparator<MemberBattlefields>() {
                        @Override
                        public int compare(MemberBattlefields o1, MemberBattlefields o2) {
                            return o1.point > o2.point ? -1 : 1;
                        }
                    });
                    Manager.gI().chatKTGprocess("Chúc mừng " + this.BXH.get(0).name + " đã trở thành vua chiến trường");
                    Manager.gI().vua_chien_truong = this.BXH.get(0).ID;
                    Player p = MapService.get_player_by_id(this.BXH.get(0).ID);
                    if (p != null) {
                        p.point_activity += 1000;
                    }
                    try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement()) {
                        st.execute("UPDATE `config_server` SET `king_battlefield` = '" + Manager.gI().vua_chien_truong + "' WHERE `id` = '" + 1 + "';");
                        connection.commit();
                        get_king_battlefield();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Manager.gI().chatKTGprocess("Chiến trường kết thúc không có làng nào giành chiến thắng");
                }

                this.list.clear();
                this.info_house = null;
            }
        } catch (Exception ex) {
            Log.gI().add_Log_Server("ERROR", ex.getMessage());
        }
    }

    public synchronized void register(Player p) throws IOException {
        if (this.list.containsKey(p.name)) {
            Service.send_notice_box(p.conn, "Đã đăng ký rồi");
        } else {
            MemberBattlefields temp = new MemberBattlefields();
            temp.name = p.name;
            temp.ID = p.ID;
            temp.point = 0;
            temp.village = 0;
            temp.received = false;
            this.list.put(p.name, temp);
            Service.send_notice_box(p.conn, "Đăng ký thành công");
        }
    }

    public synchronized void open_register() throws IOException {
        if (this.status == 0) {
            Manager.gI().chatKTGprocess("Chiến trường đã mở đăng ký");
            this.status = 1;
            this.time = 45 * 60 * 1000L + System.currentTimeMillis();
        }
    }

    public void send_info(Player p) throws IOException {
        this.update_time(p);
        for (int i = 2; i < 6; i++) {
            Message m = new Message(-94);
            m.writer().writeByte(i); //
            m.writer().writeByte(this.info_house[i - 2]); // total house
            m.writer().writeShort(total_p_of_house(i)); // total p
            m.writer().writeByte(1);
            p.conn.addmsg(m);
            m.cleanup();
        }
    }

    private int total_p_of_house(int i) {
        int result = 0;
        for (Entry<String, MemberBattlefields> en : this.list.entrySet()) {
            if (en.getValue().village == i) {
                Player p0 = Map.get_player_by_name(en.getKey());
                if (p0 != null && Map.is_map_chien_truong(p0.map.map_id)) {
                    result++;
                }
            }
        }
        return result;
    }

    private void update_time(Player p) throws IOException {
        Message m = new Message(-94);
        m.writer().writeByte(-1); //
        m.writer().writeByte(0);
        m.writer().writeShort(0);
        m.writer().writeByte(0);
        m.writer().writeLong(this.time - 60 * 60 * 1000);
        p.conn.addmsg(m);
        m.cleanup();
    }

    public void get_ai(Player p, int id) throws IOException {
        for (int i = 0; i < this.list_ai.size(); i++) {
            Player_Nhan_Ban temp = this.list_ai.get(i);
            if (temp.id == ((short) id)) {
                Message m = new Message(5);
                m.writer().writeShort(temp.id);
                m.writer().writeUTF(temp.name);
                m.writer().writeShort(temp.x);
                m.writer().writeShort(temp.y);
                m.writer().writeByte(Util.random(0, 4)); // clazz
                m.writer().writeByte(126);
                m.writer().writeByte(1); // head
                m.writer().writeByte(1); // eye
                m.writer().writeByte(Util.random(32, 34)); // hair
                m.writer().writeShort(40); // level
                m.writer().writeInt(temp.hp); // hp
                m.writer().writeInt(temp.hp_max); // hp max
                m.writer().writeByte(temp.typePk); // type
                m.writer().writeShort(0); // point pk
                m.writer().writeByte(3); // size part
                byte[] part_ = new byte[]{8, 0, 1};
                for (int j = 0; j < 3; j++) {
                    m.writer().writeByte(part_[j]);
                    m.writer().writeByte(0);
                    m.writer().writeByte(3);
                    m.writer().writeShort(-1);
                    m.writer().writeShort(-1);
                    m.writer().writeShort(-1);
                    m.writer().writeShort(-1); // eff
                }
                m.writer().writeShort(-1); // clan
                m.writer().writeByte(-1); // pet
                m.writer().writeByte(7);
                for (int i1 = 0; i1 < 7; i1++) {
                    if (p.conn.version >= 280) {
                        m.writer().writeShort(-1);
                    } else {
                        m.writer().writeByte(-1);
                    }
                }
                //
                m.writer().writeShort(temp.henshin);
                m.writer().writeByte(-1); // type use mount
                m.writer().writeBoolean(false);
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                m.writer().writeShort(-1); // mat na
                m.writer().writeByte(1); // paint mat na trc sau
                m.writer().writeShort(-1); // phi phong
                m.writer().writeShort(-1); // weapon
                m.writer().writeShort(-1);
                m.writer().writeShort(-1); // hair
                m.writer().writeShort(-1); // wing
                m.writer().writeShort(-1); // body
                m.writer().writeShort(-1); // leg
                m.writer().writeShort(-1); // bienhinh
                p.conn.addmsg(m);
                m.cleanup();
                break;
            }
        }
    }

    public static void Obj_Die(Map map, MainObject mainAtk, MainObject focus) throws IOException {
        if (!mainAtk.isPlayer() || !focus.isMob()) return;
        Player p = (Player) mainAtk;
        Mob_in_map mob = (Mob_in_map) focus;
        short[] id_item_leave3 = new short[]{};
        short[] id_item_leave4 = new short[]{};
        short[] id_item_leave7 = new short[]{};
        short sizeRandomMedal = 0;
        switch (mob.template.mob_id) {
            case 89, 90, 91, 92: {
                id_item_leave4 = new short[]{-1, -1, -1, -1, -1, -1, 54, 53};
                id_item_leave7 = new short[]{(short)Util.random(126, 136)};
                if (Util.random(100) < 5) {
                    id_item_leave3 = new short[]{(short) Util.random(4577, 4585)};
                }
                sizeRandomMedal = (short) (10);
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
        for(int i = 0; i < 1; i++){
            for(short id : id_item_leave7){
                LeaveItemMap.leave_item_by_type7(map, id, p, mob.ID, mob.ID);
            }
        }
        for (int l = 0; l < sizeRandomMedal; l++) {
            LeaveItemMap.leave_item_by_type7(map, (short) Util.random(133, 136), p, mob.ID, p.ID);
        }
    }

    public MemberBattlefields get_infor_register(String name) {
        return this.list.get(name);
    }

    public MemberBattlefields get_bxh(String name) {
        for (int i = 0; i < this.BXH.size(); i++) {
            if (this.BXH.get(i).name.equals(name)) {
                if (i < 10) {
                    return this.BXH.get(i);
                }
            }
        }
        return null;
    }

    public int get_index_bxh(MemberBattlefields temp) {
        return this.BXH.indexOf(temp);
    }

    public static void get_king_battlefield() throws SQLException {
        Map.head = -1;
        Map.eye = -1;
        Map.hair = -1;
        Map.weapon = -1;
        Map.body = -1;
        Map.leg = -1;
        Map.hat = -1;
        Map.wing = -1;
        Map.king_battlefield_name = "";
        Connection conn = SQL.gI().getConnection();
        Statement ps = conn.createStatement();        
        String query = "SELECT * FROM `player` WHERE `id` =" + Manager.gI().vua_chien_truong + " LIMIT 1";
        ResultSet rs = ps.executeQuery(query);
        if (rs.next()) {
            Map.king_battlefield_name = rs.getString("name");
            JSONArray js = (JSONArray) JSONValue.parse(rs.getString("body"));
            Map.head = Short.parseShort(js.get(0).toString());
            Map.eye = Short.parseShort(js.get(1).toString());
            Map.hair = Short.parseShort(js.get(2).toString());
            js.clear();
            js = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
            for (Object j : js) {
                JSONArray jsar2 = (JSONArray) JSONValue.parse(j.toString());
                if (jsar2 == null) {
                    return;
                }
                byte index_wear = Byte.parseByte(jsar2.get(9).toString());
                if (index_wear != 0 && index_wear != 1 && index_wear != 2 && index_wear != 7 && index_wear != 10) {
                    continue;
                }

                Part_player temp = new Part_player();
                temp.type = Byte.parseByte(jsar2.get(2).toString());
                temp.part = Byte.parseByte(jsar2.get(6).toString());
                if (temp.type == 2) {
                    Map.hat = temp.part;
                }
                if (temp.type == 0) {
                    Map.body = temp.part;
                }
                if (temp.type == 1) {
                    Map.leg = temp.part;
                }
                if (temp.type == 7) {
                    Map.wing = temp.part;
                }
                if (temp.type == 10) {
                    Map.weapon = temp.part;
                }
            }
        }
        rs.close();
        ps.close();
    }
}
