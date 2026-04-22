package Game.activities;

import Game.client.Player;
import Game.core.Manager;
import Game.core.SaveData;
import Game.core.Service;
import Game.core.Util;
import Game.map.Map;
import Game.map.MapService;
import Game.map.Vgo;
import Game.io.Message;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KingCup implements Runnable {
    public byte id;
    public List<Player> players_attack;
    public String name1;
    public String name2;
    public int point1;
    public int point2;
    public final short X1_FIXED = 225;
    public final short X2_FIXED = 485;
    public final short Y_FIXED = 221;
    public static final long TIME_BETWEEN_MATCH = 2 * 60 * 1000; // 2 phút
    public static final long TIME_WAR = 5 * 60 * 1000; // 5 phút
    public static final long TIME_TOTAL = 10 * (TIME_WAR + TIME_BETWEEN_MATCH); // 10 trận
    public long timeWar;
    public long timeWait;
    public int countMatch;
    public Map maps;
    public boolean finish;
    public static byte idBase;
    public static ArrayList<KingCup> kingCups;
    public static long totalTime;
    public static KingCup kingCup;
    public static boolean running;
    public static int count;
    public static Thread threadKingCup;
    public static long NEXT_MATCHES;
    public KingCup(final Player p1, final Player p2) {
        this.countMatch = 0;
        this.id = ++idBase;
        this.name1 = p1.name;
        this.name2 = p2.name;
        this.point1 = p1.point_king_cup;
        this.point2 = p2.point_king_cup;
        players_attack = new ArrayList<>();
        players_attack.add(p1);
        players_attack.add(p2);
        finish = false;
        this.timeWait = System.currentTimeMillis() + 7000L;
        this.timeWar = System.currentTimeMillis() + TIME_WAR;
        Map[] temp = Map.get_map_by_id(102);
        assert temp != null;
        maps = temp[this.id];
        maps.kingCupMap = this;
    }

    public KingCup() {
        count = 0;
        idBase = -1;
        kingCups = new ArrayList<>();
        totalTime = System.currentTimeMillis() + TIME_TOTAL;
        threadKingCup = new Thread(this);
    }

    public static void start() {
        kingCup = new KingCup();
        running = true;
        threadKingCup.start();
    }

    public static void close() {
        kingCup = null;
        threadKingCup = null;
        running = false;
        try {
            Manager.gI().chatKTGprocess("Lôi đài kết thúc");
            if (KingCupManager.TURN_KING_CUP == KingCupManager.MAX_TURN) {
                SaveData.process();
                Manager.gI().chatKTGprocess("Mùa giải lôi đài kết thúc, các hiệp sĩ hãy đến Mrs.Oda để nhận quà.");
                KingCupManager.endSeason(1);
                KingCupManager.endSeason(2);
                KingCupManager.endSeason(3);
                KingCupManager.endSeason(4);
                KingCupManager.endSeason(5);
                KingCupManager.endSeason(6);
                KingCupManager.endSeason(7);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (running) {
            if (System.currentTimeMillis() < totalTime) {
                if (count < 10 && NEXT_MATCHES < System.currentTimeMillis()) {
                    ArrayList<Player> gr_65_74 = KingCupManager.setGroup(KingCupManager.group_65_74);
                    ArrayList<Player> gr_75_84 = KingCupManager.setGroup(KingCupManager.group_75_84);
                    ArrayList<Player> gr_85_94 = KingCupManager.setGroup(KingCupManager.group_85_94);
                    ArrayList<Player> gr_95_104 = KingCupManager.setGroup(KingCupManager.group_95_104);
                    ArrayList<Player> gr_105_114 = KingCupManager.setGroup(KingCupManager.group_105_114);
                    ArrayList<Player> gr_115_124 = KingCupManager.setGroup(KingCupManager.group_115_124);
                    ArrayList<Player> gr_125_139 = KingCupManager.setGroup(KingCupManager.group_125_139);

                    try {
                        randomPk(gr_65_74);
                        randomPk(gr_75_84);
                        randomPk(gr_85_94);
                        randomPk(gr_95_104);
                        randomPk(gr_105_114);
                        randomPk(gr_115_124);
                        randomPk(gr_125_139);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        count += 1;
                        idBase = -1;
                        NEXT_MATCHES = System.currentTimeMillis() + TIME_WAR + TIME_BETWEEN_MATCH;
                    }
                }
            } else {
                close();
            }
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void update() throws IOException {
        if (finish) {
            return;
        }
        final Player p1 = Map.get_player_by_name(name1);
        final Player p2 = Map.get_player_by_name(name2);
        if (p1 == null && p2 == null) {
            end_match();
        } else if (timeWar - System.currentTimeMillis() > 0) {
            if (p1 == null && p2 != null) {
                end_match();
                p2.point_king_cup += 30;
                send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                        p2.name, name1, 3, 0));
            } else if (p1 != null && p2 == null) {
                end_match();
                p1.point_king_cup += 30;
                send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                        p1.name, name2, 3, 0));
            } else if (p1 != null) {
                if (p1.countWin >= 2) {
                    p1.point_king_cup += 30;
                    end_match();
                    send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                            p1.name, p2.name, p1.countWin, p2.countWin));
                } else if (p2.countWin >= 2) {
                    p2.point_king_cup += 30;
                    end_match();
                    send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                            p2.name, p1.name, p2.countWin, p1.countWin));
                }
            }
        } else {
            if (p1 == null && p2 != null) {
                end_match();
                p2.point_king_cup += 30;
                send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                        p2.name, name1, 3, 0));
            } else if (p1 != null && p2 == null) {
                end_match();
                p1.point_king_cup += 30;
                send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                        p1.name, name2, 3, 0));
            }
            if (p1.countWin == p2.countWin) {
                end_match();
                send_notify(String.format("Trận đấu đã kết thúc, %s đã hoà %s với tỉ số %s - %s",
                        p1.name, p2.name, p1.countWin, p2.countWin));
            } else if (p1.countWin >= p2.countWin) {
                p1.point_king_cup += 30;
                end_match();
                send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                        p1.name, p2.name, p1.countWin, p2.countWin));
            } else {
                p2.point_king_cup += 30;
                end_match();
                send_notify(String.format("Trận đấu đã kết thúc, %s đã thắng %s với tỉ số %s - %s",
                        p2.name, p1.name, p2.countWin, p1.countWin));
            }
        }
    }
    public void end_round() throws IOException {
        countMatch++;
        refresh();
        timeWait = System.currentTimeMillis() + 7000L;
        sendTile();
    }
    public void end_match() throws IOException {
        finish = true;
        refresh();
        timeWait = System.currentTimeMillis() + 7000L;
        sendTile();
    }

    public void send_notify(String txt) throws IOException {
        for (Player viewer : maps.players) {
            Service.send_notice_nobox_white(viewer.conn, txt);
        }
    }

    public synchronized void refresh() throws IOException {
        for (Player p : players_attack) {
            if (p != null) {
                p.isdie = false;
                p.hp = p.body.get_HpMax();
                p.mp = p.body.get_MpMax();
                Service.send_char_main_in4(p);
                // chest in4
                Service.send_combo(p.conn);
                Service.usepotion(p, 0, p.body.get_HpMax());
                Service.usepotion(p, 1, p.body.get_MpMax());

                if (name1.equals(p.name)) {
                    p.x = X1_FIXED;
                } else {
                    p.x = X2_FIXED;
                }
                p.y = Y_FIXED;

                Message m = new Message(4);
                m.writer().writeByte(0);
                m.writer().writeShort(0);
                m.writer().writeShort(p.ID);
                m.writer().writeShort(p.x);
                m.writer().writeShort(p.y);
                m.writer().writeByte(-1);
                for (int i = 0; i < maps.players.size(); i++) {
                    Player p_map = maps.players.get(i);
                    if (p_map != null) {
                        p_map.conn.addmsg(m);
                    }
                }
                m.cleanup();
            }
        }
    }

    public synchronized void finish() throws IOException {
        if (finish) {
            if (timeWait < System.currentTimeMillis()) {
                for (int i = 0; i < maps.players.size(); i++) {
                    Player p = maps.players.get(i);
                    if (p != null) {
                        if (players_attack.contains(p)) {
                            p.goMapTapKet();
                        } else {
                            p.veLang();
                        }
                    }
                }
                if (maps.players.isEmpty()) {
                    maps.kingCupMap = null;
                    kingCups.remove(this);
                }
            }
        }
    }

    public void getMapPk(final Player p1, final Player p2) throws IOException {
        if (p1 != null && p2 != null) {
            p1.countWin = 0;
            p2.countWin = 0;
            Vgo vgo = new Vgo();
            vgo.id_map_go = 102;
            vgo.x_new = X1_FIXED;
            vgo.y_new = Y_FIXED;
            p1.typepk = 12;
            goToLD(p1, vgo, this.id);
            vgo.x_new = X2_FIXED;
            vgo.y_new = Y_FIXED;
            p2.typepk = 13;
            goToLD(p2, vgo, this.id);
        } else {
            System.out.println("Loi getMapPk");
        }
    }

    public static void randomPk(ArrayList<Player> group) throws IOException {
        if (group != null) {
            Collections.shuffle(group);
            while (group.size() > 0) {
                if (group.size() == 1) {
                    Player p = group.get(0);
                    p.point_king_cup += 30;
                    Service.send_notice_box(p.conn, "Không tìm thấy đối thủ bạn nhận được 30 điểm");
                    group.remove(0);
                    break;
                }
                int c1 = Util.random(group.size());
                final Player p1 = group.get(c1);
                group.remove(c1);
                int c2 = Util.random(group.size());
                final Player p2 = group.get(c2);
                group.remove(c2);
                KingCup ld = new KingCup(p1, p2);
                KingCup.kingCups.add(ld);
                ld.getMapPk(p1, p2);
            }
        }
    }

    public static void goToLD(Player p, Vgo vgo, int id) {
        p.is_changemap = false;
        p.x_old = vgo.x_old;
        p.y_old = vgo.y_old;
        Map map = Map.get_map_by_id(vgo.id_map_go)[id];
        MapService.leave(p.map, p);
        p.map = map;
        p.x = vgo.x_new;
        p.y = vgo.y_new;
        p.x_old = p.x;
        p.y_old = p.y;
        MapService.enter(p.map, p);
    }

    public void sendTile() throws IOException {
        short[] time = new short[]{(short) ((this.maps.kingCupMap.timeWar - System.currentTimeMillis()) / 1000), (short) ((this.maps.kingCupMap.timeWait - System.currentTimeMillis()) / 1000)};
        String title = "";
        if (countMatch >= 3) {
            title = "Thoát lôi đài sau ";
        } else {
            title = "Trận đấu sẽ bắt đầu trong ";
        }
        for (Player p : players_attack) {
            Service.send_time_box(p, (byte) 2, time, new String[]{"Lôi đài", title});
        }
    }
}
