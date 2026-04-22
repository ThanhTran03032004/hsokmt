package Game.template;

import Game.client.Player;
import Game.core.Manager;
import Game.core.Util;
import Game.map.Dungeon;
import Game.map.Map;

import java.io.IOException;

public class MobDungeon extends MainObject {

    private final Dungeon dungeon;
    public int from_gate;
    public boolean is_atk;

    public MobDungeon(Dungeon map, int index, Mob mob) {
        dungeon = map;
        this.ID = (short) index;
        this.template = mob;
        is_atk = false;
        isdie = false;
        color_name = 0;
    }

    @Override
    public boolean isMobDungeon() {
        return true;
    }

    @Override
    public void SetDie(Map map, MainObject mainAtk) throws IOException {
        if (isdie) {
            return;
        }
        if (this.hp <= 0) {
            this.hp = 0;
            // mob die
            this.isdie = true;
            // send p outside
            if (20 > Util.random(0, 100)) {
                Dungeon.leave_item_by_type7(map, MaterialMedal.m_blue[Util.random(MaterialMedal.m_blue.length)], (Player) mainAtk, this.ID);
            }
            if (20 > Util.random(0, 100)) {
                short nltt = ((short) (Util.random(10) * 4 + 418));
                Dungeon.leave_item_by_type7(map, nltt, (Player) mainAtk, this.ID);
            }
            if (Manager.gI().event == 0 && Util.random(3) == 1) {
                Dungeon.leave_item_by_type4(map, (short) 259, (Player) mainAtk, this.ID, mainAtk.ID);
            }
            dungeon.num_mob--;
            if (dungeon.num_mob == 0) {
                dungeon.state = 1;
            }
        }
    }
}
