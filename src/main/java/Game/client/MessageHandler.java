package Game.client;

import Game.Quest.QuestService;
import Game.core.Manager;
import Game.core.Util;
import Game.map.MapService;
import Game.Language.US;
import Game.Language.VietNam;
import Game.ai.MobAi;
import Game.ai.NhanBan;
import Game.ai.Player_Nhan_Ban;

import java.io.IOException;

import Game.core.AdminToolServer;
import Game.core.GameSrc;
import Game.core.MenuController;
import Game.core.Service;

import static Game.core.Service.send_notice_nobox_white;

import Game.activities.ChienTruong;
import Game.activities.Lottery;
import Game.core.Rank;
import Game.io.Message;
import Game.io.Session;
import Game.map.Dungeon;
import Game.map.DungeonManager;
import Game.map.Map;
import Game.template.EffTemplate;
import Game.template.Horse;
import Game.template.Mob_MoTaiNguyen;
import Game.security.AntiCheatService;
import Game.security.ClientAttestationService;
import Game.security.SessionState;
import Game.security.ViolationType;

public class MessageHandler {

    private final Session conn;

    public MessageHandler(Session conn) {
        this.conn = conn;
    }

    public void process_msg(Message m) throws IOException {
//        System.out.println("CMD " + m.cmd);
        switch (m.cmd) {
            case ClientAttestationService.COMMAND: {
                ClientAttestationService.processResponse(conn, m);
                break;
            }
            case -100: {
                if (conn.p.isdie) {
                    return;
                }
                GameSrc.Hop_Ngoc_Kham(conn.p, m);
                break;
            }
            case -102: {
                if (conn.p.isdie) {
                    return;
                }
                GameSrc.player_store(conn, m);
                break;
            }
            case -91: {
                if (conn.p.isdie) {
                    return;
                }
                byte step = m.reader().readByte();
                if (step == 0) {
                    Lottery.sendMessage(conn, (byte) 0);
                } else if (step == 1) {
                    Lottery.startLottery(conn, m.reader().readByte());
                } else if (step == 2) {
                    Lottery.rewardLottery(conn, m.reader().readByte());
                } else if (step == 5) {
                    MenuController.send_menu_select(conn, -91, new String[]{"Đổi ngôn ngữ", "Rơi nguyên liệu mề đay", "Chỉ rơi đồ cam", "Không nhận exp","Về làng thoát kẹt"});
                    if(conn.ac_admin == 11){
                        MenuController.send_menu_select(conn, -91, new String[]{"Đổi ngôn ngữ", "Rơi nguyên liệu mề đay", "Chỉ rơi đồ cam", "Không nhận exp","Về làng thoát kẹt","Menu admin"});
                    }
                } else if (step == 6) {
                    byte cat = m.reader().readByte();
                    if (cat == 3) {
                        short index = m.reader().readShort();
                        conn.p.item_tach = conn.p.item.inventory3[index];
                        if (conn.p.item_tach != null && conn.p.item_tach.isTrangBi() && conn.p.item_tach.level >= 50
                                && conn.p.item_tach.color < 5 && conn.p.item_tach.color > 1) {
                            MenuController.send_menu_select(conn, -91, new String[]{"Tách vật phẩm"}, (byte) 1);
                        } else {
                            Service.send_notice_box(conn, "Vật phẩm tách không phù hợp");
                        }
                    } else {
                        Service.send_notice_box(conn, "Vật phẩm tách không phù hợp");
                    }
                }
                break;
            }
            case 77: {
                if (conn.p.isdie) {
                    return;
                }
                GameSrc.Wings_Process(conn, m);
                break;
            }
            case -105: {
                if (conn.p.isdie) {
                    return;
                }
                if (conn.p.isCreateItemStar) {
                    GameSrc.ActionsItemStar(conn, m);
                } else if (conn.p.isCreateArmor) {
                    GameSrc.ActionsItemArmor(conn, m);
                } else {
                    GameSrc.Create_Medal(conn, m);
                }
                break;
            }
            case 69: {
                if (conn.p.isdie) {
                    return;
                }
                byte type = m.reader().readByte();
                if (type == 15 || type == 13) {
                    int idClan = m.reader().readInt();
                    Clan clan = Clan.get_clan_by_id(idClan);
                    if (clan != null) {
                        if (type == 15) {
                            if (clan.ID != conn.p.myclan.ID) {
                                clan.send_info_clan(conn, (byte) 1);
                            } else {
                                conn.p.myclan.send_info_clan(conn, (byte) 0);
                            }
                        } else {
                            clan.send_list_mem(conn);
                        }
                    }
                } else if (type == 11) {
                    Player p0 = Map.get_player_by_name(m.reader().readUTF());
                    if (p0 != null && p0.myclan != null && p0.isOwner) {
                        p0.myclan.accept_mem(conn, p0);
                    }
                } else if (conn.p.myclan != null) {
                    conn.p.myclan.clan_process(conn, m, type);
                }
                break;
            }
            case 73: {
                if (conn.p.isdie) {
                    return;
                }
                GameSrc.replace_item_process(conn.p, m);
                break;
            }
            case 36: {
                if (conn.p.isdie) {
                    return;
                }
                GameSrc.trade_process(conn, m);
                break;
            }
            case 48: {
                if (conn.p.isdie) {
                    return;
                }
                conn.p.map.create_party(conn, m);
                break;
            }
            case 67: {
                if (conn.p.isdie) {
                    return;
                }
                GameSrc.rebuild_item(conn, m);
                break;
            }
            case 9: {
                if (conn.p.isdie) {
                    return;
                }
                MapService.use_skill(conn.p.map, conn, m, 0);
                break;
            }
            case 6: {
                if (conn.p.isdie) {
                    return;
                }
                MapService.use_skill(conn.p.map, conn, m, 1);
                break;
            }
            case 40: {
                if (conn.p.isdie) {
                    return;
                }
                MapService.buff_skill(conn.p.map, conn, m);
                break;
            }
            case 20: {
                if (conn.p.isdie) {
                    return;
                }
                conn.p.map.pick_item(conn, m);
                break;
            }
            case 11: {
                if (conn.p.isdie) {
                    return;
                }
                if (conn.p.time_speed_rebuild > System.currentTimeMillis()) {
                    return;
                }
                conn.p.time_speed_rebuild = System.currentTimeMillis() + 500L;
                UseItem.ProcessItem3(conn, m);
                break;
            }
            case -107: {
                if (conn.p.isdie) {
                    return;
                }
                if (conn.p.time_speed_rebuild > System.currentTimeMillis()) {
                    return;
                }
                conn.p.time_speed_rebuild = System.currentTimeMillis() + 500L;
                UseItem.ProcessItem7(conn, m);
                break;
            }
            case 32: {
                if (conn.p.isdie) {
                    return;
                }
                if (conn.p.time_speed_rebuild > System.currentTimeMillis()) {
                    return;
                }
                conn.p.time_speed_rebuild = System.currentTimeMillis() + 500L;
                UseItem.ProcessItem4(conn, m);
                break;
            }
            case 24: {
                if (conn.p.isdie) {
                    return;
                }
                Service.buy_item(conn.p, m);
                break;
            }
            case 18: {
                if (conn.p.isdie) {
                    return;
                }
                Service.sell_item(conn, m);
                break;
            }
            case 37: {
                // arena
                break;
            }
            case 65: {
                if (conn.p.isdie) {
                    return;
                }
                conn.p.item.char_chest_process(conn, m);
                break;
            }
            case 44: {
                if (conn.p.isdie) {
                    return;
                }
                Service.pet_process(conn, m);
                break;
            }
            case 45: {
                if (conn.p.isdie) {
                    return;
                }
                Service.pet_eat(conn, m);
                break;
            }
            case 35: {
                conn.p.friend_process(m);
                break;
            }
            case 34: {
                Service.chat_tab(conn, m);
                break;
            }
            case 22: {
                if (conn.p.isdie) {
                    return;
                }
                conn.p.plus_point(m);
                break;
            }
            case -32: {
                Process_Yes_no_box.process(conn, m);
                break;
            }
            case -106: {
                Service.send_item7_template(conn.p, m);
                break;
            }
            case -97: {
                if (conn.p.isdie) {
                    return;
                }
                conn.p.down_horse(m);
                break;
            }
            case 28: {
                Service.send_in4_item(conn, m);
                break;
            }
            case 31: {
                MapService.request_livefromdie(conn.p.map, conn, m);
                break;
            }
            case -31: {
                if (conn.p.isdie) {
                    return;
                }
                TextFromClient.process(conn, m);
                break;
            }
            case -53: {
                //TextFromClient_2.process(conn, m);
                break;
            }
            case 21: {
                Service.send_param_item_wear(conn, m);
                break;
            }
            case 51: {
                if (conn.p.isdie) {
                    return;
                }
                conn.p.change_zone(conn, m);
                break;
            }
            case 42: {
                if (conn.p.isdie) {
                    return;
                }
                try {
                    MapService.change_flag(conn.p.map, conn.p, m.reader().readByte());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 49: {
                Service.send_view_other_player_in4(conn, m);
                break;
            }
            case 71: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, conn.language.chuakichhoat);
                    return;
                }
                Service.chat_KTG(conn, m);
                break;
            }
            case -30: {
                if (conn.p.isdie) {
                    return;
                }
                MenuController.processmenu(conn, m);
                break;
            }
            case 23: {
                if (conn.p.isdie) {
                    return;
                }
                MenuController.request_menu(conn, m);
                break;
            }
            case 27: {
                MapService.send_chat(conn.p.map, conn, m);
                break;
            }
            case 12: {
                conn.p.is_changemap = false;
                if (conn.p.isdie) {
                    return;
                }
                MapService.sync_mobs_after_map_ready(conn.p);
                AdminToolServer.gI().sendFakeBotPresence(conn.p);
                if (Map.is_map_chien_truong(conn.p.map.map_id)) {
                    ChienTruong.gI().send_info(conn.p);
                    //
                    Message m22 = new Message(4);
                    for (int i = 0; i < ChienTruong.gI().list_ai.size(); i++) {
                        Player_Nhan_Ban p0 = ChienTruong.gI().list_ai.get(i);
                        if (!p0.isdie && p0.map.equals(conn.p.map)) {
                            m22.writer().writeByte(0);
                            m22.writer().writeShort(0);
                            m22.writer().writeShort(p0.id);
                            m22.writer().writeShort(p0.x);
                            m22.writer().writeShort(p0.y);
                            m22.writer().writeByte(-1);
                        }
                    }
                    if (m22.writer().size() > 0) {
                        for (int i = 0; i < conn.p.map.players.size(); i++) {
                            Player p0 = conn.p.map.players.get(i);
                            p0.conn.addmsg(m22);
                        }
                    }
                    m22.cleanup();
                }
                if (conn.p.map.map_id == 48) {
                    // weather map dungeon
                    Message mw = new Message(76);
                    mw.writer().writeByte(4);
                    mw.writer().writeShort(-1);
                    mw.writer().writeShort(-1);
                    conn.addmsg(mw);
                    mw.cleanup();
                }
                break;
            }
            case -44: {
                Dungeon d = DungeonManager.get_list(conn.p.name);
                if (d != null) {
                    d.send_in4_npc(conn, m);
                }
                break;
            }
            case 5: {
                int id = m.reader().readShort();
                if (AdminToolServer.gI().sendFakeBotInfo(conn.p, id)) {
                    break;
                }
                if (id >= -1000 && id < 0) {
                    for (MobAi temp : conn.p.map.Ai_entrys) {
                        if (temp != null && temp.ID == id) {
                            temp.send_in4(conn.p);
                            return;
                        }
                    }
                    id = Short.toUnsignedInt((short) id);
                }

                Player p0 = null;
                for (int i = 0; i < conn.p.map.players.size(); i++) {
                    Player p01 = conn.p.map.players.get(i);
                    if (p01.ID == id) {
                        p0 = p01;
                        break;
                    }
                }
                if (p0 != null) {
                    MapService.send_in4_other_char(conn.p.map, conn.p, p0);
                } else if (Map.is_map_chiem_mo(conn.p.map, true)) {
                    Mob_MoTaiNguyen moTaiNguyen = Manager.gI().chiem_mo.get_mob_in_map(conn.p.map);
                    for (int i = 0; i < moTaiNguyen.nhanBans.size(); i++) {
                        NhanBan nhanBan = moTaiNguyen.nhanBans.get(i);
                        if (nhanBan != null) {
                            nhanBan.send_in4(conn.p);
                        }
                    }
                } else if (Map.is_map_chien_truong(conn.p.map.map_id)) {
                    ChienTruong.gI().get_ai(conn.p, id);
                } else {
                    Message m3 = new Message(8);
                    m3.writer().writeShort(id);
                    conn.addmsg(m3);
                    m3.cleanup();
                }
                break;
            }
            case 7: {
                int n = Short.toUnsignedInt(m.reader().readShort());
                if (n >= 30_000 && n < 31_000)//mob event
                {
                    return;
                }
                if (n > 10_000 && n < 11_000) {//mob boss
                    conn.p.map.BossIn4(conn, n);
                    return;
                }
                Dungeon d = DungeonManager.get_list(conn.p.name);
                if (d != null) {
                    d.send_mob_in4(conn, n);
                } else {
                    Service.mob_in4(conn.p, n);
                }
                break;
            }
            case 4: {
                if (conn.p.isdie) {
                    return;
                }
                MapService.send_move(conn.p.map, conn.p, m);
                break;
            }
            case -51: {
                Service.send_icon(conn, m);
                break;
            }
            case -52: {
                try {
                    byte type = m.reader().readByte();
                    short id = m.reader().readShort();
                    if (type == 110 && id == -1) {
                        if (ChienTruong.running) {
                            ChienTruong.gI().send_info(conn.p);
                        }
                    } else {
                        Message m2 = new Message(-52);
                        m2.writer().writeByte(type);
                        m2.writer().writeShort(id);
                        byte[] arrData = Util.loadfile("data/part_char/imgver/x" + conn.zoomlv + "/Data/" + (type + "_" + id));
                        byte[] arrImg = Util.loadfile("data/part_char/imgver/x" + conn.zoomlv + "/Img/" + (type + "_" + id) + ".png");
                        m2.writer().writeInt(arrImg.length);
                        m2.writer().write(arrImg);
                        m2.writer().write(arrData);
                        conn.addmsg(m2);
                        m2.cleanup();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case 55: {
                Service.save_rms(conn, m);
                break;
            }
            case 59: {
                Service.send_health(conn.p);
                break;
            }
            case 13: {
                try {
                    if (conn.getSessionState() != SessionState.ACCOUNT_AUTHENTICATED) {
                        AntiCheatService.gI().record(conn, ViolationType.INVALID_SESSION_STATE, 5,
                                "character login in state=" + conn.getSessionState());
                        return;
                    }
                    if (System.currentTimeMillis() - conn.timeStartLogin > 5000) {
                        conn.close();
                    } else {
                        login(m);
                    }
                } catch (Exception e) {
                    if (Manager.logErrorLogin) {
                        e.printStackTrace();
                    }
                    conn.close();
                }
                break;
            }
            case 14: {
                if (conn.getSessionState() != SessionState.ACCOUNT_AUTHENTICATED) {
                    AntiCheatService.gI().record(conn, ViolationType.INVALID_SESSION_STATE, 5,
                            "character creation in state=" + conn.getSessionState());
                    return;
                }
                conn.char_create(m);
                break;
            }
            case 1: {
                if (!conn.get_in4) {
                    conn.getclientin4(m);
                }
                break;
            }
            case 61: {
                Service.send_msg_data(conn, 61, Manager.gI().msg_61);
                Service.send_item_template(conn);
                Service.send_msg_data(conn, 26, Manager.gI().msg_26);
                break;
            }
            case -103: {//click mob minuong
                byte b = m.reader().readByte();
                if (b != 0) {
                    break;
                }
                short id = (short) (m.reader().readShort() - 1000);
                MenuController.send_menu_select(conn, id, new String[]{conn.language.haiqua}, (byte) Manager.gI().event);
                break;
            }
            case 52: {
                short ID = m.reader().readShort();
                byte type = m.reader().readByte();
                byte main_sub = m.reader().readByte();
                if (type == 0) {
                    QuestService.receiveQuest(conn, ID, main_sub);
                } else if (type == 1) {
                    QuestService.finishQuest(conn, ID, main_sub);
                } else if (type == 2) {
                    QuestService.cancelQuest(conn, ID, main_sub);
                }
                System.out.println("ID: " + ID + " type " + type + " main_sub " + main_sub);
                break;
            }
            default: {
                System.out.println("default onRecieveMsg : " + m.cmd);
                break;
            }
        }
    }

    private void login(Message m) {
        try {
            if (conn.p == null) {
                m.reader().readByte(); // type login
                int id_player_login = m.reader().readInt();
                if (!conn.ownsCharacterId(id_player_login)) {
                    AntiCheatService.gI().record(conn, ViolationType.CHARACTER_OWNERSHIP, 20,
                            "requestedCharacterId=" + id_player_login);
                    conn.sendLoginNotice("Nhan vat khong thuoc tai khoan nay.");
                    conn.close();
                    return;
                }
                Player p0 = new Player(conn, id_player_login);

                if (p0 != null && p0.setup()) {
                    for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                        Session s = Session.client_entry.get(i);
                        if (s == null || s.equals(conn) || s.user == null) {
                            continue;
                        }
                        if (s.get_in4 && s.id == conn.id && s.connected) {
                            try {
                                if (conn.socket.isConnected() && s.socket.isConnected()) {
                                    System.out.println("-----errorLogin ----conn: " + conn.socket.getInetAddress() + "-----lastConnect: " + s.socket.getInetAddress());
                                } else {
                                    System.out.println("+---- errorLogin ----+");
                                }
                            } catch (Exception e) {
                            }
                            conn.close();
                            s.close();
                            return;
                        }
                    }
                    if (Manager.gI().time_login_client.containsKey(conn.user)) {
                        long time_can_login = Manager.gI().time_login_client.get(conn.user) - System.currentTimeMillis();
                        if (time_can_login > 0) {
                            Service.lastLogin(conn);
                            return;
                        }
                    }
                    conn.p = p0;
                    conn.setSessionState(SessionState.IN_GAME);
                    conn.p.set_in4();
                    conn.SaveIP();
                    MessageHandler.dataloginmap(conn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dataloginmap(Session conn) {
        try {
            QuestService.sendQuestList(conn);
            QuestService.sendQuestFinish(conn);
            QuestService.sendQuestDoing(conn);

            Service.send_auto_atk(conn);
            Service.send_char_main_in4(conn.p);
            Service.send_msg_data(conn, 1, Manager.gI().msg_1);
            Service.send_skill(conn.p);
            Service.send_login_rms(conn);
            Service.send_notice_nobox_yellow(conn, ("Trò chơi dành cho người chơi 18 tuổi trở lên. Chơi quá 180 phút mỗi ngày sẽ có hại cho sức khỏe "));
            if (Manager.ClanThue != null) {
                send_notice_nobox_white(conn, ("Bang " + Manager.ClanThue.name_clan + " Đang Sở Hữu  Quyền Thu Thuế Trong Khu Mua Bán. " + "(" + " Thuế " + Manager.thue + " % " + ")"));
            } else {
                send_notice_nobox_white(conn, "Hiện tại chưa có bang nào sở hữu quyền thu thuế.");
            }
            conn.p.set_x2_xp(1);
            //
            if (Manager.gold_offline.containsKey(conn.p.ID)) {
                conn.p.update_vang(Manager.gold_offline.get(conn.p.ID), "Nhận %s vàng offline");
                Manager.gold_offline.remove(conn.p.ID);
            }
            conn.language = new VietNam();
            if (conn.typeLanguage == 1) {
                conn.language = new US();
            }
            if (conn.p.myclan == null || !Horse.isHorseClan(conn.p.type_use_horse)) {
                conn.p.type_use_horse = -1;
            }
            if (conn.p.item.wear[14] != null && conn.p.item.wear[14].isWingClan()
                    && (conn.p.myclan == null || !conn.p.myclan.hasWing(conn.p.item.wear[14].id - 4414))) {
                conn.p.item.wear[14] = null;
            }
            if (conn.p.myclan != null) {
                EffTemplate ef = conn.p.myclan.getEffect(Clan.TIME_SACH);
                if (ef != null) {
                    Service.send_notice_nobox_white(conn, "Thời gian sách kinh nghiệm bang hội còn lại " + (ef.time - System.currentTimeMillis()) / 60000L + " phút");
                }
            }
            if (Manager.gI().time_x2_server > System.currentTimeMillis()) {
                Service.send_notice_nobox_white(conn, "Thời gian x2 kinh nghiệm toàn server còn " + (Manager.gI().time_x2_server - System.currentTimeMillis()) / 60000L + " phút");
            }
            if (Manager.gI().ty_phu.contains(conn.p.name)) {
                Service.send_notice_nobox_yellow(conn, ("Tỷ phú " + conn.p.name.toUpperCase() + " đã đăng nhập vào game."));
            }
            Rank.chatKTGLogin(conn.p);
            conn.p.already_setup = true;
            conn.isLogin = true;
            MapService.enter(conn.p.map, conn.p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
