package Game.event;

import Game.core.Manager;
import Game.core.Service;
import Game.core.Util;
import Game.io.Session;
import Game.template.BoxItem;
import Game.core.Log;
import Game.template.Item3;
import Game.template.Item47;
import Game.template.ItemTemplate3;

import java.io.IOException;
import java.util.*;

public class LunarNewYear {

    public static final String name_event = "Tết nguyên đán";
    public static String[] menu = new String[]{"Góp gạo", "Góp thịt", "Góp đậu xanh", "Góp lá", "Nhận bánh",
        "Đổi bánh dày", "Ghép chữ Happy New Year", "Dâng bánh"};
    public static ArrayList<EventManager.PlayerRegister> list_nhan_banh = new ArrayList<>();

    public static void Menu(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 0) {
            switch (index) {
                case 0:
                case 1:
                case 2:
                case 3:
                    if (EventManager.check(EventManager.registerList, conn.p.name)) {
                        Service.send_box_input_text(conn, 29 + index, menu[index], new String[]{"Số lượng"});
                    } else {
                        if (EventManager.notCanRegister()) {
                            Service.send_notice_box(conn, "Không trong thời gian đăng ký");
                            return;
                        }
                        Service.send_box_input_yesno(conn, -111, "Bạn có muốn đăng ký nấu bánh với giá 50 ngọc?");
                    }
                    break;
                case 4:
                    EventManager.PlayerRegister playerRegister = EventManager.getPlayer(list_nhan_banh, conn.p.name);
                    if (playerRegister != null) {
                        Item47 it = new Item47();
                        it.category = 4;
                        it.id = 31;
                        short quantity = (short) ((0 <= playerRegister.rank && playerRegister.rank < 5) ? Math.min(playerRegister.total * 2, 60 * 2) : Math.min(playerRegister.total, 60));
                        it.quantity = quantity;
                        conn.p.item.add_item_inventory47(4, it);
                        Service.send_notice_box(conn, "Nhận được " + quantity + " bánh chưng");
                        list_nhan_banh.remove(playerRegister);
                    } else {
                        Service.send_notice_box(conn, "Bạn không có tên trong danh sách hoặc đã nhận rồi.");
                    }
                    break;
                case 5:
                    Service.send_box_input_text(conn, 33, menu[index], new String[]{"Số lượng"});
                    break;
                case 6:
                    Service.send_box_input_text(conn, 34, menu[index], new String[]{"Số lượng"});
                    break;
                case 7:
                    if (conn.p.item.total_item_by_id(4, 31) > 0 && conn.p.item.total_item_by_id(4, 195) > 0) {
                        if (conn.p.item.get_inventory_able() > 5) {
                            List<BoxItem> ids = new ArrayList<>();
                            List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(2, 3, 33, 13, 2, 3, 33, 13, 14, 349, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13));
                            for (int i = 417; i < 457; i++) {
                                it7.add(i);
                            }
                            List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(2, 25, 48, 49, 50, 51, 52, 67, 68, 10, 24, 2, 25, 48, 49, 50, 51, 52, 2, 25, 48, 49, 50, 51, 52));
                            for (int i = 0; i < Util.random(1, 4); i++) {
                                int ran = Util.random(100);
                                if (ran < 60) {
                                    short id = Util.random(it4, new ArrayList<>()).shortValue();
                                    short quant = (short) Util.random(1, 4);
                                    ids.add(new BoxItem(id, quant, (byte) 4));
                                    conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                                } else {
                                    short id = Util.random(it7, new ArrayList<>()).shortValue();
                                    short quant = 1;
                                    ids.add(new BoxItem(id, quant, (byte) 7));
                                    conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                                }
                            }
                            if (Util.nextInt(1000) < 3) {
                                List<Integer> it3 = new ArrayList<>(java.util.Arrays.asList(2940, 4626));
                                short id3 = Util.random(it3, new ArrayList<>()).shortValue();
                                ItemTemplate3 temp3 = ItemTemplate3.item.get(id3);
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
                                if (Util.random(100) != 55) {
                                    if (it.type == 14) {
                                        it.time_use = 7 * 24 * 60 * 60 * 1000L;
                                    } else {
                                        it.expiry_date = 7 * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
                                    }
                                }
                                conn.p.item.add_item_inventory3(it);
                                ids.add(new BoxItem(id3, (short) 1, (byte) 3));
                            }
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                            conn.p.item.remove(4, 31, 1);
                            conn.p.item.remove(4, 195, 1);
                            conn.p.item.char_inventory(4);
                            conn.p.item.char_inventory(7);
                            conn.p.item.char_inventory(3);
                        } else {
                            Service.send_notice_box(conn, "Hành trang không đủ 5 ô trống");
                        }
                    } else {
                        Service.send_notice_box(conn, "Dâng bánh cần 1 bánh chưng và 1 bánh dày");
                    }
                    break;
            }
        }
    }

    public static void ban_phao(Session conn) throws IOException {
        if (conn.p.item.total_item_by_id(4, 259) >= 1 && conn.p.get_ngoc() >= 50) {
            if (conn.p.item.get_inventory_able() < 5) {
                Service.send_notice_box(conn, "Hành trang không đủ 5 ô trống");
                return;
            }
            List<BoxItem> ids = new ArrayList<>();
            List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(2, 3, 33, 13, 2, 3, 33, 13, 14, 349, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13));
            for (int i = 417; i < 457; i++) {
                it7.add(i);
            }
            List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(2, 25, 48, 49, 50, 51, 52, 67, 68, 10, 24, 2, 25, 48, 49, 50, 51, 52, 2, 25, 48, 49, 50, 51, 52));
            for (int i = 0; i < Util.random(1, 4); i++) {
                int ran = Util.random(100);
                if (ran < 60) {
                    short id = Util.random(it4, new ArrayList<>()).shortValue();
                    short quant = (short) Util.random(1, 4);
                    ids.add(new BoxItem(id, quant, (byte) 4));
                    conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                } else {
                    short id = Util.random(it7, new ArrayList<>()).shortValue();
                    short quant = 1;
                    ids.add(new BoxItem(id, quant, (byte) 7));
                    conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                }
            }
            if (Util.nextInt(1000) < 3) {
                List<Integer> it3 = new ArrayList<>(java.util.Arrays.asList(2940, 4626));
                short id3 = Util.random(it3, new ArrayList<>()).shortValue();
                ItemTemplate3 temp3 = ItemTemplate3.item.get(id3);
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
                if (Util.random(100) != 55) {
                    if (it.type == 14) {
                        it.time_use = 7 * 24 * 60 * 60 * 1000L;
                    } else {
                        it.expiry_date = 7 * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
                    }
                }
                conn.p.item.add_item_inventory3(it);
                ids.add(new BoxItem(id3, (short) 1, (byte) 3));
            }
            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
            conn.p.item.remove(4, 259, 1);
            conn.p.update_ngoc(-50);
            conn.p.item.char_inventory(4);
            conn.p.item.char_inventory(7);
            conn.p.item.char_inventory(3);
            Service.send_eff_map(conn.p.map, -66, 62, conn.p.x, conn.p.y, 0, 0, 0);
            conn.p.point_event++;
            Log.gI().add_log("event", conn.p.name + " bắn pháo hoa số lượng : " + conn.p.point_event);
        } else {
            Service.send_notice_box(conn, "Cần 1 thuốc nổ và 20 ngọc");
        }
    }

    public synchronized static void add_material(String name, int type, int index, int quant) {
        if (type == 0) {
            EventManager.PlayerRegister playerRegister = new EventManager.PlayerRegister(name);
            EventManager.registerList.add(playerRegister);
        } else {
            EventManager.PlayerRegister playerRegister = EventManager.getPlayer(EventManager.registerList, name);
            if (playerRegister != null) {
                playerRegister.material[index] += (short) quant;
            }
        }
    }

    public static void setList_nhan_banh() {
        int counter = 0;
        for (EventManager.PlayerRegister playerRegister : EventManager.registerList) {
            if (playerRegister.total > 0) {
                playerRegister.rank = counter;
                list_nhan_banh.add(playerRegister);
                counter++;
            }
        }
    }

    public static void finish() {
        list_nhan_banh.clear();
        setList_nhan_banh();
        EventManager.registerList.clear();
    }
}
