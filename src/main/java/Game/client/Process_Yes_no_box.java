package Game.client;

import Game.core.*;
import Game.activities.ChiemThanhManager;
import Game.io.Session;
import Game.map.MapService;
import Game.template.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import Game.event.EventManager;
import Game.event.LunarNewYear;

import Game.io.Message;
import Game.map.Dungeon;
import Game.map.DungeonManager;
import Game.map.Map;

public class Process_Yes_no_box {

    public static void process(Session conn, Message m) throws IOException {
        short id = m.reader().readShort(); // id
        if (id != conn.p.ID) {
            return;
        }
        byte type = m.reader().readByte(); // type
        byte value = m.reader().readByte(); // value
        if (conn.p.isdie && type != 9) {return;}
        if (value != 1) {
            switch (type) {
                case -125: {
                    conn.p.id_remove_time_use = -1;
                    break;
                }
                case -119:
                case -118:
                case -117:
                case -115:
                case -114:
                case 126: {
                    conn.p.id_buffer_126 = -1;
                    conn.p.id_temp_byte = -1;
                    break;
                }
                case 114: {
                    conn.p.id_wing_split = -1;
                    break;
                }
                case 113: {
                    conn.p.name_mem_clan_to_appoint = "";
                    break;
                }
            }
        } else {
            switch (type) {
                case -128: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    int count_update = (conn.p.maxBox - 14) / 7;
                    int gems_need = (count_update + 1) * 20;
                    if (gems_need > conn.p.get_ngoc()) {
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                        return;
                    }
                    if (count_update < 10) {
                        conn.p.maxBox += 7;
                        conn.p.update_ngoc(-gems_need);
                        Service.send_char_main_in4(conn.p);
                        Service.send_notice_box(conn, conn.language.damoruong);
                    } else {
                        Service.send_notice_box(conn, "Rương đã mở tối đa");
                    }
                    break;
                }
                case -127: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    if (conn.p.get_ngoc() < 5000) {
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                        return;
                    }
                    if (conn.p.level < 10) {
                        Service.send_notice_box(conn, conn.language.yeucaucap + 10);
                        return;
                    }
                    if (conn.p.squire == null) {
                        Squire.create(conn.p);
                        conn.p.squire = new Squire(conn, conn.p.ID);
                        conn.p.squire.load();
                        conn.p.update_ngoc(-5000);
                        Service.send_notice_box(conn, conn.language.nhandetu);
                        Squire.callSquire(conn);
                    }
                    break;
                }
                case -126: {
                    if (conn.p.pointarena < 2) {
                        Service.send_notice_box(conn, conn.language.khongdudiem);
                        return;
                    }
                    if (conn.p.item.wear[11] != null) {
                        Service.send_notice_box(conn, conn.language.yeucauthaodo);
                        return;
                    }
                    Item3 itbag = new Item3();
                    itbag.id = 3596;
                    itbag.clazz = ItemTemplate3.item.get(3596).getClazz();
                    itbag.type = ItemTemplate3.item.get(3596).getType();
                    itbag.level = ItemTemplate3.item.get(3596).getLevel();
                    itbag.icon = ItemTemplate3.item.get(3596).getIcon();
                    itbag.op = new ArrayList<>();
                    itbag.op.addAll(ItemTemplate3.item.get(3596).getOp());
                    itbag.color = 5;
                    itbag.part = ItemTemplate3.item.get(3596).getPart();
                    itbag.tier = 0;
                    itbag.islock = true;
                    itbag.time_use = 0;
                    itbag.name = ItemTemplate3.item.get(3596).getName() + " [Khóa]";
                    itbag.UpdateName();
                    conn.p.item.wear[11] = itbag;
                    conn.p.pointarena -= 2;
                    conn.p.fashion = Part_fashion.get_part(conn.p);
                    break;
                }
                case -125: {
                    if (conn.p.id_remove_time_use != -1) {
                        Item3 it = conn.p.item.inventory3[conn.p.id_remove_time_use];
                        if (it != null && it.time_use > 0) {
                            long time = it.time_use - System.currentTimeMillis();
                            int gems = (int) (time / 3_600_000) + 1;
                            int ngoc_ = conn.p.get_ngoc();
                            if (ngoc_ > gems) {
                                it.time_use -= time;
                                conn.p.update_ngoc(-gems);
                                conn.p.id_remove_time_use = -1;
                                Service.send_notice_box(conn, "Nhận được " + it.name + " +" + it.tier + "!");
                            } else {
                                Service.send_notice_box(conn, conn.language.khongdungoc);
                            }
                        }
                    }
                    break;
                }
                case -124: {
                    if (conn.p.squire != null && conn.p.isOwner) {
                        try (Connection connnect = SQL.gI().getConnection(); PreparedStatement ps = connnect.prepareStatement("" +
                                "DELETE FROM `squire` WHERE `id` = ?;")) {
                            ps.setInt(1, conn.p.squire.ID);
                            ps.executeUpdate();
                            connnect.commit();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (conn.p.isLiveSquire) {
                            Squire.squireLeaveMap(conn.p);
                        }
                        conn.p.isLiveSquire = false;
                        conn.p.squire = null;
                        Service.send_notice_box(conn, "Đã huỷ đệ tử");
                    } else {
                        Service.send_notice_box(conn, "Chưa có đệ tử");
                    }
                    break;
                }
                case -122: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    if (conn.p.get_ngoc() > 5) {
                        if ((conn.p.item.total_item_by_id(7, 472) > 50 && conn.p.item.total_item_by_id(7, 473) > 50
                                && conn.p.item.total_item_by_id(7, 474) > 50 && conn.p.item.total_item_by_id(7, 475) > 50) || conn.ac_admin > 2) {
                            short[] id_ma_phap = new short[]{4578, 4579, 4581, 4584};
                            ItemTemplate3 temp3 = ItemTemplate3.item.get(id_ma_phap[Util.random(id_ma_phap.length)]);
                            Item3 it = new Item3();
                            it.id = temp3.getId();
                            it.name = temp3.getName();
                            it.clazz = temp3.getClazz();
                            it.type = temp3.getType();
                            it.level = temp3.getLevel();
                            it.icon = temp3.getIcon();
                            it.op = temp3.getOp();
                            it.color = 3;
                            it.islock = true;
                            it.part = temp3.getPart();

                            conn.p.item.add_item_inventory3(it);
                            conn.p.item.remove(7, 472, 50);
                            conn.p.item.remove(7, 473, 50);
                            conn.p.item.remove(7, 474, 50);
                            conn.p.item.remove(7, 475, 50);
                            conn.p.update_ngoc(-5);

                            Service.send_notice_box(conn, "Ghép thành công");
                        } else {
                            Service.send_notice_box(conn, "Bạn cần có 50 mảnh (Mảnh sách đỏ 1, Mảnh sách đỏ 2," +
                                    " Mảnh sách đỏ 3, Mảnh sách đỏ 4)");
                        }
                    } else {
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                    }
                    break;
                }
                case -123: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    if (conn.p.get_ngoc() >= 10) {
                        if ((conn.p.item.total_item_by_id(7, 476) > 50 && conn.p.item.total_item_by_id(7, 477) > 50
                                && conn.p.item.total_item_by_id(7, 478) > 50 && conn.p.item.total_item_by_id(7, 479) > 50) || conn.ac_admin > 2) {
                            short[] id_vat_ly = new short[]{4577, 4580, 4582, 4583};
                            ItemTemplate3 temp3 = ItemTemplate3.item.get(id_vat_ly[Util.random(id_vat_ly.length)]);
                            Item3 it = new Item3();
                            it.id = temp3.getId();
                            it.name = temp3.getName();
                            it.clazz = temp3.getClazz();
                            it.type = temp3.getType();
                            it.level = temp3.getLevel();
                            it.icon = temp3.getIcon();
                            it.op = temp3.getOp();
                            it.color = temp3.getColor();
                            it.islock = true;
                            it.part = temp3.getPart();
                            conn.p.item.add_item_inventory3(it);
                            conn.p.item.remove(7, 476, 50);
                            conn.p.item.remove(7, 477, 50);
                            conn.p.item.remove(7, 478, 50);
                            conn.p.item.remove(7, 479, 50);
                            conn.p.update_ngoc(-10);

                            Service.send_notice_box(conn, "Ghép thành công");
                        } else {
                            Service.send_notice_box(conn, "Bạn cần có 50 mảnh (Mảnh sách xanh 1, Mảnh sách xanh 2," +
                                    " Mảnh sách xanh 3, Mảnh sách xanh 4)");
                        }
                    } else {
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                    }
                    break;
                }
                case -121: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    int level = conn.p.skill_110[conn.p.id_temp_byte];
                    if (conn.p.skill_110[conn.p.id_temp_byte] >= 10) {
                        conn.p.id_temp_byte = -1;
                        Service.send_notice_box(conn, "Kỹ năng được nâng cấp tối đa");
                        return;
                    }
                    int id_book = -1;
                    if (conn.p.id_temp_byte == 1) {
                        id_book = switch (conn.p.clazz) {
                            case 0 -> 4577;
                            case 1 -> 4580;
                            case 2 -> 4582;
                            case 3 -> 4583;
                            default -> id_book;
                        };
                    } else if (conn.p.id_temp_byte == 0) {
                        id_book = switch (conn.p.clazz) {
                            case 0 -> 4578;
                            case 1 -> 4579;
                            case 2 -> 4581;
                            case 3 -> 4584;
                            default -> id_book;
                        };
                    }
                    if (conn.p.get_ngoc() < level * 5 + 10) {
                        conn.p.id_temp_byte = -1;
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                        return;
                    }
                    if (conn.p.item.total_item_book(5, id_book) >= (level + 1)) {
                        if (Util.nextInt(100) < 20 - level || level == 0) {
                            conn.p.skill_110[conn.p.id_temp_byte] += 1;
                            Service.send_notice_box(conn, "Nâng cấp thành công");
                            conn.p.load_skill();
                            Service.send_skill(conn.p);
                        } else {
                            Service.send_notice_box(conn, "Thất bại rồi");
                        }
                        conn.p.item.remove_item_book(id_book, 5, (level + 1));
                        conn.p.item.char_inventory(3);
                        conn.p.id_temp_byte = -1;
                        conn.p.update_ngoc(-(level * 5 + 10));
                    } else {
                        Service.send_notice_box(conn, "Không đủ sách");
                    }
                    break;
                }
                case -120: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    int level = conn.p.skill_110[conn.p.id_temp_byte];
                    if (conn.p.skill_110[conn.p.id_temp_byte] >= 10) {
                        conn.p.id_temp_byte = -1;
                        Service.send_notice_box(conn, "Kỹ năng được nâng cấp tối đa");
                        return;
                    }
                    int id_book = -1;
                    int type_book = -1;
                    if (conn.p.id_temp_byte == 0) {
                        type_book = 0;
                        id_book = switch (conn.p.clazz) {
                            case 0 -> 4577;
                            case 1 -> 4580;
                            case 2 -> 4582;
                            case 3 -> 4583;
                            default -> id_book;
                        };
                    } else if (conn.p.id_temp_byte == 1) {
                        type_book = 3;
                        id_book = switch (conn.p.clazz) {
                            case 0 -> 4578;
                            case 1 -> 4579;
                            case 2 -> 4581;
                            case 3 -> 4584;
                            default -> id_book;
                        };
                    }
                    if (conn.p.get_ngoc() < level * 5 + 10) {
                        conn.p.id_temp_byte = -1;
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                        return;
                    }
                    if (conn.p.item.total_item_book(type_book, id_book) >= (level + 1) || conn.ac_admin > 2) {
                        if (Util.nextInt(100) < 20 - level || level == 0) {
                            conn.p.skill_110[conn.p.id_temp_byte] += 1;
                            Service.send_notice_box(conn, "Nâng cấp thành công");
                            conn.p.load_skill();
                            Service.send_skill(conn.p);
                        } else {
                            Service.send_notice_box(conn, "Thất bại rồi");
                        }
                        conn.p.id_temp_byte = -1;
                        conn.p.item.remove_item_book(id_book, type_book, (level + 1));
                        conn.p.item.char_inventory(3);
                        conn.p.update_ngoc(-(level * 5 + 10));
                    } else {
                        Service.send_notice_box(conn, "Không đủ sách");
                    }
                    break;
                }
                case -119:
                case -118:
                case -117:
                case -116:
                case -115:
                case -114:
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_nobox_white(conn, "Hành trang đầy");
                        return;
                    }
                    Item3 item_remove = conn.p.item.wear[conn.p.id_temp_byte];
                    if (item_remove != null) {
                        conn.p.item.wear[conn.p.id_temp_byte] = null;
                        if (!item_remove.isWingClan()) {
                            conn.p.item.add_item_inventory3(item_remove);
                            conn.p.item.char_inventory(3);
                        }
                        conn.p.fashion = Part_fashion.get_part(conn.p);
                        conn.p.id_temp_byte = -1;
                        Service.send_wear(conn.p);
                        Service.send_char_main_in4(conn.p);
                        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                        Service.send_notice_box(conn, "Đã tháo " + item_remove.name);
                    } else {
                        Service.send_notice_nobox_white(conn, "Không thể thực hiện");
                    }
                    break;
//                case -113: {
//                    if (conn.p.get_ngoc() >= 2000) {
//                        EffTemplate eff = conn.p.get_EffDefault(-126);
//                        if (eff != null) {
//                            long time_extra = (eff.time - System.currentTimeMillis()) + (1000 * 60 * 120 - 1);
//                            if (time_extra < (1000 * 60 * 60 * 10 - 1)) {
//                                conn.p.update_ngoc(-2000);
//                                conn.p.add_EffDefault(-126, 5000, (int) time_extra);
//                                Service.send_notice_box(conn, "Thời gian chống pk còn lại " + (time_extra/60000 + 120) + " phút");
//                            } else {
//                                Service.send_notice_box(conn, "Thời gian chống pk còn lại " + (time_extra/60000) + " phút");
//                            }
//                        } else {
//                            int time = 2 * 60 * 60 * 1000;
//                            conn.p.add_EffDefault(-126, 1, time);
//                            conn.p.update_ngoc(-2000);
//                            Service.send_notice_box(conn, "Thời gian chống pk còn lại 2 giờ");
//                        }
//                    } else {
//                        Service.send_notice_box(conn, conn.language.khongdungoc);
//                    }
//                    break;
//                }
                case -112: {
                    if (conn.p.get_ngoc() < Map.NGOC_KHU_2) {
                        Service.send_notice_box(conn, "Không đủ ngọc");
                        return;
                    }
                    Map map = Map.get_map_by_id(conn.p.map.map_id)[1];
                    if (map != null && map.players.size() >= map.maxplayer) {
                        Service.send_notice_box(conn, conn.language.khuvucday);
                        return;
                    }
                    conn.p.update_ngoc(-Map.NGOC_KHU_2);
                    conn.p.add_EffDefault(-127, 1, 2 * 60 * 60 * 1000);
                    MapService.leave(conn.p.map, conn.p);
                    conn.p.map = map;
                    MapService.enter(conn.p.map, conn.p);
                    break;
                }
                case -111: {
                    if (EventManager.notCanRegister()) {
                        Service.send_notice_box(conn, "Không trong thời gian đăng ký!");
                        return;
                    }
                    if (conn.p.get_ngoc() < 50) {
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                        return;
                    }
                    LunarNewYear.add_material(conn.p.name, 0, (short) 0, 0);
                    conn.p.update_ngoc(-50);
                    Service.send_notice_box(conn, "Đăng ký thành công");
                    break;
                }
                case 11:
                case 12: {
                    Item3 item = conn.p.item.wear[type];
                    if (item != null) {
                        conn.p.item.wear[type] = null;
                        if (item.id < 3593 || item.id > 3601) {
                            conn.p.item.add_item_inventory3(item);
                        }
                        conn.p.item.char_inventory(3);
                        conn.p.fashion = Part_fashion.get_part(conn.p);
                        Service.send_wear(conn.p);
                        Service.send_char_main_in4(conn.p);
                        Service.send_notice_box(conn, conn.language.dathao + item.name);
                    }
                    break;
                }
                case 97: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    conn.p.Store_Sell_ToPL = "no name";
                    Service.send_box_input_text(conn, 20, "Bán riêng cho nhân vật", new String[]{"Tên nhân vật"});
                    break;
                }
                case 113: {
                    if (conn.p.name_mem_clan_to_appoint.isEmpty()) {
                        return;
                    }
                    if (conn.p.myclan != null && conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                        boolean suc = false;
                        for (int i = 1; i < conn.p.myclan.mems.size(); i++) {
                            if (conn.p.myclan.mems.get(i).name.equals(conn.p.name_mem_clan_to_appoint)) {
                                MemberClan temp = conn.p.myclan.mems.get(0);
                                //
                                conn.p.myclan.mems.get(i).mem_type = 127;
                                conn.p.myclan.mems.get(0).mem_type = 122;
                                //
                                conn.p.myclan.mems.set(0, conn.p.myclan.mems.get(i));
                                conn.p.myclan.mems.set(i, temp);

                                //
                                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                                MapService.send_in4_other_char(conn.p.map, conn.p, conn.p);
                                Service.send_char_main_in4(conn.p);

                                Player p0 = Map.get_player_by_name(conn.p.myclan.mems.get(0).name);
                                if (p0 != null) {
                                    MapService.update_in4_2_other_inside(p0.map, p0);
                                    MapService.send_in4_other_char(p0.map, p0, p0);
                                    Service.send_char_main_in4(p0);
                                }
                                //
                                suc = true;
                                break;
                            }
                        }
                        if (suc) {
                            Service.send_notice_box(conn, conn.language.thanhcong);
                        } else {
                            Service.send_notice_box(conn, conn.language.khongcoten);
                        }
                    } else {
                        Service.send_notice_box(conn, conn.language.coloixayra);
                    }
                    break;
                }
                case 114: {
                    Item3 item = conn.p.item.wear[10];
                    if (item != null) {
                        int quant1 = 40;
                        int quant2 = 10;
                        int quant3 = 50;
                        for (int i = 0; i < item.tier; i++) {
                            quant1 += GameSrc.wing_upgrade_material_long_khuc_xuong[i];
                            quant2 += GameSrc.wing_upgrade_material_kim_loai[i];
                            quant3 += GameSrc.wing_upgrade_material_da_cuong_hoa[i];
                            if ((i + 1) == 10 || (i + 1) == 20 || (i + 1) == 30) {
                                item.part--;
                            }
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
                        short[] id_ = new short[]{8, 9, 10, 11, 3, 0};
                        int[] quant_ = new int[]{quant1, quant1, quant1, quant1, quant2, quant3};
                        for (int i = 0; i < id_.length; i++) {
                            Item47 it = new Item47();
                            it.category = 7;
                            it.id = id_[i];
                            it.quantity = (short) quant_[i];
                            conn.p.item.add_item_inventory47(7, it);
                        }
                        conn.p.item.wear[10] = null;
                        conn.p.item.char_inventory(3);
                        conn.p.item.char_inventory(4);
                        conn.p.item.char_inventory(7);
                        Service.send_char_main_in4(conn.p);
                        Service.send_notice_box(conn, conn.language.thanhcong);
                    } else {
                        Service.send_notice_box(conn, conn.language.coloixayra);
                        conn.p.id_wing_split = -1;
                    }
                    break;
                }
                case 9: {
                    if (conn.p.map.isMapChiemThanh()) {
                        ChiemThanhManager.ActionHoiSinh(conn.p.map, conn.p);
                    } else {
                        if (conn.p.get_ngoc() >= 5) {
                            conn.p.isdie = false;
                            conn.p.hp = conn.p.body.get_HpMax();
                            conn.p.mp = conn.p.body.get_MpMax();
                            conn.p.update_ngoc(-5);
                            Service.send_char_main_in4(conn.p);
                            // chest in4
                            Service.send_combo(conn);
                            Service.usepotion(conn.p, 0, conn.p.body.get_HpMax());
                            Service.usepotion(conn.p, 1, conn.p.body.get_MpMax());
                        } else {
                            Service.send_notice_box(conn, conn.language.khongdungoc);
                        }
                    }
                    break;
                }
                case 86: {
                    Manager.gI().lucky_draw_normal.send_in4(conn.p);
                    break;
                }
                case 87: {
                    Manager.gI().lucky_draw_vip.send_in4(conn.p);
                    break;
                }
                case 94: {
                    GameSrc.ChangeCS_Medal(conn, 94);
                    break;
                }
                case 98: {
                    GameSrc.ChangeCS_Medal(conn, 98);
                    break;
                }
                case 115: {
                    if (conn.p.id_remove_time_use != -1) {
                        Item3 it = conn.p.item.inventory3[conn.p.id_remove_time_use];
                        if (it != null && it.time_use > 0) {
                            int ngoc_ = conn.p.get_ngoc();
                            if (ngoc_ > 4) {
                                long price = it.time_use - System.currentTimeMillis();
                                price /= 30_600_000;
                                price = (price > 4) ? (price + 1) : 5;
                                boolean ch = false;
                                if (ngoc_ >= price) {
                                    ch = true;
                                } else {
                                    price = ngoc_;
                                }
                                it.time_use -= (price * 30_600_000);
                                conn.p.update_ngoc(-price);
                                conn.p.id_remove_time_use = -1;
                                if (ch) {
                                    Service.send_notice_box(conn, "Nhận được " + it.name + " +" + it.tier + "!");
                                }
                            } else {
                                Service.send_notice_box(conn, conn.language.khongdungoc);
                            }
                        }
                    }
                    break;
                }
                case 116: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    if (conn.p.myclan != null && conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                        String query = "DELETE FROM `clan` WHERE `id` = " + conn.p.myclan.ID + ";";
                        try (Connection connection = SQL.gI().getConnection();PreparedStatement ps = connection.prepareStatement(query);) {
                            if (ps.executeUpdate() > 0) {
                                connection.commit();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Service.send_notice_box(conn, "Lỗi huỷ bang, chụp mành hình gửi admin");
                            return;
                        }
                        conn.p.myclan.remove_all_mem();
                        conn.p.myclan.remove_mem(conn.p.name);
                        Clan.entries.remove(conn.p.myclan);
                        conn.p.myclan = null;
                        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                        MapService.send_in4_other_char(conn.p.map, conn.p, conn.p);
                        Service.send_char_main_in4(conn.p);
                        Service.send_notice_box(conn, conn.language.huybangthanhcong);
                    }
                    break;
                }
                case 118: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                        if (((long) Clan.vang_upgrade[1] * conn.p.myclan.level) > conn.p.myclan.get_vang()) {
                            Service.send_notice_box(conn, "Clan " + conn.language.khongduvang);
                            return;
                        }
                        if ((Clan.ngoc_upgrade[1] * conn.p.myclan.level) > conn.p.myclan.get_ngoc()) {
                            Service.send_notice_box(conn, "Clan " + conn.language.khongdungoc);
                            return;
                        }
                        conn.p.myclan.update_vang((long) -Clan.vang_upgrade[1] * conn.p.myclan.level);
                        conn.p.myclan.update_ngoc(-Clan.ngoc_upgrade[1] * conn.p.myclan.level);
                        conn.p.myclan.update_level();
                        Service.send_notice_box(conn, "Bang hội đã được nâng lên cấp " + conn.p.myclan.level);
                    }
                    break;
                }
                case 119: {
                    if (conn.p.count_dungeon < 1) {
                        if (conn.p.get_ngoc() < 1000) {
                            Service.send_notice_box(conn, conn.language.khongdungoc);
                            return;
                        }
                        conn.p.update_ngoc(-1000);
                        conn.p.item.char_chest(5);
                    }
                    Dungeon d = DungeonManager.get_list(conn.p.name);
                    if (d == null) {
                        try {
                            d = new Dungeon();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        if (conn.p.count_dungeon <= 0)
//                            Service.send_notice_box(conn, conn.language.quaylaingaymai);
                        if (d != null) {
                            conn.p.count_dungeon--;
                            //
                            d.name_party = conn.p.name;
                            d.setInfo(conn.p);
                            //
                            MapService.leave(conn.p.map, conn.p);
                            conn.p.map = d.template;
                            conn.p.x = 584;
                            conn.p.y = 672;
                            MapService.enter(conn.p.map, conn.p);
                            d.send_map_data(conn.p);
                            //
                            DungeonManager.add_list(d);
                        } else {
                            Service.send_notice_box(conn, conn.language.coloixayra);
                        }
                    }
                    break;
                }
                case 70: {
                    // Đệ tử
                    if (!conn.p.isOwner) {
                        return;
                    }
                    if (conn.p.level < 30) {
                        Service.send_notice_box(conn, "Yêu cầu cấp độ 30");
                        return;
                    }
                    if (conn.p.get_ngoc() < 25000) {
                        Service.send_notice_box(conn, conn.language.khongdungoc);
                        return;
                    }
                    Service.send_box_input_text(conn, 23, conn.language.clan, new String[]{"Tên (4-20 ký tự) :", "Tên viết tắt (3 ký tự) :"});
                    break;
                }
                case 122: {
                    byte itemTier = conn.p.item.inventory3[conn.p.item_replace].tier;
                    
                    if (itemTier <= 10 && conn.p.item.total_item_by_id(7, 346) < 1) {
                        Service.send_notice_box(conn, "Không đủ đá thạch anh cấp 1.");
                        return;
                    } else if (itemTier > 10 && itemTier <= 13 && conn.p.item.total_item_by_id(7, 347) < 1) {
                        Service.send_notice_box(conn, "Không đủ đá thạch anh cấp 2.");
                        return;
                    } else if (itemTier > 13 && itemTier <= 15 && conn.p.item.total_item_by_id(7, 348) < 1) {
                        Service.send_notice_box(conn, "Không đủ đá thạch anh cấp 3.");
                        return;
                    }
                    
                    if (itemTier <= 10) {
                        conn.p.item.remove(7, 346, 1);
                    } else if (itemTier > 10 && itemTier <= 13) {
                        conn.p.item.remove(7, 347, 1);
                    } else if (itemTier > 13 && itemTier <= 15) {
                        conn.p.item.remove(7, 348, 1);
                    }
                    
                    conn.p.item.inventory3[conn.p.item_replace2].tier = itemTier;
                    conn.p.item.inventory3[conn.p.item_replace].tier = 0;
                    
                    conn.p.item.char_inventory(3);
                    Service.send_notice_box(conn, conn.language.thanhcong);
                    //
                    Message m3 = new Message(73);
                    m3.writer().writeByte(0);
                    m3.writer().writeShort(conn.p.item_replace2);
                    m3.writer().writeByte(0);
                    conn.addmsg(m3);
                    m3.cleanup();
                    //
                    m3 = new Message(73);
                    m3.writer().writeByte(0);
                    m3.writer().writeShort(conn.p.item_replace);
                    m3.writer().writeByte(1);
                    conn.addmsg(m3);
                    m3.cleanup();
                    //
                    break;
                }
                case 124: {
                    conn.p.rest_skill_point();
                    conn.p.item.remove(4, 7, 1);
                    Service.send_notice_box(conn, conn.language.thanhcong);
                    break;
                }
                case 125: {
                    conn.p.rest_potential_point();
                    conn.p.item.remove(4, 6, 1);
                    Service.send_notice_box(conn, conn.language.thanhcong);
                    break;
                }
                case 126: {
                    if (conn.p.id_buffer_126 != -1) {
                        Item3 temp3 = conn.p.item.inventory3[conn.p.id_buffer_126];
                        temp3.islock = true;
                        switch (temp3.type) {
                            case 0: // coat
                            case 1: // pant
                            case 2: // crown
                            case 3: // grove
                            case 4: // ring
                            case 5: // chain
                            case 6: // shoes
                            case 7: // wing
                            case 15:
                            case 8:
                            case 9:
                            case 10:
                            case 16:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 11: { // weapon
                                conn.p.player_wear(temp3, conn.p.id_buffer_126, conn.p.id_temp_byte);
                                break;
                            }
                        }
                    }
                    conn.p.id_buffer_126 = -1;
                    conn.p.id_temp_byte = -1;
                    break;
                }
                case 88: {
                    if (conn.ac_admin < 10) {
                        Service.send_notice_box(conn, conn.language.khongcoquyen);
                        return;
                    }
                    ServerManager.gI().close();
                    System.out.println("Close server is processing....");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SaveData.process();
                            for (int k = Session.client_entry.size() - 1; k >= 0; k--) {

                                try {
                                    Session.client_entry.get(k).p = null;
                                    Session.client_entry.get(k).close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Manager.gI().close();
                        }
                    }).start();
                    break;
                }
            }
        }
    }
}
