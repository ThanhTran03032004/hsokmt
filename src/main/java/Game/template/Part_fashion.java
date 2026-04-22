package Game.template;

import java.util.ArrayList;
import java.util.List;
import Game.client.Player;

public class Part_fashion {
    public static final List<Short> fashions = new ArrayList<>();
    public static final List<Part_fashion> entry = new ArrayList<>();
    public short id;
    public short[] part;

    public static short[] get_part(Player p) {
        if (p.item.wear[11] != null) {
            for (Part_fashion temp : entry) {
                if (temp.id == p.item.wear[11].id) {
                    return temp.part;
                }
            }
        }
        if (p.item.wear[20] != null) {
            for (Part_fashion temp : entry) {
                if (temp.id == p.item.wear[20].id) {
                    return temp.part;
                }
            }
        }
        return new short[]{-1, -1, -1, -1, -1, -1, -1};
    }
}
