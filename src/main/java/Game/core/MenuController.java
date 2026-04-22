package Game.core;

import Game.Boss.BossServer;
import Game.Helps.CheckItem;
import Game.Helps.Save_Log;
import Game.NPC.NpcTemplate;
import Game.Quest.DailyQuest;
import Game.client.Clan;
import Game.client.Player;
import Game.event.*;
import Game.activities.*;
import Game.io.Session;
import Game.map.*;
import Game.template.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Game.client.Pet;

import Game.io.Message;

import java.util.HashMap;
import java.util.Map.Entry;

public class MenuController {

    public static void request_menu(Session conn, Message m) throws IOException {
        byte idnpc = m.reader().readByte();
        if (conn.p.map.find_npc_in_map(idnpc) == null) {
            Service.send_notice_nobox_white(conn, "Không thấy npc");
            return;
        }
        if (idnpc == -43 || idnpc == -45 || idnpc == -48 || idnpc == -46 || idnpc == -47) {
            Menu_ChangeZone(conn);
            return;
        }
        // create menu per id npc
        String[] menu;
        switch (idnpc) {
            case -127:
                menu = new String[]{"Nhiệm vụ hàng ngày", "Đổi coin sang ngọc", "Đổi coin sang vàng", "Thành tích", "Nhận quà top Level", "Nhận quà top event", "Shop Trang Bị Ngọc", "Đồ tinh tú"};
                menu = new String[]{"Nhiệm vụ hàng ngày", "Đổi coin sang ngọc", "Đổi coin sang vàng", "Thành tích", "Nhận quà mốc nạp","Mua gói rương black","Mua gói ngọc khảm","Mua gói hồ quang"};
                break;
            case -104:
                if (conn.p.map.map_id == 135) {
                    menu = new String[]{"Về Làng sói trắng", "Vượt Làng phủ sương"};
                } else {
                    menu = new String[]{"Về Làng sói trắng"};
                }
                break;
//            case -90: { // keva
//                menu = new String[]{"Shop", "Khu Phủ Sương Up", "Map Boss", "Quay Về Làng Phủ Sương", "Về Làng "};
//                break;
//            }
            case -89: { //
                menu = new String[]{"Bắn pháo"};
                break;
            }
            case -87: {
                menu = new String[]{"Điều ước"};
                break;
            }
            case -81: {
                Npc.chat(conn.p.map, "Để đăng ký lôi đài yêu cầu tối thiểu cấp 65 và 1000 ngọc", idnpc);
                menu = conn.language.menu_Oda;
                break;
            }
            case -63: {
                menu = new String[]{""};
                if (Manager.gI().event == 0) {
                    menu = LunarNewYear.menu;
                }
                break;
            }
            case -3, -20: { // Lisa
                menu = conn.language.menu_Lisa;
                break;
            }
            case -5, -21, -75: { // Hammer
                menu = conn.language.menu_Hammer;
                break;
            }
            case -4, -22, -77: {// Doubar
                menu = conn.language.menu_Doubar;
                break;
            }
            case -33: { // da dich chuyen
                menu = conn.language.menu_Tele33;
                break;
            }
            case -55: { // da dich chuyen
                menu = conn.language.menu_Tele55;
                break;
            }
            case -10: { // da dich chuyen
                menu = conn.language.menu_Tele10;
                break;
            }
            case -8: {
                menu = new String[]{""};
                if (conn.p.maxInventory < 126) {
                    menu = conn.language.menu_Zulu;
                } else {
                    Menu_Zulu(conn, (byte) 0);
                    return;
                }
                break;
            }
            case -36: {
                menu = conn.language.menu_PhapSu[0];
                Item3 item = conn.p.item.wear[12];
                if (item != null) {
                    if (item.hasOpPercentDame()) {
                        menu = conn.language.menu_PhapSu[1];
                    } else {
                        menu = conn.language.menu_PhapSu[2];
                    }
                }
                break;
            }
            case -44: {
                Item3 item = conn.p.item.wear[11];
                if (item != null) {
                    menu = conn.language.menu_Anna[0];
                } else {
                    menu = conn.language.menu_Anna[1];
                }
                break;
            }
            case -32: {
                menu = menu = conn.language.menu_Rank;
                break;
            }
            case -7: {
                if (conn.user.contains("knightauto_hsr_")) {
                    menu = conn.language.menu_Aman[0];
                } else {
                    menu = conn.language.menu_Aman[1];
                }
                break;
            }
            case -34: { // cuop bien
                menu = conn.language.menu_CuopBien;
                break;
            }
            case -2, -19: { // zoro
                if (conn.p.myclan != null) {
                    if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                        menu = conn.language.menu_Zoro[0];
                    } else {
                        menu = conn.language.menu_Zoro[1];
                    }
                } else {
                    menu = conn.language.menu_Zoro[2];
                }
                break;
            }
            case -85: { // mr edgar
                menu = new String[]{"Báo Thù", "Hướng dẫn báo thù"};
                break;
            }
            case -42: { // pet
                menu = new String[]{"Chuồng thú", "Shop thức ăn", "Shop trứng", "Tháo pet"};
                break;
            }
            case -37: {
                menu = conn.language.menu_PhoChiHuy;
                break;
            }
            case -41: {
                menu = conn.language.menu_TienCanh;
                break;
            }
            case -49: {
                menu = new String[]{"LIKE"};
                break;
            }
            case -82: {
                menu = new String[]{"Rời khỏi đây"};
                break;
            }
            case -69: {
                if (Manager.gI().event == 1) {
                    menu = new String[]{"Đổi hộp đồ chơi", "Hướng dẫn", "Đăng ký nấu kẹo", "Bỏ nguyên liệu vào nồi kẹo",
                        "Lấy kẹo đã nấu", "Đổi túi kẹo", "Đổi trứng phượng hoàng băng", "Đổi trứng yêu tinh",
                        "Đổi giày băng giá", "Đổi mặt nạ băng giá", "Đổi kẹo gậy", "Đổi gậy tuyết", "Đổi xe trượt tuyết",
                        "Đổi trứng khỉ nâu"};

                } else if (Manager.gI().event == 2) {
                    menu = new String[]{"Mâm trái cây", "Top sự kiện", "Đổi quà may mắn"};
                    send_menu_select(conn, -69, menu, (byte) Manager.gI().event);
                    return;
                } else if (Manager.gI().event == 3) {
                    menu = new String[]{"Đổi bó sen trắng", "Đổi hoa sen hồng", "Đổi bó sen hồng", "Xem top", "Đổi con lân", "Đổi trứng khỉ nâu", "Đổi trứng tiểu yêu", "Đổi cánh thời trang"};
                    send_menu_select(conn, -69, menu, (byte) Manager.gI().event);
                    return;

                } else {
                    menu = conn.language.menu_Sophia_Normal;
                    send_menu_select(conn, -69, menu, (byte) 0);
                    return;
                }
                break;
            }
            case -62: {
                if (Manager.gI().event != -1) {
                    menu = new String[]{"Thêm củi", "Thông tin"};
                } else {
                    return;
                }
                break;
            }
            case -66: {
                if (Manager.gI().event != -1) {
                    menu = new String[]{"Top sự kiện"};
                } else {
                    menu = new String[]{""};
                }
                break;
            }
            case -57: {
                menu = new String[]{"Mua bán"};
                break;
            }
            case -54: {
                menu = new String[]{"Đến Thành Phó Kho Báu"};
                break;
            }
            case -58: {
                menu = new String[]{"Mua lạc đà", "Bán đá quý", "Đồ thương nhân"};
                break;
            }
            case -59: {
                menu = new String[]{"Mua lạc đà", "Bán đá quý", "Đồ cướp"};
                break;
            }
            case -53: {
                menu = new String[]{" Đăng Ký Chiến trường", "Hướng dẫn", "Đổi đại bàng", "Vào Chiến Trường"};
                break;
            }
            default: {
                return;
            }
        }
        //
        send_menu_select(conn, idnpc, menu);
    }

    public static void processmenu(Session conn, Message m) throws IOException {
        short idnpc = m.reader().readShort();
        @SuppressWarnings("unused")
        byte idmenu = m.reader().readByte();
        byte index = m.reader().readByte();
        if (index < 0) {
            return;
        }
        if (idnpc == -56) {
            send_menu_select(conn, 119, new String[]{"Thông tin", "Bảo hộ", "Hồi máu", "Tăng tốc"});
            return;
        }
        if (conn.p.map.find_npc_in_map(idnpc) == null && NpcTemplate.getNpcById(idnpc) != null) {
            Service.send_notice_nobox_white(conn, "Không thấy npc");
            return;
        }
        if (idnpc >= 30000 && idmenu == Manager.gI().event) {
            Menu_MobEvent(conn, idnpc, idmenu, index);
            return;
        }
        switch (idnpc) {
            case -43: {
                if (idmenu == 1) {
                    switch (index) {
                        case 0:
                            if (conn.p.item.total_item_by_id(4, 54) >= 1) {
                                Map map = Map.get_map_by_id(conn.p.map.map_id)[1];
                                if (map != null && map.players.size() >= map.maxplayer) {
                                    Service.send_notice_box(conn, conn.language.khuvucday);
                                    return;
                                }
                                conn.p.item.remove(4, 54, 1);
                                conn.p.add_EffDefault(-127, 1, 2 * 60 * 60 * 1000);
                                MapService.leave(conn.p.map, conn.p);
                                conn.p.map = map;
                                MapService.enter(conn.p.map, conn.p);
                            } else {
                                Service.send_notice_box(conn, "Không đủ Đồng bạc Tyche");
                            }
                            break;
                        case 1:
    // Vào khu 2 không hỏi Yes/No, không hiển thị thông báo
    if (conn.p.get_ngoc() >= Map.NGOC_KHU_2) {
        conn.p.update_ngoc(-Map.NGOC_KHU_2);
        conn.p.add_EffDefault(-127, 1, 2 * 60 * 60 * 1000); // hiệu ứng buff 2 tiếng
        Map[] maps = Map.get_map_by_id(conn.p.map.map_id);
        if (maps != null && maps.length > 2) {
            Map map = maps[2];
            if (map != null && map.players.size() < map.maxplayer) {
                MapService.leave(conn.p.map, conn.p);
                conn.p.map = map;
                MapService.enter(conn.p.map, conn.p);
            } else {
                Service.send_notice_box(conn, conn.language.khuvucday);
            }
        }
    } else {
        Service.send_notice_box(conn, "Không đủ " + Map.NGOC_KHU_2 + " ngọc để vào khu 2");
    }
    break;

                    }
                }
                break;
            }
            case -128: {
                Menu_Nang_Skill(conn, index);
                break;
            }
            case -129: {
                Mob_MoTaiNguyen moTaiNguyen = Manager.gI().chiem_mo.get_mob_in_map(conn.p.map);
                if (moTaiNguyen != null) {
                    if (index == 0) {
                        if (conn.p.get_ngoc() > 100) {
                            if (moTaiNguyen.nhanBans.size() < 10) {
                                conn.p.update_ngoc(-100);
                                ChiemMo.trieu_hoi(conn.p, moTaiNguyen);
                            } else {
                                Service.send_notice_box(conn, "Đã triệu hồi tối đa.");
                            }
                        } else {
                            Service.send_notice_box(conn, "Không đủ ngọc");
                        }
                    } else if (index == 1) {
                        ChiemMo.nang_cap_nhan_ban(conn.p, moTaiNguyen);
                    }
                }
                break;
            }
            case -104: {
                Menu_Serena(conn, index);
                break;
            }
            case -63: {
                if (Manager.gI().event == 0) {
                    Menu_Ong_Do(conn, index);
                }
                break;
            }
            case 4: {
                Menu_DoiDongMeDaySTG(conn, index);
                break;
            }
            case 5: {
                Menu_DoiDongMeDaySTPT(conn, index);
                break;
            }
            case 117: {
                Menu_ThaoKhamNgoc(conn, index);
                break;
            }
            case -54: {
                Menu_Mr_Haku(conn, index);
                break;
            }
            case -81: {
                Menu_Mrs_Oda(conn, index, idmenu);
                break;
            }
            case -127:
                Menu_ADMIN_SHARINGAN(conn, idnpc, index, idmenu);
                break;
            case -82: {
                Menu_Miss_Anwen(conn, index);
                break;
            }
            case -53: {
                Menu_Mr_Ballard(conn, idnpc, idmenu, index);
                break;
            }
            case 210: {
                Menu_Kich_Hoat_Canh(conn, index);
                break;
            }
            case 119: {
                Menu_Pet_di_buon(conn, index);
                break;
            }
            case -57: {
                Menu_Mr_Dylan(conn, index);
                break;
            }
            case -58: {
                Menu_Graham(conn, index);
                break;
            }
            case -59: {
                Menu_Mr_Frank(conn, index);
                break;
            }
            case -3, -20: { // Lisa
                Menu_Lisa(conn, index);
                break;
            }
            case -90: { // keva
//                Menu_keva(conn, index);
                break;
            }
            case -89: {
                if (Manager.gI().event == 0) {
                    LunarNewYear.ban_phao(conn);
                }
                break;
            }
            case -4, -22, -77: {
                Menu_Doubar(conn, index, idmenu);
                break;
            }
            case -5, -21, -75: {
                Menu_Hammer(conn, index, idmenu);
                break;
            }
            case -33: {
                Menu_DaDichChuyen33(conn, index);
                break;
            }
            case -55: {
                Menu_DaDichChuyen55(conn, index);
                break;
            }
            case -10: {
                Menu_DaDichChuyen10(conn, index);
                break;
            }
            case -8: {
                Menu_Zulu(conn, index);
                break;
            }
            case 126: {
                Menu_Admin(conn, index);
                break;
            }
            case -36: {
                Menu_Phap_Su(conn, index);
                break;
            }
            case -44: {
                Menu_Miss_Anna(conn, index);
                break;
            }
            case -32: {
                Menu_Rank(conn, index, idmenu);
                break;
            }

            case -7: {
                Menu_Aman(conn, index);
                break;
            }
            case -34: {
                Menu_CuopBien(conn, index);
                break;
            }
            case 125: { // vxmm
                menuLuckyDrawVip(conn, index);
                break;
            }
            case 132: { // vxmm
                menuLuckyDrawNormal(conn, index);
                break;
            }
            case -2, -19: { // vxmm
                Menu_Zoro(conn, index);
                break;
            }
            case -85: { //
                Menu_Mr_Edgar(conn, index);
                break;
            }
            case 124: {
                Service.revenge(conn, index);
                break;
            }
            case 122: {
                Menu_Clan_Manager(conn, index);
                break;
            }
            case 127: {
                Menu_Shop_Clan(conn, index);
                break;
            }
            case -42: {
                Menu_Pet_Manager(conn, index);
                break;
            }
            case -37: {
                Menu_PhoChiHuy(conn, index);
                break;
            }
            case -38:
            case -40: {
                break;
            }
            case -41: {
                Menu_TienCanh(conn, index);
                break;
            }
            case -49: {
                Menu_Vua_Chien_Truong(conn, index);
                break;
            }
            case -69: {
                if (Manager.gI().event == 1) {
                    Menu_Event(conn, index);
                } else if (Manager.gI().event == 2) {
                    Menu_MissSophia(conn, idnpc, idmenu, index);
                } else if (Manager.gI().event == 3) {
                    Menu_MissSophia(conn, idnpc, idmenu, index);
                } else {
                    Menu_MissSophia(conn, idnpc, idmenu, index);
                }
                break;
            }
            case -62: {
                if (index == 0) {
                    if (EventManager.notCanRegister()) {
                        if (conn.p.get_vang() < 500000) {
                            Service.send_notice_box(conn, "Không đủ 500,000 vàng");
                            return;
                        }
                        conn.p.update_vang(-500000L, "Trừ %s vàng tăng tốc nấu");
                        EventManager.update(1);
                        Service.send_notice_box(conn, "Thời gian nấu còn lại " + EventManager.time + " phút");
                    } else {
                        Service.send_notice_box(conn, "Chưa đến thời gian nấu");
                    }
                } else if (index == 1) {
                    EventManager.send_info(conn);
                }
                break;
            }
            case -66: {
                if (Manager.gI().event != -1 && idmenu == 0) {
                    EventManager.top_event(conn);
                }
                break;
            }
            case -91: {
                Menu_Khac(conn, idmenu, index);
                break;
            }
            case 111: {
                Menu_Krypton(conn, idmenu, index);
                break;
            }
            default: {
                Service.send_notice_box(conn, "Đã xảy ra lỗi");
                break;
            }
        }
    }

    private static void Menu_Serena(Session conn, byte index) throws IOException {
        if (conn.p.map.map_id == 135) {
            if (index == 0) {
                conn.p.veLang();
            } else if (index == 1) {
                if (conn.p.get_EffDefault(-128) == null) {
                    if (conn.p.item.total_item_by_id(4, 315) < 1) {
                        Service.send_notice_box(conn, "Không có Vé vào làng phủ sương trong hành trang");
                        return;
                    } else {
                        conn.p.add_EffDefault(-128, 1, 4 * 60 * 60 * 1000);
                        conn.p.item.remove(4, 315, 1);
                    }
                }
                EffTemplate eff = conn.p.get_EffDefault(-128);
                if (eff != null) {
                    Service.send_time_box(conn.p, (byte) 1, new short[]{(short) ((eff.time - System.currentTimeMillis()) / 1000)}, new String[]{"Làng phủ sương"});
                    if (100 <= conn.p.level && conn.p.level < 110) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 125;
                        vgo.x_new = 100;
                        vgo.y_new = 100;
                        conn.p.change_map(conn.p, vgo);
                    } else if (110 <= conn.p.level && conn.p.level < 120) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 127;
                        vgo.x_new = 100;
                        vgo.y_new = 100;
                        conn.p.change_map(conn.p, vgo);
                    } else if (120 <= conn.p.level && conn.p.level < 130) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 129;
                        vgo.x_new = 200;
                        vgo.y_new = 200;
                        conn.p.change_map(conn.p, vgo);
                    } else if (130 <= conn.p.level) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 132;
                        vgo.x_new = 100;
                        vgo.y_new = 100;
                        conn.p.change_map(conn.p, vgo);
                    }
                }
            }
        } else {
            conn.p.veLang();
        }
    }

    private static void Menu_Mr_Ballard(Session conn, int idNPC, byte idmenu, byte index) throws IOException {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (idmenu) {
            case 0: {
                switch (index) {
                    case 0: { // dang ky
                        if (ChienTruong.gI().getStatus() == 1) {
                            ChienTruong.gI().register(conn.p);
                        } else {
                            Service.send_notice_box(conn, "Chiến trường mở đăng ký vào 20h45 Thứ 3,5,7 hàng tuần");
                        }
                        break;
                    }
                    case 1: {
                        break;
                    }
                    case 3: {
                        if (ChienTruong.gI().getStatus() == 2) {
                            MemberBattlefields info = ChienTruong.gI().get_infor_register(conn.p.name);
                            if (info != null) {
                                Vgo vgo = new Vgo();
                                switch (info.village) {
                                    case 2: { // lang gio
                                        vgo.id_map_go = 55;
                                        vgo.x_new = 224;
                                        vgo.y_new = 256;
                                        MapService.change_flag(conn.p.map, conn.p, 2);
                                        break;
                                    }
                                    case 3: { // lang lua
                                        vgo.id_map_go = 59;
                                        vgo.x_new = 240;
                                        vgo.y_new = 224;
                                        MapService.change_flag(conn.p.map, conn.p, 1);
                                        break;
                                    }
                                    case 4: { // lang set
                                        vgo.id_map_go = 57;
                                        vgo.x_new = 264;
                                        vgo.y_new = 272;
                                        MapService.change_flag(conn.p.map, conn.p, 4);
                                        break;
                                    }
                                    default: { // 5 lang anh sang
                                        vgo.id_map_go = 53;
                                        vgo.x_new = 276;
                                        vgo.y_new = 246;
                                        MapService.change_flag(conn.p.map, conn.p, 5);
                                        break;
                                    }
                                }
                                conn.p.change_map(conn.p, vgo);
                            } else {
                                Service.send_notice_box(conn, "Chưa đăng ký");
                            }
                        } else {
                            Service.send_notice_box(conn, "Chiến trường chưa bắt đầu");
                        }
                        break;
                    }
                    case 2: {
                        if (conn.p.pointarena < 30000) {
                            Service.send_notice_box(conn, "Phải cần tối thiểu 3000 điểm tích lũy chiến trường để có thể đổi trứng đại bàng.");
                        } else if (conn.p.item.get_inventory_able() < 1) {
                            Service.send_notice_box(conn, "Cần tối thiểu 1 ô trống để có thể đổi.");
                        } else {
                            try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM `history_doi_dai_bang` WHERE `user` = '" + conn.user + "' AND `time` >= DATE_SUB(NOW(), INTERVAL 1 WEEK);")) {
                                if (rs.next()) {
                                    Service.send_notice_box(conn, "Trong vòng 1 tuần 1 tài khoản chỉ có thể đổi 1 lần.");
                                    return;
                                } else {
                                    int last_point = conn.p.pointarena;
                                    short iditem = 3269;
                                    Item3 itbag = new Item3();
                                    itbag.id = iditem;
                                    itbag.name = ItemTemplate3.item.get(iditem).getName();
                                    itbag.clazz = ItemTemplate3.item.get(iditem).getClazz();
                                    itbag.type = ItemTemplate3.item.get(iditem).getType();
                                    itbag.level = ItemTemplate3.item.get(iditem).getLevel();
                                    itbag.icon = ItemTemplate3.item.get(iditem).getIcon();
                                    itbag.op = new ArrayList<>();
                                    itbag.op.addAll(ItemTemplate3.item.get(iditem).getOp());
                                    itbag.color = ItemTemplate3.item.get(iditem).getColor();
                                    itbag.part = ItemTemplate3.item.get(iditem).getPart();
                                    itbag.tier = 0;
                                    itbag.islock = false;
                                    itbag.time_use = 0;
                                    conn.p.item.add_item_inventory3(itbag);
                                    conn.p.pointarena -= 30000;
                                    String query
                                            = "INSERT INTO `history_doi_dai_bang` (`user`, `name_player`, `last_point` , `point_arena`) VALUES ('"
                                            + conn.user + "', '" + conn.p.name + "', '" + last_point + "', '" + conn.p.pointarena + "')";
                                    if (st.executeUpdate(query) > 0) {
                                        connection.commit();
                                    }
                                    List<BoxItem> ids = new ArrayList<>();
                                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private static void Menu_Ong_Do(Session conn, byte index) throws IOException {
        EventManager.processMenu(conn, index);
    }

    private static void Menu_MissSophia(Session conn, int idNPC, byte idmenu, byte index) throws IOException {
        if (idmenu == 0) {
            switch (index) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    byte index_remove = (byte) (index + 13);
                    Item3 item = conn.p.item.wear[index_remove];
                    if (item != null) {
                        conn.p.id_temp_byte = index_remove;
                        Service.send_box_input_yesno(conn, -119 + index, "Bạn có muốn tháo " + item.name + "?");
                    } else {
                        Service.send_notice_nobox_white(conn, "Không thể thực hiện");
                    }
                    break;
                case 6:
                    send_menu_select(conn, idNPC, LunarNewYear.menu, (byte) 4);
                    break;
            }
        } else if (idmenu == 2 && Manager.gI().event == 2) {
            switch (index) {
                case 0: {
                    if (conn.p.level < 40) {
                        Service.send_notice_box(conn, "Level quá thấp.");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 4) {
                        Service.send_notice_box(conn, "Hành trang đầy");
                        return;
                    }
                    if (conn.p.item.total_item_by_id(4, 141) < 1 && (!Manager.BuffAdminMaterial || conn.ac_admin < 4)) {
                        Service.send_notice_box(conn, "Thiếu " + ItemTemplate4.item.get(141).getName());
                        return;
                    }
                    for (int i = 254; i <= 258; i++) {
                        if (conn.p.item.total_item_by_id(4, i) < 1 && (!Manager.BuffAdminMaterial || conn.ac_admin < 4)) {
                            Service.send_notice_box(conn, "Thiếu " + ItemTemplate4.item.get(i).getName());
                            return;
                        }
                    }

                    conn.p.item.remove(4, 141, 1);
                    for (int i = 254; i <= 258; i++) {
                        conn.p.item.remove(4, i, 1);
                    }
                    List<BoxItem> ids = new ArrayList<>();

                    List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(0, 1, 4, 8, 9, 10, 11, 12, 13, 14));
                    List<Integer> it7_vip = new ArrayList<>(java.util.Arrays.asList(33, 346, 347, 349));
                    List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(2, 5, 61, 67, 269));
                    List<Integer> it4_vip = new ArrayList<>(java.util.Arrays.asList(131, 123, 132, 133, 52, 235, 147));
                    for (int i = 0; i < Util.random(1, 5); i++) {
                        int ran = Util.random(100);
                        if (ran < 0) {
                            short id = Util.random(it7, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(2, 5);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 6) {
                            short idsach = (short) 4762;
                            ids.add(new BoxItem(idsach, (short) 1, (byte) 3));
                            conn.p.item.add_item_inventory3_default(idsach, Util.random(10, 20), true);
                        } else if (ran < 14) {
                            short id = (short) Util.random(46, 246);
                            short quant = (short) 1;
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 24) {
                            short id = (short) Util.random(417, 464);
                            short quant = (short) Util.random(3);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 41) {
                            short id = Util.random(it7_vip, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 2);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 57) {
                            short id = Util.random(it4_vip, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 2);
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else if (ran < 77) {
                            short id = Util.random(it4, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(2, 5);
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else {
                            short id = Util.random(it7, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(2, 5);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        }
                    }
                    Event_2.add_caythong(conn.p.name, 1);
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                case 1: {
                    send_menu_select(conn, 120, Event_2.get_top());
                    break;
                }
                case 2: {
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Hành trang đầy");
                        return;
                    }
                    if (conn.p.item.total_item_by_id(4, 123) < 5) {
                        Service.send_notice_box(conn, "Cần tối thiểu 5 chuông vàng");
                        return;
                    }
                    List<BoxItem> ids = new ArrayList<>();
                    conn.p.item.remove(4, 123, 5);
                    List<Integer> it = new ArrayList<>(java.util.Arrays.asList(4612, 4632, 4633, 4634, 4635));
                    List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(299, 205, 207));
                    if (Util.random(100) < 60) {
                        short id = Util.random(it4, new ArrayList<>()).shortValue();
                        short quant = (short) Util.random(1, 3);
                        ids.add(new BoxItem(id, quant, (byte) 4));
                        conn.p.item.add_item_box47(id, quant, (byte) 4);
                    } else {
                        short id = Util.random(it, new ArrayList<>()).shortValue();
                        ids.add(new BoxItem(id, (short) 1, (byte) 3));
                        conn.p.item.add_item_inventory3_default(id, Util.random(5, 7), true);
                    }

                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                default:
                    Service.send_notice_box(conn, "Chưa có chức năng ev2!");
                    break;
            }
        } else if (idmenu == 3 && Manager.gI().event == 3) {
            switch (index) {
                case 0: {
                    Service.send_box_input_text(conn, 25, "Đổi bó sen trắng", new String[]{"30 sen trắng + 100k vàng"});
                    break;
                }
                case 1: {
                    Service.send_box_input_text(conn, 26, "Đổi hoa sen hồng", new String[]{"10 sen trắng + 25k vàng"});
                    break;
                }
                case 2: {
                    Service.send_box_input_text(conn, 27, "Đổi bó sen hồng", new String[]{"5 sen hồng + 30 ngọc"});
                    break;
                }
                case 3: {
                    send_menu_select(conn, 120, Event_3.get_top());
                    break;
                }
                case 4: {
                    if (conn.p.get_ngoc() < 30 || conn.p.item.total_item_by_id(4, 304) < 10) {
                        Service.send_notice_box(conn, "Cần tối thiểu 50 ngọc và 10 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-30);
                    conn.p.item.remove(4, 304, 10);
                    Item47 itbag = new Item47();
                    itbag.id = 246;
                    itbag.quantity = (short) 100;
                    itbag.category = 4;
                    conn.p.item.add_item_inventory47(4, itbag);

                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{246}, new int[]{100}, new short[]{4});
                    break;
                }
                case 5: {
                    if (conn.p.get_ngoc() < 100 || conn.p.item.total_item_by_id(4, 304) < 50) {
                        Service.send_notice_box(conn, "Cần tối thiểu 100 ngọc và 50 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-100);
                    conn.p.item.remove(4, 304, 50);
                    short iditem = 3616;
                    Item3 itbag = new Item3();
                    itbag.id = iditem;
                    itbag.name = ItemTemplate3.item.get(iditem).getName();
                    itbag.clazz = ItemTemplate3.item.get(iditem).getClazz();
                    itbag.type = ItemTemplate3.item.get(iditem).getType();
                    itbag.level = ItemTemplate3.item.get(iditem).getLevel();
                    itbag.icon = ItemTemplate3.item.get(iditem).getIcon();
                    itbag.op = new ArrayList<>();
                    itbag.op.addAll(ItemTemplate3.item.get(iditem).getOp());
                    itbag.color = ItemTemplate3.item.get(iditem).getColor();
                    itbag.part = ItemTemplate3.item.get(iditem).getPart();
                    itbag.tier = 0;
                    itbag.islock = false;
                    itbag.time_use = 0;
                    itbag.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 15;
                    conn.p.item.add_item_inventory3(itbag);

                    List<BoxItem> ids = new ArrayList<>();
                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                case 6: {
                    if (conn.p.get_ngoc() < 100 || conn.p.item.total_item_by_id(4, 304) < 50) {
                        Service.send_notice_box(conn, "Cần tối thiểu 100 ngọc và 50 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-100);
                    conn.p.item.remove(4, 304, 50);
                    short iditem = 4761;
                    Item3 itbag = new Item3();
                    itbag.id = iditem;
                    itbag.name = ItemTemplate3.item.get(iditem).getName();
                    itbag.clazz = ItemTemplate3.item.get(iditem).getClazz();
                    itbag.type = ItemTemplate3.item.get(iditem).getType();
                    itbag.level = ItemTemplate3.item.get(iditem).getLevel();
                    itbag.icon = ItemTemplate3.item.get(iditem).getIcon();
                    itbag.op = new ArrayList<>();
                    itbag.op.addAll(ItemTemplate3.item.get(iditem).getOp());
                    itbag.color = ItemTemplate3.item.get(iditem).getColor();
                    itbag.part = ItemTemplate3.item.get(iditem).getPart();
                    itbag.tier = 0;
                    itbag.islock = false;
                    itbag.time_use = 0;
                    itbag.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 15;
                    conn.p.item.add_item_inventory3(itbag);

                    List<BoxItem> ids = new ArrayList<>();
                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                case 7: {
                    if (conn.p.get_ngoc() < 100 || conn.p.item.total_item_by_id(4, 304) < 50) {
                        Service.send_notice_box(conn, "Cần tối thiểu 100 ngọc và 50 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-100);
                    conn.p.item.remove(4, 304, 50);
                    short iditem = 4642;
                    Item3 itbag = new Item3();
                    itbag.id = iditem;
                    itbag.name = ItemTemplate3.item.get(iditem).getName();
                    itbag.clazz = ItemTemplate3.item.get(iditem).getClazz();
                    itbag.type = ItemTemplate3.item.get(iditem).getType();
                    itbag.level = ItemTemplate3.item.get(iditem).getLevel();
                    itbag.icon = ItemTemplate3.item.get(iditem).getIcon();
                    itbag.op = new ArrayList<>();
                    itbag.op.addAll(ItemTemplate3.item.get(iditem).getOp());
                    itbag.color = ItemTemplate3.item.get(iditem).getColor();
                    itbag.part = ItemTemplate3.item.get(iditem).getPart();
                    itbag.tier = 0;
                    itbag.islock = false;
                    itbag.time_use = 0;
                    itbag.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30;
                    conn.p.item.add_item_inventory3(itbag);

                    List<BoxItem> ids = new ArrayList<>();
                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                default:
                    Service.send_notice_box(conn, "Chưa có chức năng ev3!");
                    break;
            }
        }

    }

    private static void Menu_MobEvent(Session conn, int idmob, byte idmenu, byte index) throws IOException {
        if (idmenu == 2) {
            if (index != 0) {
                return;
            }
            if (conn.p.level < 40) {
                Service.send_notice_box(conn, "Cần lên level 40 để có thể chơi sự kiện.");
                return;
            }
            MobCay mob = Event_2.getMob(idmob);
            if (mob == null || !mob.map.equals(conn.p.map)) {
                Message m2 = new Message(17);
                m2.writer().writeShort(-1);
                m2.writer().writeShort(idmob);
                conn.addmsg(m2);
                m2.cleanup();
                Service.send_notice_box(conn, "Không tìm thấy");
                return;
            }
            if (!(mob.map.equals(conn.p.map) && Math.abs(mob.x - conn.p.x) < 150 && Math.abs(mob.y - conn.p.y) < 150)) {
                Service.send_notice_box(conn, "Khoảng cách quá xa.\nNếu thực sự ở gần hãy thử load lại map.");
                return;
            }
            if (mob.Owner != null) {
                Service.send_notice_box(conn, "Đã có người khác hái quả.");
                return;
            }
            if (conn.p.item.get_inventory_able() < 1) {
                Service.send_notice_nobox_white(conn, "Hành trang đầy.");
                return;
            }
            if (conn.p.item.total_item_by_id(4, 252) < 1) {
                Service.send_notice_nobox_white(conn, "Hãy mua giỏ hái quả để chứa.");
                return;
            }
            conn.p.item.remove(4, 252, 1);
            mob.setOwner(conn.p);
            short id = (short) Util.random(254, 259);
            conn.p.item.add_item_inventory47(id, (short) 1, (byte) 4);
            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{4});
            //Service.send_notice_box(conn, "Nhận quả: "+mob.nameOwner);
        }
    }

    private static void Menu_Krypton(Session conn, byte idmenu, byte index) throws IOException {
        if (idmenu == 0) {
            GameSrc.UpgradeMedal(conn, index);
        } else if (idmenu == 1) {
            GameSrc.UpgradeItemStar(conn, index);
        }
        conn.p.id_Upgrade_Medal_Star = -1;
    }

    private static void Menu_Khac(Session conn, byte idmenu, byte index) throws IOException {
        if (idmenu == 0) {
            switch (index) {
                case 0: {
                    send_menu_select(conn, idmenu, new String[]{"Vietnamese", "English"}, (byte) 9);
                    break;
                }
                case 1: {
                    conn.p.isDropMaterialMedal = !conn.p.isDropMaterialMedal;
                    Service.send_notice_box(conn, "Rơi nguyên liệu mề đay đã " + (conn.p.isDropMaterialMedal ? "Bật" : "Tắt"));
                    break;
                }
                case 2: {
                    conn.p.isDropItemColor4 = !conn.p.isDropItemColor4;
                    Service.send_notice_box(conn, "Chỉ rơi đồ cam đã " + (conn.p.isDropItemColor4 ? "Bật" : "Tắt"));
                    break;
                }
                case 3: {
                    conn.p.isUpExp = !conn.p.isUpExp;
                    Service.send_notice_box(conn, "Không nhận exp " + (conn.p.isUpExp ? "Bật" : "Tắt"));
                    break;
                }
                case 4: {
                    conn.p.veLang();
                    break;
                }
                case 5: {
                    if(conn.ac_admin == 11){
                        Service.send_box_input_text(conn, 99, "Nhập tên", new String[]{"PlayerName"});
                    }
                    break;
                }
            }
        } else if (idmenu == 1) {
            if (conn.p.item_tach != null) {
                Item3 item = conn.p.item.inventory3[conn.p.item_tach.index];
                if (item != null && item == conn.p.item_tach && item.level >= 50 && item.color < 5 && item.color > 1 && item.isTrangBi()) {
                    List<BoxItem> ids = new ArrayList<>();
                    byte plus = item.tier;
                    int count_material = 2;
                    if (plus >= 8 && plus <= 11) {
                        count_material = 3;
                    } else if (plus >= 11) {
                        count_material = 4;
                    }
                    for (int i = 0; i < 3; i++) {
                        Item47 it = new Item47();
                        if (item.color == 2) {
                            it.id = ((short) (Util.random(5) * 4 + 417));
                        } else if (item.color == 3) {
                            it.id = ((short) (Util.random(7) * 4 + 417));
                        } else if (item.color == 4) {
                            it.id = ((short) (Util.random(10) * 4 + 417));
                        }
                        it.quantity = (short) Util.random(1, count_material);
                        it.category = 7;
                        conn.p.item.add_item_inventory47(7, it);
                        conn.p.item.remove(3, item.index, 1);
                        ids.add(new BoxItem(it.id, it.quantity, (byte) 7));
                    }
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được:", ids);
                }
            }
        } else if (idmenu == 9) {
            conn.typeLanguage = index;
        }
    }

    private static void Menu_Mrs_Oda(Session conn, byte index, byte idMenu) throws IOException {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (idMenu == 0) {
            switch (index) {
                case 0: {
                    if (conn.p.type_reward_king_cup != 0) {
                        Service.send_notice_box(conn, "Phải nhận quà lôi đài trước");
                        return;
                    }
                    if (KingCupManager.list_name.contains(conn.p.name)) {
                        Service.send_notice_box(conn, "Bạn đã đăng ký rồi.");
                    } else {
                        KingCupManager.register(conn.p);
                    }
                    break;
                }
                case 1: {
                    if (!KingCupManager.list_name.contains(conn.p.name)) {
                        Service.send_notice_box(conn, "Bạn không thể vào khi chưa đăng ký tham gia lôi đài");
                        return;
                    }
                    conn.p.goMapTapKet();
                    break;
                }
                case 2: {
                    if (KingCup.kingCup != null && KingCup.kingCups != null) {
                        String[] arrKingCup = new String[KingCup.kingCups.size()];
                        for (int i = 0; i < KingCup.kingCups.size(); i++) {
                            KingCup ld = KingCup.kingCups.get(i);
                            arrKingCup[i] = ld.name1 + "(" + ld.players_attack.get(0).level + ") vs " + ld.name2 + "(" + ld.players_attack.get(1).level + ")";
                        }
                        send_menu_select(conn, -81, arrKingCup, (byte) 1);
                        break;
                    } else {
                        Service.send_notice_box(conn, "Chưa tới giờ thi đấu lôi đài");
                    }
                    break;
                }
                case 3: {
                    if (!KingCupManager.list_name.contains(conn.p.name)) {
                        Service.send_notice_box(conn, "Bạn chưa đăng ký tham gia lôi đài");
                        return;
                    }
                    Service.send_notice_box(conn, "Điểm lôi đài : " + conn.p.point_king_cup);
                    break;
                }
                case 4:
                    KingCupManager.rewardKingCup(conn.p);
                    break;
                // Đệ tử
                case 5:
                    if (conn.p.squire != null) {
                        conn.p.squire.switchToSquire(conn.p);
                    } else {
                        Service.send_box_input_yesno(conn, -127, "Bạn có muốn nhận đệ tử với giá 5000 ngọc?");
                    }
                    break;
                case 6:
                    if (conn.p.squire != null) {
                        Service.send_box_input_yesno(conn, -124, "Huỷ đệ tử sẽ mất hết trang bị đang mặc.Bạn có muốn huỷ?");
                    } else {
                        Service.send_notice_box(conn, "Chưa có đệ tử");
                    }
                    break;
                case 7: {
                    conn.p.list_thao_kham_ngoc.clear();
                    for (int i = 0; i < conn.p.item.wear.length; i++) {
                        Item3 it = conn.p.item.wear[i];
                        if (it != null) {
                            short[] b = conn.p.item.check_kham_ngoc(it);
                            boolean check = false;
                            if ((b[0] != -2 && b[0] != -1) || (b[1] != -2 && b[1] != -1) || (b[2] != -2 && b[2] != -1)) {
                                check = true;
                            }
                            if (check) {
                                conn.p.list_thao_kham_ngoc.add(it);
                            }
                        }
                    }
                    String[] list_show = new String[]{"Trống"};
                    if (!conn.p.list_thao_kham_ngoc.isEmpty()) {
                        list_show = new String[conn.p.list_thao_kham_ngoc.size()];
                        for (int i = 0; i < list_show.length; i++) {
                            list_show[i] = conn.p.list_thao_kham_ngoc.get(i).name;
                        }
                    }
                    MenuController.send_menu_select(conn, 117, list_show);
                    break;
                }
                case 8: {
                    if (conn.p.level < 100) {
                        Service.send_notice_box(conn, "Bạn phải đạt từ cấp độ 100 mới có thể thực hiện chức năng này");
                        return;
                    }
                    conn.p.langPhuSuong();
                    break;
                }
            }
        } else if (idMenu == 1) {
            viewKingCup(conn, index);
        }
    }

    private static void viewKingCup(Session conn, byte index) {
        Vgo vgo = new Vgo();
        vgo.id_map_go = 102;
        vgo.x_new = 365;
        vgo.y_new = 395;
        KingCup.goToLD(conn.p, vgo, index);
    }

    private static void Menu_Pet_di_buon(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 0: {
                String notice = null;
                if (conn.p.pet_di_buon != null && conn.p.pet_di_buon.item.size() > 0) {
                    notice = "%s " + ItemTemplate3.item.get(3590).getName() + "\n";
                    notice += "%s " + ItemTemplate3.item.get(3591).getName() + "\n";
                    notice += "%s " + ItemTemplate3.item.get(3592).getName() + "\n";
                    int n1 = 0, n2 = 0, n3 = 0;
                    for (int i = 0; i < conn.p.pet_di_buon.item.size(); i++) {
                        if (null == conn.p.pet_di_buon.item.get(i)) {
                            n3++;
                        } else switch (conn.p.pet_di_buon.item.get(i)) {
                            case 3590 -> n1++;
                            case 3591 -> n2++;
                            default -> n3++;
                        }
                    }
                    notice = String.format(notice, n1, n2, n3);
                } else {
                    notice = "Trống";
                }
                Service.send_notice_box(conn, notice);
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                if (conn.p.get_ngoc() > 5) {
                    conn.p.pet_di_buon.update_hp(conn.p, 100);
                } else {
                    Service.send_notice_box(conn, "Không đủ 5 ngọc");
                }
                break;
            }
            case 3: {
                if (conn.p.get_ngoc() > 5) {
                    conn.p.pet_di_buon.update_speed(conn.p);
                } else {
                    Service.send_notice_box(conn, "Không đủ 5 ngọc");
                }
                break;
            }
        }
    }

    private static void Menu_Mr_Frank(Session conn, byte index) throws IOException {// Đệ tử
        if (conn.p.level < 40) {
            Service.send_notice_box(conn, "Yeu cau cap do 40");
            return;
        }
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.map.map_id != 17) {
            return;
        }
        if (conn.status != 0) {
            Service.send_notice_box(conn, conn.language.chuakichhoat);
            return;
        }
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 39);
                break;
            }
            case 1: {
                if (conn.p.isRobber()) {
                    if (conn.p.pet_di_buon != null && Math.abs(conn.p.pet_di_buon.x - conn.p.x) < 75
                            && Math.abs(conn.p.pet_di_buon.y - conn.p.y) < 75) {
                        //
                        int vang_recei = 0;
                        for (int i = 0; i < conn.p.pet_di_buon.item.size(); i++) {
                            vang_recei += (conn.p.pet_di_buon.item.get(i) - 3589) * 70_000;
                        }
                        if (vang_recei > 0) {
                            conn.p.update_vang(vang_recei, "Nhận %s vàng từ việc cướp.");
                            conn.p.point_z6 += vang_recei;
                            //
                            Message mout = new Message(8);
                            mout.writer().writeShort(conn.p.pet_di_buon.ID);
                            for (int i = 0; i < conn.p.map.players.size(); i++) {
                                Player p0 = conn.p.map.players.get(i);
                                if (p0 != null) {
                                    p0.conn.addmsg(mout);
                                }
                            }
                            mout.cleanup();
                            //
                            Pet_di_buon_manager.remove(conn.p.pet_di_buon.name);
                            conn.p.pet_di_buon = null;
                            Service.send_notice_box(conn, "Nhận được " + vang_recei + " vàng!");
                        }
                    } else {
                        Service.send_notice_box(conn, "Ta không thấy con vật đi buôn của ngươi");
                    }
                } else {
                    Service.send_notice_box(conn, "Không phải là cướp đừng nói chuyện với ta.");
                }
                break;
            }
            case 2: {
                Item3 itbag = new Item3();
                itbag.id = 3593;
                itbag.clazz = ItemTemplate3.item.get(3593).getClazz();
                itbag.type = ItemTemplate3.item.get(3593).getType();
                itbag.level = ItemTemplate3.item.get(3593).getLevel();
                itbag.icon = ItemTemplate3.item.get(3593).getIcon();
                itbag.op = new ArrayList<>();
                itbag.op.addAll(ItemTemplate3.item.get(3593).getOp());
                itbag.color = 5;
                itbag.part = ItemTemplate3.item.get(3593).getPart();
                itbag.tier = 0;
                itbag.islock = true;
                itbag.time_use = 0;
                // thao do
                if (conn.p.item.wear[11] != null && conn.p.item.wear[11].id != 3593 && conn.p.item.wear[11].id != 3599
                        && conn.p.item.wear[11].id != 3596) {
                    Item3 buffer = conn.p.item.wear[11];
                    conn.p.item.wear[11] = null;
                    conn.p.item.add_item_inventory3(buffer);
                }
                itbag.name = ItemTemplate3.item.get(3593).getName() + " [Khóa]";
                itbag.UpdateName();
                conn.p.item.wear[11] = itbag;
                conn.p.fashion = Part_fashion.get_part(conn.p);
                Service.send_notice_box(conn, "Nhận thành công");
                break;
            }
        }
    }

    private static void Menu_Graham(Session conn, byte index) throws IOException {// Đệ tử
        if (conn.p.level < 40) {
            Service.send_notice_box(conn, "Yeu cau cap do 40");
            return;
        }
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.map.map_id != 8) {
            return;
        }
        if (conn.status != 0) {
            Service.send_notice_box(conn, conn.language.chuakichhoat);
            return;
        }
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 32);
                break;
            }
            case 1: {
                if (conn.p.isTrader()) {
                    if (conn.p.pet_di_buon != null && Math.abs(conn.p.pet_di_buon.x - conn.p.x) < 75
                            && Math.abs(conn.p.pet_di_buon.y - conn.p.y) < 75) {
                        //
                        int vang_recei = 0;
                        for (int i = 0; i < conn.p.pet_di_buon.item.size(); i++) {
                            vang_recei += (conn.p.pet_di_buon.item.get(i) - 3589) * 70_000;
                        }
                        if (vang_recei > 0) {
                            conn.p.update_vang(vang_recei, "Nhận %s vàng từ đi buôn");
                            conn.p.point_z6 += vang_recei;
                            //
                            Message mout = new Message(8);
                            mout.writer().writeShort(conn.p.pet_di_buon.ID);
                            for (int i = 0; i < conn.p.map.players.size(); i++) {
                                Player p0 = conn.p.map.players.get(i);
                                if (p0 != null) {
                                    p0.conn.addmsg(mout);
                                }
                            }
                            mout.cleanup();
                            //
                            Pet_di_buon_manager.remove(conn.p.pet_di_buon.name);
                            conn.p.pet_di_buon = null;
                            Service.send_notice_box(conn, "Nhận được " + vang_recei + " vàng!");
                        } else {
                            Service.send_notice_box(conn, "Vật đi buôn không có hàng để bán");
                        }
                    } else {
                        Service.send_notice_box(conn, "Ta không thấy con vật đi buôn của ngươi");
                    }
                } else {
                    Service.send_notice_box(conn, "Ta chỉ tiếp các thương nhân");
                }
                break;
            }
            case 2: {
                Item3 itbag = new Item3();
                itbag.id = 3599;
                itbag.clazz = ItemTemplate3.item.get(3599).getClazz();
                itbag.type = ItemTemplate3.item.get(3599).getType();
                itbag.level = ItemTemplate3.item.get(3599).getLevel();
                itbag.icon = ItemTemplate3.item.get(3599).getIcon();
                itbag.op = new ArrayList<>();
                itbag.op.addAll(ItemTemplate3.item.get(3599).getOp());
                itbag.color = 5;
                itbag.part = ItemTemplate3.item.get(3599).getPart();
                itbag.tier = 0;
                itbag.islock = true;
                itbag.time_use = 0;
                // thao do
                if (conn.p.item.wear[11] != null && conn.p.item.wear[11].id != 3593 && conn.p.item.wear[11].id != 3599
                        && conn.p.item.wear[11].id != 3596) {
                    Item3 buffer = conn.p.item.wear[11];
                    conn.p.item.wear[11] = null;
                    conn.p.item.add_item_inventory3(buffer);
                }
                itbag.name = ItemTemplate3.item.get(3599).getName() + " [Khóa]";
                itbag.UpdateName();
                conn.p.item.wear[11] = itbag;
                conn.p.fashion = Part_fashion.get_part(conn.p);
                Service.send_notice_box(conn, "Nhận thành công");
                break;
            }
            default: {
                Service.send_notice_box(conn, "Chưa có chức năng");
                break;
            }
        }
    }

    private static void Menu_Mr_Dylan(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.map.map_id != 52) {
            return;
        }
        if (!conn.p.isTrader()) {
            Service.send_notice_box(conn, "Không phải là thương nhân đừng nói chuyện với ta.");
            return;
        }
        if (conn.p.pet_di_buon != null && Math.abs(conn.p.pet_di_buon.x - conn.p.x) < 75
                && Math.abs(conn.p.pet_di_buon.y - conn.p.y) < 75) {
            if (index == 0) {
                Service.send_box_UI(conn, 31);
            }
        } else {
            Service.send_notice_box(conn, "Ta không thấy con vật đi buôn của ngươi");
        }
    }

    private static void Menu_NauKeo(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 1) {
            switch (index) {
                case 0: {
                    // Service.send_box_input_text(conn, 11, "Nhập số lượng", new String[] {"Số lượng :"});
                    if (conn.p.get_ngoc() < 10) {
                        Service.send_notice_box(conn, "Không đủ 10 ngọc");
                        return;
                    }
                    if (EventManager.eventManager.time <= 30) {
                        Service.send_notice_box(conn, "Không thể tăng tốc");
                        return;
                    }
                    conn.p.update_ngoc(-10);
                    EventManager.eventManager.update(1);
                    Service.send_notice_box(conn, "Tăng tốc thành công");
                    break;
                }
                case 1: {
                    Service.send_notice_box(conn, "Nguyên liệu cần để nấu kẹo như sau: Đường, Sữa, Bơ, Vani\r\n"
                            + "- Mỗi ngày server cho nấu kẹo 1 lần vào lúc 17h , thời gian nấu là 2 tiếng.\r\n"
                            + "- Thời gian đăng ký là từ 19h ngày hôm trước đến 16h30 ngày hôm sau. Phí đăng ký là 5 ngọc\r\n"
                            + "- Một lần tăng tốc mất 10 ngọc và sẽ giảm được 2 phút nấu\r\n"
                            + "- Số kẹo tối đa nhận được là 20 kẹo.Tuy nhiên nếu các hiệp sĩ góp càng nhiều thì càng có lợi vì 10 người chơi góp nhiều nguyên liệu nhất sẽ được cộng thêm 20 cái\r\n"
                            + "+ Số kẹo nhận được sẽ tính theo công thức 1 Kẹo = 1 Đường + 1 Sữa + 1 Bơ+ 1 Vani");
                    break;
                }
                case 2: {
                    Service.send_notice_box(conn,
                            "Thông tin:\nĐã góp : " + Noel.get_keo_now(conn.p.name) + "\nThời gian nấu còn lại : "
                            + ((EventManager.eventManager.time == 0) ? "Không trong thời gian nấu"
                                    : ("Còn lại " + EventManager.eventManager.time + "p")));
                    break;
                }
                case 3: {
                    send_menu_select(conn, 120, Noel.get_top_naukeo());
                    break;
                }
            }
        }
    }

    private static void Menu_Event(Session conn, byte index) throws IOException {

    }

    private static void Menu_Miss_Anwen(Session conn, byte index) throws IOException {
        if (index == 0) {
            conn.p.veLang();
        }
    }

    private static void Menu_Vua_Chien_Truong(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (index == 0) {
            if (conn.p.diemdanh == 1) {
                conn.p.diemdanh = 0;
                int ngoc_ = Util.random(100, 500);
                int vang_ = Util.random(10000, 50000);
                conn.p.update_ngoc(ngoc_);
                conn.p.update_vang(vang_, "Nhận % vàng like vua chiến trường");
                Npc.chat(conn.p.map, "Cảm ơn " + conn.p.name + " đã like", -49);
            }
        }
    }

    private static void Menu_TienCanh(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                Service.send_msg_data(conn, 23, "create_wings");
                break;
            }
            case 1: {
                Message m2 = new Message(77);
                m2.writer().writeByte(6);
                conn.addmsg(m2);
                m2.cleanup();
                //
                m2 = new Message(77);
                m2.writer().writeByte(1);
                m2.writer().writeUTF("Nâng cấp cánh");
                conn.addmsg(m2);
                m2.cleanup();
                conn.p.is_create_wing = false;
                break;
            }
            case 2: {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < conn.p.item.inventory3.length; i++) {
                    Item3 it = conn.p.item.inventory3[i];
                    if (it != null && it.type == 7 && it.tier > 10) {
                        list.add(it.name + " +" + it.tier);
                    }
                }

                String[] list_2 = new String[]{"Trống"};
                if (!list.isEmpty()) {
                    list_2 = new String[list.size()];
                    for (int i = 0; i < list_2.length; i++) {
                        list_2[i] = list.get(i);
                    }
                }
                MenuController.send_menu_select(conn, 210, list_2);
                break;
            }
            case 3: {
                if (conn.p.item.wear[10] != null) {
                    Item3 item = conn.p.item.wear[10];
                    int quant1 = 40;
                    int quant2 = 10;
                    int quant3 = 50;
                    for (int i = 0; i < item.tier; i++) {
                        quant1 += GameSrc.wing_upgrade_material_long_khuc_xuong[i];
                        quant2 += GameSrc.wing_upgrade_material_kim_loai[i];
                        quant3 += GameSrc.wing_upgrade_material_da_cuong_hoa[i];
                    }
                    if (item.tier > 15) {
                        quant1 /= 2;
                        quant2 /= 2;
                        quant3 /= 2;
                    } else {
                        quant1 /= 3;
                        quant2 /= 3;
                        quant3 /= 3;
                    }
                    Service.send_box_input_yesno(conn, 114, "Bạn có muốn tách cánh này và nhận được: " + quant1
                            + " lông và khúc xương, " + quant2 + " kim loại, " + quant3 + " đá cường hóa?");
                } else {
                    Service.send_notice_nobox_white(conn, "Mặc cánh lên người để tách");
                }
                break;
            }
            case 4: {
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy");
                    return;
                }
                Item3 item_remove = conn.p.item.wear[10];
                if (item_remove != null) {
                    conn.p.item.wear[10] = null;
                    conn.p.item.add_item_inventory3(item_remove);
                    conn.p.fashion = Part_fashion.get_part(conn.p);
                    Service.send_wear(conn.p);
                    Service.send_char_main_in4(conn.p);
                    MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                    Service.send_notice_box(conn, "Đã tháo " + item_remove.name);
                } else {
                    Service.send_notice_nobox_white(conn, "Không thể thực hiện");
                }
                break;
            }
        }
    }

    private static void Menu_Kich_Hoat_Canh(Session conn, byte index) throws IOException {
        if (conn.p.get_ngoc() < 1000) {
            Service.send_notice_box(conn, "Không đủ 1000 ngọc");
            return;
        }
        conn.p.update_ngoc(-1000);
        Log.gI().add_log(conn.p.name, "hết 1000 ngọc");
        Item3 it_process = null;
        for (int i = 0; i < conn.p.item.inventory3.length; i++) {
            Item3 it = conn.p.item.inventory3[i];
            if (it != null && it.type == 7 && it.tier >= 10) {
                if (index == 0) {
                    it_process = it;
                    break;
                }
                index--;
            }
        }
        if (it_process != null) {
            Option[] process = new Option[2];
            for (int i = 0; i < it_process.op.size(); i++) {
                if (it_process.op.get(i).id >= 7 && it_process.op.get(i).id <= 11) {
                    if (process[0] == null) {
                        process[0] = it_process.op.get(i);
                    } else if (process[1] == null) {
                        process[1] = it_process.op.get(i);
                    } else {
                        break;
                    }
                }
            }
            if (process[1] == null) {
                Option option = new Option(Util.random(7, 12), 0);
                while (option.id == process[0].id) {
                    option.id = (byte) Util.random(7, 12);
                }
                option.param = process[0].param;
                it_process.op.add(option);
            } else if (process[0] != null) {
                process[1].id = (byte) Util.random(7, 12);
                while (process[1].id == process[0].id) {
                    process[1].id = (byte) Util.random(7, 12);
                }
            }
            Service.send_notice_box(conn, "Thành công");
            conn.p.item.char_inventory(3);
        }
    }

    private static void Menu_Clan_Manager(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
            switch (index) {
                case 0: {
                    conn.p.myclan.open_box_clan(conn);
                    break;
                }
                case 1: {
                    if (conn.p.myclan.get_percent_level() >= 100 && (conn.p.myclan.level == 9 || conn.p.myclan.level == 19
                            || conn.p.myclan.level == 29 || conn.p.myclan.level == 39)) {
                        Service.send_box_input_yesno(conn, 118,
                                "Bạn có muốn nâng cấp bang lên level " + (conn.p.myclan.level + 1) + " với "
                                + (Clan.vang_upgrade[1] * conn.p.myclan.level) + " vàng "
                                + " với " + (Clan.ngoc_upgrade[1] * conn.p.myclan.level) + " ngọc không?");
                    } else {
                        Service.send_notice_box(conn, "Chưa đủ exp để nâng cấp!");
                    }
                    break;
                }
                case 2: {
                    Service.send_box_input_yesno(conn, 116,
                            "Hãy xác nhận việc hủy bang?");
                    break;
                }
                case 3: {
                    Service.send_box_input_text(conn, 13, "Nhập tên :", new String[]{"Nhập tên :"});
                    break;
                }
            }
        }
    }

    private static void Menu_Shop_Clan(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
            switch (index) {
                case 0: {
                    Service.send_box_UI(conn, 30);
                    break;
                }
                case 1: {
                    Service.send_box_UI(conn, 29);
                    break;
                }
            }
        }
    }

    private static void Menu_PhoChiHuy(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                if (conn.p.level < 30) {
                    Service.send_notice_box(conn, "Đạt level 30 mới có thể vào phó bản");
                    return;
                }
//                if (conn.p.count_dungeon < 1) {
//                    Service.send_notice_box(conn, "Đã hết lượt tham gia, hãy quay lại vào ngày mai");
//                    return;
//                }
                if (conn.p.party != null) {
                    Service.send_notice_box(conn, "Phó bản hiện tại chỉ đi 1 mình");
                    return;
                }
                Dungeon d = DungeonManager.get_list(conn.p.name);
                if (d != null) {
                    DungeonManager.remove_list(d);
                }
                if (conn.p.count_dungeon < 1) {
                    Service.send_box_input_yesno(conn, 119, "Bạn đã hết lượt tham gia miễn phí, bạn có muốn tham gia với phí 1000 ngọc?");
                } else {
                    Service.send_box_input_yesno(conn, 119, "Bạn có muốn tham gia?");
                }
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                Service.send_box_input_yesno(conn, -126, "Để trở thành hiệp sĩ bạn cần có 2 điểm chiến trường, bạn có muốn thực hiện?");
                break;
            }
            case 4: {// Đệ tử
                if (!conn.p.isOwner) {
                    return;
                }
                ChiemThanhManager.ClanRegister(conn.p);
                break;
            }
        }
    }

    private static void Menu_Pet_Manager(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 21);
                break;
            }
            case 1: {
                Service.send_box_UI(conn, 22);
                break;
            }
            case 2: {
                Service.send_box_UI(conn, 23);
                break;
            }
            case 3: {
                if (conn.p.pet_follow_id != -1) {
                    for (Pet temp : conn.p.mypet) {
                        if (temp.is_follow) {
                            temp.is_follow = false;
                            Message m = new Message(44);
                            m.writer().writeByte(28);
                            m.writer().writeByte(1);
                            m.writer().writeByte(9);
                            m.writer().writeByte(9);
                            m.writer().writeUTF(temp.name);
                            m.writer().writeByte(temp.type);
                            m.writer().writeShort(conn.p.mypet.indexOf(temp)); // id
                            m.writer().writeShort(temp.level);
                            m.writer().writeShort(temp.getlevelpercent()); // exp
                            m.writer().writeByte(temp.type);
                            m.writer().writeByte(temp.icon);
                            m.writer().writeByte(temp.nframe);
                            m.writer().writeByte(temp.color);
                            m.writer().writeInt(temp.get_age());
                            m.writer().writeShort(temp.grown);
                            m.writer().writeShort(temp.maxgrown);
                            m.writer().writeShort(temp.sucmanh);
                            m.writer().writeShort(temp.kheoleo);
                            m.writer().writeShort(temp.theluc);
                            m.writer().writeShort(temp.tinhthan);
                            m.writer().writeShort(temp.maxpoint);
                            m.writer().writeByte(temp.op.size());
                            for (int i2 = 0; i2 < temp.op.size(); i2++) {
                                OptionPet temp2 = temp.op.get(i2);
                                m.writer().writeByte(temp2.id);
                                m.writer().writeInt(temp.getParam(temp2.id));
                                m.writer().writeInt(temp.getMaxDame(temp2.id));
                            }
                            conn.p.conn.addmsg(m);
                            m.cleanup();
                            Service.send_notice_box(conn, "Đã tháo " + temp.name);
                            break;
                        }
                    }
                    conn.p.pet_follow_id = -1;
                    Service.send_wear(conn.p);
                    Service.send_char_main_in4(conn.p);
                }
                break;
            }
        }
    }

    private static void Menu_Mr_Edgar(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                if (!conn.p.list_enemies.isEmpty()) {
                    String[] name = new String[conn.p.list_enemies.size()];
                    for (int i = 0; i < name.length; i++) {
                        name[i] = conn.p.list_enemies.get(name.length - i - 1);
                    }
                    send_menu_select(conn, 124, name);
                } else {
                    Service.send_notice_box(conn, "Danh sách chưa có ai");
                }
                break;
            }
            case 1: {
                Service.send_notice_box(conn,
                        "Bị người chơi khác pk thì sẽ được lưu vào danh sách, "
                        + "mỗi lần trả thù sẽ được đưa tới nơi kẻ thù đang đứng với chi phí chỉ vỏn vẹn 10 ngọc.\n"
                        + "Sau khi được đưa tới nơi, tên kẻ thù sẽ được loại ra khỏi danh sách");
                break;
            }
        }
    }

    private static void Menu_Zoro(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.myclan != null) {
            if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                switch (index) {
                    case 0: {
                        send_menu_select(conn, 122,
                                new String[]{"Kho bang", "Nâng cấp bang", "Hủy bang hội", "Chuyển thủ lĩnh"});
                        break;
                    }
                    case 1: {
                        send_menu_select(conn, 127, new String[]{"Shop vật phẩm bang", "Shop Icon Bang Hội"});
                        break;
                    }
                    case 2: {
                        break;
                    }
                }
            } else {
                switch (index) {
                    case 0: {
                        conn.p.myclan.open_box_clan(conn);
                        break;
                    }
                }
            }
        } else {
            if (index == 0) {
                if (conn.p.level < 30) {
                    Service.send_notice_box(conn, "Yêu cầu cấp độ 30");
                    return;
                }
                Service.send_box_input_yesno(conn, 70, "Bạn có muốn đăng ký tạo bang với phí là 25.000 ngọc");
            }
        }
    }

    private static void menuLuckyDrawVip(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 0: {
                Manager.gI().lucky_draw_vip.send_in4(conn.p);
                break;
            }
            case 1: {
                Service.send_box_input_text(conn, 3, "Vòng xoay Vip", new String[]{"Tham gia"});
                break;
            }
        }
    }

    private static void menuLuckyDrawNormal(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 0: {
                Manager.gI().lucky_draw_normal.send_in4(conn.p);
                break;
            }
            case 1: {
                Service.send_box_input_text(conn, 17, "Vòng xoay thường", new String[]{"Tham gia"});
                break;
            }
        }
    }

    private static void Menu_CuopBien(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 2: {
                send_menu_select(conn, 125, new String[]{"Xem thông tin", "Tham gia"});
                break;
            }
            case 4: {
                send_menu_select(conn, 132, new String[]{"Xem thông tin", "Tham gia"});
                break;
            }
            default: {
                Service.send_notice_box(conn, "Chúc bạn chơi game vui vẻ");
                break;
            }
        }
    }

    public static void send_menu_select(Session conn, int idnpc, String[] menu) throws IOException {
        if (!conn.p.isdie) {
            if (menu != null && menu.length > 0) {
                Message m2 = new Message(-30);
                m2.writer().writeShort(idnpc);
                m2.writer().writeByte(0);
                m2.writer().writeByte(menu.length);
                for (int i = 0; i < menu.length; i++) {
                    m2.writer().writeUTF(menu[i]);
                }
                if (conn.ac_admin > 0) {
                    m2.writer().writeUTF("MENU : " + idnpc);
                } else {
                    m2.writer().writeUTF("MENU");
                }
                conn.addmsg(m2);
                m2.cleanup();
            }
        }
    }

    public static void send_menu_select(Session conn, int idnpc, String[] menu, byte idmenu) throws IOException {
        if (!conn.p.isdie) {
            if (menu != null && menu.length > 0) {
                Message m2 = new Message(-30);
                m2.writer().writeShort(idnpc);
                m2.writer().writeByte(idmenu);
                m2.writer().writeByte(menu.length);
                for (int i = 0; i < menu.length; i++) {
                    m2.writer().writeUTF(menu[i]);
                }
                if (conn.ac_admin > 0) {
                    m2.writer().writeUTF("MENU : " + idnpc);
                } else {
                    m2.writer().writeUTF("MENU");
                }
                conn.addmsg(m2);
                m2.cleanup();
            }
        }
    }

    private static void Menu_Aman(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                conn.p.item.char_chest(3);
                conn.p.item.char_chest(4);
                conn.p.item.char_chest(7);
                conn.p.type_process_chest = 0;
                Message m = new Message(23);
                m.writer().writeUTF("Rương đồ");
                m.writer().writeByte(3);
                m.writer().writeShort(0);
                conn.addmsg(m);
                m.cleanup();
                break;
            }
            case 1: {
                int count_update = (conn.p.maxBox - 14) / 7;
                int gems_need = (count_update + 1) * 20;
                if (count_update < 10) {
                    Service.send_box_input_yesno(conn, -128, "Bạn có muốn nâng cấp thêm 7 ô giá " + gems_need + " ngọc");
                }
                break;
            }
            case 2: {
                Service.send_box_UI(conn, 48);
                break;
            }
            case 3: {
                if (conn.user.contains("knightauto_hsr_")) {
                    if (conn.p.level < 0) {
                        Service.send_notice_box(conn, "Đạt level 1 mới có thể đăng ký tài khoản");
                        return;
                    }
                    Service.send_box_input_text(conn, 6, "Đăng ký tài khoản",
                            new String[]{"Tên đăng nhập", "Mật khẩu"});
                }
                break;
            }
        }
    }

    private static void Menu_Rank(Session conn, byte index, byte idMenu) throws IOException {
        if (idMenu == 0) {
            switch (index) {
                case 0: {
                    Rank.send(conn, 0);
                    break;
                }
                case 1: {
                    Rank.send(conn, 6);
                    break;
                }
                case 2: {
                    send_menu_select(conn, -32, new String[]{"Bang giàu có nhất", "Bang nhiều châu báu nhất", "Bang hùng mạnh nhất"}, (byte) 1);
                    break;
                }
                case 3: {
                    Rank.send(conn, 4);
                    break;
                }
                case 4: {
                    Rank.send(conn, 5);
                    break;
                }
            }
        } else if (idMenu == 1) {
            switch (index) {
                case 0: {
                    Rank.send(conn, 2);
                    break;
                }
                case 1: {
                    Rank.send(conn, 3);
                    break;
                }
                case 2: {
                    Rank.send(conn, 1);
                    break;
                }
            }
        }
    }

    private static void Menu_Miss_Anna(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 38);
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                Item3 item = conn.p.item.wear[11];
                if (item != null) {
                    Service.send_box_input_yesno(conn, 11, "Bạn có muốn tháo " + item.name);
                } else {
                    Service.send_box_input_text(conn, 0, "Nhập mã code", new String[]{"Code"});
                }
                break;
            }
            case 3: {
                Service.send_box_input_text(conn, 0, "Nhập mã code", new String[]{"Code"});
                break;
            }
        }
    }

    private static void Menu_Phap_Su(Session conn, byte index) throws IOException {
        conn.p.ResetCreateItemStar();
        switch (index) {
            case 0: {
                conn.p.id_item_rebuild = -1;
                conn.p.is_use_mayman = false;
                conn.p.id_use_mayman = -1;
                Service.send_box_UI(conn, 18);
                break;
            }
            case 1: {
                conn.p.item_replace = -1;
                conn.p.item_replace2 = -1;
                Service.send_box_UI(conn, 19);
                break;
            }
            case 2: {
                Service.send_box_UI(conn, 17);
                break;
            }
            case 7: {
                Service.send_box_UI(conn, 35);
                break;
            }
            case 9: {
                Service.send_box_UI(conn, 34);
                break;
            }
            case 11: {
                Service.send_box_UI(conn, 36);
                break;
            }
            case 12: {
                Service.send_box_UI(conn, 24);
                break;
            }
            case 13: {
                Service.send_box_UI(conn, 25);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 0;
                break;
            }
            case 14: {
                Service.send_box_UI(conn, 26);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 1;
                break;
            }
            case 15: {
                Service.send_box_UI(conn, 27);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 2;
                break;
            }
            case 16: {
                Service.send_box_UI(conn, 28);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 3;
                break;
            }
            case 17: {
                conn.p.ResetCreateItemStar();
                Service.send_box_UI(conn, 33);
                break;
            }
            case 18:
            case 19: {
                ArrayList<String> myList = new ArrayList<>();
                Item3[] itemw = conn.p.item.wear;

                if (itemw == null) {
                    return;
                }
                if (itemw[12] != null && CheckItem.isMeDay(itemw[12].id)) {
                    myList.add(itemw[12].name + "(1000 ngọc)");
                }
                if (myList.isEmpty()) {
                    return;
                }
                send_menu_select(conn, index == 18 ? 4 : 5, myList.toArray(new String[0]));
                break;
            }
        }
    }

    private static void Menu_Admin(Session conn, byte index) throws IOException {
        if (conn.ac_admin < 1) {
            return;
        }
        switch (index) {
            case 0: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền để thực hiện!");
                    return;
                }
                Service.send_box_input_yesno(conn, 88, "Bạn có chắc chắn muốn bảo trì server?");
                break;
            }
            case 1: {
                if (conn.ac_admin <= 3) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                conn.p.update_vang(1_000_000_000, "Nhận %s vàng từ lệnh admin");
                Service.send_notice_nobox_white(conn, "+ 1.000.000.000 vàng");
                break;
            }
            case 2: {
                if (conn.ac_admin <= 3) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                conn.p.update_ngoc(1_000_000);
                Service.send_notice_nobox_white(conn, "+ 1.000.000 ngọc");
                break;
            }
            case 3: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                SaveData.process();
                Service.send_notice_nobox_white(conn, "data đã đc cập nhật");
                break;
            }
            case 4: {
                Service.send_box_input_text(conn, 1, "Get Item",
                        new String[]{"Nhập loại (3,4,7) vật phẩm :", "Nhập id vật phẩm", "Nhập số lượng"});
                break;
            }
            case 5: {
                Service.send_box_input_text(conn, 2, "Plus Level", new String[]{"Nhập level :"});
                break;
            }
            case 6: {
                Service.send_box_input_text(conn, 4, "Set Xp", new String[]{"Nhập mức x :"});
                break;
            }
            case 7: {
                Service.send_box_input_text(conn, 18, "Tên nhân vật", new String[]{"Nhập Tên nhân vật :"});
                break;
            }
            case 8: {
                Service.send_box_input_text(conn, 19, "Tên nhân vật", new String[]{"Nhập Tên nhân vật :"});
                break;
            }
            case 9: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.isLockVX = !Manager.isLockVX;
                Service.send_notice_box(conn, "Vòng xoay vàng ngọc đã " + (Manager.isLockVX ? "khóa" : "mở"));
                //Service.send_box_input_text(conn, 19, "Tên nhân vật", new String[]{"Nhập Tên nhân vật :"});
                break;
            }
            case 10: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.isTrade = !Manager.isTrade;
                Service.send_notice_box(conn, "Giao dịch đã " + (Manager.isTrade ? "mở" : "khóa"));
                break;
            }
            case 11: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.isKmb = !Manager.isKmb;
                Service.send_notice_box(conn, "Giao dịch đã " + (Manager.isKmb ? "mở" : "khóa"));
                break;
            }
            case 12: {
                if (conn.ac_admin < 4) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                for (Pet pet : conn.p.mypet) {
                    if (pet.time_born > 0) {
                        pet.time_born = 3;
                    }
                }
                Service.send_notice_box(conn, "Đã xong");
                break;
            }
            case 13: {
                if (conn.ac_admin < 4) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.BuffAdmin = !Manager.BuffAdmin;
                Service.send_notice_box(conn, "Buff Admin đã: " + (Manager.BuffAdmin ? "Bật" : "Tắt"));
                break;
            }
            case 14: {
                if (conn.ac_admin < 4) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.BuffAdminMaterial = !Manager.BuffAdminMaterial;
                Service.send_notice_box(conn, "Buff nguyên liệu cho Admin Đã: " + (Manager.BuffAdminMaterial ? "Bật" : "Tắt"));
                break;
            }
            case 15: {
                if (conn.ac_admin < 5) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.gI().chiem_mo.mo_open_atk();
                Manager.gI().chatKTGprocess(" Thời gian chiếm mỏ đã đến!");
                break;
            }
            case 16: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.gI().chiem_mo.mo_close_atk();
                Manager.gI().chatKTGprocess(" Thời gian chiếm mỏ đã đóng!");
                break;
            }
            case 17: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                break;
            }
            case 18: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                if (Manager.gI().event == 2) {
                    Event_2.ClearMob();
                    Event_2.ResetMob();
                    Service.send_notice_box(conn, "Đã thực hiện reset mob events");
                }
                break;
            }
            case 19: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                if (ChiemThanhManager.isRegister) {
                    ChiemThanhManager.EndRegister();
                } else {
                    ChiemThanhManager.StartRegister();
                }
                Service.send_notice_box(conn, "Đã thực hiện " + (ChiemThanhManager.isRegister ? "mở" : "đóng") + " đăng kí chiếm thành");
                break;
            }
            case 20: {
                Service.send_notice_box(conn, "Chức năng đang được hoàn thiện.");
                break;
            }
            case 21: {
                if (conn.ac_admin < 4) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Service.send_box_input_text(conn, 21, "Dịch chuyển map",
                        new String[]{"Nhập idMap", "Nhập tọa độ x", "Nhập tọa độ y"});
                break;
            }
            case 22: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.gI().load_config();
                break;
            }
            case 23: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.logErrorLogin = !Manager.logErrorLogin;
                Service.send_notice_box(conn, "Bạn đã " + (Manager.logErrorLogin ? "Bật" : "Tắt") + " log error");
                break;
            }
            case 24: {
                Service.send_box_input_text(conn, 24, "Disconnect", new String[]{"Nhập loại :", "Nhập Tên :"});
                break;
            }
            case 25: {
                String ssss = "Start Check \n-----------------------------\n";
                try {
                    Message m = new Message(53);
                    m.writer().writeUTF("check log");
                    m.writer().writeByte(1);
                    int mapnulls = 0;
                    int mapnull = 0;
                    int pnull = 0;
                    ssss += "\nvo ne";
                    for (Map[] map : Map.entrys) {
                        if (map == null) {
                            mapnulls++;
                            continue;
                        }
                        for (Map map0 : map) {
                            if (map0 == null) {
                                mapnull++;
                                continue;
                            }
                            for (int i = 0; i < map0.players.size(); i++) {
                                if (map0.players.get(i) == null || map0.players.get(i).conn == null) {
                                    pnull++;
                                    continue;
                                }
                                map0.players.get(i).conn.addmsg(m);
                            }
                        }
                    }
                    ssss += "\n" + mapnulls + " Map[]Null";
                    ssss += "\n" + mapnull + " MapNull";
                    ssss += "\n" + pnull + " PlayerNull";
                    m.cleanup();
                } catch (Exception ex) {
                    Service.send_notice_box(conn, "Lỗi: " + ex.getMessage());
                    ex.printStackTrace();
                    StackTraceElement[] stackTrace = ex.getStackTrace(); // Lấy thông tin ngăn xếp gọi hàm

                    for (StackTraceElement element : stackTrace) {
                        ssss += ("Class: " + element.getClassName());
                        ssss += ("\nMethod: " + element.getMethodName());
                        ssss += ("\nFile: " + element.getFileName());
                        ssss += ("\nLine: " + element.getLineNumber());
                        ssss += ("------------------------\n");
                    }

                }
                Save_Log.process("checkbug.txt", ssss);
                break;
            }
            case 26: {
                String ssss = "Start Fix \n-----------------------------\n";
                try {
                    Message m = new Message(53);
                    m.writer().writeUTF("check log");
                    m.writer().writeByte(1);
                    int mapnulls = 0;
                    int mapnull = 0;
                    int pnull = 0;
                    ssss += "\nvo ne";
                    for (Map[] map : Map.entrys) {
                        if (map == null) {
                            mapnulls++;
                            continue;
                        }
                        for (Map map0 : map) {
                            if (map0 == null) {
                                mapnull++;
                                continue;
                            }
                            for (int i = map0.players.size() - 1; i >= 0; i--) {
                                if (map0.players.get(i) == null || map0.players.get(i).conn == null) {
                                    map0.players.remove(i);
                                }
                            }
                        }
                    }
                    ssss += "\n" + mapnulls + " Map[]Null";
                    ssss += "\n" + mapnull + " MapNull";
                    ssss += "\n" + pnull + " PlayerNull";
                    m.cleanup();
                } catch (Exception ex) {
                    Service.send_notice_box(conn, "Lỗi: " + ex.getMessage());
                    ex.printStackTrace();
                    StackTraceElement[] stackTrace = ex.getStackTrace(); // Lấy thông tin ngăn xếp gọi hàm

                    for (StackTraceElement element : stackTrace) {
                        ssss += ("Class: " + element.getClassName());
                        ssss += ("\nMethod: " + element.getMethodName());
                        ssss += ("\nFile: " + element.getFileName());
                        ssss += ("\nLine: " + element.getLineNumber());
                        ssss += ("------------------------\n");
                    }

                }
                Service.send_notice_box(conn, "xong");
                Save_Log.process("checkbug.txt", ssss);
                break;
            }
            case 27: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Service.send_box_input_text(conn, 99, "Nhập thông tin",
                        new String[]{"Tên nhân vật", "Số tiền", "Coin"});

                break;
            }
        }
    }

    private static void Menu_Zulu(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                switch (conn.p.clazz) {
                    case 0: {
                        Service.send_msg_data(conn, 23, "tocchienbinh");
                        break;
                    }
                    case 1: {
                        Service.send_msg_data(conn, 23, "tocsatthu");
                        break;
                    }
                    case 2:
                    case 3: {
                        Service.send_msg_data(conn, 23, "tocphapsu");
                        break;
                    }
                }
                break;
            }
            case 1: {
                if (conn.p.get_ngoc() >= 150) {
                    if (conn.p.maxInventory < 126) {
                        conn.p.maxInventory = 126;
                        conn.p.item.inventory3 = Arrays.copyOf(conn.p.item.inventory3, 126);
                        conn.p.update_ngoc(-150);
                        conn.p.item.char_inventory(3);
                        conn.p.item.char_inventory(4);
                        conn.p.item.char_inventory(7);
                        Service.send_notice_box(conn, "Đã mở rộng hành trang");
                        Service.send_char_main_in4(conn.p);
                    }
                } else {
                    Service.send_notice_box(conn, conn.language.khongdungoc);
                }
                break;
            }
        }
    }

    private static void Menu_ChangeZone(Session conn) throws IOException {
        Map[] map = Map.get_map_by_id(conn.p.map.map_id);
        if (map != null) {
            FakePlayerBotManager fakeBots = FakePlayerBotManager.gI();
            Message m = new Message(54);
            m.writer().writeByte(conn.p.map.maxzone);
            //
            for (int i = 0; i < conn.p.map.maxzone; i++) {
                int shownPlayers = map[i].players.size() + fakeBots.countBotsInMap(map[i]);
                if (shownPlayers > (map[i].maxplayer - 2)) {
                    m.writer().writeByte(2); // redzone
                } else if (shownPlayers >= (map[i].maxplayer / 2)) {
                    m.writer().writeByte(1); // yellow zone
                } else {
                    m.writer().writeByte(0); // green zone
                }
                if (i == 4 && Map.is_map_chiem_mo(conn.p.map, false)) {
                    m.writer().writeByte(i);
                } else if (i == 5 && conn.p.map.is_map_buon()) {
                    m.writer().writeByte(i);
                } else if (i == 1 && !Map.is_map_not_zone2(conn.p.map.map_id)) {
                    m.writer().writeByte(3);
                } else {
                    m.writer().writeByte(0);
                }
            }
            for (int i = 0; i < conn.p.map.maxzone; i++) {
                int shownPlayers = map[i].players.size() + fakeBots.countBotsInMap(map[i]);
                if (conn.p.map.is_map_buon() && i == 5) {
                    m.writer().writeUTF("Khu đi buôn");
                } else {
                    m.writer().writeUTF(
                            "Khu " + (map[i].zone_id + 1) + " (" + shownPlayers + ")");
                }
            }
            conn.addmsg(m);
            m.cleanup();
        }
    }

    private static void Menu_DaDichChuyen10(Session conn, byte index) throws IOException {
        if (conn.p.item.wear[11] != null && (conn.p.item.wear[11].id == 3599 || conn.p.item.wear[11].id == 3593
                || conn.p.item.wear[11].id == 3596)) {
            return;
        }
        Vgo vgo = null;
        switch (index) {
            case 0: {
                vgo = new Vgo();
                vgo.id_map_go = 1;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 1: {
                vgo = new Vgo();
                vgo.id_map_go = 33;
                vgo.x_new = 432;
                vgo.y_new = 480;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 2: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
                    return;
                }
                vgo = new Vgo();
                vgo.id_map_go = 82;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 3: {
                vgo = new Vgo();
                vgo.id_map_go = 4;
                vgo.x_new = 888;
                vgo.y_new = 672;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 4: {
                vgo = new Vgo();
                vgo.id_map_go = 5;
                vgo.x_new = 1056;
                vgo.y_new = 864;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 5: {
                vgo = new Vgo();
                vgo.id_map_go = 8;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 6: {
                vgo = new Vgo();
                vgo.id_map_go = 9;
                vgo.x_new = 1243;
                vgo.y_new = 876;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 7: {
                vgo = new Vgo();
                vgo.id_map_go = 11;
                vgo.x_new = 286;
                vgo.y_new = 708;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 8: {
                vgo = new Vgo();
                vgo.id_map_go = 12;
                vgo.x_new = 240;
                vgo.y_new = 732;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 9: {
                vgo = new Vgo();
                vgo.id_map_go = 13;
                vgo.x_new = 150;
                vgo.y_new = 979;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 10: {
                vgo = new Vgo();
                vgo.id_map_go = 15;
                vgo.x_new = 469;
                vgo.y_new = 1099;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 11: {
                vgo = new Vgo();
                vgo.id_map_go = 16;
                vgo.x_new = 673;
                vgo.y_new = 1093;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 12: {
                vgo = new Vgo();
                vgo.id_map_go = 17;
                vgo.x_new = 660;
                vgo.y_new = 612;
                conn.p.change_map(conn.p, vgo);
                break;
            }
        }
    }

    private static void Menu_DaDichChuyen33(Session conn, byte index) throws IOException {
        if (conn.p.item.wear[11] != null && (conn.p.item.wear[11].id == 3599 || conn.p.item.wear[11].id == 3593
                || conn.p.item.wear[11].id == 3596)) {
            return;
        }
        Vgo vgo = null;
        switch (index) {
            case 0: {
                vgo = new Vgo();
                vgo.id_map_go = 67;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 1: {
                vgo = new Vgo();
                vgo.id_map_go = 33;
                vgo.x_new = 432;
                vgo.y_new = 480;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 2: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
                    return;
                }
                vgo = new Vgo();
                vgo.id_map_go = 82;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 3: {
                vgo = new Vgo();
                vgo.id_map_go = 20;
                vgo.x_new = 787;
                vgo.y_new = 966;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 4: {
                vgo = new Vgo();
                vgo.id_map_go = 22;
                vgo.x_new = 120;
                vgo.y_new = 678;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 5: {
                vgo = new Vgo();
                vgo.id_map_go = 24;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 6: {
                vgo = new Vgo();
                vgo.id_map_go = 26;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 7: {
                vgo = new Vgo();
                vgo.id_map_go = 29;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 8: {
                vgo = new Vgo();
                vgo.id_map_go = 31;
                vgo.x_new = 360;
                vgo.y_new = 624;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 9: {
                vgo = new Vgo();
                vgo.id_map_go = 37;
                vgo.x_new = 150;
                vgo.y_new = 674;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 10: {
                vgo = new Vgo();
                vgo.id_map_go = 39;
                vgo.x_new = 199;
                vgo.y_new = 882;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 11: {
                vgo = new Vgo();
                vgo.id_map_go = 41;
                vgo.x_new = 187;
                vgo.y_new = 462;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 12: {
                vgo = new Vgo();
                vgo.id_map_go = 43;
                vgo.x_new = 228;
                vgo.y_new = 43;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 13: {
                vgo = new Vgo();
                vgo.id_map_go = 45;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 14: {
                vgo = new Vgo();
                vgo.id_map_go = 50;
                vgo.x_new = 300;
                vgo.y_new = 300;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            default: {
                Service.send_notice_box(conn, "Chưa có chức năng");
                break;
            }
        }
    }

    private static void Menu_DaDichChuyen55(Session conn, byte index) throws IOException {
        if (conn.p.item.wear[11] != null && (conn.p.item.wear[11].id == 3599 || conn.p.item.wear[11].id == 3593
                || conn.p.item.wear[11].id == 3596)) {
            return;
        }
        Vgo vgo = null;
        switch (index) {
            case 0: {
                vgo = new Vgo();
                vgo.id_map_go = 67;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 1: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
                    return;
                }
                vgo = new Vgo();
                vgo.id_map_go = 82;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 2: {
                vgo = new Vgo();
                vgo.id_map_go = 74;
                vgo.x_new = 258;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 3: {
                vgo = new Vgo();
                vgo.id_map_go = 77;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 4: {
                vgo = new Vgo();
                vgo.id_map_go = 93;
                vgo.x_new = 462;
                vgo.y_new = 342;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 5: {
                vgo = new Vgo();
                vgo.id_map_go = 94;
                vgo.x_new = 306;
                vgo.y_new = 240;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 6: {
                vgo = new Vgo();
                vgo.id_map_go = 95;
                vgo.x_new = 390;
                vgo.y_new = 162;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 7: {
                vgo = new Vgo();
                vgo.id_map_go = 96;
                vgo.x_new = 198;
                vgo.y_new = 666;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 8: {
                vgo = new Vgo();
                vgo.id_map_go = 97;
                vgo.x_new = 432;
                vgo.y_new = 168;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 9: {
                vgo = new Vgo();
                vgo.id_map_go = 98;
                vgo.x_new = 270;
                vgo.y_new = 132;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 10: {
                vgo = new Vgo();
                vgo.id_map_go = 33;
                vgo.x_new = 432;
                vgo.y_new = 480;
                conn.p.change_map(conn.p, vgo);
                break;
            }
        }
    }

    private static void Menu_Hammer(Session conn, byte index, byte idmenu) throws IOException {
        if (idmenu == 0) {
            switch (index) {
                case 0: {
                    Service.send_box_UI(conn, 5);
                    break;
                }
                case 1: {
                    Service.send_box_UI(conn, 6);
                    break;
                }
                case 2: {
                    Service.send_box_UI(conn, 7);
                    break;
                }
                case 3: {
                    Service.send_box_UI(conn, 8);
                    break;
                }
                case 4: // chế tạo tinh tú
                {
                    send_menu_select(conn, -5, new String[]{"Chiến binh", "Sát thủ", "Pháp sư", "Xạ thủ"}, (byte) 1);
                    break;
                }
                case 5:// nâng cấp tinh tú
                {
                    conn.p.isCreateArmor = false;
                    conn.p.isCreateItemStar = true;
                    Service.send_box_UI(conn, 33);
                    break;
                }
                case 6: { // giap sieu nhan
                    if (conn.p.item.wear[20] == null) {
                        Service.send_notice_box(conn, "Không thể thực hiện");
                    } else {
                        Item3 buffer = conn.p.item.wear[20];
                        conn.p.item.wear[20] = null;
                        conn.p.item.add_item_inventory3(buffer);
                        conn.p.item.char_inventory(3);
                        conn.p.fashion = Part_fashion.get_part(conn.p);
                        Service.send_wear(conn.p);
                        Service.send_char_main_in4(conn.p);
                        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                        Service.send_notice_box(conn, "Tháo thành công");
                    }
                    break;
                }
                case 7: { // thao danh hiẹu
                    if (conn.p.item.wear[19] == null) {
                        Service.send_notice_box(conn, "Không thể thực hiện");
                    } else {
                        Item3 buffer = conn.p.item.wear[19];
                        conn.p.item.wear[19] = null;
                        conn.p.item.add_item_inventory3(buffer);
                        conn.p.item.char_inventory(3);
                        conn.p.fashion = Part_fashion.get_part(conn.p);
                        Service.send_wear(conn.p);
                        Service.send_char_main_in4(conn.p);
                        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                        Service.send_notice_box(conn, "Tháo thành công");
                    }
                    break;
                }
                case 8: {
                    String[] nemu = new String[]{"Kháng băng", "Kháng lửa", "Kháng điện", "Kháng độc"};
                    send_menu_select(conn, -5, nemu, (byte) 15);
                    break;
                }
                case 9: {
                    conn.p.isCreateArmor = true;
                    Service.send_box_UI(conn, 33);
                    break;
                }
                case 10: {
                    String[] nemu = new String[]{"Sách vật lý", "Sách ma pháp"};
                    send_menu_select(conn, -5, nemu, (byte) 14);
                    break;
                }
            }
        } else if (idmenu == 1) {
            conn.p.CountItemStarCreate = 1;
            if (conn.ac_admin > 3) {
                send_menu_select(conn, -5, new String[]{"Tao 1 mon", "Tao 10 mon"}, (byte) (20 + index));
                return;
            }
            String[] nemu = new String[]{"Nón", "Áo", "Quần", "Giày", "Găng tay", "Nhẫn", "Vũ khí", "Dây chuyền"};
            send_menu_select(conn, -5, nemu, (byte) (10 + index));
        } else if (idmenu >= 20 && idmenu <= 23) {
            conn.p.CountItemStarCreate = (byte) (index == 1 ? 10 : 1);
            String[] nemu = new String[]{"Non", "Ao", "Quan", "Giay", "Gang tay", "Nhan", "Vu khi", "Day chuyen"};
            send_menu_select(conn, -5, nemu, (byte) (10 + idmenu - 20));
        } else if (idmenu >= 10 && idmenu <= 13) {
            conn.p.isCreateItemStar = true;
            conn.p.ClazzItemStar = (byte) (idmenu - 10);
            conn.p.TypeItemStarCreate = index;
            if (conn.p.CountItemStarCreate < 1) {
                conn.p.CountItemStarCreate = 1;
            }
            Service.send_box_UI(conn, 40 + index);
        } else if (idmenu == 14) {
            Service.send_box_input_yesno(conn, -123 + index, "Giá ghép sách là 30 ngọc, bạn có muốn tiếp tục không?");
        } else if (idmenu == 15) {
            conn.p.type_armor_create = index;
            String[] nemu = new String[]{"Giáp siêu nhân bạc", "Giáp siêu nhân tím", "Giáp siêu nhân xanh", "Giáp siêu nhân vàng"};
            send_menu_select(conn, -5, nemu, (byte) 16);
        } else if (idmenu == 16) {
            conn.p.id_armor_create = index;
            conn.p.isCreateArmor = true;
            Service.send_box_UI(conn, 50);
        }
    }

    private static void Menu_Doubar(Session conn, byte index, byte idmenu) throws IOException {
        if (idmenu == 0) {
            switch (index) {
                case 0: {
                    Service.send_box_UI(conn, 1);
                    break;
                }
                case 1: {
                    Service.send_box_UI(conn, 2);
                    break;
                }
                case 2: {
                    Service.send_box_UI(conn, 3);
                    break;
                }
                case 3: {
                    Service.send_box_UI(conn, 4);
                    break;
                }
                case 4: {
                    Item3 item = conn.p.item.wear[12];
                    ;
                    if (item != null) {
                        Service.send_box_input_yesno(conn, 12, "Bạn có muốn tháo " + item.name);
                    }
                    break;
                }
                case 5: {
                    conn.p.down_horse_clan();
                    break;
                }
                case 6: {
                    if (BossServer.listBossActive().length == 0) {
                        Service.send_notice_box(conn, "Hiện tại tất cả boss đều còn sống");
                        return;
                    }
                    send_menu_select(conn, -4, BossServer.listBossActive(), (byte) 1);
                    break;
                }
            }
        } else if (idmenu == 1) {
            send_menu_select(conn, -4, BossServer.sendInfo(index), (byte) 2);
        }
    }

    private static void Menu_keva(Session conn, byte index) throws IOException {
        if (1 == 1) {
            return;
        }
        switch (index) {
            case 0: { // cua hang potion
                Service.send_box_UI(conn, 0);
                break;
            }
        }
    }

    private static void Menu_Mr_Haku(Session conn, byte index) throws IOException {
        if (index == 0) {
            if (conn.status != 0) {
                Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
                return;
            }
            if (conn.p.get_vang() < 500) {
                Service.send_notice_box(conn, "Không đủ 500 vàng");
                return;
            }
            conn.p.update_vang(-500, "Trừ %s vàng NPC Haku");
            Vgo vgo = new Vgo();
            vgo.id_map_go = 67;
            vgo.x_new = 576;
            vgo.y_new = 222;
            conn.p.change_map(conn.p, vgo);
        }
    }

    private static void Menu_Lisa(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: { // cua hang potion
                Service.send_box_UI(conn, 0);
                break;
            }
            case 1: {
                Lottery.sendMessage(conn, (byte) 0);
                break;
            }
            case 2: { // cua hang potion
                Service.send_box_input_text(conn, 22, "% thuế", new String[]{"Nhập % thuế 5 - 15"});
                break;
            }
            case 3: {
                MemberBattlefields temp = ChienTruong.gI().get_bxh(conn.p.name);
                if (temp != null) {
                    switch (ChienTruong.gI().get_index_bxh(temp)) {
                        case 0: {
                            short[] id_ = new short[]{3, 2, 53, 54, 18};
                            short[] id2_ = new short[]{5, 5, 1, 1, 10};
                            short[] id3_ = new short[]{7, 7, 4, 4, 4};
                            for (int i = 0; i < id_.length; i++) {
                                Item47 it = new Item47();
                                it.id = id_[i];
                                it.quantity = id2_[i];
                                conn.p.item.add_item_inventory47(id3_[i], it);
                            }
                            break;
                        }
                        case 1:
                        case 2: {
                            short[] id_ = new short[]{3, 2, 18};
                            short[] id2_ = new short[]{5, 5, 10};
                            short[] id3_ = new short[]{7, 7, 4};
                            for (int i = 0; i < id_.length; i++) {
                                Item47 it = new Item47();
                                it.id = id_[i];
                                it.quantity = id2_[i];
                                conn.p.item.add_item_inventory47(id3_[i], it);
                            }
                            break;
                        }
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9: {
                            short[] id_ = new short[]{3, 18};
                            short[] id2_ = new short[]{5, 10};
                            short[] id3_ = new short[]{7, 4};
                            for (int i = 0; i < id_.length; i++) {
                                Item47 it = new Item47();
                                it.id = id_[i];
                                it.quantity = id2_[i];
                                conn.p.item.add_item_inventory47(id3_[i], it);
                            }
                            break;
                        }
                    }
                } else {
                    Service.send_notice_box(conn, "Không có tên trong danh sách");
                }
                break;
            }
            case 4: {
                ChiemThanhManager.NhanQua(conn.p);
                break;
            }
        }
    }

    private static void Menu_CayThong(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 1) {
            switch (index) {
                case 0:
                case 1:
                case 2:
                case 3: {
                    int quant = conn.p.item.total_item_by_id(4, (113 + index));
                    if (quant > 0) {
                        //
                        short[] id_4 = new short[]{2, 5, 52, 142, 225, 271};
                        short[] id_7 = new short[]{0, 4, 23, 34, 39, 352, 357, 362, 367, 372, 377, 382, 387, 392, 397, 402,
                            407, 412,};
                        HashMap<Short, Short> list_4 = new HashMap<>();
                        HashMap<Short, Short> list_7 = new HashMap<>();
                        for (int i = 0; i < quant; i++) {
                            if (conn.p.item.get_inventory_able() > 1) {
                                if (80 > Util.random(100)) {
                                    Item47 it = new Item47();
                                    it.category = 4;
                                    it.id = id_4[Util.random(id_4.length)];
                                    it.quantity = (short) Util.random(1, 3);
                                    if (!list_4.containsKey(it.id)) {
                                        list_4.put(it.id, it.quantity);
                                    } else {
                                        short quant_ = it.quantity;
                                        list_4.put(it.id, (short) (list_4.get(it.id) + quant_));
                                    }
                                    conn.p.item.add_item_inventory47(4, it);
                                } else {
                                    Item47 it = new Item47();
                                    it.category = 7;
                                    it.id = id_7[Util.random(id_7.length)];
                                    it.quantity = (short) Util.random(1, 2);
                                    if (!list_7.containsKey(it.id)) {
                                        list_7.put(it.id, it.quantity);
                                    } else {
                                        short quant_ = it.quantity;
                                        list_7.put(it.id, (short) (list_7.get(it.id) + quant_));
                                    }
                                    conn.p.item.add_item_inventory47(7, it);
                                }
                            }
                        }
                        //
                        Noel.add_caythong(conn.p.name, quant);
                        conn.p.item.remove(4, (113 + index), quant);
                        String item_receiv = "\n";
                        for (Entry<Short, Short> en : list_4.entrySet()) {
                            item_receiv += ItemTemplate4.item.get(en.getKey()).getName() + " " + en.getValue() + "\n";
                        }
                        for (Entry<Short, Short> en : list_7.entrySet()) {
                            item_receiv += ItemTemplate7.item.get(en.getKey()).getName() + " " + en.getValue() + "\n";
                        }
                        Service.send_notice_box(conn, "Trang trí thành công " + quant + " lần và nhận được:" + item_receiv);
                    } else {
                        Service.send_notice_box(conn, "Không đủ trong hành trang!");
                    }
                    break;
                }
                case 4: {
                    send_menu_select(conn, 120, Noel.get_top_caythong());
                    break;
                }
                default: {
                    Service.send_notice_box(conn, "Đang bảo trì");
                    break;
                }
            }
        }
    }

    private static void Menu_ThaoKhamNgoc(Session conn, byte index) throws IOException {
        if (!conn.p.list_thao_kham_ngoc.isEmpty()) {
            if (conn.p.item.get_inventory_able() < 3) {
                Service.send_notice_box(conn, "Hành trang không đủ chỗ");
                return;
            }
            Item3 it = conn.p.list_thao_kham_ngoc.get(index);
            if (it != null) {
                for (int i = it.op.size() - 1; i >= 0; i--) {
                    byte id = it.op.get(i).id;
                    if (id == 58 || id == 59 || id == 60) {
                        if (it.op.get(i).getParam(0) != -1) {
                            Item47 it_add = new Item47();
                            it_add.id = (short) (it.op.get(i).getParam(0));
                            it_add.quantity = 1;
                            it_add.category = 7;
                            conn.p.item.add_item_inventory47(7, it_add);
                        }
                        it.op.get(i).setParam(-1);
                    } else if (id >= 100 && id <= 107) {
                        it.op.remove(i);
                    }
                }
                Service.send_wear(conn.p);
                Service.send_notice_box(conn, "Tháo thành công");
            }
        }
    }

    private static void Menu_DoiDongMeDaySTG(Session conn, byte index) throws IOException {
        if (conn.p.item.wear != null && conn.p.item.wear.length > 12 && CheckItem.isMeDay(conn.p.item.wear[12].id)) {
            Service.send_box_input_yesno(conn, 94, "Bạn có chắc chắn muốn đổi?");
        } else {
            Service.send_notice_box(conn, "Không có vật phẩm phù hợp!");
        }
    }

    private static void Menu_Nang_Skill(Session conn, byte index) throws IOException {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.skill_110[conn.p.id_temp_byte] >= 10) {
            conn.p.id_temp_byte = -1;
            Service.send_notice_box(conn, "Kỹ năng được nâng cấp tối đa");
            return;
        }
        int level = conn.p.skill_110[conn.p.id_temp_byte];
        String name_book = "";
        if (conn.p.id_temp_byte == 1) {
            name_book = switch (conn.p.clazz) {
                case 0 ->
                    "sách học kiếm địa chấn";
                case 1 ->
                    "sách học thần tốc";
                case 2 ->
                    "sách học cơn phẫn nộ";
                case 3 ->
                    "sách học súng điện từ";
                default ->
                    name_book;
            };
        } else if (conn.p.id_temp_byte == 0) {
            name_book = switch (conn.p.clazz) {
                case 0 ->
                    "sách học bão lửa";
                case 1 ->
                    "sách học bão độc";
                case 2 ->
                    "sách học băng trận";
                case 3 ->
                    "sách học súng thần công";
                default ->
                    name_book;
            };
        }
        String format = String.format("Để nâng từ cấp %s lên cấp %s bạn cần %s sách %s và %s ngọc."
                + " Bạn có muốn thực hiện", level, level + 1, level + 1, name_book, level * 5 + 10);
        if (index == 0) {
            Service.send_box_input_yesno(conn, -121, format);
        } else if (index == 1) {
            Service.send_box_input_yesno(conn, -120, format);
        }
    }

    private static void Menu_DoiDongMeDaySTPT(Session conn, byte index) throws IOException {
        if (conn.p.item.wear != null && conn.p.item.wear.length > 12 && CheckItem.isMeDay(conn.p.item.wear[12].id)) {
            Service.send_box_input_yesno(conn, 98, "Bạn có chắc chắn muốn đổi?");
        } else {
            Service.send_notice_box(conn, "Không có vật phẩm phù hợp!");
        }
    }

    private static final int[][] coin_to_gems = {
        {10000, 2500},
        {20000, 5200},
        {50000, 13000},
        {100000, 26500},
        {200000, 53500},
        {500000, 150000}
    };
    private static final int[][] coin_to_gold = {
        {10000, 5000000},
        {20000, 10200000},
        {50000, 26000000},
        {100000, 53000000},
        {200000, 108000000},
        {500000, 275000000}
    };

    private static void Menu_ADMIN_SHARINGAN(Session conn, int idNpc, byte index, byte idMenu) {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        try {
            if (idMenu == 0) {
                switch (index) {
                    case 0: {
                        send_menu_select(conn, -127, new String[]{"Hướng đẫn", "Nhận nhiệm vụ", "Huỷ nhiệm vụ", "Trả nhiệm vụ", "Thông tin"}, (byte) 1);
                        break;
                    }
                    case 1: {
                        String[] menu = new String[coin_to_gems.length];
                        for (int i = 0; i < coin_to_gems.length; i++) {
                            menu[i] = "Đổi " + Util.number_format(coin_to_gems[i][0]) + " coin lấy " + Util.number_format(coin_to_gems[i][1]) + " ngọc";
                        }
                        send_menu_select(conn, -127, menu, (byte) 3);
                        break;
                    }
                    case 2: {
                        String[] menu = new String[coin_to_gold.length];
                        for (int i = 0; i < coin_to_gold.length; i++) {
                            menu[i] = "Đổi " + Util.number_format(coin_to_gold[i][0]) + " coin lấy " + Util.number_format(coin_to_gold[i][1]) + " vàng";
                        }
                        send_menu_select(conn, -127, menu, (byte) 4);
                        break;
                    }
                    case 3: {
                        send_menu_select(conn, -127, new String[]{"Đổi Áo choàng tỷ phú (" + (5 - Manager.gI().ty_phu.size()) + ")",
                            "Đổi Áo choàng triệu phú (" + (10 - Manager.gI().trieu_phu.size()) + ")",
                            "Đổi Áo choàng đại gia (" + (20 - Manager.gI().dai_gia.size()) + ")",
                            "Hướng dẫn", "Shop"}, (byte) 5);
                        break;
                    }
                    case 4: {
                        send_menu_select(conn, -127, new String[]{
                            "Quà mốc 200.000",
                            "Quà mốc 500.000",
                            "Quà mốc 1.000.000",
                            "Quà mốc 2.000.000",
                            "Quà mốc 3.000.000",
                            "Quà mốc 5.000.000",
                            "Quà mốc 7.000.000",
                            "Quà mốc 10.000.000",
                            "Quà mốc 15.000.000",}, (byte) 7);
                        break;
                    }case 5: {
                        send_menu_select(conn, -127, new String[]{
                            "x10 rương black 150.000 coin",
                            "x20 rương black 300.000 coin",
                            "x30 rương black 450.000 coin",
                            "x100 rương black 1.350.000 coin",}, (byte) 8);
                        break;
                    }
                    case 6: {
                        send_menu_select(conn, -127, new String[]{
                            "x100 ngọc khảm câp 5 các loại 499.999 coin",}, (byte) 9);
                        break;
                    }
                    case 7: {
                        send_menu_select(conn, -127, new String[]{
                            "x32.000 hồ quang bạc cấp một 200.000 coin",
                            "x32.000 hồ quang tím cấp một 200.000 coin",
                            "x32.000 hồ quang xanh cấp một 200.000 coin",
                            "x32.000 hồ quang vàng cấp một 200.000 coin",
                            
                            "x32.000 hồ quang bạc cấp hai 300.000 coin",
                            "x32.000 hồ quang tím cấp hai 300.000 coin",
                            "x32.000 hồ quang xanh cấp hai 300.000 coin",
                            "x32.000 hồ quang vàng cấp hai 300.000 coin",
                            
                            "x32.000 hồ quang bạc cấp ba 500.000 coin",
                            "x32.000 hồ quang tím cấp ba 500.000 coin",
                            "x32.000 hồ quang xanh cấp ba 500.000 coin",
                            "x32.000 hồ quang vàng cấp ba 500.000 coin",}, (byte) 10);
                        break;
                    }
//                    case 4: {
//                        Admin.quatopLevel(conn);
//                        break;
//                    }
//                    case 5: {
//                        Admin.quatopEvent(conn);
//                        break;
//                    }
//                    case 6: {
//                        Service.send_box_UI(conn, 49);
//                        break;
//                    }
//                    case 7: {
//                        send_menu_select(conn, -127, new String[]{
//                            "Mũ (200k ngọc)",
//                            "Áo (200k ngọc)",
//                            "Quần (200k ngọc)",
//                            "Giày (200k ngọc)",
//                            "Găng tay (200k ngọc)",
//                            "Nhẫn (200k ngọc)",
//                            "Vũ khí (500k ngọc)",
//                            "Dây chuyền (200k ngọc)",}, (byte) 6);
//                        break;
//                    }
                }
            }else if (idMenu == 10) {
                int totalCoin = conn.p.getCoin();
                switch (index) {
                    case 0: {
                        if(totalCoin  < 200000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-200000);
                        conn.p.item.add_item_inventory47((short)481, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)481, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 1: {
                        if(totalCoin  < 200000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-200000);
                        conn.p.item.add_item_inventory47((short)482, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)482, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 2: {
                        if(totalCoin  < 200000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-200000);
                        conn.p.item.add_item_inventory47((short)483, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)483, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 3: {
                        if(totalCoin  < 200000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-200000);
                        conn.p.item.add_item_inventory47((short)484, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)484, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 4: {
                        if(totalCoin  < 300000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-300000);
                        conn.p.item.add_item_inventory47((short)485, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)485, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 5: {
                        if(totalCoin  < 300000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-300000);
                        conn.p.item.add_item_inventory47((short)486, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)486, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 6: {
                        if(totalCoin  < 300000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-300000);
                        conn.p.item.add_item_inventory47((short)487, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)487, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 7: {
                        if(totalCoin  < 300000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-300000);
                        conn.p.item.add_item_inventory47((short)488, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)488, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 8: {
                        if(totalCoin  < 500000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-500000);
                        conn.p.item.add_item_inventory47((short)489, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)489, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 9: {
                        if(totalCoin  < 500000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-500000);
                        conn.p.item.add_item_inventory47((short)490, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)490, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 10: {
                        if(totalCoin  < 500000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-500000);
                        conn.p.item.add_item_inventory47((short)491, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)491, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 11: {
                        if(totalCoin  < 500000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-500000);
                        conn.p.item.add_item_inventory47((short)492, (short)32000, (byte)7);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)492, (short)32000, (byte)7));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                }
            }else if (idMenu == 8) {
                int totalCoin = conn.p.getCoin();
                switch (index) {
                    case 0: {
                        if(totalCoin  < 150000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-150000);
                        conn.p.item.add_item_inventory47((short)318, (short)10, (byte)4);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)318, (short)10, (byte)4));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 1: {
                        if(totalCoin  < 300000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-300000);
                        conn.p.item.add_item_inventory47((short)318, (short)20, (byte)4);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)318, (short)20, (byte)4));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 2: {
                        if(totalCoin  < 450000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-450000);
                        conn.p.item.add_item_inventory47((short)318, (short)30, (byte)4);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)318, (short)30, (byte)4));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                    case 3: {
                        if(totalCoin  < 1350000){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-1350000);
                        conn.p.item.add_item_inventory47((short)318, (short)100, (byte)4);//con lan x50
                        List<BoxItem> ids = new ArrayList<>();
                        ids.add(new BoxItem((short)318, (short)100, (byte)4));
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                }
            } else if (idMenu == 9) {
                int totalCoin = conn.p.getCoin();
                switch (index) {
                    case 0: {
                        if(totalCoin  < 499999){
                            Service.send_notice_box(conn, "Coi của bạn không đủ số coin hiện tại của bạn là : "+totalCoin);
                            break;
                        }
                        conn.p.update_coin(-499999);
                        int[] itemIds = {356, 361, 366, 371, 376, 381, 416};
                        List<BoxItem> ids = new ArrayList<>();
                        for (int id : itemIds) {
                            conn.p.item.add_item_inventory47((short)id, (short)100, (byte)7);
                            ids.add(new BoxItem((short)id, (short)100, (byte)7));
                        }
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        break;
                    }
                }
            } else if (idMenu == 7) {
                int tongNap = conn.p.getTongNap();
                switch (index) {
                    case 0:
                        if(tongNap  < 200000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc1")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4741);
                            conn.p.item.add_item_inventory47((short)14, (short)15, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)5, (byte)7);//da chuyen hoa 3
                            conn.p.item.add_item_inventory47((short)226, (short)100, (byte)4);//ve mo ly dac biet
                            conn.p.addGiftcode("moc1");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)14, (short)15, (byte)7));
                            ids.add(new BoxItem((short)348, (short)5, (byte)7));
                            ids.add(new BoxItem((short)226, (short)100, (byte)4));
                            ids.add(new BoxItem((short)4741, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    case 1:
                        if(tongNap  < 500000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc2")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4707);//canh mua xuan
                            ShopCustom.buy(conn.p, 4761);//trung yeu tinh
                            ShopCustom.buy(conn.p, 4690);// ao choang cong tuoc
                            conn.p.item.add_item_inventory47((short)246, (short)50, (byte)4);//con lan x50
                            conn.p.item.add_item_inventory47((short)14, (short)20, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)5, (byte)7);//da chuyen hoa 3
                            conn.p.addGiftcode("moc2");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)246, (short)50, (byte)4));
                            ids.add(new BoxItem((short)14, (short)20, (byte)7));
                            ids.add(new BoxItem((short)348, (short)5, (byte)7));
                            
                            ids.add(new BoxItem((short)4690, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4707, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4761, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    case 2:
                        if(tongNap  < 1000000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc3")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4716);// khau trang xanh vv
                            conn.p.item.add_item_inventory47((short)248, (short)1, (byte)4);//con lan vv
                            conn.p.item.add_item_inventory47((short)14, (short)30, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)10, (byte)7);//da chuyen hoa 3
                            conn.p.item.add_item_inventory47((short)349, (short)3, (byte)7);//da krypton cap 1
                            conn.p.addGiftcode("moc3");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)248, (short)1, (byte)4));
                            ids.add(new BoxItem((short)14, (short)30, (byte)7));
                            ids.add(new BoxItem((short)348, (short)10, (byte)7));
                            ids.add(new BoxItem((short)349, (short)3, (byte)7));
                            
                            ids.add(new BoxItem((short)4716, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    case 3: {
                        if(tongNap  < 2000000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc4")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4719);// tai nghe mong mo vv
                            ShopCustom.buy(conn.p, 4711);// hiep si chuot vv
                            conn.p.item.add_item_inventory47((short)14, (short)30, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)15, (byte)7);//da chuyen hoa 3
                            conn.p.item.add_item_inventory47((short)349, (short)5, (byte)7);//da krypton cap 1
                            conn.p.addGiftcode("moc4");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)14, (short)30, (byte)7));
                            ids.add(new BoxItem((short)348, (short)15, (byte)7));
                            ids.add(new BoxItem((short)349, (short)5, (byte)7));
                            
                            ids.add(new BoxItem((short)4719, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4711, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    }
                    case 4: {
                        if(tongNap  < 3000000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc5")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4717); // khau trang do vv
                            ShopCustom.buy(conn.p, 4708); // pet de vv
                            conn.p.item.add_item_inventory47((short)214, (short)1, (byte)4);// long den vv
                            conn.p.item.add_item_inventory47((short)14, (short)50, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)20, (byte)7);//da chuyen hoa 3
                            conn.p.addGiftcode("moc5");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)214, (short)1, (byte)4));
                            ids.add(new BoxItem((short)14, (short)50, (byte)7));
                            ids.add(new BoxItem((short)348, (short)20, (byte)7));
                            
                            ids.add(new BoxItem((short)4717, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4708, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    }
                    case 5: {
                        if(tongNap  < 5000000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc6")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4773); // canh hoa long
                            ShopCustom.buy(conn.p, 4748); // ao choang dai gia
                            ShopCustom.buy(conn.p, 3269); // pet dai bang vv 
                            conn.p.item.add_item_inventory47((short)14, (short)70, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)30, (byte)7);//da chuyen hoa 3
                            conn.p.item.add_item_inventory47((short)350, (short)5, (byte)7);// krypton c2
                            conn.p.addGiftcode("moc6");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)14, (short)70, (byte)7));
                            ids.add(new BoxItem((short)348, (short)30, (byte)7));
                            ids.add(new BoxItem((short)350, (short)5, (byte)7));
                            
                            ids.add(new BoxItem((short)4773, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4748, (short)1, (byte)3));
                            ids.add(new BoxItem((short)3269, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    }
                    case 6: {
                        if(tongNap  < 7000000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc7")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4747); // ao trieu phu
                            ShopCustom.buy(conn.p, 4633);// mat na tho dan
                            conn.p.item.add_item_inventory47((short)313, (short)1, (byte)4);// phl vv 
                            conn.p.item.add_item_inventory47((short)14, (short)100, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)40, (byte)7);//da chuyen hoa 3
                            conn.p.item.add_item_inventory47((short)351, (short)5, (byte)7);// krypton c3
                            conn.p.addGiftcode("moc7");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)313, (short)1, (byte)4));
                            ids.add(new BoxItem((short)14, (short)100, (byte)7));
                            ids.add(new BoxItem((short)348, (short)40, (byte)7));
                            ids.add(new BoxItem((short)351, (short)5, (byte)7));
                            
                            ids.add(new BoxItem((short)4747, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4633, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        
                        break;
                    }
                    case 7: {
                        if(tongNap  < 10000000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc8")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4801); // ao ty phu
                            ShopCustom.buy(conn.p, 4617);// rong lua
                            conn.p.item.add_item_inventory47((short)14, (short)150, (byte)7);//da 3m
                            conn.p.item.add_item_inventory47((short)348, (short)50, (byte)7);//da chuyen hoa 3
                            conn.p.item.add_item_inventory47((short)351, (short)5, (byte)7);// krypton c3
                            conn.p.addGiftcode("moc8");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)14, (short)150, (byte)7));
                            ids.add(new BoxItem((short)348, (short)50, (byte)7));
                            ids.add(new BoxItem((short)351, (short)5, (byte)7));
                            
                            ids.add(new BoxItem((short)4801, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4617, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    }
                    case 8: {
                        if(tongNap  < 15000000){
                            Service.send_notice_box(conn, "Bạn chưa nạp đủ để nhận mốc này điểm nạp tích lũy của bạn là: "+tongNap);
                            break;
                        }
                        if (conn.p.hasGiftcode("moc9")) {
                            Service.send_notice_box(conn, "Bạn đã nhận quà mốc này rồi!");
                        } else {
                            ShopCustom.buy(conn.p, 4626);// phb
                            ShopCustom.buy(conn.p, 4731);// gay mat trang
                            ShopCustom.buy(conn.p, 4730);// gay trai tim
                            ShopCustom.buy(conn.p, 4635);// mat na dau si
                            conn.p.item.add_item_inventory47((short)351, (short)10, (byte)7);// krypton c3
                            conn.p.addGiftcode("moc9");
                            
                            List<BoxItem> ids = new ArrayList<>();
                            ids.add(new BoxItem((short)351, (short)10, (byte)7));
                            
                            ids.add(new BoxItem((short)4626, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4731, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4730, (short)1, (byte)3));
                            ids.add(new BoxItem((short)4635, (short)1, (byte)3));
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                        }
                        break;
                    }
                }
            } else if (idMenu == 1) {
                switch (index) {
                    case 0: {
                        String notice
                                = "Nhiệm vụ Ngày: đánh quái ngẫu nhiên theo level, tối đa ngày nhận 20 nhiệm vụ, mỗi nhiệm vụ sẽ nhận được phần thưởng kinh nghiệm, ngọc và có cơ hội nhận nguyên liệu mề đay."
                                + "\n Dễ : Vàng Ngọc + Exp" + "\n Bình Thường : Vàng Ngọc, Exp + NL mề Xanh"
                                + "\n Khó :Vàng Ngọc, Exp + NL mề Vàng" + "\n Siêu Khó : Vàng Ngọc, Exp + NL mề Tím";
                        Service.send_notice_box(conn, notice);
                        break;
                    }
                    case 1: {
                        if (conn.p.quest_daily[0] != -1) {
                            Service.send_notice_box(conn, "Đã nhận nhiệm vụ rồi!");
                        } else {
                            if (conn.p.quest_daily[4] > 0) {
                                send_menu_select(conn, idNpc, new String[]{"Cực Dễ", "Bình thường", "Khó", "Siêu Khó"}, (byte) 2);
                            } else {
                                Service.send_notice_box(conn, "Hôm nay đã hết lượt, quay lại vào ngày mai");
                            }
                        }
                        break;
                    }
                    case 2: {
                        DailyQuest.remove_quest(conn.p);
                        break;
                    }
                    case 3: {
                        DailyQuest.finish_quest(conn.p);
                        break;
                    }
                    case 4: {
                        Service.send_notice_box(conn, DailyQuest.info_quest(conn.p));
                        break;
                    }
                }
            } else if (idMenu == 2) {
                DailyQuest.get_quest(conn.p, index);
            } else if (idMenu == 3) {
                int my_coin = conn.p.getCoin();
                if (my_coin >= coin_to_gems[index][0]) {
                    conn.p.update_coin(-coin_to_gems[index][0]);
                    conn.p.update_ngoc(coin_to_gems[index][1]);
                    Service.send_notice_box(conn, "Đổi thành công");
                } else {
                    Service.send_notice_box(conn, "Không đủ coin, bạn chỉ có " + my_coin + " coin");
                }
            } else if (idMenu == 4) {
                int my_coin = conn.p.getCoin();
                if (my_coin >= coin_to_gold[index][0]) {
                    conn.p.update_coin(-coin_to_gold[index][0]);
                    conn.p.update_vang(coin_to_gold[index][1], "Nhận %s vàng từ đổi coin");
                    Service.send_notice_box(conn, "Đổi thành công");
                } else {
                    Service.send_notice_box(conn, "Không đủ coin, bạn chỉ có " + my_coin + " coin");
                }
            } else if (idMenu == 5) {
                switch (index) {
                    case 0:
                        if (Manager.hour < 10) {
                            Service.send_notice_box(conn, "Đổi thành tích mở vào 10h-23h59' hàng ngày");
                            return;
                        }
                        if (Manager.gI().thanh_tich.containsKey(conn.p.name)) {
                            Service.send_notice_box(conn, "Chỉ được đổi 1 lần");
                            return;
                        }
                        if (5 <= Manager.gI().ty_phu.size()) {
                            Service.send_notice_box(conn, "Đã hết");
                            return;
                        }
                        addInventoryItem(conn, 4746, 1000000);
                        break;
                    case 1:
                        if (Manager.hour < 10) {
                            Service.send_notice_box(conn, "Đổi thành tích mở vào 10h-23h59' hàng ngày");
                            return;
                        }
                        if (Manager.gI().thanh_tich.containsKey(conn.p.name)) {
                            Service.send_notice_box(conn, "Chỉ được đổi 1 lần");
                            return;
                        }
                        if (10 <= Manager.gI().trieu_phu.size()) {
                            Service.send_notice_box(conn, "Đã hết");
                            return;
                        }
                        addInventoryItem(conn, 4747, 500000);
                        break;
                    case 2:
                        if (Manager.hour < 10) {
                            Service.send_notice_box(conn, "Đổi thành tích mở vào 10h-23h59' hàng ngày");
                            return;
                        }
                        if (Manager.gI().thanh_tich.containsKey(conn.p.name)) {
                            Service.send_notice_box(conn, "Chỉ được đổi 1 lần");
                            return;
                        }
                        if (20 <= Manager.gI().dai_gia.size()) {
                            Service.send_notice_box(conn, "Đã hết");
                            return;
                        }
                        addInventoryItem(conn, 4748, 200000);
                        break;
                    case 3: {
                        String notice
                                = "- Có 3 mốc thành tích là Đại Gia , Triệu Phú , Tỷ Phú"
                                + "\n- Khi các bạn nạp 1 VNĐ sẽ nhận ngay 1 điểm nạp."
                                + "\n- Mốc tỷ phú yêu cầu tối thiểu 100,000 điểm nạp, số lượng 5 người"
                                + "\n- Mốc triệu phú yêu cầu tối thiểu 50,000 điểm nạp, số lượng 10 người"
                                + "\n- Mốc đại gia yêu cầu tối thiểu 20,000 điểm nạp, số lượng 20 người"
                                + "\nĐiểm nạp sẽ được reset hàng tuần. Khi đổi sẽ bị trừ điểm nạp";
                        Service.send_notice_box(conn, notice);
                        break;
                    }
                    case 4: {
//                        Service.send_box_UI(conn, 37);
                        break;
                    }
                }
            } else if (idMenu == 6) {
                switch (index) {
                    case 0, 1, 2, 3, 4, 5, 7: {
                        if (conn.p.get_ngoc() < 200000) {
                            Service.send_notice_box(conn, "Không đủ ngọc");
                            return;
                        }
                        conn.p.update_ngoc(-200000);
                        Admin.randomTT(conn, (byte) 5, index);
                        break;
                    }
                    case 6: {
                        if (conn.p.get_ngoc() < 500000) {
                            Service.send_notice_box(conn, "Không đủ ngọc");
                            return;
                        }
                        conn.p.update_ngoc(-500000);
                        Admin.randomTT(conn, (byte) 5, index);
                        Service.send_notice_box(conn, "Bạn đã mua thành công.");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addInventoryItem(Session conn, int itemId, int diemNeeded) throws IOException {
        int diem = conn.p.getDiemNap();
        if (diem >= diemNeeded) {
            ItemTemplate3 buffer = ItemTemplate3.item.get(itemId);
            Item3 itbag = new Item3();
            itbag.id = buffer.getId();
            itbag.clazz = buffer.getClazz();
            itbag.type = buffer.getType();
            itbag.level = buffer.getLevel();
            itbag.icon = buffer.getIcon();
            itbag.color = buffer.getColor();
            itbag.part = buffer.getPart();
            itbag.islock = true;
            itbag.name = buffer.getName();
            itbag.tier = 0;
            itbag.op = new ArrayList<>(buffer.getOp());
            itbag.expiry_date = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L;
            itbag.UpdateName();
            conn.p.item.add_item_inventory3(itbag);
            conn.p.item.char_inventory(3);
            conn.p.update_diem_nap(-diemNeeded);
            Manager.gI().thanh_tich.put(conn.p.name, (itemId - 4746));
            if (itemId == 4746) {
                Manager.gI().ty_phu.add(conn.p.name);
            } else if (itemId == 4747) {
                Manager.gI().trieu_phu.add(conn.p.name);
            } else {
                Manager.gI().dai_gia.add(conn.p.name);
            }
            Service.send_notice_box(conn, "Đổi thành công");
        } else {
            Service.send_notice_box(conn, "Chưa đủ " + diemNeeded + " điểm nạp, bạn chỉ có " + diem + " điểm.");
        }
    }
}
