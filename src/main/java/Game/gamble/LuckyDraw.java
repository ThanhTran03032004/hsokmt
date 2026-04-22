package Game.gamble;

import Game.client.Player;
import Game.core.Manager;
import Game.core.Service;
import Game.core.Util;
import Game.io.Session;
import Game.History.His_VXMM;
import Game.core.Log;
import Game.io.Message;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuckyDraw implements Runnable {

    public static final int NORMAL = 0;
    public static final int VIP = 1;
    public Thread mainloopThread;
    private boolean running;
    private boolean started;
    private final int type;
    private short time;
    private String winner = "";
    private int vang_win = 0;
    private int vang_join = 0;
    public int max_join;
    public int min_join;
    private final List<PlayerJoin> listPlayer = new ArrayList<>();

    private String vipNextWinnerName = null;

    public LuckyDraw(int type) {
        time = 20;
        started = false;
        this.type = type;
        if (type == 0) {
            this.max_join = 500000;
            this.min_join = 100000;
        } else {
            this.max_join = 50000000;
            this.min_join = 1000000;
        }
        mainloopThread = new Thread(this);
        mainloopThread.start();
    }

    public synchronized void setVipNextWinnerName(String name) {
        if (this.type != VIP) return; // chỉ cho vòng VIP
        if (name == null) {
            this.vipNextWinnerName = null;
        } else {
            this.vipNextWinnerName = name.trim();
            if (this.vipNextWinnerName.isEmpty()) this.vipNextWinnerName = null;
        }
    }

    public void send_in4(Player p) throws IOException {
        Message m = new Message(-32);
        m.writer().writeShort(p.ID);
        m.writer().writeByte(86 + this.type);
        String text = "";
        if (this.type == NORMAL) {
            text = "Vòng xoay thường\r\n" + "Thời gian\r\n" + get_time() + "\n"
                    + Util.number_format(get_total_gold()) + " vàng\r\n"
                    + "Tỷ lệ thắng: " + get_percent(p) + "%\r\n" + "Số người hiện tại: " + get_size() + "\r\n"
                    + "Người vừa chiến thắng: " + get_last_winner() + "\r\n" + "Số vàng ăn được: " + get_vang_win() + " vàng\r\n"
                    + "Số vàng tham gia: " + get_vang_join() + " vàng";
        } else {
            text = "Vòng xoay Vip\r\n" + "Thời gian\r\n" + get_time() + "\n"
                    + Util.number_format(get_total_gold()) + " vàng\r\n"
                    + "Tỷ lệ thắng: " + get_percent(p) + "%\r\n" + "Số người hiện tại: " + get_size() + "\r\n"
                    + "Người vừa chiến thắng: " + get_last_winner() + "\r\n" + "Số vàng ăn được: " + get_vang_win() + " vàng\r\n"
                    + "Số vàng tham gia: " + get_vang_join() + " vàng";
        }
        m.writer().writeUTF(text);
        p.conn.addmsg(m);
        m.cleanup();
    }

    private String get_percent(Player p) {
        PlayerJoin playerJoin = getPlayer(p.ID);
        if (playerJoin != null) {
            return String.format("%.3f", ((float) playerJoin.getGold() * 100) / get_total_gold());
        }
        return "0.0";
    }

    private int get_size() {
        return listPlayer.size();
    }

    private int get_total_gold() {
        int total = 0;
        for (PlayerJoin player : listPlayer) {
            total += player.getGold();
        }
        return total;
    }

    private String get_vang_join() {
        if (vang_join > 0) {
            return Util.number_format(vang_join);
        }
        return "";
    }

    private String get_vang_win() {
        if (vang_win > 0) {
            return Util.number_format(vang_win);
        }
        return "";
    }

    private String get_last_winner() {
        return winner;
    }

    private String get_time() {
        if (started) {
            if (time <= 120 && time > 60) {
                if (time > 60 && time <= 69) {
                    return "01:0" + (time - 60);
                } else {
                    return "01:" + (time - 60);
                }
            } else if (time >= 0 && time <= 60) {
                if (time > 0 && time <= 9) {
                    return "00:0" + time;
                } else {
                    return "00:" + time;
                }
            }
        } else {
            return "02:00";
        }
        return "0";
    }

    @Override
    public void run() {
        running = true;
        long time1 = 0;
        long time2 = 0;
        while (running) {
            try {
                time1 = System.currentTimeMillis();
                update();
                time2 = System.currentTimeMillis();
                long time3 = (1000L - (time2 - time1));
                if (time3 > 0) {
                    Thread.sleep(time3);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    private synchronized void update() {
        if (started) {
            time--;
            if (time <= 0) {
                try {
                    notice_winner();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int findIndexByNameIgnoreCase(String name) {
        if (name == null) return -1;
        for (int i = 0; i < listPlayer.size(); i++) {
            if (listPlayer.get(i).name != null
                    && listPlayer.get(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    private synchronized void notice_winner() throws IOException {
        His_VXMM hist = new His_VXMM((byte) 0);
        int index = -1;

        // Nếu là vòng VIP và có cài trước người thắng
        if (this.type == VIP && vipNextWinnerName != null) {
            index = findIndexByNameIgnoreCase(vipNextWinnerName);
        }

        // Logic cũ nếu chưa có index hợp lệ
        if (index == -1) {
            int dem = 0;
            for (int i = 0; i < 20 && index == -1; i++) {
                dem = 0;
                for (PlayerJoin player : listPlayer) {
                    long percent = (((long) player.getGold()) * 100L) / get_total_gold();
                    if (percent > Util.random(100)) {
                        index = dem;
                    }
                    if (index != -1) {
                        break;
                    }
                    dem++;
                }
            }
            if (index == -1) {
                index = Util.random(0, listPlayer.size()); // random win
            }
        }

        int dem = 0;
        for (PlayerJoin player : listPlayer) {
            if (dem == index) {
                Player p0 = null;
                for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                    Session s = Session.client_entry.get(i);
                    if (s == null || s.p == null) {
                        continue;
                    }
                    if (s.p.ID == player.ID) {
                        p0 = s.p;
                        break;
                    }
                }
                winner = player.getName();
                vang_join = player.getGold();
                vang_win = get_total_gold();
                long thue = (get_total_gold() / 100L) * Manager.thue;
                vang_win -= thue;
                if (p0 != null && p0.map != null) {
                    hist.namePWin = p0.name;
                    hist.lastMoney = p0.get_vang();
                    hist.moneyround = get_total_gold();

                    if (Manager.ClanThue != null)
                        Manager.ClanThue.update_vang(thue);
                    Manager.gI().chatKTGprocess(winner + " đã chiến thắng " + Util.number_format(vang_win)
                            + " vàng khi tham gia vòng xoay may mắn");
                    p0.update_vang(vang_win, "Nhận %s vàng từ vòng xoay may mắn");

                    Log.gI().add_log(p0.name, "VXMM ăn được " + Util.number_format(vang_win) + " vàng");

                    hist.affterMoney = p0.get_vang();
                    hist.Logger = "có mặt";
                    hist.moneyJoin = vang_join;
                    hist.Flus();
                } else {
                    hist.moneyJoin = player.getGold();
                    hist.moneyround = get_total_gold();
                    hist.Logger = "Vắng mặt";
                    hist.Flus();
                    Manager.gold_offline.compute(player.getID(), (key, oldValue) -> (oldValue == null) ? vang_win : oldValue + vang_win);
                }
                break;
            }
            dem++;
        }

        // Reset tên cài sẵn sau mỗi vòng VIP
        if (this.type == VIP) {
            vipNextWinnerName = null;
        }

        refresh();
    }

    public synchronized void refresh() {
        started = false;
        this.listPlayer.clear();
        time = 120;
        if (this.type == VIP) {
            vipNextWinnerName = null; // reset an toàn
        }
    }

    public void close() {
        for(PlayerJoin player : listPlayer) {
            Manager.gold_offline.compute(player.getID(), (key, oldValue) -> (oldValue == null) ? player.getGold() : oldValue + player.getGold());
        }
        running = false;
        mainloopThread.interrupt();
        mainloopThread = null;
    }

    public synchronized void join_lucky_draw(Player p, int vang_join_vxmm) throws IOException {
        // Đệ tử
        if (!p.isOwner) {
            return;
        }
        if (time > 10) {
            if (Manager.isLockVX) {
                Service.send_notice_box(p.conn, "Chức năng bảo trì");
                return;
            }
            PlayerJoin playerJoin = Manager.gI().lucky_draw_normal.getPlayer(p.ID);
            PlayerJoin playerJoin2 = Manager.gI().lucky_draw_vip.getPlayer(p.ID);
            if (playerJoin != null || playerJoin2 != null) {
                Service.send_notice_box(p.conn, "Mỗi vòng chỉ được tham gia 1 lần 1 vòng xoay");
                return;
            }
            if (p.conn.status != 0) {
                Service.send_notice_box(p.conn, "Bạn không thể tham gia");
                return;
            }
            if ((get_total_gold() + vang_join_vxmm) > 500_000_000) {
                Service.send_notice_box(p.conn, "Tổng đặt cược tối đa chỉ 500tr");
                return;
            }
            p.update_vang(-vang_join_vxmm, "Trừ %s vàng chơi vòng xoay may mắn");
            Log.gI().add_log(p.name, "VXMM chơi " + Util.number_format(vang_join_vxmm) + " vàng");
            PlayerJoin pj = new PlayerJoin(p.name, p.ID, vang_join_vxmm);
            listPlayer.add(pj);
            send_in4(p);
            if (listPlayer.size() > 1 && !started) {
                this.start_rotate();
            }
        } else {
            Service.send_notice_box(p.conn, "Khoá đặt cược");
        }
    }

    private synchronized void start_rotate() {
        started = true;
    }

    public PlayerJoin getPlayer(int id) {
        for (int i = 0; i < listPlayer.size(); i++) {
            if (listPlayer.get(i).ID == id) {
                return listPlayer.get(i);
            }
        }
        return null;
    }

    @Setter
    @Getter
    public static class PlayerJoin {
        public String name;
        public int ID;
        public int gold;

        public PlayerJoin(String name, int ID, int gold) {
            this.name = name;
            this.ID = ID;
            this.gold = gold;
        }
    }
}
