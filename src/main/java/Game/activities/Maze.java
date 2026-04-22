package Game.activities;

import Game.client.Player;
import Game.core.Util;
import Game.io.Message;
import Game.map.Map;
import Game.map.Vgo;

import java.io.IOException;

public class Maze {
    public static final short[] id_106_go = new short[]{107, 110, 111};
    public static final short[] id_109_go = new short[]{105, 112, 110};
    public static final short[] cua_map_105 = new short[]{468, 12};
    public static final short[] cua_trai_2 = new short[]{814, 342};
    public static final short[] cua_tren_2 = new short[]{384, 12};
    public static final short[] cua_duoi_2 = new short[]{96, 834};

    public static final short[] cua_trai_1 = new short[]{762, 360};
    public static final short[] cua_phai_1 = new short[]{24, 378};
    public static final short[] cua_tren_1 = new short[]{426, 12};
    public static final short[] cua_duoi_1 = new short[]{384, 702};

    public static void updatePlayer(Player p, Map map) throws IOException {
        if (map.isMapMaze() && !p.isSquire) {
            if (p.time_maze < System.currentTimeMillis()) {
                p.veLang();
            } else {
                int time = (int) ((p.time_maze - System.currentTimeMillis()) / 1000);
                int min = time / 60;
                int sec = (time - min * 60);
                if (sec == 0 || sec == 30) {
                    String chat = "";
                    if (min > 0) {
                        chat = "Thời gian vượt mê cung còn lại : " + min + " phút " + sec + " giây.";
                    } else {
                        chat = "Thời gian vượt mê cung còn lại :" + sec + " giây.";
                    }
                    Message m = new Message(27);
                    m.writer().writeShort(p.ID);
                    m.writer().writeByte(0);
                    m.writer().writeUTF(chat);
                    p.conn.addmsg(m);
                    m.cleanup();
                }
            }
        }
    }
    public static Vgo getVgo(Player p) {
        if (p != null) {
            if (p.map.map_id == 105) {
                if (isCua105(p.x, p.y)) {
                    return defaultVgo((short) 106);
                }
            } else if (p.map.map_id == 106) {
                if (isCuaPhai1(p.x, p.y) || isCuaTrai1(p.x, p.y) || isCuaTren1(p.x, p.y) || isCuaDuoi1(p.x, p.y)) {
                    short id_map = id_106_go[Util.random(id_106_go.length)];
                    return defaultVgo(id_map);
                }
            } else if (p.map.map_id == 107) {
                if (isCuaDuoi2(p.x, p.y)) {
                    return defaultVgo((short) 106);
                } else if (isCuaTren2(p.x, p.y) || isCuaTrai2(p.x, p.y)) {
                    return defaultVgo((short) 113);
                }
            } else if (p.map.map_id == 109) {
                if (isCuaPhai1(p.x, p.y) || isCuaTrai1(p.x, p.y) || isCuaTren1(p.x, p.y) || isCuaDuoi1(p.x, p.y)) {
                    short id_map = id_109_go[Util.random(id_109_go.length)];
                    return defaultVgo(id_map);
                }
            } else if (p.map.map_id == 110) {
                if (isCuaDuoi2(p.x, p.y)) {
                    return defaultVgo((short) 106);
                } else if (isCuaTren2(p.x, p.y) || isCuaTrai2(p.x, p.y)) {
                    return defaultVgo((short) 111);
                }
            } else if (p.map.map_id == 111) {
                if (isCuaDuoi2(p.x, p.y)) {
                    return defaultVgo((short) 106);
                } else if (isCuaTren2(p.x, p.y) || isCuaTrai2(p.x, p.y)) {
                    return defaultVgo((short) 114);
                }
            } else if (p.map.map_id == 114) {
                if (isCuaDuoi2(p.x, p.y)) {
                    return defaultVgo((short) 106);
                } else if (isCuaTren2(p.x, p.y) || isCuaTrai2(p.x, p.y)) {
                    return defaultVgo((short) 109);
                }
            } else if (p.map.map_id == 113) {
                if (isCuaDuoi2(p.x, p.y)) {
                    return defaultVgo((short) 115);
                } else if (isCuaTren2(p.x, p.y) || isCuaTrai2(p.x, p.y)) {
                    return defaultVgo((short) 107);
                }
            } else if (p.map.map_id == 115) {
                if (isCuaDuoi2(p.x, p.y)) {
                    return defaultVgo((short) 106);
                } else if (isCuaTren2(p.x, p.y) || isCuaTrai2(p.x, p.y)) {
                    return defaultVgo((short) 113);
                }
            } else if (p.map.map_id == 112) {
                if (isCuaDuoi2(p.x, p.y)) {
                    if (Util.random(4) == 2) {
                        return defaultVgo((short) 108);
                    } else {
                        return defaultVgo((short) 110);
                    }
                } else if (isCuaTren2(p.x, p.y) || isCuaTrai2(p.x, p.y)) {
                    return defaultVgo((short) 106);
                }
            }
        }
        return null;
    }

    public static boolean isCuaTren2(int x, int y) {
        return (Math.abs(cua_tren_2[0] - x)) < 40 && (Math.abs(cua_tren_2[1] - y)) < 40;
    }

    public static boolean isCuaDuoi2(int x, int y) {
        return (Math.abs(cua_duoi_2[0] - x)) < 40 && (Math.abs(cua_duoi_2[1] - y)) < 40;
    }

    public static boolean isCuaTrai2(int x, int y) {
        return (Math.abs(cua_trai_2[0] - x)) < 40 && (Math.abs(cua_trai_2[1] - y)) < 40;
    }

    public static boolean isCuaTren1(int x, int y) {
        return (Math.abs(cua_tren_1[0] - x)) < 40 && (Math.abs(cua_tren_1[1] - y)) < 40;
    }

    public static boolean isCuaDuoi1(int x, int y) {
        return (Math.abs(cua_duoi_1[0] - x)) < 40 && (Math.abs(cua_duoi_1[1] - y)) < 40;
    }

    public static boolean isCuaTrai1(int x, int y) {
        return (Math.abs(cua_trai_1[0] - x)) < 40 && (Math.abs(cua_trai_1[1] - y)) < 40;
    }

    public static boolean isCuaPhai1(int x, int y) {
        return (Math.abs(cua_phai_1[0] - x)) < 40 && (Math.abs(cua_phai_1[1] - y)) < 40;
    }

    public static boolean isCua105(int x, int y) {
        return (Math.abs(cua_map_105[0] - x)) < 40 && (Math.abs(cua_map_105[1] - y)) < 40;
    }

    public static Vgo defaultVgo(short map_id) {
        if (map_id == 106 || map_id == 109) {
            Vgo vgo = new Vgo();
            vgo.id_map_go = map_id;
            vgo.x_new = 456;
            vgo.y_new = 408;
            return vgo;
        } else if (map_id == 107 || map_id == 110 || map_id == 111 || map_id == 113 || map_id == 114 || map_id == 115) {
            Vgo vgo = new Vgo();
            vgo.id_map_go = map_id;
            vgo.x_new = 408;
            vgo.y_new = 432;
            return vgo;
        } else if (map_id == 112) {
            Vgo vgo = new Vgo();
            vgo.id_map_go = map_id;
            vgo.x_new = 741;
            vgo.y_new = 536;
            return vgo;
        } else if (map_id == 105) {
            Vgo vgo = new Vgo();
            vgo.id_map_go = map_id;
            vgo.x_new = 696;
            vgo.y_new = 528;
            return vgo;
        } else if (map_id == 108) {
            Vgo vgo = new Vgo();
            vgo.id_map_go = map_id;
            vgo.x_new = 444;
            vgo.y_new = 108;
            return vgo;
        }
        return null;
    }
}
