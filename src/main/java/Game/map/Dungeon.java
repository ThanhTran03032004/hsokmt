package Game.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Game.core.Util;
import Game.io.Session;
import Game.client.Player;
import Game.core.Service;
import Game.io.Message;
import Game.template.ItemTemplate4;
import Game.template.ItemTemplate7;
import Game.template.Mob;
import Game.template.MobDungeon;

public class Dungeon {

    public Map template;
    public long time_live;
    public int state;
    private int mob_speed;
    public int wave;
    public int num_mob;
    private int num_mob_max;
    private int dame_buff;
    private int id_mob_in_wave;
    public int index_mob;
    private int hp;
    private int from_gate;
    private short from_x;
    private short from_y;
    public String name_party;
    private int level;
    private List<MobDungeon> mobs;

    public Dungeon() throws IOException {
        init();
    }

    private void init() throws IOException {
        Map temp = Map.get_map_dungeon(48);
        template = new Map((short) 48, 0, temp.name, temp.typemap, temp.ismaplang, temp.showhs,
                temp.maxplayer, temp.maxzone, temp.vgos, 0);
        template.mobs = new Mob_in_map[0];
        template.start_map();
        template.d = this;
        time_live = System.currentTimeMillis();
        index_mob = 1;
        state = -1;
        wave = 1;
        mobs = new ArrayList<>();
    }

    public void update() {
        if (this.index_mob > 65_000) {
            this.state = 7;
        }
        int time_now = getTime_live();
        try {
            if (this.state == -1) {
                if (time_now == 4) {
                    this.state = 0;
                    notice_all_player_in_dungeon("Quái vật sẽ xuất hiện ở cửa phía " + getNameGate() + " sau 10 giây");
                }
            } else if (this.state == 0) { // wait to begin
                if (time_now == 13) {
                    this.state = 1;
                }
            } else if (this.state == 1) { // wait at wave 20
                if (wave != 21) {
                    int dem = 0;
                    if (!mobs.isEmpty()) {
                        for (MobDungeon mob_temp : mobs) {
                            for (Player p0 : template.players) {
                                Message m2 = new Message(17);
                                m2.writer().writeShort(p0.ID);
                                m2.writer().writeShort(mob_temp.ID);
                                p0.conn.addmsg(m2);
                                m2.cleanup();
                                dem++;
                                if (dem > 10_000) {
                                    break;
                                }
                            }
                            if (dem > 10_000) {
                                break;
                            }
                        }
                    }
                    this.state = 2;
                    notice_all_player_in_dungeon("Hiệp " + (wave++) + " bắt đầu");
                    this.num_mob = num_mob_max;
                    mobs.clear();
                    if (wave <= 198) {
                        id_mob_in_wave = wave - 1;
                    } else {
                        id_mob_in_wave = Util.random(0, 198);
                    }
                } else {
                    notice_all_player_in_dungeon("Nhiệm vụ bảo vệ viên đá đã hoàn thành, xin chúc mừng các hiệp sĩ, tự động thoát sau 30s");
                    this.num_mob = num_mob_max;
                    mobs.clear();
                    this.state = 8;
                }
            } else if (this.state == 2) { // fight
                if (mobs.size() < num_mob_max) {
                    MobDungeon mob = new MobDungeon(this, this.index_mob++, Mob.entry.get(id_mob_in_wave));
                    mob.x = (short) (from_x + Util.random(-10, 10));
                    mob.y = (short) (from_y + Util.random(-10, 10));
                    mob.from_gate = this.from_gate;
                    if (this.wave >= 21) {
                        mob.Set_hpMax(mob.template.hpmax * (this.wave / 10));
                    } else {
                        mob.Set_hpMax(mob.template.hpmax);
                    }
                    mob.hp = mob.get_HpMax();
                    mobs.add(mob);
                    Message m = new Message(4);
                    m.writer().writeByte(1);
                    m.writer().writeShort(mob.template.mob_id);
                    m.writer().writeShort(mob.ID); // index
                    m.writer().writeShort(mob.x);
                    m.writer().writeShort(mob.y);
                    m.writer().writeByte(-1);
                    for (int i = 0; i < template.players.size(); i++) {
                        Player p0 = template.players.get(i);
                        p0.conn.addmsg(m);
                    }
                    m.cleanup();
                }
                update_mob();
            } else if (this.state == 4) {
                // this.state = 6;
            } else if (this.state == 5) {
                for (int i = 0; i < mobs.size(); i++) {
                    MobDungeon mob = mobs.get(i);
                    Message m2 = new Message(17);
                    m2.writer().writeShort(-1);
                    m2.writer().writeShort(mob.ID);
                    for (int i2 = 0; i2 < template.players.size(); i2++) {
                        Player p0 = template.players.get(i2);
                        p0.conn.addmsg(m2);
                    }
                    m2.cleanup();
                }
                mobs.clear();
                for (int i2 = 0; i2 < template.players.size(); i2++) {
                    Player p0 = template.players.get(i2);
                    Service.send_notice_box(p0.conn,
                            "Vượt phó bản hất bại, thoát sau 5s");
                }
                notice_all_player_in_dungeon("Thiên thạch đã vỡ, vượt phó bản thất bại!");
                this.state = 6;
            } else if (this.state == 6) {
                this.state = 7;
                //
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (this.state == 7) {
                for (int i = 0; i < template.players.size(); i++) {
                    Player p0 = template.players.get(i);
                    p0.x = 432;
                    p0.y = 354;
                    Map[] map_enter = Map.get_map_by_id(1);
                    int d = 0;
                    while ((d < (map_enter[d].maxzone - 1)) && map_enter[d].players.size() >= map_enter[d].maxplayer) {
                        d++;
                    }
                    p0.map = map_enter[d];
                    //
                    p0.is_changemap = false;
                    p0.x_old = p0.x;
                    p0.y_old = p0.y;
                    MapService.enter(p0.map, p0);
                }
                this.template.stop_map();
                this.template.d = null;
                DungeonManager.remove_list(this);
            } else if (this.state >= 8 && this.state <= 13) {
                if (this.state == 13) {
                    this.state = -2;
                } else {
                    this.state++;
                }
            } else if (this.state == -2) {
                this.state = 7;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notice_all_player_in_dungeon(String s) throws IOException {
        Message m = new Message(53);
        m.writer().writeUTF(s);
        m.writer().writeByte(0);
        for (int i = 0; i < template.players.size(); i++) {
            Player p0 = template.players.get(i);
            p0.conn.addmsg(m);
        }
        m.cleanup();
    }
    private void update_mob() throws IOException {
        for (int i = 0; i < mobs.size(); i++) {
            MobDungeon mob = mobs.get(i);
            if (!mob.isdie) {
                mob_act(mob);
            }
        }
    }

    private void mob_act(MobDungeon mob) throws IOException {
        short distance = (short) Util.random(1, mob_speed);
        boolean is_atk = true;
        switch (mob.from_gate) {
            case 1: {
                if (!(Math.abs((mob.y + distance) - 672) < mob_speed)) {
                    mob.y += distance;
                    is_atk = false;
                }
                break;
            }
            case 2: {
                if (!(Math.abs((mob.x - distance) - 684) < mob_speed)) {
                    mob.x -= distance;
                    is_atk = false;
                }
                break;
            }
            case 3: {
                if (!(Math.abs((mob.y - distance) - 672) < mob_speed)) {
                    mob.y -= distance;
                    is_atk = false;
                }
                break;
            }
            case 4: {
                if (!(Math.abs((mob.x + distance) - 684) < mob_speed)) {
                    mob.x += distance;
                    is_atk = false;
                }
                break;
            }
        }
        if (is_atk) {
            int dame = (mob.get_HpMax() / 100) * (mob.template.level / 2);
            dame = Util.random((dame * 9) / 10, (dame + 1));
            dame += this.wave * 10;
            dame *= dame_buff;
            Message m = new Message(10);
            m.writer().writeByte(1);
            m.writer().writeShort(mob.ID);
            m.writer().writeInt(mob.hp);
            m.writer().writeByte(2);
            m.writer().writeByte(1);
            m.writer().writeShort(32001); // index thien thach
            m.writer().writeInt(dame); // dame mob
            this.hp -= dame;
            if (this.hp <= 0) {
                this.hp = 0;
                this.state = 5;
            }
            m.writer().writeInt(this.hp);
            m.writer().writeByte(6); // id skill mob
            m.writer().writeByte(0);
            for (int i = 0; i < template.players.size(); i++) {
                Player p0 = template.players.get(i);
                p0.conn.addmsg(m);
            }
            m.cleanup();
        } else {
            Message m = new Message(4);
            m.writer().writeByte(1);
            m.writer().writeShort(mob.template.mob_id);
            m.writer().writeShort(mob.ID); // index
            m.writer().writeShort(mob.x);
            m.writer().writeShort(mob.y);
            m.writer().writeByte(-1);
            for (int i = 0; i < template.players.size(); i++) {
                Player p0 = template.players.get(i);
                p0.conn.addmsg(m);
            }
            m.cleanup();
        }
    }

    public int getTime_live() {
        return (int) ((System.currentTimeMillis() - time_live + 2) / 1000);
    }

    public void send_map_data(Player p) throws IOException {
        // thien thach
        Message m = new Message(4);
        m.writer().writeByte(2);
        m.writer().writeShort(0);
        m.writer().writeShort(32001); // index
        m.writer().writeShort(684);
        m.writer().writeShort(672);
        m.writer().writeByte(-1);
        p.conn.addmsg(m);
        m.cleanup();
    }

    public void send_in4_npc(Session conn, Message m2) throws IOException {
        short type = m2.reader().readShort();
        if (type == 32001) {
            Message m = new Message(-44);
            m.writer().writeShort(32001); // index
            m.writer().writeUTF("Thiên thạch");
            m.writer().writeInt(this.hp);
            m.writer().writeInt(this.hp);
            m.writer().writeShort(0);
            m.writer().writeShort(684);
            m.writer().writeShort(672);
            m.writer().writeByte(2);
            m.writer().writeByte(2);
            m.writer().writeUTF("");
            m.writer().writeShort(25);
            m.writer().writeByte(1);
            m.writer().writeShort(17);
            conn.addmsg(m);
            m.cleanup();
        }
    }

    public void send_mob_in4(Session conn, int index) throws IOException {
        int index_ = -1;
        for (int i = 0; i < mobs.size(); i++) {
            MobDungeon mob = mobs.get(i);
            if (mob.ID == index) {
                index_ = i;
                break;
            }
        }
        if (index_ > -1) {
            MobDungeon temp = mobs.get(index_);
            Message m = new Message(7);
            m.writer().writeShort(index);
            m.writer().writeByte(temp.template.level);
            m.writer().writeShort(temp.x);
            m.writer().writeShort(temp.y);
            m.writer().writeInt(temp.hp);
            m.writer().writeInt(temp.get_HpMax());
            m.writer().writeByte(20); // id skill monster (Spec: 32, ...)
            m.writer().writeInt(-4);
            m.writer().writeShort(temp.x);
            m.writer().writeShort(temp.y);
            m.writer().writeShort(-1); // clan monster
            m.writer().writeByte(1);
            m.writer().writeByte(this.mob_speed / 25); // speed
            m.writer().writeByte(0);
            m.writer().writeUTF("");
            m.writer().writeLong(-1);
            m.writer().writeByte(0); // color name 1: blue, 2: yellow
            conn.addmsg(m);
            m.cleanup();
        }
    }
    public void setInfo(Player p) throws IOException {
        hp = 50_000;
        mob_speed = 50;
        num_mob_max = 20;
        num_mob = num_mob_max;
        dame_buff = 1;
        from_gate = Util.random(1,5);
        level = p.level;
        if (from_gate == 1) {
            from_x = 690;
            from_y = 90;
        } if (from_gate == 2) {
            from_x = 1290;
            from_y = 672;
        } if (from_gate == 3) {
            from_x = 690;
            from_y = 1278;
        } if (from_gate == 4) {
            from_x = 72;
            from_y = 672;
        }
    }
    public String getNameGate() {
        if (from_gate == 1) {
            return "Bắc";
        } if (from_gate == 2) {
            return "Đông";
        } if (from_gate == 3) {
            return "Nam";
        } if (from_gate == 4) {
            return "Tây";
        }
        return "";
    }
    public MobDungeon get_mob(int id) {
        for (int i = 0; i < this.mobs.size(); i++) {
            if (this.mobs.get(i).ID == id) {
                return this.mobs.get(i);
            }
        }
        return null;
    }

    public static void leave_item_by_type7(Map map, short idItem, Player p_master, int index) throws IOException {
        int index_item_map = map.get_item_map_index_able();
        if (index_item_map > -1) {
            //
            map.item_map[index_item_map] = new ItemMap();
            map.item_map[index_item_map].id_item = idItem;
            if (ItemTemplate7.item.get(map.item_map[index_item_map].id_item).getColor() == 21) {
                map.item_map[index_item_map].color = 1;
            } else {
                map.item_map[index_item_map].color = 0;
            }
            map.item_map[index_item_map].quantity = 1;
            map.item_map[index_item_map].category = 7;
            map.item_map[index_item_map].setIdmaster((short) p_master.ID);
            map.item_map[index_item_map].time_exist = System.currentTimeMillis() + 60_000L;
            map.item_map[index_item_map].time_pick = System.currentTimeMillis() + 1_500L;
            // add in4 game scr
            Message mi = new Message(19);
            mi.writer().writeByte(7);
            mi.writer().writeShort(index); // id mob die
            mi.writer().writeShort(ItemTemplate7.item.get(map.item_map[index_item_map].id_item).getIcon());
            mi.writer().writeShort(index_item_map); //
            mi.writer().writeUTF(ItemTemplate7.item.get(map.item_map[index_item_map].id_item).getName());
            mi.writer().writeByte(map.item_map[index_item_map].color); // color
            mi.writer().writeShort(-1); // id player
            MapService.send_msg_player_inside(map, p_master, mi, true);
            mi.cleanup();
        }
    }
    
    public static void leave_item_by_type4(Map map, short index_real, Player p_master, int index, int idp) throws IOException {
        int index_item_map = map.get_item_map_index_able();
        if (index_item_map > -1) {
            //
            map.item_map[index_item_map] = new ItemMap();
            map.item_map[index_item_map].id_item = index_real;
            map.item_map[index_item_map].color = 0;
            map.item_map[index_item_map].quantity = 1;
            map.item_map[index_item_map].category = 4;
            map.item_map[index_item_map].setIdmaster((short) idp);
            map.item_map[index_item_map].time_exist = System.currentTimeMillis() + 60_000L;
            map.item_map[index_item_map].time_pick = System.currentTimeMillis() + 1_500L;
            // add in4 game scr
            Message mi = new Message(19);
            mi.writer().writeByte(4);
            mi.writer().writeShort(index); // id mob die
            mi.writer().writeShort(ItemTemplate4.item.get(map.item_map[index_item_map].id_item).getIcon());
            mi.writer().writeShort(index_item_map); //
            mi.writer().writeUTF(ItemTemplate4.item.get(map.item_map[index_item_map].id_item).getName());
            mi.writer().writeByte(0); // color
            mi.writer().writeShort(-1); // id player
            MapService.send_msg_player_inside(map, p_master, mi, true);
            mi.cleanup();
        }
    }
}
