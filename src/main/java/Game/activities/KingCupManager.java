package Game.activities;

import Game.client.Player;
import Game.core.Service;
import Game.core.Util;
import Game.map.Map;
import Game.template.*;
import Game.core.SQL;
import Game.io.Message;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KingCupManager {
    public static int TURN_KING_CUP = -1;
    public static int MAX_TURN = 7;
    public static int DAY_OFF = 1;
    public static List<String> group_65_74 = new ArrayList<>();
    public static List<String> group_75_84 = new ArrayList<>();
    public static List<String> group_85_94 = new ArrayList<>();
    public static List<String> group_95_104 = new ArrayList<>();
    public static List<String> group_105_114 = new ArrayList<>();
    public static List<String> group_115_124 = new ArrayList<>();
    public static List<String> group_125_139 = new ArrayList<>();
    public static List<String> list_name = new ArrayList<>();

    public static void register(final Player p) throws IOException {
        int level = p.level;
        if (level > 64) {
            if (p.get_ngoc() < 1000) {
                Service.send_notice_box(p.conn, "Bạn không đủ 1000 ngọc");
                return;
            }
            if (level <= 74) {
                group_65_74.add(p.name);
                updateData(group_65_74, "group_65_74");
                p.group_king_cup = 1;
            } else if (level <= 84) {
                group_75_84.add(p.name);
                updateData(group_75_84, "group_75_84");
                p.group_king_cup = 2;
            } else if (level <= 94) {
                group_85_94.add(p.name);
                updateData(group_85_94, "group_85_94");
                p.group_king_cup = 3;
            } else if (level <= 104) {
                group_95_104.add(p.name);
                updateData(group_95_104, "group_95_104");
                p.group_king_cup = 4;
            } else if (level <= 114) {
                group_105_114.add(p.name);
                updateData(group_105_114, "group_105_114");
                p.group_king_cup = 5;
            } else if (level <= 124) {
                group_115_124.add(p.name);
                updateData(group_115_124, "group_115_124");
                p.group_king_cup = 6;
            } else if (level > 125) {
                group_125_139.add(p.name);
                updateData(group_125_139, "group_125_139");
                p.group_king_cup = 7;
            }
            KingCupManager.list_name.add(p.name);
            p.point_king_cup = 0;
            p.update_ngoc(-1000);
            Service.send_notice_box(p.conn, "Đã đăng ký thành công");
        } else {
            Service.send_notice_box(p.conn, "Yêu cầu cấp độ tối thiểu 65 mới có thể đăng ký");
        }
    }

    public static ArrayList setGroup(List<String> group) {
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 0; i < group.size(); i++) {
            Player p = Map.get_player_by_name(group.get(i));
            if (p != null && p.map.map_id == 100) {
                if (!p.isKnight() && !p.isTrader() && !p.isRobber() && !p.isSonTinh() && !p.isThuyTinh()) {
                    players.add(p);
                }
            }
        }
        return players;
    }

    public static void updateData(List<String> group, String column) {
        try (
                Connection connection = SQL.gI().getConnection();
                PreparedStatement ps = connection.prepareStatement("UPDATE `king_cup` SET `" + column + "` = ?;")) {

            JSONArray array = new JSONArray();
            for (String name : group) {
                array.add(name);
            }

            String jsonArrayString = array.toJSONString();
            ps.setString(1, jsonArrayString);

            if (ps.executeUpdate() > 0) {
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTurn() {
        try (
                Connection connection = SQL.gI().getConnection();
                PreparedStatement ps = connection.prepareStatement("UPDATE `king_cup` SET `turn_king_cup` = ?;")) {

            ps.setInt(1, KingCupManager.TURN_KING_CUP);

            if (ps.executeUpdate() > 0) {
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void endSeason(int group) throws SQLException {
        try (Connection conn = SQL.gI().getConnection();
             PreparedStatement psSelect = conn.prepareStatement("SELECT `name`,`point_king_cup` FROM `player` WHERE `point_king_cup` > 0 AND `group_king_cup` = ? ORDER BY `point_king_cup` DESC;");
             PreparedStatement psUpdate = conn.prepareStatement("UPDATE `player` SET `type_reward_king_cup` = ? WHERE `name` = ?;")) {

            psSelect.setInt(1, group);

            try (ResultSet rs = psSelect.executeQuery()) {
                short i = 0;
                while (rs.next()) {
                    String name = rs.getString("name");
                    short type = (short) (i + 4 * (group - 1) + 1);
                    psUpdate.setShort(1, type);
                    psUpdate.setString(2, name);
                    Player player = Map.get_player_by_name(name);
                    if (player != null) {
                        player.type_reward_king_cup = (byte) type;
                    }
                    psUpdate.addBatch();
                    i++;
                }
            } catch (SQLException ee) {
                ee.printStackTrace();
            }
            psUpdate.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (group == 1) {
            group_65_74.clear();
            updateData(group_65_74, "group_65_74");
        } else if (group == 2) {
            group_75_84.clear();
            updateData(group_75_84, "group_75_84");
        } else if (group == 3) {
            group_85_94.clear();
            updateData(group_85_94, "group_85_94");
        } else if (group == 4) {
            group_95_104.clear();
            updateData(group_95_104, "group_95_104");
        } else if (group == 5) {
            group_105_114.clear();
            updateData(group_105_114, "group_105_114");
        } else if (group == 6) {
            group_115_124.clear();
            updateData(group_115_124, "group_115_124");
        } else if (group == 7) {
            group_125_139.clear();
            updateData(group_125_139, "group_125_139");
        }
    }

    public static void rewardKingCup(Player p) throws IOException {
        if (p.type_reward_king_cup == 0 || p.point_king_cup <= 0) {
            Service.send_notice_box(p.conn, "Bạn không có trong danh sách hoặc đã nhận rồi.");
            return;
        }

        short[] id_reward_7;
        short[] quantity_reward_7;
        short[] id_reward_4;
        short[] quantity_reward_4;
        boolean isHaveBook = false;

        switch (p.type_reward_king_cup) {
            case 1, 5, 9, 13 -> {
                id_reward_7 = new short[]{14, (short) Util.random(8, 10), 11, 349};
                quantity_reward_7 = new short[]{3, 10, 10, 1};
                id_reward_4 = new short[]{53, 54};
                quantity_reward_4 = new short[]{1, 1};
            }
            case 17, 21, 25 -> {
                id_reward_7 = new short[]{14, (short) Util.random(8, 10), 11, 349};
                quantity_reward_7 = new short[]{3, 10, 10, 1};
                id_reward_4 = new short[]{10, 53, 54};
                quantity_reward_4 = new short[]{1, 1, 1};
                isHaveBook = true;
            }
            case 2, 6, 10, 14 -> {
                id_reward_7 = new short[]{(short) Util.random(8, 10), 11};
                quantity_reward_7 = new short[]{5, 5};
                id_reward_4 = new short[]{53, 54};
                quantity_reward_4 = new short[]{1, 1};
            }
            case 18, 22, 26 -> {
                id_reward_7 = new short[]{14};
                quantity_reward_7 = new short[]{2};
                id_reward_4 = new short[]{10, 53, 54};
                quantity_reward_4 = new short[]{1, 1, 1};
            }
            case 3, 7, 11, 15 -> {
                id_reward_7 = new short[]{(short) Util.random(8, 10), 11};
                quantity_reward_7 = new short[]{3, 3};
                id_reward_4 = new short[]{53};
                quantity_reward_4 = new short[]{1};
            }
            case 19, 23, 27 -> {
                id_reward_7 = new short[]{14};
                quantity_reward_7 = new short[]{1};
                id_reward_4 = new short[]{10, 53, 54};
                quantity_reward_4 = new short[]{1, 1, 1};
            }
            default -> {
                return;
            }
        }
        if (p.item.get_inventory_able() < (id_reward_7.length + id_reward_4.length + 1)) {
            Service.send_notice_nobox_white(p.conn, "Hành trang đầy!");
            return;
        }

        int gold = calculateGold(p.point_king_cup);

        Message m = new Message(78);
        m.writer().writeUTF("Bạn nhận được");
        if (isHaveBook) {
            m.writer().writeByte(id_reward_7.length + id_reward_4.length + 2);
            addBookSkill(m, p, 1);
        } else {
            m.writer().writeByte(id_reward_7.length + id_reward_4.length + 1);
        }
        writeRewardsToMessage(p, m, id_reward_7, quantity_reward_7, (byte) 7);
        writeRewardsToMessage(p, m, id_reward_4, quantity_reward_4, (byte) 4);

        m.writer().writeUTF("vàng");
        m.writer().writeShort(0);
        m.writer().writeInt(gold);
        m.writer().writeByte(4);
        m.writer().writeByte(0);
        m.writer().writeByte(0);

        m.writer().writeUTF("");
        m.writer().writeByte(1);
        m.writer().writeByte(0);
        p.conn.addmsg(m);
        m.cleanup();

        p.update_vang(gold, "Nhận %s vàng từ quà lôi đài");
        p.point_king_cup = 0;
        p.group_king_cup = -1;
        p.type_reward_king_cup = 0;
    }

    private static void writeRewardsToMessage(Player p, Message m, short[] ids, short[] quantities, byte type) throws IOException {
        for (int i = 0; i < ids.length; i++) {
            if (type == 7) {
                ItemTemplate7 item = ItemTemplate7.item.get(ids[i]);
                m.writer().writeUTF(item.getName());
                m.writer().writeShort(item.getIcon());

                Item47 item47 = new Item47();
                item47.id = item.getId();
                item47.quantity = quantities[i];
                p.item.add_item_inventory47(7, item47);
            } else if (type == 4) {
                ItemTemplate4 item = ItemTemplate4.item.get(ids[i]);
                m.writer().writeUTF(item.getName());
                m.writer().writeShort(item.getIcon());

                Item47 item47 = new Item47();
                item47.id = item.getId();
                item47.quantity = quantities[i];
                p.item.add_item_inventory47(4, item47);
            }
            m.writer().writeInt(quantities[i]);
            m.writer().writeByte(type);
            m.writer().writeByte(0); // tier
            m.writer().writeByte(0); // color
        }
    }

    private static void addBookSkill(Message m, Player p, int quantity) throws IOException {
        for (int i = 0; i < quantity; i++) {
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
            p.item.add_item_inventory3(it);

            m.writer().writeUTF(temp3.getName());
            m.writer().writeShort(temp3.getIcon());
            m.writer().writeInt(1);
            m.writer().writeByte(3);
            m.writer().writeByte(0);
            m.writer().writeByte(4);
        }
    }

    private static int calculateGold(int point) {
        if (point >= 2520) {
            return point * 5000;
        } else if (point >= 1680) {
            return point * 4000;
        } else if (point >= 1260) {
            return point * 1000;
        } else if (point >= 840) {
            return point * 800;
        } else {
            return point * 500;
        }
    }
}
