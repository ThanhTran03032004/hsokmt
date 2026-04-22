//package Game.gamble;
//
//import Game.client.Player;
//import Game.core.Log;
//import Game.core.SQL;
//import Game.core.Service;
//import Game.core.Util;
//import Game.io.Message;
//import Game.map.Map;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.io.IOException;
//import java.util.List;
//
///**
// * @author Administrator
// */
//public class KMT implements Runnable {
//
//    public static final byte TAI_WIN = 0;
//    public static final byte XIU_WIN = 1;
//    public static final byte NORMAL = -1;
//
//    public Thread mainThread;
//    public List<PlayerTX> playerTai;
//    public List<PlayerTX> playerXiu;
//    public List<String> soicau;
//    public int time;
//    public boolean running;
//    public boolean isLock;
//    public byte typeWin;
//    public String result;
//
//    public KMT() {
//        playerTai = new ArrayList<>();
//        playerXiu = new ArrayList<>();
//        soicau = new ArrayList<>();
//        running = true;
//        time = 10;
//        result = "";
//        typeWin = NORMAL;
//        mainThread = new Thread(this);
//        mainThread.start();
//    }
//
//    @Override
//    public void run() {
//        long time1 = 0;
//        long time2 = 0;
//        while (running) {
//            try {
//                time1 = System.currentTimeMillis();
//                update();
//                time2 = System.currentTimeMillis();
//                long time3 = (1000L - (time2 - time1));
//                if (time3 > 0) {
//                    Thread.sleep(time3);
//                }
//            } catch (InterruptedException e) {
//            }
//        }
//    }
//
//    public void message(Player p) throws IOException {
//        Message m = new Message(-32);
//        m.writer().writeShort(3);
//        m.writer().writeByte(86);
//        String text = "Tài Xỉu\r\n" + "Thời gian \uD83C\uDFB2: " + get_time() + "\r\n"
//                + "Tài" + "            " + "Xỉu" + "\r\n"
//                + "Tổng: " + Util.number_format(totalTai()) + "      " + "Tổng: " + Util.number_format(totalXiu()) + "\r\n"
//                + "Bạn: " + Util.number_format(myJoin(playerTai, p)) + "      " + "Bạn: " + Util.number_format(myJoin(playerXiu, p)) + "\r\n"
//                + "Kết quả ván trước : " + result
//                + "\n" + "\n"
//                + "Cầu : " + soicau();
//        m.writer().writeUTF(text);
//        p.conn.addmsg(m);
//        m.cleanup();
//    }
//
//    private synchronized void update() {
//        time--;
//        if (0 < time && time <= 10) {
//            isLock = true;
//        } else if (time <= 0) {
//            resultTX();
//            refresh();
//        }
//    }
//
//    private void refresh() {
//        time = 60;
//        isLock = false;
//        playerTai.clear();
//        playerXiu.clear();
//        typeWin = NORMAL;
//    }
//
//    private void resultTX() {
//        if (typeWin == TAI_WIN) {
//            taiWin();
//        } else if (typeWin == XIU_WIN) {
//            xiuWin();
//        } else if (typeWin == NORMAL) {
//            setNormal();
//        }
//    }
//
//    public void taiWin() {
//        int xuc_sac_1;
//        int xuc_sac_2;
//        int xuc_sac_3;
//
//        do {
//            xuc_sac_1 = Util.random(1, 7);
//            xuc_sac_2 = Util.random(1, 7);
//            xuc_sac_3 = Util.random(1, 7);
//        } while (xuc_sac_1 + xuc_sac_2 + xuc_sac_3 <= 10);
//        String emoji1 = getDiceEmoji(xuc_sac_1);
//        String emoji2 = getDiceEmoji(xuc_sac_2);
//        String emoji3 = getDiceEmoji(xuc_sac_3);
//        result = "Tài " + emoji1 + " " + emoji2 + " " + emoji3;
//        addSoiCau("T");
//        reward(playerTai);
//    }
//
//    public void xiuWin() {
//        int xuc_sac_1;
//        int xuc_sac_2;
//        int xuc_sac_3;
//
//        do {
//            xuc_sac_1 = Util.random(1, 7);
//            xuc_sac_2 = Util.random(1, 7);
//            xuc_sac_3 = Util.random(1, 7);
//
//
//        } while (xuc_sac_1 + xuc_sac_2 + xuc_sac_3 <= 10);
//
//        String emoji1 = getDiceEmoji(xuc_sac_1);
//        String emoji2 = getDiceEmoji(xuc_sac_2);
//        String emoji3 = getDiceEmoji(xuc_sac_3);
//        result = "Xỉu " + emoji1 + " " + emoji2 + " " + emoji3;
//
//        addSoiCau("X");
//        reward(playerXiu);
//    }
//
//    public void setNormal() {
//        int xuc_sac_1;
//        int xuc_sac_2;
//        int xuc_sac_3;
//
//        xuc_sac_1 = Util.random(1, 7);
//        xuc_sac_2 = Util.random(1, 7);
//        xuc_sac_3 = Util.random(1, 7);
//
//        String emoji1 = getDiceEmoji(xuc_sac_1);
//        String emoji2 = getDiceEmoji(xuc_sac_2);
//        String emoji3 = getDiceEmoji(xuc_sac_3);
//
//        if ((xuc_sac_1 + xuc_sac_2 + xuc_sac_3) <= 10) {
//            result = "Xỉu " + emoji1 + " " + emoji2 + " " + emoji3;
//            addSoiCau("X");
//            reward(playerXiu);
//        } else {
//            result = "Tài " + emoji1 + " " + emoji2 + " " + emoji3;
//            addSoiCau("T");
//            reward(playerTai);
//        }
//        System.out.println(result);
//    }
//
//    private void reward(List<PlayerTX> list) {
//        try {
//            for (PlayerTX playerTX : list) {
//                int coinWin = (int) (playerTX.coinJoin * 1.9);
//                updateCoin(playerTX.username, coinWin);
//                Player pwin = Map.get_player_by_name(playerTX.name);
//                if (pwin != null) {
//                    Service.send_notice_nobox_white(pwin.conn, "Bạn đã thắng " + coinWin + " từ tài xỉu.");
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String get_time() {
//        if (time >= 0 && time <= 60) {
//            if (time > 0 && time <= 9) {
//                return "00:0" + time;
//            } else {
//                return "00:" + time;
//            }
//        }
//        return "0";
//    }
//
//    private int totalTai() {
//        int sum = 0;
//        for (PlayerTX p : playerTai) {
//            sum += p.coinJoin;
//        }
//        return sum;
//    }
//
//    private int totalXiu() {
//        int sum = 0;
//        for (PlayerTX p : playerXiu) {
//            sum += p.coinJoin;
//        }
//        return sum;
//    }
//
//    private int myJoin(List<PlayerTX> list, Player p) {
//        for (PlayerTX pTX : list) {
//            if (p.name.equals(pTX.name)) {
//                return pTX.coinJoin;
//            }
//        }
//        return 0;
//    }
//
//    private String soicau() {
//        String str = "";
//        for (String s : soicau) {
//            str += s + ", ";
//        }
//        return str;
//    }
//
//    private void addSoiCau(String t) {
//        if (soicau.size() < 15) {
//            soicau.add(t);
//        } else {
//            soicau.remove(0);
//            soicau.add(t);
//        }
//    }
//
//    public void join(Player p, int type, int coin) throws IOException {
//        if (type == 0) { // Tài
//            if (check(playerXiu, p.conn.id)) {
//                Service.send_notice_box(p.conn, "Không thể đặt 2 bên");
//                return;
//            }
//            if (updateCoin(p.conn.user, -coin)) {
//                if (check(playerTai, p.conn.id)) {
//                    PlayerTX pTX = getPlayer(playerTai, p.conn.id);
//                    if (pTX != null) {
//                        pTX.coinJoin += coin;
//                    }
//                } else {
//                    PlayerTX pTX = new PlayerTX(p.conn.id, p.conn.user, p.name, coin);
//                    playerTai.add(pTX);
//                }
//                Service.send_notice_box(p.conn, "Bạn đã đặt vào Tài " + coin + " coin thành công");
//            }
//        } else if (type == 1) {
//            if (check(playerTai, p.conn.id)) {
//                Service.send_notice_box(p.conn, "Không thể đặt 2 bên");
//                return;
//            }
//            if (updateCoin(p.conn.user, -coin)) {
//                if (check(playerXiu, p.conn.id)) {
//                    PlayerTX pTX = getPlayer(playerXiu, p.conn.id);
//                    if (pTX != null) {
//                        pTX.coinJoin += coin;
//                    }
//                } else {
//                    PlayerTX pTX = new PlayerTX(p.conn.id, p.conn.user, p.name, coin);
//                    playerXiu.add(pTX);
//                }
//                Service.send_notice_box(p.conn, "Bạn đã đặt vào Xỉu " + coin + " coin thành công");
//            }
//        }
//    }
//
//    private boolean check(List<PlayerTX> list, int id) {
//        for (PlayerTX playerTX : list) {
//            if (playerTX.id == id) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private PlayerTX getPlayer(List<PlayerTX> list, int id) {
//        for (PlayerTX playerTX : list) {
//            if (playerTX.id == id) {
//                return playerTX;
//            }
//        }
//        return null;
//    }
//
//    private boolean updateCoin(String name, int amount) {
//        try (Connection conn = SQL.gI().getConnection(); Statement statement = conn.createStatement()) {
//            if (statement.executeUpdate(
//                    "UPDATE `account` SET `coin` = `coin` +" + amount + " WHERE `user` = '" + name + "';") > 0) {
//                conn.commit();
//                if (amount < 0) {
//                    Log.gI().add_Log_Server("TaiXiu", "Người chơi có tài khoản " + name + " tham gia " + amount + " coin trò chơi tài xỉu");
//                } else {
//                    Log.gI().add_Log_Server("TaiXiu", "Người chơi có tài khoản " + name + " chiến thắng " + amount + " coin trò chơi tài xỉu");
//                }
//                return true;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return false;
//    }
//
//    private String getDiceEmoji(int number) {
//        return switch (number) {
//            case 1 -> "1";
//            case 2 -> "2";
//            case 3 -> "3";
//            case 4 -> "4";
//            case 5 -> "5";
//            case 6 -> "6";
//            default -> "";
//        };
//    }
//
//    public static class PlayerTX {
//        public int id;
//        public String username;
//        public String name;
//        public int coinJoin;
//
//        public PlayerTX(int id, String username, String name, int coinJoin) {
//            this.id = id;
//            this.name = name;
//            this.username = username;
//            this.coinJoin = coinJoin;
//        }
//    }
//}
//
