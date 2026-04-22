package Game.map;

import java.io.IOException;

import Game.client.Player;
import Game.io.Message;

public class Eff_player_in_map {

    public static void add(Player p, int index) throws IOException {
        Message mm = new Message(75);
        mm.writer().writeByte(2);
        mm.writer().writeByte(1);
        mm.writer().writeShort(index);
        mm.writer().writeShort(-1);
        mm.writer().writeByte(0);
        mm.writer().writeShort(p.ID);
        for (int i = 0; i < p.map.players.size(); i++) {
            p.map.players.get(i).conn.addmsg(mm);
        }
        mm.cleanup();
    }
    public static void add(Player p, int b, int cat, int idx_mob, int time, int b2, int id_form)throws IOException{
        Message mm = new Message(75);
        mm.writer().writeByte(b);
        mm.writer().writeByte(cat);
        mm.writer().writeShort(idx_mob);
        mm.writer().writeShort(time);
        mm.writer().writeByte(b2);
        mm.writer().writeShort(id_form);
        p.conn.addmsg(mm);
        mm.cleanup();
    }
}
