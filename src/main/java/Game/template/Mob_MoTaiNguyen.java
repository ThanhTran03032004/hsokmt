package Game.template;

import Game.ai.NhanBan;
import Game.client.Clan;
import Game.client.Player;
import Game.core.Manager;
import Game.map.Eff_player_in_map;
import Game.map.Map;
import Game.map.MapService;
import Game.io.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mob_MoTaiNguyen extends MainObject{
    public Map map;
    public String name_monster;
    public List<NhanBan> nhanBans;
    public NhanBan nhanban;
    public NhanBan nhanban_save;
    public Clan clan;
    public boolean is_atk;
    public boolean isbuff_hp;
    public long time_buff;
    

    public Mob_MoTaiNguyen(int index, int x, int y, int hp, int hp_max, int level, Map map, String name_monster) {
        this.ID = Short.toUnsignedInt((short) index);
        this.x = (short) x;
        this.y = (short) y;
        this.hp = hp;
        this.hp_max = hp_max;
        this.level = (short)level;
        this.map = map;
        this.name_monster = name_monster;
        this.is_atk = false;
        this.isbuff_hp = false;
        this.nhanBans = new ArrayList<>();
    }
    @Override
    public boolean isMoTaiNguyen() {
        return true;
    }
    
    @Override
    public int get_DefBase(){
        return nhanban!= null && !nhanban.isdie ? nhanban.get_DefBase():0;
    }
    public void set_target_all(Player p) {
        for (NhanBan nhanBan : nhanBans) {
            if (nhanBan != null) {
                nhanBan.p_target = p;
                break;
            }
        }
    }
    @Override
    public void SetDie(Map map, MainObject mainAtk){
        if(hp >0 || !mainAtk.isPlayer())return;
        try{
            this.hp = 0;
            Manager.gI()
                    .chatKTGprocess(mainAtk.name + " bang "
                            + ((Player)mainAtk).myclan.name_clan.toUpperCase() + " chiếm được "
                            + this.name_monster + " tại " + map.name);
            ((Player)mainAtk).myclan.add_mo_tai_nguyen(this);
            if (this.clan != null) {
                this.clan.remove_mo_tai_nguyen(this);
            }
            this.clan = ((Player)mainAtk).myclan;
            if (this.nhanBans != null) {
                for (NhanBan nhanBan : nhanBans) {
                    Message m13 = new Message(8);
                    m13.writer().writeShort(nhanBan.ID);
                    for (int j = 0; j < map.players.size(); j++) {
                        map.players.get(j).conn.addmsg(m13);
                    }
                    m13.cleanup();
                }
            }
            this.nhanBans.clear();
            this.nhanban = new NhanBan();
            this.nhanban_save = this.nhanban;
            this.nhanban.typepk = 10;
            this.nhanban.hp_max *= 7;
            this.nhanban.hp = this.nhanban.hp_max;
            this.nhanban.setup((Player)mainAtk);
            this.nhanban.p_skill_id = 1;
            this.nhanBans.add(this.nhanban);
            Message m12 = new Message(4);
            m12.writer().writeByte(0);
            m12.writer().writeShort(0);
            m12.writer().writeShort(this.nhanban.ID);
            m12.writer().writeShort(this.nhanban.x);
            m12.writer().writeShort(this.nhanban.y);
            m12.writer().writeByte(-1);
            MapService.send_msg_player_inside(map, this, m12, true);
            m12.cleanup();
            
            this.hp = this.hp_max = 4_000_000;
            //
            Message mm = new Message(7);
            mm.writer().writeShort(this.ID);
            mm.writer().writeByte((byte) this.level);
            mm.writer().writeShort(this.x);
            mm.writer().writeShort(this.y);
            mm.writer().writeInt(this.hp);
            mm.writer().writeInt(this.hp_max);
            mm.writer().writeByte(0);
            mm.writer().writeInt(4);
            if (this.clan != null) {
                mm.writer().writeShort(this.clan.icon);
                mm.writer().writeInt(Clan.get_id_clan(this.clan));
                mm.writer().writeUTF(this.clan.name_clan_shorted);
                mm.writer().writeByte(122);
            } else {
                mm.writer().writeShort(-1);
            }
            mm.writer().writeUTF(this.name_monster);
            mm.writer().writeByte(0);
            mm.writer().writeByte(2);
            mm.writer().writeByte(0);
            mm.writer().writeUTF("");
            mm.writer().writeLong(-1);
            mm.writer().writeByte(4);
            for (int j = 0; j < map.players.size(); j++) {
                Player p1 = map.players.get(j);
                if (p1.ID != mainAtk.ID) {
                    MapService.change_flag(map, p1, -1);
                    p1.veLang();
                }
            }
            final int a = this.ID;
            new Thread(() -> {
                try {
                    Thread.sleep(5500L);
                    MapService.send_msg_player_inside(map, this, mm, true);
                    mm.cleanup();
                    if(mainAtk.isPlayer())
                        Eff_player_in_map.add((Player)mainAtk, a);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }catch(Exception e){}
    }
    public NhanBan getNhanban(int id) {
        for (NhanBan nhanBan : nhanBans) {
            if (nhanBan.ID == id) {
                return nhanBan;
            }
        }
        return null;
    }
}
