package Game.template;

import java.util.ArrayList;
import java.util.List;

import Game.map.Map;

public class Mob {
    public static final List<Mob> entry = new ArrayList<>();
    public short mob_id;
    public String name;
    public short level;
    public int hpmax;
    public byte typemove;
    public Map map;
    public boolean is_boss;
    public boolean isBossEvent() {
        return mob_id == 174;
    }
}
