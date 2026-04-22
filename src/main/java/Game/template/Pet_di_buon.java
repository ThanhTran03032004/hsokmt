package Game.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Game.map.MapService;
import Game.client.Player;
import Game.core.Service;
import Game.io.Message;
import Game.map.ItemMap;
import Game.map.Map;

public class Pet_di_buon extends MainObject {

    public Player p;
    public short type;
    public List<Short> item;
    public int id_map;
    public long time_move;
    public long time_skill;
    public int speed;
    private byte countSpeed;

    public Pet_di_buon(int type, int index_mob, int x, int y, int id_map, String name, Player p) {
        this.type = (short) type;
        this.ID = Short.toUnsignedInt((short) index_mob);
        this.item = new ArrayList<>();
        this.x = (short) x;
        this.y = (short) y;
        this.time_move = System.currentTimeMillis() + 1000L;
        this.id_map = id_map;
        this.name = name;
        this.hp = 400000;
        this.hp_max = 400000;
        this.time_skill = System.currentTimeMillis() + 15_000L;
        this.speed = 1;
        this.level = p.level;
        this.p = p;
    }

    public synchronized void update_hp(Player p, int hp) throws IOException {
        if (this.time_skill < System.currentTimeMillis()) {
            this.time_skill = System.currentTimeMillis() + 5_000L;
            this.hp += hp;
            if (this.hp > this.hp_max) {
                this.hp = this.hp_max;
            }
            if (this.hp < 0) {
                this.hp = 0;
            }
            this.update_all(p);
            p.update_ngoc(-5);
        } else {
            Service.send_notice_box(p.conn,
                    "Thời gian dùng lần tiếp theo : " + (this.time_skill - System.currentTimeMillis()) + "ms");
        }
    }

    public synchronized void update_speed(Player p) throws IOException {
        if (countSpeed > 0) {
            Service.send_notice_box(p.conn, "Chỉ có thể tăng tốc 1 lần");
            return;
        }
        if (this.time_skill < System.currentTimeMillis()) {
            this.time_skill = System.currentTimeMillis() + 10_000L;
            this.speed = 4;
            countSpeed++;
            this.update_all(p);
            p.update_ngoc(-5);
        } else {
            Service.send_notice_box(p.conn,
                    "Thời gian dùng lần tiếp theo : " + (this.time_skill - System.currentTimeMillis()) + "ms");
        }
    }

    public void update_all(Player p) throws IOException {
        Message mm = new Message(7);
        mm.writer().writeShort(this.ID);
        mm.writer().writeByte(this.level);
        mm.writer().writeShort(this.x);
        mm.writer().writeShort(this.y);
        mm.writer().writeInt(this.hp);
        mm.writer().writeInt(this.hp_max);
        mm.writer().writeByte(0);
        mm.writer().writeInt(-1);
        mm.writer().writeShort(-1);
        mm.writer().writeByte(1);
        mm.writer().writeByte(this.speed);
        mm.writer().writeByte(0);
        mm.writer().writeUTF(this.name);
        mm.writer().writeLong(-1);
        mm.writer().writeByte(4);
        MapService.send_msg_player_inside(p.map, p, mm, true);
        mm.cleanup();
    }

    @Override
    public boolean isMobDiBuon() {
        return true;
    }

    @Override
    public void SetDie(Map map, MainObject mainAtk) {
        if (isdie) return;
        try {
            if (this.hp <= 0) {
                this.isdie = true;
                this.hp = 0;
                Pet_di_buon_manager.remove(this.name);
                this.p.pet_di_buon = null;
                for (int j = 0; j < this.item.size(); j++) {
                    ItemMap it_leave = new ItemMap();
                    it_leave.id_item = (short) this.item.get(j);
                    it_leave.color = (byte) 0;
                    it_leave.quantity = 1;
                    it_leave.category = 3;
                    it_leave.idmaster = (short) mainAtk.ID;
                    it_leave.op = new ArrayList<>();
                    it_leave.time_exist = System.currentTimeMillis() + 60_000L;
                    it_leave.time_pick = System.currentTimeMillis() + 1_500L;
                    map.add_item_map_leave(map, (Player)this.p, it_leave, this.ID);
                }
            }
        } catch (Exception e) {
        }
    }
}
