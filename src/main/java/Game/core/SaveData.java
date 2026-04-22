package Game.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import Game.client.Clan;
import Game.client.Player;
import Game.event.*;
import Game.map.MapService;
import Game.map.Map;

import java.sql.DriverManager;

import org.json.simple.JSONObject;

public class SaveData {

    @SuppressWarnings("unchecked")
    public synchronized static void process() {
        if (Manager.isServerTest)
            return;
        long time_check = System.currentTimeMillis();
        try {
            Connection conn = DriverManager.getConnection(SQL.gI().url, Manager.gI().mysql_user, Manager.gI().mysql_pass);
            Manager.gI().chiem_mo.SaveData(conn);
            Manager.save_config_database(conn);
            // clan
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE `clan` SET `level` = ?, `exp` = ?, `slogan` = ?, `rule` = ?, `mems` = ?, `item` = ?, `notice` = ?, `vang` = ?, `kimcuong` = ?, `icon` = ?, `max_mem` = ?, `time_item` = ? WHERE `name` = ?;");
            // clan
            List<Clan> list_to_remove = new ArrayList<>();
            for (int i = 0; i < Clan.entries.size(); i++) {
                Clan clan = Clan.entries.get(i);
                if (clan.mems.isEmpty()) {
                    list_to_remove.add(clan);
                    Clan.entries.remove(clan);
                    i--;
                } else {
                    clan.updateEffect();
                    ps.clearParameters();
                    ps.setInt(1, clan.level);
                    ps.setLong(2, clan.exp);
                    ps.setNString(3, clan.slogan);
                    ps.setNString(4, clan.rule);
                    ps.setNString(5, Clan.flush_mem_json(clan.mems));
                    ps.setNString(6, Clan.flush_item_json(clan));
                    ps.setNString(7, clan.notice);
                    ps.setLong(8, clan.get_vang());
                    ps.setInt(9, clan.get_ngoc());
                    ps.setInt(10, clan.icon);
                    ps.setInt(11, clan.max_mem);
                    ps.setNString(12, Clan.flush_time_item(clan.time_items));
                    ps.setNString(13, clan.name_clan);
                    ps.executeUpdate();
                }
            }

            ps.close();
            ps = conn.prepareStatement("DELETE FROM `clan` WHERE `name` = ?;");
            for (int i = 0; i < list_to_remove.size(); i++) {
                Clan clan = list_to_remove.get(i);
                ps.clearParameters();
                ps.setNString(1, clan.name_clan);
                ps.executeUpdate();
            }
            ps.close();
            // flush player
            String query = "UPDATE `player` SET `level` = ?  WHERE `id` = ?;";
            ps = conn.prepareStatement(query);
            for (Map[] map : Map.entrys) {
                if (map == null) continue;
                for (Map map0 : map) {
                    if (map0 == null || map0.players == null) continue;
                    for (int i1 = 0; i1 < map0.players.size(); i1++) {
                        try {
                            ps.clearParameters();
                            Player p0 = map0.players.get(i1);
                            if (p0.conn == null || p0.conn.socket == null || p0.conn.socket.isClosed() || !p0.conn.connected) {
                                MapService.leave(map0, p0);
                                continue;
                            }
                            p0.flush();
                        } catch (Exception ee) {
                            Log.gI().add_Log_Server("save_data", ee.getMessage());
                            ee.printStackTrace();
                        }
                    }
                }
            }

            ps.close();
            if (Manager.gI().event == 0) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("register", EventManager.save(EventManager.registerList));
                jsonObject.put("receive", EventManager.save(LunarNewYear.list_nhan_banh));
                ps = conn.prepareStatement("UPDATE `event` SET `data` = ? WHERE `id` = ?;");
                ps.clearParameters();
                //
                ps.setNString(1, jsonObject.toJSONString());
                ps.setInt(2, 0);
                ps.executeUpdate();
                ps.close();
            }
            // event
            if (Manager.gI().event == 1) {
                ps = conn.prepareStatement("UPDATE `event` SET `data` = ? WHERE `id` = ?;");
                ps.clearParameters();
                //
                ps.setNString(1, Noel.SaveData().toJSONString());
                ps.setInt(2, 1);
                ps.executeUpdate();
                ps.close();
            } else if (Manager.gI().event == 2) {
                ps = conn.prepareStatement("UPDATE `event` SET `data` = ? WHERE `id` = ?;");
                ps.clearParameters();
                //
                ps.setNString(1, Event_2.SaveData().toJSONString());
                ps.setInt(2, 2);
                ps.executeUpdate();
                ps.close();
            } else if (Manager.gI().event == 3) {
                ps = conn.prepareStatement("UPDATE `event` SET `data` = ? WHERE `id` = ?;");
                ps.clearParameters();
                //
                ps.setNString(1, Event_3.SaveData().toJSONString());
                ps.setInt(2, 3);
                ps.executeUpdate();
                ps.close();
            }
            // bxh
            Rank.init(conn);
            //
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[" + Util.get_now_by_time() + "] save data fail!");
            return;
        }
        ServerManager.gI().time_l = System.currentTimeMillis() + 60_000L;
    }
}
