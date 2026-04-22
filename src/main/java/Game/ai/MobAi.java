
package Game.ai;

import Game.client.Player;
import Game.io.Message;

import java.io.IOException;
import java.util.List;
import Game.map.Map;
import Game.map.Mob_in_map;
import Game.template.MainObject;
import Game.template.Mob;
import Game.template.Part_player;


public class MobAi extends Mob_in_map {
    public long timeATK;
    public List<Part_player> part_p;//
    private byte head;//
    private byte eye;//
    private byte hair;//
    private int pointpk;//
    private short clan_icon = -1;
    private int clan_id = -1;
    private String clan_name_clan_shorted;
    private byte clan_mem_type;
    private byte[] fashion = new byte[]{-1,-1,-1,-1,-1,-1,-1};//
    private short mat_na=-1;//
    private short phi_phong=-1;//
    private short weapon=-1;//
    private short id_horse=-1;//
    private short id_hair=-1;//
    private short id_wing=-1;//
    private short id_img_mob=-1;//
    private byte type_use_horse =-1;//
    public long act_time;
    public boolean is_move=true;//
    public Player p_target;
    public int p_skill_id;//
    public int crit;
    public long time_hp_buff;
    public int pierce;
    
    
    public MobAi(int map_id, int zone_id, int id_temp_mob, int index, int x, int y, int clazz, int head, int eye, int hair, int lv, int hp, int pk, int dame, int def, int crit,List<Part_player> part, int id_img_mob){
        Mob temp = Mob.entry.get(id_temp_mob);
        this.map_id = (byte)map_id;
        this.zone_id = (byte)zone_id;
        this.name = temp.name;
        this.clazz = (byte) clazz;
        this.ID = index;
        this.x = this.x_old = (short)x;
        this.y = this.y_old = (short)y;
        this.head = (byte)head;
        this.eye = (byte) eye;
        this.hair = (byte) hair;
        this.level = (short) lv;
        this.hp = hp_max = hp;
        this.pointpk = (byte)pk;
        this.dame = dame;
        this.def = def;
        this.crit = crit;
        this.part_p = part;
        this.id_img_mob = (short)id_img_mob;
        this.template = temp;
        this.time_refresh = 11;
    }
    @Override
    public boolean isMobCTruong(){
        return true;
    }
    
    public MobAi(int map_id, int zone_id, int id_temp_mob,String name, int index, int x, int y, int clazz, int head, int eye, int hair, int lv, int hp, int pk, int dame, int def, int crit,List<Part_player> part, int id_img_mob){
        Mob temp = Mob.entry.get(id_temp_mob);
        this.map_id = (byte)map_id;
        this.zone_id = (byte)zone_id;
        this.name = name;
        this.clazz = (byte) clazz;
        this.ID = index;
        this.x = this.x_old = (short)x;
        this.y = this.y_old = (short)y;
        this.head = (byte)head;
        this.eye = (byte) eye;
        this.hair = (byte) hair;
        this.level = (short) lv;
        this.hp = this.hp_max = hp;
        this.pointpk = (byte)pk;
        this.dame = dame;
        this.def = def;
        this.crit = crit;
        this.part_p = part;
        this.id_img_mob = (short)id_img_mob;
        this.template = temp;
        this.time_refresh = 11;
    }
    
    public void send_in4(Player p)throws IOException{
        Message m = new Message(5);
        m.writer().writeShort(this.ID);
        m.writer().writeUTF(this.name);
        m.writer().writeShort(this.x);
        m.writer().writeShort(this.y);
        m.writer().writeByte(this.clazz);
        m.writer().writeByte(-1);
        m.writer().writeByte(this.head);
        m.writer().writeByte(this.eye);
        m.writer().writeByte(this.hair);
        m.writer().writeShort(this.level);
        m.writer().writeInt(this.hp);
        m.writer().writeInt(this.hp_max);
        m.writer().writeByte(this.typepk); // type pk
        m.writer().writeShort(this.pointpk);
        m.writer().writeByte(this.part_p.size());
        //
        for (int i = 0; i < this.part_p.size(); i++) {
            m.writer().writeByte(this.part_p.get(i).type);
            m.writer().writeByte(this.part_p.get(i).part);
            m.writer().writeByte(3);
            m.writer().writeShort(-1);
            m.writer().writeShort(-1);
            m.writer().writeShort(-1);
            m.writer().writeShort(-1); // eff
        }
        //
        m.writer().writeShort(this.clan_icon);
        if(clan_icon>-1)
        {
            m.writer().writeInt(this.clan_id);
            m.writer().writeUTF(this.clan_name_clan_shorted);
            m.writer().writeByte(this.clan_mem_type);
        }
        m.writer().writeByte(-1); // pet
        m.writer().writeByte(this.fashion.length);
        for (int i = 0; i < this.fashion.length; i++) {
            if (p.conn.version >= 280) {
                m.writer().writeShort(this.fashion[i]);
            } else {
                m.writer().writeByte(this.fashion[i]);
            }
        }
        //
        m.writer().writeShort(id_img_mob);//id_img_mob
        m.writer().writeByte(this.type_use_horse);
        m.writer().writeBoolean(false);
        m.writer().writeByte(1);
        m.writer().writeByte(0);
        m.writer().writeShort(this.mat_na); // mat na
        m.writer().writeByte(1); // paint mat na trc sau
        m.writer().writeShort(this.phi_phong); // phi phong
        m.writer().writeShort(this.weapon); // weapon
        m.writer().writeShort(this.id_horse);
        m.writer().writeShort(this.id_hair); // hair
        m.writer().writeShort(this.id_wing); // wing
        m.writer().writeShort(-1); // body
        m.writer().writeShort(-1); // leg
        m.writer().writeShort(-1); // bienhinh
        p.conn.addmsg(m);
        m.cleanup();
    }
    
    @Override
    public int get_TypeObj(){
        return 0;
    }
    
    @Override
    public void SetDie(Map map, MainObject mainAtk){
        if (isdie) return;
        try{
            if(this.hp >0)return;
            this.hp =0;
            this.isdie = true;
            this.time_back = System.currentTimeMillis() + 120_000;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
