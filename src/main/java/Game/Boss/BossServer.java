
package Game.Boss;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import Game.client.Player;
import Game.core.*;
import Game.io.Session;
import Game.map.Map;
import Game.io.Message;
import Game.map.Mob_in_map;


public class BossServer {
    public int idMap;
    public String bossName;
    public long time_refresh_z1;
    public long time_refresh_z2;
    public long time_refresh_z3;
    public long time_refresh_z4;
    public long time_refresh_z5;

    public static void setTimeRefresh(int idMap, int zone, long timeRefresh) {
        for (BossServer boss : Manager.gI().listBossServers) {
            if (boss.idMap == idMap) {
                boss.setTimeRefresh(zone, timeRefresh);
                try (Connection conn = SQL.gI().getConnection(); Statement statement = conn.createStatement()) {
                    if (statement.executeUpdate(
                            "UPDATE `time_boss` SET `zone_" + (zone + 1) + "` = '" + timeRefresh + "' WHERE `map_id` = " + idMap + ";") > 0) {
                        conn.commit();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isRefresh(int zone) {
        if (zone == 0) {
            return time_refresh_z1 < System.currentTimeMillis();
        } else if (zone == 1) {
            return time_refresh_z2 < System.currentTimeMillis();
        } else if (zone == 2) {
            return time_refresh_z3 < System.currentTimeMillis();
        } else if (zone == 3) {
            return time_refresh_z4 < System.currentTimeMillis();
        } else if (zone == 4) {
            return time_refresh_z5 < System.currentTimeMillis();
        }
        return false;
    }

    public void setTimeRefresh(int zone, long timeRefresh) {
        if (zone == 0) {
            time_refresh_z1 = timeRefresh;
        } else if (zone == 1) {
            time_refresh_z2 = timeRefresh;
        } else if (zone == 2) {
            time_refresh_z3 = timeRefresh;
        } else if (zone == 3) {
            time_refresh_z4 = timeRefresh;
        } else if (zone == 4) {
            time_refresh_z5 = timeRefresh;
        }
    }
    public long getTimeRefreshByZone(int zone) {
        if (zone == 0) {
            return time_refresh_z1;
        } else if (zone == 1) {
            return time_refresh_z2;
        } else if (zone == 2) {
            return time_refresh_z3;
        } else if (zone == 3) {
            return time_refresh_z4;
        } else if (zone == 4) {
            return time_refresh_z5;
        }
        return -1;
    }
    public static void refresh_boss(int idMap, int zone, Mob_in_map mob) throws IOException {
        boolean isRefresh = false;
        for (BossServer boss : Manager.gI().listBossServers) {
            if (boss.idMap == idMap && boss.isRefresh(zone)) {
                isRefresh = true;
                break;
            }
        }
        if (!isRefresh) {
            return;
        }
        Map map = Map.get_map_by_id(idMap)[zone];
        if (map == null || mob == null) {
            return;
        }
        if (mob.template.isBossEvent()) {
            switch (zone) {
                case 0 -> {
                    mob.level = 84;
                    mob.count_meterial = 500;
                }
                case 2 -> {
                    mob.level = 104;
                    mob.hp_max *= 2;
                    mob.count_meterial = 500;
                }
                case 3 -> {
                    mob.level = 124;
                    mob.hp_max *= 3;
                    mob.count_meterial = 500;
                }
                default -> {
                    return;
                }
            }
        } else {
            mob.count_meterial = 30;
        }

        for (Map[] maps : Map.entrys) {
            for (Map map0 : maps) {
                for (int i = 0; i < map0.players.size(); i++) {
                    Session conn = map0.players.get(i).conn;
                    Service.send_notice_nobox_yellow(conn, mob.template.name + conn.language.xuathienboss);
                }
            }
        }
        Log.gI().add_Log_Server("BossEvent", mob.template.name + " hồi sinh.");
        mob.isdie = false;
        mob.list_fight.clear();
        mob.hp = mob.hp_max;
        mob.color_name = 3;
        mob.is_boss_active = true;
        mob.top_dame.clear();
        for (int j = 0; j < map.players.size(); j++) {
            Player pp = map.players.get(j);
            if (pp != null) {
                if (!pp.other_mob_inside.containsKey(mob.ID)) {
                    pp.other_mob_inside.put(mob.ID, true);
                }
                if (pp.other_mob_inside.get(mob.ID)) {
                    Message mm = new Message(4);
                    mm.writer().writeByte(1);
                    mm.writer().writeShort(mob.template.mob_id);
                    mm.writer().writeShort(mob.ID);
                    mm.writer().writeShort(mob.x);
                    mm.writer().writeShort(mob.y);
                    mm.writer().writeByte(-1);
                    pp.conn.addmsg(mm);
                    mm.cleanup();
                    pp.other_mob_inside.replace(mob.ID, true, false);
                } else {
                    Service.mob_in4(pp, mob.ID);
                }
            }
        }
    }
    public static void refresh_boss_battlefield() throws IOException {
        Map[] maps = Map.get_map_by_id(61);
        for (Map map : maps) {
            if (map.zone_id >= map.maxzone) continue;
            if (map.mobs == null) {return;}
            Mob_in_map mob = map.mobs[0];
            mob.isdie = false;
            mob.list_fight.clear();
            mob.hp = mob.hp_max;
            mob.color_name = 5;
            mob.is_boss_active = true;
            mob.count_meterial = 100;
            for (int j = 0; j < map.players.size(); j++) {
                Player pp = map.players.get(j);
                if (pp != null) {
                    if (!pp.other_mob_inside.containsKey(mob.ID)) {
                        pp.other_mob_inside.put(mob.ID, true);
                    }
                    if (pp.other_mob_inside.get(mob.ID)) {
                        Message mm = new Message(4);
                        mm.writer().writeByte(1);
                        mm.writer().writeShort(mob.template.mob_id);
                        mm.writer().writeShort(mob.ID);
                        mm.writer().writeShort(mob.x);
                        mm.writer().writeShort(mob.y);
                        mm.writer().writeByte(-1);
                        pp.conn.addmsg(mm);
                        mm.cleanup();
                        pp.other_mob_inside.replace(mob.ID, true, false);
                    } else {
                        Service.mob_in4(pp, mob.ID);
                    }
                }
            }
        }
    }
    public static String[] listBossActive() {
        ArrayList<String> list_name = new ArrayList<>();
        for (BossServer boss : Manager.gI().listBossServers) {
            if (!boss.isRefresh(0) || !boss.isRefresh(1) || !boss.isRefresh(2)
                    || !boss.isRefresh(3) || !boss.isRefresh(4)) {
                list_name.add(boss.bossName);
            }
        }
        return list_name.toArray(new String[0]);
    }

    public static String[] sendInfo(int index) {
        String[] list_boss = listBossActive();
        ArrayList<String> list_name = new ArrayList<>();
        for (BossServer boss : Manager.gI().listBossServers) {
            if (boss.bossName.equals(list_boss[index])) {
                Map map = Map.get_map_by_id(boss.idMap)[0];
                for (int i = 0; i < 5; i++) {
                    if (!boss.isRefresh(i)) {
                        list_name.add(boss.bossName + ": " + Util.format_time(boss.getTimeRefreshByZone(i)) + ". "
                                + map.name + " K " + (i + 1));
                    }
                }
            }
        }
        return list_name.toArray(new String[0]);
    }
}
