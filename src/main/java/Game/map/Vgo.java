package Game.map;

public class Vgo {
    public short id_map_go;
    public String name_map_go;
    public short x_old;
    public short y_old;
    public short x_new;
    public short y_new;

    public Vgo(int idmap, String name, int x_o, int y_o, int x_n, int y_n) {
        id_map_go = (byte) idmap;
        name_map_go = name;
        x_old = (short) x_o;
        y_old = (short) y_o;
        x_new = (short) x_n;
        y_new = (short) y_n;
    }

    public Vgo(int idmap, String name, int x_n, int y_n) {
        id_map_go = (byte) idmap;
        name_map_go = name;
        x_new = (short) x_n;
        y_new = (short) y_n;
    }

    public Vgo() {}
}
