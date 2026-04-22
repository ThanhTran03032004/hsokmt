package Game.client;

import Game.core.*;
import Game.io.Session;
import Game.map.MapService;
import Game.template.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import Game.io.Message;

import Game.map.Map;

public class Clan {
    public static final short[] item_shop
            = new short[]{19, 20, 146, 159, 160, 161, 163, 228, 229, 230, 231, 232, 233, 234};
    public static final List<Clan> entries = new ArrayList<>();
    public static int[] vang_upgrade = new int[]{0, 2_000_000};
    public static int[] ngoc_upgrade = new int[]{0, 2_000};
    public static int TIME_SACH = 0;
    public static int TIME_KINH_LUP = 1;
    public int ID;
    public List<MemberClan> mems;
    public String name_clan;
    public String name_clan_shorted;
    public short icon;
    public short level;
    public long exp;
    public String slogan;
    public String rule;
    public int kimcuong;
    public long vang;
    public String notice;
    public int max_mem;
    public List<Item47> item_clan;
    public List<Mob_MoTaiNguyen> mo_tai_nguyen;
    public List<EffTemplate> time_items = new ArrayList<>();
    public static void ResetMoTaiNguyen() {
        try {
            for (Clan c : entries) {
                if (c.mo_tai_nguyen != null)
                    c.mo_tai_nguyen.clear();
            }
        } catch (Exception e) {
            Log.gI().add_Log_Server("Clans", "Reset MoTaiNguyen: " + e.getMessage());
        }
    }

    public synchronized void add_mo_tai_nguyen(Mob_MoTaiNguyen temp_mob) {
        this.mo_tai_nguyen.add(temp_mob);
    }

    public synchronized void remove_mo_tai_nguyen(Mob_MoTaiNguyen temp_mob) {
        this.mo_tai_nguyen.remove(temp_mob);
    }
    
    public void sortMember() {
        mems.sort((m1,m2) -> Byte.compare(m2.mem_type, m1.mem_type));
    }

    public synchronized void clan_process(Session conn, Message m2, int type) throws IOException {
        switch (type) {
            case 21: {
                this.open_box_clan(conn);
                break;
            }
            case 4: {
                if (!conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                    Service.send_notice_box(conn, "Bạn không phải thủ lĩnh!");
                    return;
                }
                byte mem_type = m2.reader().readByte();
                String name_mem = m2.reader().readUTF();
                if (get_mem_type_size(mem_type) > 2 && mem_type == 126) {
                    Service.send_notice_box(conn, "1 bang hội không có quá 2 Phó chỉ huy");
                    return;
                }
                MemberClan member = null;
                for (int i = 0; i < conn.p.myclan.mems.size(); i++) {
                    if (conn.p.myclan.mems.get(i).name.equals(name_mem)) {
                        member = conn.p.myclan.mems.get(i);
                        break;
                    }
                }
                if (member == null) {
                    Service.send_notice_box(conn, "Thành viên này không có trong bang hội.");
                    return;
                }
                String name_mem_type = "";
                switch (mem_type) {
                    case 126: {
                        name_mem_type += "Phó Chỉ Huy";
                        break;
                    }
                    case 125: {
                        name_mem_type += "Đại Hiệp Sĩ";
                        break;
                    }
                    case 124: {
                        name_mem_type += "Hiệp Sĩ Cao Quý";
                        break;
                    }
                    case 123: {
                        name_mem_type += "Hiệp Sĩ Danh Dự";
                        break;
                    }
                    case 122: {
                        name_mem_type += "Thành Viên Mới";
                        break;
                    }
                }
                for (int i = 0; i < conn.p.myclan.mems.size(); i++) {
                    if (conn.p.myclan.mems.get(i).name.equals(name_mem)) {
                        conn.p.myclan.mems.get(i).mem_type = mem_type;
                        break;
                    }
                }
                Service.send_notice_box(conn, "Bổ nhiệm " + name_mem + " thành " + name_mem_type);
                Player p0 = Map.get_player_by_name(name_mem);
                if (p0 != null) {
                    MapService.update_in4_2_other_inside(conn.p.map, p0);
                    MapService.send_in4_other_char(p0.map, p0, p0);
                    Service.send_char_main_in4(p0);
                }
                // update list
                this.update_list_mem(conn, name_mem, mem_type);
                break;
            }
            case 18: {
                String name = m2.reader().readUTF();
                if (get_mem_type(name) == 121) {
                    return;
                }
                if (get_mem_type(name) == 127) {
                    return;
                }
                if (get_mem_type(name) == 127 && !conn.p.name.equals(name)) {
                    Service.send_notice_box(conn, "Bạn không có quyền trục xuất thủ lĩnh");
                    return;
                }
                if (get_mem_type(conn.p.name) == 127 && !conn.p.name.equals(name)) {
                    this.remove_mem(name);
                    Player p0 = Map.get_player_by_name(name);
                    if (p0 != null) {
                        Service.send_notice_box(p0.conn, "Bạn bị trục xuất khỏi bang.");
                        p0.myclan = null;
                        if (Horse.isHorseClan(p0.type_use_horse)) {
                            p0.type_use_horse = -1;
                        }
                        if (p0.item.wear[14].isWingClan()) {
                            p0.item.wear[14] = null;
                        }
                        MapService.update_in4_2_other_inside(conn.p.map, p0);
                        MapService.send_in4_other_char(p0.map, p0, p0);
                        Service.send_char_main_in4(p0);
                        this.update_list_mem(conn, name, 121);
                    }
                } else if (conn.p.name.equals(name)) {
                    conn.p.myclan.remove_mem(conn.p.name);
                    conn.p.myclan = null;
                    if (Horse.isHorseClan(conn.p.type_use_horse)) {
                        conn.p.type_use_horse = -1;
                    }
                    if (conn.p.item.wear[14].isWingClan()) {
                        conn.p.item.wear[14] = null;
                    }
                    MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                    MapService.send_in4_other_char(conn.p.map, conn.p, conn.p);
                    Service.send_char_main_in4(conn.p);
                    Service.send_notice_box(conn, "Đã rời bang");
                    this.update_list_mem(conn, name, 121);
                }
                break;
            }
            case 13: {
                if (conn.p.myclan != null) {
                    send_list_mem(conn);
                }
                break;
            }
            case 10: {
                if (this.mems.size() >= this.max_mem) {
                    Service.send_notice_box(conn, "Số lượng thành viên đã đầy!");
                } else {
                    Player p0 = Map.get_player_by_name(m2.reader().readUTF());
                    if (p0 != null && p0.isOwner) {
                        if (p0.myclan != null) {
                            if (p0.myclan.name_clan.equals(conn.p.myclan.name_clan)) {
                                Service.send_notice_box(conn, "Đối phương đã là thành viên của bang!");
                            } else {
                                Service.send_notice_box(conn, "Đối phương là thành viên của bang khác!");
                            }
                            return;
                        }
                        Message m = new Message(69);
                        m.writer().writeByte(10);
                        m.writer().writeUTF(conn.p.name);
                        p0.conn.addmsg(m);
                        m.cleanup();
                    }
                }
                break;
            }
            case 6: {
                long value = m2.reader().readInt();
                if (value < 0 || value > 2_000_000_000 || value > conn.p.get_vang()) {
                    Service.send_notice_box(conn, "Số nhập vào không hợp lệ");
                    return;
                }
                this.member_contribute_vang(conn, value);
                break;
            }
            case 7: {
                long value = m2.reader().readInt();
                if (value < 0 || value > 2_000_000_000L || ((value + this.kimcuong) > 2_000_000_000L)
                        || value > conn.p.get_ngoc()) {
                    Service.send_notice_box(conn, "Số nhập vào không hợp lệ");
                    return;
                }
                this.member_contribute_ngoc(conn, value);
                break;
            }
            case 14: {
                String name = m2.reader().readUTF();
                MemberClan p0 = null;
                for (int i = 0; i < mems.size(); i++) {
                    MemberClan mem = mems.get(i);
                    if (mem.name.equals(name)) {
                        p0 = mem;
                        break;
                    }
                }
                if (p0 != null) {
                    Message m = new Message(69);
                    m.writer().writeByte(14);
                    m.writer().writeUTF(p0.name);
                    m.writer().writeShort(p0.level);
                    m.writer().writeByte(p0.mem_type);
                    m.writer().writeLong(this.get_mem_contribution_vang(p0.name));
                    m.writer().writeInt(this.get_mem_contribution_ngoc(p0.name));
                    conn.addmsg(m);
                    m.cleanup();
                } else {
                    Service.send_notice_box(conn, "Có lỗi xảy ra!");
                }
                break;
            }
            case 2: {
                this.notice = m2.reader().readUTF();
                this.update_in4_clan_box_notice(conn, 2);
                Service.send_notice_box(conn, "Thay đổi thông báo thành công");
                break;
            }
            case 16: {
                this.slogan = m2.reader().readUTF();
                this.update_in4_clan_box_notice(conn, 16);
                Service.send_notice_box(conn, "Thay đổi khẩu hiệu thành công");
                break;
            }
            case 17: {
                this.rule = m2.reader().readUTF();
                this.update_in4_clan_box_notice(conn, 17);
                Service.send_notice_box(conn, "Thay đổi nội quy thành công");
                break;
            }
            case 15: {
                send_info_clan(conn, (byte) 0);
                break;
            }
        }
    }

    public synchronized void remove_mem(String name) {
        MemberClan mem = null;
        for (int i = 1; i < this.mems.size(); i++) {
            if (this.mems.get(i).name.equals(name)) {
                mem = this.mems.get(i);
            }
        }
        if (mem != null) {
            this.mems.remove(mem);
        }
    }

    public void member_contribute_ngoc(Session conn, long value) throws IOException {
        if (conn.status != 0) {
            Service.send_notice_box(conn, "Kích hoạt tài khoản để sử dụng chức năng");
            return;
        }
        conn.p.update_ngoc(-value);
        Log.gI().add_log(conn.p.name, "Góp " + Util.number_format(value) + " ngọc bang " + this.name_clan);
        this.kimcuong += value;
        this.update_in4_clan_box_notice(conn, 7);
        this.update_contribution_ngoc(conn.p.name, (int) value);
        Service.send_notice_box(conn, "Đóng góp " + Util.number_format(value) + " ngọc thành công");
    }

    public void member_contribute_vang(Session conn, long value) throws IOException {
        if (conn.status != 0) {
            Service.send_notice_box(conn, "Kích hoạt tài khoản để sử dụng chức năng");
            return;
        }
        conn.p.update_vang(-value, "Góp %s vàng vào bang " + this.name_clan);
        this.vang += value;
        this.update_in4_clan_box_notice(conn, 6);
        this.update_contribution_vang(conn.p.name, (int) value);
        Service.send_notice_box(conn, "Đóng góp " + Util.number_format(value) + " vàng thành công");
    }
    public void send_info_clan(Session conn, byte type) throws IOException {
        Message m = new Message(69);
        m.writer().writeByte(15);
        m.writer().writeByte(type);
        m.writer().writeByte(0);
        m.writer().writeInt(this.ID);
        m.writer().writeShort(this.icon);
        m.writer().writeUTF(this.name_clan_shorted);
        m.writer().writeUTF(this.name_clan);
        m.writer().writeShort(this.level);
        m.writer().writeShort(this.get_percent_level());
        m.writer().writeShort(Rank.clan_index(this, Rank.top_level_clan)); // index bxh
        m.writer().writeShort(this.mems.size()); // mem
        m.writer().writeShort(this.max_mem); // max mem
        m.writer().writeUTF(this.mems.get(0).name);
        m.writer().writeUTF(this.getSlogan()); // slogan
        m.writer().writeUTF(this.getRule()); // noi quy
        m.writer().writeLong(this.vang);
        m.writer().writeInt(this.kimcuong);
        m.writer().writeByte(0); // thanh tich
        conn.addmsg(m);
        m.cleanup();
    }
    private void update_list_mem(Session conn, String name_mem, int mem_type) throws IOException {
        Message m = new Message(69);
        m.writer().writeByte(19);
        m.writer().writeShort(32000);
        m.writer().writeUTF(name_mem);
        m.writer().writeInt(this.ID);
        m.writer().writeUTF(this.name_clan);
        m.writer().writeUTF(this.name_clan_shorted);
        m.writer().writeShort(this.icon);
        m.writer().writeByte(mem_type);
        conn.addmsg(m);
        m.cleanup();
    }

    public synchronized void send_list_mem(Session conn) throws IOException {
        Message m = new Message(56);
        m.writer().writeByte(4);
        m.writer().writeUTF(this.name_clan);
        m.writer().writeByte(99);
        m.writer().writeInt(0);
        m.writer().writeByte(this.mems.size());
        for (int i = 0; i < this.mems.size(); i++) {
            MemberClan mem = this.mems.get(i);
            Player p0 = Map.get_player_by_name(mem.name);
            if (p0 != null) {
                mem.head = p0.head;
                mem.eye = p0.eye;
                mem.hair = p0.hair;
                mem.level = p0.level;
                mem.head = p0.head;
                mem.itemwear.clear();
                for (int i1 = 0; i1 < p0.item.wear.length; i1++) {
                    Item3 it = p0.item.wear[i1];
                    if (it != null && (i1 == 0 || i1 == 1 || i1 == 6 || i1 == 7 || i1 == 10)) {
                        Part_player part = new Part_player();
                        part.type = it.type;
                        part.part = it.part;
                        mem.itemwear.add(part);
                    }
                }
            }
            m.writer().writeUTF(mem.name);
            m.writer().writeByte(mem.head);
            m.writer().writeByte(mem.eye);
            m.writer().writeByte(mem.hair);
            m.writer().writeShort(mem.level);
            m.writer().writeByte(mem.itemwear.size());
            for (Part_player it : mem.itemwear) {
                m.writer().writeByte(it.part);
                m.writer().writeByte(it.type);
            }

            if (p0 != null) {
                m.writer().writeByte(1);
            } else {
                m.writer().writeByte(0);
            }
            switch (mem.mem_type) {
                case 127: {
                    m.writer().writeUTF("Thủ lĩnh");
                    break;
                }
                default: { // type 122
                    m.writer().writeUTF("Thành viên mới");
                    break;
                }
            }
            m.writer().writeShort(this.icon);
            m.writer().writeUTF(this.name_clan_shorted);
            m.writer().writeByte(mem.mem_type);
        }
        conn.addmsg(m);
        m.cleanup();
    }

    private synchronized void update_in4_clan_box_notice(Session conn, int type) throws IOException {
        switch (type) {
            case 6:
            case 7: {
                Message m = new Message(69);
                m.writer().writeByte(15);
                m.writer().writeByte(0);
                m.writer().writeByte(1);
                m.writer().writeLong(this.vang);
                m.writer().writeInt(this.kimcuong);
                conn.addmsg(m);
                m.cleanup();
                break;
            }
            case 2:
            case 17: {
                Message m = new Message(69);
                m.writer().writeByte(15);
                m.writer().writeByte(0);
                m.writer().writeByte(2);
                m.writer().writeUTF(this.getRule());
                conn.addmsg(m);
                m.cleanup();
                break;
            }
            case 16: {
                Message m = new Message(69);
                m.writer().writeByte(15);
                m.writer().writeByte(0);
                m.writer().writeByte(3);
                m.writer().writeUTF(this.getSlogan());
                conn.addmsg(m);
                m.cleanup();
                break;
            }
        }
    }

    private synchronized void update_contribution_ngoc(String name, int quant) {
        for (int i = 0; i < mems.size(); i++) {
            MemberClan temp = mems.get(i);
            if (temp.name.equals(name)) {
                temp.kimcuong += quant;
            }
        }
    }

    private synchronized void update_contribution_vang(String name, int quant) {
        for (int i = 0; i < mems.size(); i++) {
            MemberClan temp = mems.get(i);
            if (temp.name.equals(name)) {
                temp.vang += quant;
            }
        }
    }

    private synchronized int get_mem_contribution_ngoc(String name) {
        for (int i = 0; i < mems.size(); i++) {
            MemberClan temp = mems.get(i);
            if (temp.name.equals(name)) {
                return temp.kimcuong;
            }
        }
        return 0;
    }

    private synchronized long get_mem_contribution_vang(String name) {
        for (int i = 0; i < mems.size(); i++) {
            MemberClan temp = mems.get(i);
            if (temp.name.equals(name)) {
                return temp.vang;
            }
        }
        return 0;
    }

    private synchronized String getRule() {
        String text = "";
        if (this.notice.equals("")) {
            if (this.rule.equals("")) {
                return "";
            }
            return ("Nội quy: " + this.rule);
        } else {
            if (this.rule.equals("")) {
                text += "\n";
            } else {
                text += "Nội quy: " + this.rule;
                text += "\n";
            }
            text += "Thông báo: " + this.notice;
        }
        return text;
    }

    private synchronized String getSlogan() {
        if (this.slogan.equals("")) {
            return "";
        }
        return ("Khẩu hiệu: " + this.slogan);
    }

    public int get_percent_level() {
        return (int) ((exp * 1000) / Level.entry.get(level - 1).exp);
    }

    public synchronized static boolean start_create_clan(Session conn, String name, String name_shorted) throws IOException {
        for (Clan clan : entries) {
            if (clan.name_clan.equals(name)) {
                Service.send_notice_box(conn, "Tên này đã tồn tại, xin hãy chọn lại!");
                return false;
            }
            if (clan.name_clan_shorted.equals(name_shorted)) {
                Service.send_notice_box(conn, "Tên rút gọn này đã tồn tại, xin hãy chọn lại!");
                return false;
            }
        }
        conn.p.name_clan_temp = name;
        conn.p.short_name_clan_temp = name_shorted;
        return true;
    }
    public synchronized static void end_create_clan(Session conn, short idIcon) throws IOException {
        if (conn.p.name_clan_temp.isEmpty() || conn.p.short_name_clan_temp.isEmpty()) {
            return;
        }
        Clan temp = new Clan();
        temp.mems = new ArrayList<>();
        //
        MemberClan temp_mem = new MemberClan();
        temp_mem.name = conn.p.name;
        temp_mem.mem_type = 127; // thu linh
        temp_mem.kimcuong = 0;
        temp_mem.vang = 0;
        temp_mem.head = conn.p.head;
        temp_mem.eye = conn.p.eye;
        temp_mem.hair = conn.p.hair;
        temp_mem.level = conn.p.level;
        temp_mem.itemwear = new ArrayList<>();
        temp.mo_tai_nguyen = new ArrayList<>();
        for (int i = 0; i < conn.p.item.wear.length; i++) {
            if (conn.p.item.wear[i] == null || (i != 0 && i != 1 && i != 6 && i != 7 && i != 10)) {
                continue;
            }
            Part_player temp2 = new Part_player();
            temp2.type = conn.p.item.wear[i].type;
            temp2.part = conn.p.item.wear[i].part;
            temp_mem.itemwear.add(temp2);
        }
        //
        temp.mems.add(temp_mem);
        temp.name_clan = conn.p.name_clan_temp;
        temp.name_clan_shorted = conn.p.short_name_clan_temp;
        temp.icon = idIcon;
        temp.level = 1;
        temp.exp = 0;
        temp.slogan = "";
        temp.rule = "";
        temp.notice = "";
        temp.setVang(0);
        temp.setKimcuong(0);
        temp.max_mem = 5;
        temp.item_clan = new ArrayList<>();
        temp.time_items = new ArrayList<>();

        conn.p.name_clan_temp = "";
        conn.p.short_name_clan_temp = "";
        String query
                = "INSERT INTO `clan` (`name`, `name_short`, `mems`, `item`, `level`, `exp`, `slogan`, `rule`, `notice`, `vang`, `kimcuong`, `max_mem`, `icon`, `time_item`) VALUES ('"
                + temp.name_clan + "', '" + temp.name_clan_shorted + "', '" + Clan.flush_mem_json(temp.mems) + "', '"
                + Clan.flush_item_json(temp) + "', " + temp.level + ", " + temp.exp + ", '" + temp.slogan
                + "', '" + temp.rule + "', '" + temp.notice + "', " + temp.vang + ", " + temp.kimcuong + ", "
                + temp.max_mem + ", " + temp.icon + ", '" + Clan.flush_time_item(temp.time_items) + "')";
        try (Connection connection = SQL.gI().getConnection(); PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    temp.ID = rs.getInt(1);
                }
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Service.send_notice_box(conn, "Lỗi tạo bang, chụp mành hình gửi admin");
            return;
        }
        entries.add(temp);
        conn.p.myclan = temp;
        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
        Service.send_char_main_in4(conn.p);
    }
    @SuppressWarnings("unchecked")
    public static String flush_item_json(Clan clan) {
        JSONArray js = new JSONArray();
        Iterator<Item47> iterator = clan.item_clan.iterator();
        while (iterator.hasNext()) {
            Item47 temp = iterator.next();
            if (temp.expiry < System.currentTimeMillis() && temp.expiry > 0) {
                iterator.remove();
            } else {
                JSONArray js2 = new JSONArray();
                js2.add(temp.id);
                js2.add(temp.quantity);
                js2.add(temp.expiry);
                js.add(js2);
            }
        }
        return js.toJSONString();
    }
    public static String flush_time_item(List<EffTemplate> effTemplates) {
        JSONArray js = new JSONArray();
        for (EffTemplate effTemplate : effTemplates) {
            JSONArray js2 = new JSONArray();
            if (effTemplate.time < System.currentTimeMillis()) {
                continue;
            }
            js2.add(effTemplate.id);
            js2.add(effTemplate.time);
            js.add(js2);
        }
        return js.toJSONString();
    }
    @SuppressWarnings("unchecked")
    public synchronized static String flush_mem_json(List<MemberClan> mems2) {
        JSONArray js = new JSONArray();
        for (MemberClan temp : mems2) {
            JSONArray js2 = new JSONArray();
            js2.add(temp.name);
            js2.add(temp.mem_type);
            js2.add(temp.kimcuong);
            js2.add(temp.vang);
            js2.add(temp.head);
            js2.add(temp.eye);
            js2.add(temp.hair);
            js2.add(temp.level);
            JSONArray js3 = new JSONArray();
            for (Part_player part : temp.itemwear) {
                JSONArray js4 = new JSONArray();
                js4.add(part.part);
                js4.add(part.type);
                js3.add(js4);
            }
            js2.add(js3);
            js.add(js2);
        }
        return js.toJSONString();
    }

    public synchronized static Clan get_clan_of_player(String name) {
        for (Clan temp : entries) {
            for (int j = 0; j < temp.mems.size(); j++) {
                MemberClan temp2 = temp.mems.get(j);
                if (temp2.name.equals(name)) {
                    return temp;
                }
            }
        }
        return null;
    }

    public synchronized byte get_mem_type(String name) {
        for (MemberClan temp : mems) {
            if (temp.name.equals(name)) {
                return temp.mem_type;
            }
        }
        return 121;
    }

    public synchronized static int get_id_clan(Clan myclan) {
        return myclan.ID;
    }

    public synchronized static void set_clan(List<Clan> clan_list) {
        Clan.entries.addAll(clan_list);
    }

    public synchronized void setVang(long vang) {
        this.vang = vang;
    }

    public synchronized void setKimcuong(int kimcuong) {
        this.kimcuong = kimcuong;
    }
    public synchronized void accept_mem(Session conn, Player p0) throws IOException {
        for (int i = 0; i < p0.myclan.mems.size(); i++) {
            if (p0.myclan.mems.get(i).name.equals(conn.p.name)) {
                Service.send_notice_box(conn, "Đã ở trong bang rồi!");
                return;
            }
        }
        if (p0.myclan.mems.size() >= p0.myclan.max_mem) {
            Service.send_notice_box(conn, "Số lượng thành viên đã đầy!");
        } else {
            Clan temp = new Clan();
            temp.mems = new ArrayList<>();
            //
            MemberClan temp_mem = new MemberClan();
            temp_mem.name = conn.p.name;
            temp_mem.mem_type = 122;
            temp_mem.kimcuong = 0;
            temp_mem.vang = 0;
            temp_mem.head = conn.p.head;
            temp_mem.eye = conn.p.eye;
            temp_mem.hair = conn.p.hair;
            temp_mem.level = conn.p.level;
            temp_mem.itemwear = new ArrayList<>();
            for (int i = 0; i < conn.p.item.wear.length; i++) {
                if (conn.p.item.wear[i] == null || (i != 0 && i != 1 && i != 6 && i != 7 && i != 10)) {
                    continue;
                }
                Part_player temp2 = new Part_player();
                temp2.type = conn.p.item.wear[i].type;
                temp2.part = conn.p.item.wear[i].part;
                temp_mem.itemwear.add(temp2);
            }
            p0.myclan.mems.add(temp_mem);
            //
            conn.p.myclan = p0.myclan;
            MapService.update_in4_2_other_inside(conn.p.map, conn.p);
            Service.send_char_main_in4(conn.p);
            Service.send_notice_box(conn, ("Bạn đã nhập bang " + p0.myclan.name_clan));
            Service.send_notice_box(p0.conn, (conn.p.name + " gia nhập bang của bạn"));
        }
    }

    public void open_box_clan(Session conn) throws IOException {
        Message m = new Message(69);
        m.writer().writeByte(21);
        m.writer().writeByte(3);
        m.writer().writeShort(this.item_clan.size());
        for (Item47 it : this.item_clan) {
            m.writer().writeShort(it.id);
            m.writer().writeShort(it.quantity);
        }
        conn.addmsg(m);
        m.cleanup();
    }

    public synchronized void update_exp(int exp) {
        this.exp += exp;
        if (this.get_percent_level() >= 100 && this.level != 9 && this.level != 19 && this.level != 29 && this.level != 39 && this.level != 49) {
            this.exp = 0;
            this.level++;
        } else {
            this.exp = Level.entry.get(this.level - 1).exp -1;
        }
    }
    public synchronized void update_level() {
        if (this.get_percent_level() >= 100 && (this.level == 9 || this.level == 19 || this.level == 29 || this.level == 39)) {
            this.exp = 0;
            this.level++;
            this.max_mem += 15;
        }
    }
    public synchronized long get_vang() {
        return this.vang;
    }

    public synchronized int get_ngoc() {
        return this.kimcuong;
    }

    public synchronized void update_vang(long quant) {
        this.vang += quant;
    }

    public synchronized void update_ngoc(int quant) {
        this.kimcuong += quant;
    }

    public boolean check_id(short id) {
        for (Item47 it : this.item_clan) {
            if (it.id == id && it.quantity > 0) {
                return true;
            }
        }
        return false;
    }

    public synchronized void remove_all_mem() throws IOException {
        while (this.mems.size() > 1) {
            MemberClan mem = this.mems.get(1);
            this.mems.remove(mem);
            Player p0 = Map.get_player_by_name(mem.name);
            if (p0 != null) {
                p0.myclan = null;
                MapService.update_in4_2_other_inside(p0.map, p0);
                MapService.send_in4_other_char(p0.map, p0, p0);
                Service.send_char_main_in4(p0);
                Service.send_notice_box(p0.conn, p0.conn.language.giaitanbang);
            }
        }
        this.mems.clear();
    }

    public static List<Clan> get_all_clan() {
        return Clan.entries;
    }

    public static int get_mem_by_level(short level) {
        int quant = (level / 5) * 5;
        quant += 5;
        return (quant < 45) ? quant : 45;
    }

    public synchronized Mob_MoTaiNguyen get_mo_tai_nguyen(int n2) {
        for (int j = 0; j < this.mo_tai_nguyen.size(); j++) {
            if (this.mo_tai_nguyen.get(j).map.map_id == n2) {
                return this.mo_tai_nguyen.get(j);
            }
        }
        return null;
    }
    public static Clan get_clan_by_name(String name) {
        for (Clan temp : entries) {
            if (temp.name_clan.equals(name)) {
                return temp;
            }
        }
        return null;
    }
    public static Clan get_clan_by_id(int id) {
        for (Clan temp : entries) {
            if (temp.ID == id) {
                return temp;
            }
        }
        return null;
    }
    public int get_mem_type_size(int mem_type) {
        int mem_type_size = 0;
        for (int i = 0; i < mems.size(); i++) {
            if (mems.get(i).mem_type == mem_type) {
                mem_type_size++;
            }
        }
        return mem_type_size;
    }
    public boolean hasWing(int id) {
        for (int i = 0; i < item_clan.size(); i++) {
            if (id == item_clan.get(i).id) {
                return true;
            }
        }
        return false;
    }
    public static boolean isItemShop(int id) {
        for (short value : item_shop) {
            if (id == value) {
                return true;
            }
        }
        return false;
    }
    public void updateEffect() {
        time_items.removeIf(eff -> eff.time < System.currentTimeMillis());
    }
    public EffTemplate getEffect(int id) {
        for (EffTemplate eff : time_items) {
            if (id == eff.id && eff.time > System.currentTimeMillis()) {
                return eff;
            }
        }
        return null;
    }
    public void remove_item(int id) {
        for (Item47 item : item_clan) {
            if (id == item.id && item.quantity > 0) {
                item.quantity -= 1;
                if (item.quantity <= 0) {
                    item_clan.remove(item);
                }
            }
        }
    }
}
