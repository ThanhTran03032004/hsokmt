package Game.core;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Game.io.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import Game.client.Clan;
import Game.client.Player;
import Game.io.Message;
import Game.map.Map;
import Game.template.Item3;
import Game.template.Part_player;
import Game.template.Level;

public class Rank {

    public static final List<PlayerInfo> top_activity = new ArrayList<>();
    public static final List<PlayerInfo> top_arena = new ArrayList<>();
    public static final List<PlayerInfo> cay = new ArrayList<>();
    public static final List<ClanInfo> top_level_clan = new ArrayList<>();
    public static final List<ClanInfo> top_gold_clan = new ArrayList<>();
    public static final List<ClanInfo> top_gems_clan = new ArrayList<>();
    public static final List<PlayerInfo> top_z6 = new ArrayList<>();

    public static void send(Session conn, int b) {
        switch (b) {
            case 0: {
                Rank.sendTopPlayer(conn, top_activity, "Top Danh Vọng");
                break;
            }
            case 1: {
                Rank.sendTopClan(conn, top_level_clan, "Bang hùng mạnh nhất");
                break;
            }
            case 2: {
                Rank.sendTopClan(conn, top_gold_clan, "Bang giàu có nhất");
                break;
            }
            case 3: {
                Rank.sendTopClan(conn, top_gems_clan, "Bang nhiều châu báu nhất");
                break;
            }
            case 4: {
                Rank.sendTopPlayer(conn, top_arena, "Top Chiến Trường");
                break;
            }
            case 5: {
                Rank.sendTopPlayer(conn, top_z6, "Top Thương Nhân");
                break;
            }
            case 6: {
                Rank.sendTopPlayer(conn, cay, "Top cao thủ");
                break;
            }
        }
    }

    public static void init(Connection conn) {
        try {
            Rank.top_activity.clear();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear`, `point_activity` FROM `player` WHERE `point_activity` >= 0 ORDER BY `point_activity` DESC;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                temp.point_activity = rs.getLong("point_activity");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();
                for (Object o : jsar) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    byte index_wear = Byte.parseByte(jsar2.get(9).toString());
                    if (index_wear != 0 && index_wear != 1 && index_wear != 6 && index_wear != 7 && index_wear != 10) {
                        continue;
                    }
                    Part_player temp2 = new Part_player();
                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());
                    temp.item_wear.add(temp2);
                }
                temp.clan = Clan.get_clan_of_player(temp.name);
                temp.info = "Điểm danh vọng: " + Util.number_format(temp.point_activity);
                Rank.top_activity.add(temp);
            }
            rs.close();
            Rank.cay.clear();
            rs = ps.executeQuery(
                    "SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear` FROM `player` WHERE `level` > 10 ORDER BY `level` DESC, exp DESC LIMIT 20;");
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();
                for (Object o : jsar) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    byte index_wear = Byte.parseByte(jsar2.get(9).toString());
                    if (index_wear != 0 && index_wear != 1 && index_wear != 6 && index_wear != 7 && index_wear != 10) {
                        continue;
                    }
                    Part_player temp2 = new Part_player();
                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());
                    temp.item_wear.add(temp2);
                }
                temp.clan = Clan.get_clan_of_player(temp.name);
            String percent
                        = String.format("%.1f", (((float) temp.exp * 1000) / Level.entry.get(temp.level - 1).exp) / 10f);
                temp.info = "Level : " + (temp.level) + "\t-\t" + percent + "%";
                Rank.cay.add(temp);
            }
            rs.close();
            Rank.top_level_clan.clear();
            rs = ps.executeQuery("SELECT `id`, `name`, `icon`, `name_short` FROM `clan` WHERE `level` >= 0 ORDER BY `level` DESC;");
            while (rs.next()) {
                ClanInfo temp = new ClanInfo();
                temp.idClan = rs.getShort("id");
                temp.name = rs.getString("name");
                temp.shortName = rs.getString("name_short");
                temp.idIcon = rs.getShort("icon");
                Clan clan = Clan.get_clan_by_name(temp.name);
                if (clan != null) {
                    temp.info = "Cấp độ: " + clan.level + "+" + clan.get_percent_level() / 10 + "% - " + clan.mems.size() + "/" + clan.max_mem + " thành viên";
                }
                Rank.top_level_clan.add(temp);
            }
            rs.close();

            Rank.top_gold_clan.clear();
            rs = ps.executeQuery("SELECT `id`, `name`, `icon`, `name_short` FROM `clan` WHERE `level` >= 0 ORDER BY `vang` DESC;");
            while (rs.next()) {
                ClanInfo temp = new ClanInfo();
                temp.idClan = rs.getShort("id");
                temp.name = rs.getString("name");
                temp.shortName = rs.getString("name_short");
                temp.idIcon = rs.getShort("icon");
                Clan clan = Clan.get_clan_by_name(temp.name);
                if (clan != null) {
                    temp.info = "Vàng: " + Util.number_format(clan.get_vang()) + " - " + clan.mems.size() + "/" + clan.max_mem + " thành viên";
                }
                Rank.top_gold_clan.add(temp);
            }
            rs.close();

            Rank.top_gems_clan.clear();
            rs = ps.executeQuery("SELECT `id`, `name`, `icon`, `name_short` FROM `clan` WHERE `level` >= 0 ORDER BY `kimcuong` DESC;");
            while (rs.next()) {
                ClanInfo temp = new ClanInfo();
                temp.idClan = rs.getShort("id");
                temp.name = rs.getString("name");
                temp.shortName = rs.getString("name_short");
                temp.idIcon = rs.getShort("icon");
                Clan clan = Clan.get_clan_by_name(temp.name);
                if (clan != null) {
                    temp.info = "Ngọc: " + Util.number_format(clan.get_ngoc()) + " - " + clan.mems.size() + "/" + clan.max_mem + " thành viên";
                }
                Rank.top_gems_clan.add(temp);
            }
            rs.close();

            Rank.top_arena.clear();
            rs = ps.executeQuery("SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear`, `point_arena` FROM `player` WHERE `point_arena` >= 0 ORDER BY `point_arena` DESC;");
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                temp.point_arena = rs.getLong("point_arena");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();
                for (Object o : jsar) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    byte index_wear = Byte.parseByte(jsar2.get(9).toString());
                    if (index_wear != 0 && index_wear != 1 && index_wear != 6 && index_wear != 7 && index_wear != 10) {
                        continue;
                    }
                    Part_player temp2 = new Part_player();
                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());
                    temp.item_wear.add(temp2);
                }
                temp.clan = Clan.get_clan_of_player(temp.name);
                temp.info = "Điểm: " + Util.number_format(temp.point_arena / 10);
                Rank.top_arena.add(temp);
            }
            rs.close();

            Rank.top_z6.clear();
            rs = ps.executeQuery("SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear`, `point_z6` FROM `player` WHERE `point_z6` >= 0 ORDER BY `point_z6` DESC;");
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                temp.point_z6 = rs.getLong("point_z6");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();
                for (Object o : jsar) {
                    JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    byte index_wear = Byte.parseByte(jsar2.get(9).toString());
                    if (index_wear != 0 && index_wear != 1 && index_wear != 6 && index_wear != 7 && index_wear != 10) {
                        continue;
                    }
                    Part_player temp2 = new Part_player();
                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());
                    temp.item_wear.add(temp2);
                }
                temp.clan = Clan.get_clan_of_player(temp.name);
                temp.info = "Điểm: " + Util.number_format(temp.point_arena);
                Rank.top_z6.add(temp);
            }
            rs.close();
            ps.close();
//            System.out.println("Load Rank Completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void chatKTGLogin(Player p) throws IOException {
        if (!top_activity.isEmpty() && p.name.equals(top_activity.get(0).name)) {
            Manager.gI().chatKTGprocess("Chào mừng TOP 1 danh vọng " + p.name.toUpperCase() + " đăng nhập vào game");
        } else if (!cay.isEmpty() &&  p.name.equals(cay.get(0).name)) {
            Manager.gI().chatKTGprocess("Chào mừng TOP 1 cấp độ " + p.name.toUpperCase() + " đăng nhập vào game");
        } else if (!top_arena.isEmpty() && p.name.equals(top_arena.get(0).name)) {
            Manager.gI().chatKTGprocess("Chào mừng TOP 1 chiến trường " + p.name.toUpperCase() + " đăng nhập vào game");
        } else if (!top_z6.isEmpty() && p.name.equals(top_z6.get(0).name)) {
            Manager.gI().chatKTGprocess("Chào mừng TOP 1 khu 6 " + p.name.toUpperCase() + " đăng nhập vào game");
        }
    }
    
    public static void sendTopClan(Session conn, List<ClanInfo> list, String rank_name) {
        try {
            Message m = new Message(56);
            m.writer().writeByte(3);
            m.writer().writeUTF(rank_name);
            m.writer().writeByte(99); // page
            int my_index = -1;
            if (conn.p.myclan != null) {
                my_index = Rank.clan_index(conn.p.myclan, list);
            }
            m.writer().writeInt(my_index - 1); // my index in bxh
            int size = list.size();
            if (size > 20) {
                size = 20;
            }
            if (my_index > size) {
                size += 1;
            }
            m.writer().writeByte(size); // num2
            for (int i = 0; i < size; i++) {
                if (i >= 20) continue;
                ClanInfo clan = list.get(i);
                m.writer().writeUTF(clan.name);
                m.writer().writeInt(clan.idClan);
                m.writer().writeShort(clan.idIcon);
                m.writer().writeUTF(clan.shortName);
                m.writer().writeUTF(clan.info);
            }
            if (size > 20) {
                ClanInfo cif = ClanInfo.my_clan(conn, list);
                m.writer().writeUTF(cif.name);
                m.writer().writeInt(cif.idClan);
                m.writer().writeShort(cif.idIcon);
                m.writer().writeUTF(cif.shortName);
                m.writer().writeUTF(cif.info);
            }
            conn.addmsg(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTopPlayer(Session conn, List<PlayerInfo> list, String rank_name) {
        try {
            Message m = new Message(56);
            m.writer().writeByte(1);
            m.writer().writeUTF(rank_name);
            m.writer().writeByte(99); // page
            int my_index = PlayerInfo.my_index(conn, list);
            m.writer().writeInt(my_index); // my index in bxh
            int size = list.size();
            if (size > 20) {
                size = 20;
            }
            if (my_index > size) {
                size += 1;
            }
            m.writer().writeByte(size); // num2
            for (int i = 0; i < size; i++) {
                if (i >= 20) continue;
                PlayerInfo temp = list.get(i);
                Player p0 = Map.get_player_by_name(temp.name);
                if (p0 != null) {
                    temp.head = p0.head;
                    temp.eye = p0.eye;
                    temp.hair = p0.hair;
                    temp.level = p0.level;
                    temp.item_wear.clear();
                    for (int i1 = 0; i1 < p0.item.wear.length; i1++) {
                        Item3 it = p0.item.wear[i1];
                        if (it != null && (i1 == 0 || i1 == 1 || i1 == 6 || i1 == 7 || i1 == 10)) {
                            Part_player part = new Part_player();
                            part.type = it.type;
                            part.part = it.part;
                            temp.item_wear.add(part);
                        }
                    }
                    temp.clan = p0.myclan;
                }
                m.writer().writeUTF(temp.name);
                m.writer().writeByte(temp.head);
                m.writer().writeByte(temp.eye);
                m.writer().writeByte(temp.hair);
                m.writer().writeShort(temp.level);
                m.writer().writeByte(temp.item_wear.size());
                for (Part_player it : temp.item_wear) {
                    m.writer().writeByte(it.part);
                    m.writer().writeByte(it.type);
                }
                m.writer().writeByte((p0 != null) ? (byte) 1 : (byte) 0); // type online
                m.writer().writeUTF(temp.info);
                if (temp.clan != null) {
                    m.writer().writeShort(temp.clan.icon);
                    m.writer().writeUTF(temp.clan.name_clan_shorted);
                    m.writer().writeByte(temp.clan.get_mem_type(temp.name));
                } else {
                    m.writer().writeShort(-1);
                }
            }
            if (size > 20) {
                PlayerInfo temp = list.get(my_index);
                Player p0 = Map.get_player_by_name(temp.name);
                if (p0 != null) {
                    temp.head = p0.head;
                    temp.eye = p0.eye;
                    temp.hair = p0.hair;
                    temp.level = p0.level;
                    temp.item_wear.clear();
                    for (int i1 = 0; i1 < p0.item.wear.length; i1++) {
                        Item3 it = p0.item.wear[i1];
                        if (it != null && (i1 == 0 || i1 == 1 || i1 == 6 || i1 == 7 || i1 == 10)) {
                            Part_player part = new Part_player();
                            part.type = it.type;
                            part.part = it.part;
                            temp.item_wear.add(part);
                        }
                    }
                    temp.clan = p0.myclan;
                }
                m.writer().writeUTF(temp.name);
                m.writer().writeByte(temp.head);
                m.writer().writeByte(temp.eye);
                m.writer().writeByte(temp.hair);
                m.writer().writeShort(temp.level);
                m.writer().writeByte(temp.item_wear.size());
                for (Part_player it : temp.item_wear) {
                    m.writer().writeByte(it.part);
                    m.writer().writeByte(it.type);
                }
                m.writer().writeByte((p0 != null) ? (byte) 1 : (byte) 0); // type online
                m.writer().writeUTF(temp.info);
                if (temp.clan != null) {
                    m.writer().writeShort(temp.clan.icon);
                    m.writer().writeUTF(temp.clan.name_clan_shorted);
                    m.writer().writeByte(temp.clan.get_mem_type(temp.name));
                } else {
                    m.writer().writeShort(-1);
                }
            }
            conn.addmsg(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int clan_index(Clan clan, List<ClanInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            if (clan.ID == list.get(i).idClan) {
                return i;
            }
        }
        return -1;
    }

    public static class PlayerInfo {
        public short level;
        public long exp;
        public String name;
        public long point_activity;
        public long point_z6;
        public long point_arena;
        public byte head;
        public byte eye;
        public byte hair;
        public List<Part_player> item_wear;
        public Clan clan;
        public String info;
        public short id;

        public static int my_index(Session conn, List<PlayerInfo> list) {
            for (int i = 0; i < list.size(); i++) {
                if (conn.p.name.equals(list.get(i).name)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static class ClanInfo {
        public String name;
        public int idClan;
        public short idIcon;
        public String shortName;
        public String info;

        public static ClanInfo my_clan(Session conn, List<ClanInfo> list) {
            for (ClanInfo clanInfo : list) {
                if (conn.p.myclan != null && conn.p.myclan.ID == clanInfo.idClan) {
                    return clanInfo;
                }
            }
            return null;
        }
    }
}
