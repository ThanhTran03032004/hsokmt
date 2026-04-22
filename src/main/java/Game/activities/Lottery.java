package Game.activities;

import Game.core.Manager;
import Game.core.Service;
import Game.core.Util;
import Game.io.Session;
import Game.template.*;
import Game.io.Message;

import java.io.IOException;
import java.util.ArrayList;

public class Lottery {
    public static ItemTemplate3[] item3;
    public static ItemTemplate4[] item4;
    public static ItemTemplate7[] item7;
    public static short[] idItem3 = new short[]{2939, 4640, 4639, 4641, 4638};
    public static short[] idItem4 = new short[]{};
    public static short[] idItem7 = new short[]{14};

    public static void sendMessage(final Session conn, final byte type) throws IOException {
        if (type == 0) {
            Message msg = new Message(-91);
            msg.writer().writeByte(type);
            byte size = (byte) (item3.length + item4.length + item7.length);
            msg.writer().writeByte(size);
            for (int i = 0; i < size; i++) {
                for (ItemTemplate3 itemTemplate3 : item3) {
                    msg.writer().writeByte(3);
                    msg.writer().writeUTF(itemTemplate3.getName());
                    msg.writer().writeByte(itemTemplate3.getClazz());
                    msg.writer().writeShort(itemTemplate3.getId());
                    msg.writer().writeByte(itemTemplate3.getType());
                    msg.writer().writeShort(itemTemplate3.getIcon());
                    msg.writer().writeByte(0);
                    msg.writer().writeShort(itemTemplate3.getLevel());
                    msg.writer().writeByte(5);
                    msg.writer().writeByte(itemTemplate3.getOp().size());
                    for (int k = 0; k < itemTemplate3.getOp().size(); k++) {
                        msg.writer().writeByte(itemTemplate3.getOp().get(k).id);
                        msg.writer().writeInt((itemTemplate3.getOp().get(k).param));
                    }
                }
                for (ItemTemplate4 itemTemplate4 : item4) {
                    msg.writer().writeByte(4);
                    msg.writer().writeShort(itemTemplate4.getId());
                    msg.writer().writeShort(0);
                }
                for (ItemTemplate7 itemTemplate7 : item7) {
                    msg.writer().writeByte(7);
                    msg.writer().writeShort(itemTemplate7.getId());
                    msg.writer().writeShort(1);
                }
            }
            conn.addmsg(msg);
            msg.cleanup();
        }
    }

    public static void startLottery(final Session conn, byte index) throws IOException {
        if (index < (item3.length + item4.length + item7.length)) {
            if ((index == 0 || index == 5) && (conn.p.item.total_item_by_id(4, 52) < 1 && conn.p.item.total_item_by_id(4, 143) < 1)) {
                Service.send_notice_box(conn, "Cần có vé mở ly");
                return;
            }
            if ((index > 0 && index < 5) && (conn.p.item.total_item_by_id(4, 226) < 1)) {
                Service.send_notice_box(conn, "Cần có vé mở ly đặc biệt");
            } else {
                conn.p.indexLottery = index;
                Message msg = new Message(-91);
                msg.writer().writeByte(1);
                msg.writer().writeByte(index);
                msg.writer().writeByte(Util.random(4));
                conn.addmsg(msg);
                msg.cleanup();
            }
        }
    }

    public static void rewardLottery(final Session conn, byte index) throws IOException {
        if ((conn.p.indexLottery == 0 || conn.p.indexLottery == 5) && conn.p.item.total_item_by_id(4, 52) < 1) {
            Service.send_notice_box(conn, "Cần có vé mở ly");
            return;
        }
        if ((conn.p.indexLottery > 0 && conn.p.indexLottery < 5) && (conn.p.item.total_item_by_id(4, 226) < 1)) {
            Service.send_notice_box(conn, "Cần có vé mở ly đặc biệt");
            return;
        }
        if (index <= 4 && index >= 0) {
            if (conn.p.item.get_inventory_able() < 3) {
                Service.send_notice_nobox_white(conn, "Cần ít nhất 3 ô hành trang");
            } else {
                try {
                    int tile = 1;
                    if (conn.p.indexLottery == 0 || conn.p.indexLottery == 5) {
                        conn.p.item.remove(4, 52, 1);
                    }
                    if (conn.p.indexLottery > 0 && conn.p.indexLottery < 5) {
                        tile = 4;
                        conn.p.item.remove(4, 226, 1);
                    }
                    conn.p.item.char_inventory(4);
                    Message msg = new Message(-91);
                    msg.writer().writeByte(2);
                    if (Util.random(200 * tile) < 1) { // tỉ lệ trúng
                        msg.writer().writeByte(1);
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(index);
                        messageReward(conn, (byte) 1);
                    } else {
                        msg.writer().writeByte(2);
                        int ind = Util.random(0, 4);
                        while (ind == index) {
                            ind = Util.random(0, 4);
                        }
                        msg.writer().writeByte(ind);
                        msg.writer().writeByte(0);
                        messageReward(conn, (byte) 0);
                    }
                    conn.addmsg(msg);
                    msg.cleanup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void messageReward(final Session conn, final byte type) throws IOException {
        if (conn.status == 0) {
            Manager.gI().mo_ly++;
            if (Manager.gI().mo_ly % 100 == 0) {
                Manager.gI().chatKTGprocess("Số lượng mở ly hiện tại " + Manager.gI().mo_ly + "/20000");
            }
        }
        if (Manager.gI().mo_ly >= 20000) {
            Manager.gI().mo_ly = 0;
            if (Manager.gI().time_x2_server > System.currentTimeMillis()) {
                Manager.gI().time_x2_server += 24 * 60 * 60 * 1000L;
            } else {
                Manager.gI().time_x2_server = System.currentTimeMillis() + 24 * 60 * 60 * 1000L;
            }
            Manager.gI().chatKTGWhite("Thời gian x2 kinh nghiệm toàn server còn " + (Manager.gI().time_x2_server - System.currentTimeMillis()) / 60000 + " phút");
        }
        Message m = new Message(78);
        if (type == 0) {
            m.writer().writeUTF("Chúc bạn may mắn lần sau");
        } else if (type == 1) {
            m.writer().writeUTF("Bạn nhận được");
        }
        m.writer().writeByte(3); // size
        for (int i = 0; i < 3; i++) {
            if (type == 1 && i == 1) {
                if (conn.p.indexLottery == 5) {
                    ItemTemplate7 item = ItemTemplate7.item.get(14);
                    Service.send_notice_nobox_white(conn, "Chúc mừng " + conn.p.name + " đã trúng " + item.getName());
                    m.writer().writeUTF(item.getName()); // name
                    m.writer().writeShort(item.getIcon()); // icon
                    m.writer().writeInt(1); // quantity
                    m.writer().writeByte(7); // type in bag
                    m.writer().writeByte(0); // tier
                    m.writer().writeByte(0); // color
                    Item47 item47 = new Item47();
                    item47.id = item.getId();
                    item47.quantity = 1;
                    conn.p.item.add_item_inventory47(7, item47);
                } else {
                    ItemTemplate3 item = ItemTemplate3.item.get(idItem3[conn.p.indexLottery]);
                    Manager.gI().chatKTGprocess("Chúc mừng " + conn.p.name + " đã trúng " + item.getName());
                    m.writer().writeUTF(item.getName()); // name
                    m.writer().writeShort(item.getIcon()); // icon
                    m.writer().writeInt(1); // quantity
                    m.writer().writeByte(3); // type in bag
                    m.writer().writeByte(0); // tier
                    m.writer().writeByte(5); // color

                    Item3 itbag = new Item3();
                    itbag.id = item.getId();
                    itbag.name = item.getName();
                    itbag.clazz = item.getClazz();
                    itbag.type = item.getType();
                    itbag.level = 10;
                    itbag.icon = item.getIcon();
                    itbag.op = new ArrayList<>();
                    itbag.op.addAll(item.getOp());
                    itbag.color = 5;
                    itbag.part = item.getPart();
                    itbag.tier = 0;
                    itbag.islock = true;
                    itbag.time_use = 0;
                    if (conn.p.indexLottery != 0) {
                        itbag.expiry_date = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L;
                    }
                    conn.p.item.add_item_inventory3(itbag);
                }
            } else {
                if (Util.random(3) == 2) {
                    int gold = Util.random(500,2000);
                    m.writer().writeUTF("");
                    m.writer().writeShort(0);
                    m.writer().writeInt(gold);
                    m.writer().writeByte(4);
                    m.writer().writeByte(0);
                    m.writer().writeByte(0);
                    conn.p.update_vang(gold, "Nhận %s vàng từ mở ly");
                } else {
                    ItemTemplate4 item = ItemTemplate4.item.get(Util.random(5));
                    m.writer().writeUTF(item.getName()); // name
                    m.writer().writeShort(item.getIcon()); // icon
                    m.writer().writeInt(1); // quantity
                    m.writer().writeByte(4); // type in bag
                    m.writer().writeByte(0); // tier
                    m.writer().writeByte(0); // color
                    Item47 item47 = new Item47();
                    item47.id = item.getId();
                    item47.quantity = 1;
                    conn.p.item.add_item_inventory47(4, item47);
                }
            }
        }

        m.writer().writeUTF("");
        m.writer().writeByte(1);
        m.writer().writeByte(0);
        conn.addmsg(m);
        m.cleanup();
    }

    public static void setItem() {
        item3 = new ItemTemplate3[idItem3.length];
        for (int i = 0; i < idItem3.length; i++) {
            item3[i] = ItemTemplate3.item.get(idItem3[i]);
        }
        item4 = new ItemTemplate4[idItem4.length];
        for (int i = 0; i < idItem4.length; i++) {
            item4[i] = ItemTemplate4.item.get(idItem4[i]);
        }
        item7 = new ItemTemplate7[idItem7.length];
        for (int i = 0; i < idItem7.length; i++) {
            item7[i] = ItemTemplate7.item.get(idItem7[i]);
        }
    }
}
