package Game.map;

import java.io.IOException;

import Game.client.Player;
import Game.io.Message;

public class Eff_special_skill {
    public static void send_eff(Player p, int i, int time) throws IOException {

        Message m = new Message(-49);
        m.writer().writeByte(2);
        m.writer().writeShort(0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        switch (i) {
            case 0: {
                m.writer().writeByte(13);
                break;
            }
            case 1: {
                m.writer().writeByte(9);
                break;
            }
            case 2: {
                m.writer().writeByte(12);
                break;
            }
            case 3: {
                m.writer().writeByte(6);
                break;
            }
            case 4: {
                m.writer().writeByte(17);
                break;
            }
        }
        m.writer().writeShort(p.ID);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeInt(time);
        MapService.send_msg_player_inside(p.map, p, m, true);
        m.cleanup();
        // t6 : eff electric
        // t12 : eff ice
        // t13 : eff fire
        // t9: eff poison
        // t17 : eff vat ly
    }

    public static void send_eff_Vip(Player p, int i, int time, boolean isTangHinh) throws IOException {
        Message m = new Message(-49);
        m.writer().writeByte(2);
        m.writer().writeShort(0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeByte((byte) i);

        m.writer().writeShort(p.ID);
        m.writer().writeByte(0);
        m.writer().writeByte((byte) 0);//
        m.writer().writeInt(time);
        MapService.send_msg_player_inside(p.map, p, m, true);
        m.cleanup();
    }

    public static void send_eff_Medal(Player p, int id, int time) throws IOException {
        byte i = 0;
        boolean isTangHinh = false;
        switch (id) {
            case 77://bỏng lửa
                i = 13;
                break;
            case 79: // bỏng lạnh
                i = 12;
                break;
            case 80: // giáp hắc ám
                i = 16;
                //ratio[2] += op.getParam(temp.tier);
                break;
            case 81: // tàng hình
                i = 0;
                isTangHinh = true;
                break;
            case 86: // khiên ma thuật
                i = 15;
                break;
            case 88: // lú lẫn
                i = 14;
                break;
            default:
                break;
        }
        Message m = new Message(-49);
        m.writer().writeByte(2);
        //m.writer().writeByte(0);
        m.writer().writeShort(0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeByte((byte) i);

        m.writer().writeShort(p.ID);
        m.writer().writeByte(0);
        if (isTangHinh)
            m.writer().writeByte(17);
        else
            m.writer().writeByte(0);
        m.writer().writeInt(time);
        MapService.send_msg_player_inside(p.map, p, m, true);
        m.cleanup();
    }

    public static void send_eff_TangHinh(Player p, int id, int time) throws IOException {
        byte i = 0;
        boolean isTangHinh = true;
        if (id != 81) return;
        p.isTangHinh = true;
        Message m = new Message(-49);
        m.writer().writeByte(2);
        //m.writer().writeByte(0);
        m.writer().writeShort(0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeByte((byte) i);

        m.writer().writeShort(p.ID);
        m.writer().writeByte(0);
        m.writer().writeByte(17);
        m.writer().writeInt(time);
        p.conn.addmsg(m);
        m.cleanup();
        try {
            m = new Message(8);
            m.writer().writeShort(p.ID);
            MapService.send_msg_player_inside(p.map, p, m, false);
            m.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send_eff_kham(Player p, int id, int time) throws IOException {
        Message m = new Message(-49);
        m.writer().writeByte(2);
        //m.writer().writeByte(0);
        m.writer().writeShort(0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeByte((byte) id);

        m.writer().writeShort(p.ID);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeInt(time);
        MapService.send_msg_player_inside(p.map, p, m, true);
        m.cleanup();
    }
}
