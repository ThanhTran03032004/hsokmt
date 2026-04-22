package Game.template;

import Game.client.Clan;
import Game.client.Player;
import Game.io.Message;
import Game.map.Map;

import java.io.IOException;

/**
 * @author KMT
 */
public class LuaThieng {

    public Map map;
    public long time;
    public short x;
    public short y;
    public Clan clan;
    public int binhdau;
    public int radius = 100;

    public LuaThieng(Map map, Player p) {
        this.map = map;
        this.time = System.currentTimeMillis() + 60 * 60 * 1000L;
        this.x = p.x;
        this.y = p.y;
        this.clan = p.myclan;
        this.binhdau = 0;
    }

    public int getPercentExp() {
        return 20 + 5 * binhdau;
    }

    public boolean isPointInCircle(int x, int y) {
        double distance = Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
        return distance <= (this.radius + binhdau * 10);
    }

    public void send_move(Player p) throws IOException {
        Message m = new Message(4);
        m.writer().writeByte(1);
        m.writer().writeShort(110);
        m.writer().writeShort(-1);
        m.writer().writeShort(x);
        m.writer().writeShort(y);
        m.writer().writeByte(-1);
        p.conn.addmsg(m);
        m.cleanup();
    }

    public void sendInfo(Player p) throws IOException {
        Message m = new Message(7);
        m.writer().writeShort(-1);
        m.writer().writeByte(this.binhdau + 1);
        m.writer().writeShort(this.x);
        m.writer().writeShort(this.y);
        m.writer().writeInt(1);
        m.writer().writeInt(1);
        m.writer().writeByte(20);
        m.writer().writeInt(4);
        m.writer().writeShort(this.clan.icon);
        m.writer().writeInt(Clan.get_id_clan(this.clan));
        m.writer().writeUTF(this.clan.name_clan_shorted);
        m.writer().writeByte(122);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeUTF("");
        m.writer().writeLong(this.time);
        m.writer().writeByte(0);
        p.conn.addmsg(m);
        m.cleanup();
    }
}
