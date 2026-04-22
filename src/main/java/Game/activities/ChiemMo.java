package Game.activities;

import Game.ai.NhanBan;
import Game.client.Clan;
import Game.client.Player;
import Game.core.Log;
import Game.map.Map;
import Game.map.MapService;
import Game.core.SQL;
import Game.io.Message;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import Game.template.Mob_MoTaiNguyen;

public class ChiemMo {

    @Getter
    private boolean running;
    private List<Mob_MoTaiNguyen> list_mo_tai_nguyen;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void init() {
        this.running = false;
        this.list_mo_tai_nguyen = new ArrayList<>();
        int[] x_ = new int[]{444, 1068, 228, 804, 516, 684, 540, 612, 1020, 444, 228, 612, 540, 492, 492, 756};
        int[] y_ = new int[]{156, 348, 516, 972, 372, 588, 588, 204, 204, 108, 372, 708, 396, 612, 420, 300};
        int[] map_ = new int[]{3, 5, 8, 9, 11, 12, 15, 16, 19, 21, 22, 24, 26, 27, 37, 42};
        String[] name_ = new String[]{"Mỏ Vàng", "Mỏ Tri Thức", "Mỏ Ngọc", "Mỏ Tri Thức", "Mỏ Vàng", "Mỏ Vàng",
                "Mỏ Tri Thức", "Mỏ Vàng", "Mỏ Vàng", "Mỏ Ngọc", "Mỏ Tri Thức", "Mỏ Vàng", "Mỏ Tri Thức", "Mỏ Ngọc",
                "Mỏ Vàng", "Mỏ Ngọc"};
        for (int i = 0; i < x_.length; i++) {
            this.list_mo_tai_nguyen.add(new Mob_MoTaiNguyen((i - 19), x_[i], y_[i], 4_000_000, 4_000_000, 120,
                    Map.get_map_by_id(map_[i])[4], name_[i]));
        }
    }

    public Mob_MoTaiNguyen get_mob_in_map(Map map) {
        for (Mob_MoTaiNguyen mob_MoTaiNguyen : list_mo_tai_nguyen) {
            if (mob_MoTaiNguyen.map.equals(map)) {
                return mob_MoTaiNguyen;
            }
        }
        return null;
    }

    public void mo_open_atk() {
        ResetChiemMo();
        setRunning(true);
        for (Mob_MoTaiNguyen mob_MoTaiNguyen : list_mo_tai_nguyen) {
            mob_MoTaiNguyen.is_atk = true;
        }
    }

    public void mo_close_atk() throws IOException {
        setRunning(false);
        for (Mob_MoTaiNguyen mob_MoTaiNguyen : list_mo_tai_nguyen) {
            mob_MoTaiNguyen.is_atk = false;
            mob_MoTaiNguyen.Set_hpMax(4_000_000);
            mob_MoTaiNguyen.hp = mob_MoTaiNguyen.get_HpMax();
            //
            Message m_hp = new Message(32);
            m_hp.writer().writeByte(1);
            m_hp.writer().writeShort(mob_MoTaiNguyen.ID);
            m_hp.writer().writeShort(-1);
            m_hp.writer().writeByte(0);
            m_hp.writer().writeInt(mob_MoTaiNguyen.get_HpMax());
            m_hp.writer().writeInt(mob_MoTaiNguyen.hp);
            m_hp.writer().writeInt(0); // param use
            for (int i = 0; i < mob_MoTaiNguyen.map.players.size(); i++) {
                mob_MoTaiNguyen.map.players.get(i).conn.addmsg(m_hp);
            }
            m_hp.cleanup();
        }
    }

    public static void trieu_hoi(Player p, Mob_MoTaiNguyen moTaiNguyen) {
        try {
            NhanBan nhanBan = new NhanBan(moTaiNguyen.nhanban);
            nhanBan.x = (short) (moTaiNguyen.x - 30);
            nhanBan.y = (short) (moTaiNguyen.y - 30);
            nhanBan.is_move = true;
            nhanBan.hp = nhanBan.hp_max;
            nhanBan.isdie = false;
            moTaiNguyen.nhanBans.add(nhanBan);
            //
            Message m12 = new Message(4);
            m12.writer().writeByte(0);
            m12.writer().writeShort(0);
            m12.writer().writeShort(nhanBan.ID);
            m12.writer().writeShort(nhanBan.x);
            m12.writer().writeShort(nhanBan.y);
            m12.writer().writeByte(-1);
            MapService.send_msg_player_inside(p.map, p, m12, true);
            m12.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void nang_cap_nhan_ban(Player p, Mob_MoTaiNguyen moTaiNguyen) {
        try {
            Message m;
            for (int i = 0; i < moTaiNguyen.nhanBans.size(); i++) {
                NhanBan nhanBan = moTaiNguyen.nhanBans.get(i);
                if (nhanBan.count_update < 4) {
                    nhanBan.dame = nhanBan.dame * 11 / 10;
                    nhanBan.def = nhanBan.def * 11 / 10;
                    nhanBan.hp_max = nhanBan.hp_max * 11 / 10;
                    nhanBan.hp = nhanBan.hp_max;
                    nhanBan.count_update++;
                    m = new Message(33);
                    m.writer().writeShort(nhanBan.ID);
                    m.writer().writeByte(nhanBan.level);
                    MapService.send_msg_player_inside(p.map, p, m, true);
                    nhanBan.update_info_other_inside(p.map);
                    m.cleanup();
                    p.update_ngoc(-100);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void harvest_all() {
        for (Mob_MoTaiNguyen mob_MoTaiNguyen : list_mo_tai_nguyen) {
            if (mob_MoTaiNguyen.clan != null) {
                switch (mob_MoTaiNguyen.name_monster) {
                    case "Mỏ Vàng": {
                        mob_MoTaiNguyen.clan.vang += 15_000;
                        break;
                    }
                    case "Mỏ Ngọc": {
                        mob_MoTaiNguyen.clan.kimcuong += 5;
                        break;
                    }
                    case "Mỏ Tri Thức": {
                        mob_MoTaiNguyen.clan.exp += 50_000;
                        break;
                    }
                }
            }
        }
    }

    public void ResetChiemMo() {
        try {
            synchronized (list_mo_tai_nguyen) {
                if (list_mo_tai_nguyen != null)
                    list_mo_tai_nguyen.clear();
                init();
            }
            Clan.ResetMoTaiNguyen();
        } catch (Exception e) {
            Log.gI().add_Log_Server("ChiemMo", "Reset: " + e.getMessage());
        }
    }

    public boolean LoadData() {
        try (Connection connection = SQL.gI().getConnection();
             Statement st = connection.createStatement();
        ) {
            for (Mob_MoTaiNguyen m : list_mo_tai_nguyen) {
                ResultSet rs = st.executeQuery("SELECT * FROM `chiem_mo` WHERE `idx` = '" + m.ID + "' ;");
                if (rs.next()) {
                    m.isbuff_hp = rs.getBoolean("isbuff_hp");
                    String nameClan = rs.getString("name_clan");
                    String nb = rs.getString("nhanban");
                    String nbs = rs.getString("nhanban_save");
                    if (nameClan == null) continue;

                    JSONArray jar = null;
                    if (nb != null) {
                        jar = (JSONArray) JSONValue.parse(nb);
                        if (jar != null && !jar.isEmpty()) {
                            m.nhanban = new NhanBan(jar);
                            m.nhanBans.add(m.nhanban);
                        }
                    }
                    if (jar != null)
                        jar.clear();
                    if (nbs != null) {
                        jar = (JSONArray) JSONValue.parse(nbs);
                        if (jar != null && !jar.isEmpty())
                            m.nhanban_save = new NhanBan(jar);
                    }
                    if (jar != null)
                        jar.clear();

                    for (Clan c : Clan.entries) {
                        if (c.name_clan.equals(nameClan)) {
                            c.add_mo_tai_nguyen(m);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void SaveData(Connection connection) {
        try (
                Statement st = connection.createStatement();
                PreparedStatement ps = connection.prepareStatement("UPDATE `chiem_mo` SET `isbuff_hp` = ?, `name_clan` = ?, `nhanban` = ?, `nhanban_save` = ?  WHERE `idx` = ?;");
        ) {
            synchronized (list_mo_tai_nguyen) {
                for (Mob_MoTaiNguyen m : list_mo_tai_nguyen) {
                    ResultSet rs = st.executeQuery("SELECT `idx` FROM `chiem_mo` WHERE `idx` = '" + m.ID + "' ;");
                    if (!rs.next()) {
                        String query = "INSERT INTO `chiem_mo` (`idx`) VALUES ('" + m.ID + "')";
                        if (st.executeUpdate(query) > 0) {
                            connection.commit();
                        }
                    } else {
                        ps.setBoolean(1, m.isbuff_hp);
                        if (m.clan != null)
                            ps.setString(2, m.clan.name_clan);
                        else
                            ps.setString(2, null);
                        if (m.nhanban != null)
                            ps.setString(3, m.nhanban.GetData().toJSONString());
                        else
                            ps.setString(3, null);
                        if (m.nhanban_save != null)
                            ps.setString(4, m.nhanban_save.GetData().toJSONString());
                        else
                            ps.setString(4, null);
                        ps.setInt(5, m.ID);
                        ps.executeUpdate();
                        ps.clearParameters();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
