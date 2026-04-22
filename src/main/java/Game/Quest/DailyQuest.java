package Game.Quest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import Game.client.Player;
import Game.core.Service;
import Game.core.Util;
import Game.map.Map;

import static javax.swing.UIManager.get;

import Game.map.Mob_in_map;
import Game.template.Item47;
import Game.template.Mob;

public class DailyQuest {
    public static void get_quest(Player p, byte select) throws IOException {
        List<Integer> list_mob = new ArrayList<>();
        for (int i = 0; i < Mob.entry.size(); i++) {
            if (Math.abs(Mob.entry.get(i).level - p.level) <= 10 && !Mob.entry.get(i).is_boss && Mob.entry.get(i).mob_id != 167
                    && Mob.entry.get(i).mob_id != 168 && Mob.entry.get(i).mob_id != 169 && Mob.entry.get(i).mob_id != 170
                    && Mob.entry.get(i).mob_id != 171 && Mob.entry.get(i).mob_id != 172 && Mob.entry.get(i).mob_id != 5
                    && Mob.entry.get(i).mob_id != 11 && Mob.entry.get(i).mob_id != 23 && Mob.entry.get(i).mob_id != 91
                    && Mob.entry.get(i).mob_id != 51 && Mob.entry.get(i).mob_id != 24 && Mob.entry.get(i).mob_id != 17
                    && Mob.entry.get(i).mob_id != 52 && Mob.entry.get(i).mob_id != 53 && Mob.entry.get(i).mob_id != 89
                    && Mob.entry.get(i).mob_id != 92 && Mob.entry.get(i).mob_id != 155 && Mob.entry.get(i).mob_id != 106
                    && Mob.entry.get(i).mob_id != 79 && Mob.entry.get(i).mob_id != 149 && Mob.entry.get(i).mob_id != 174
                    && Mob.entry.get(i).mob_id != 83 && Mob.entry.get(i).mob_id != 105 && Mob.entry.get(i).mob_id != 84
                    && Mob.entry.get(i).mob_id != 101 && Mob.entry.get(i).mob_id != 104 && Mob.entry.get(i).mob_id != 103) {
                if (checkmob(i)) {
                    list_mob.add(i);
                }
            }
        }

        if (list_mob.isEmpty()) {
            Mob_in_map mob_add = null;
            for (Entry<Integer, Mob_in_map> en : Mob_in_map.ENTRY.entrySet()) {
                if (mob_add == null) {
                    mob_add = en.getValue();
                } else if (mob_add.level < en.getValue().level) {
                    mob_add = en.getValue();
                }
            }
            list_mob.add(mob_add.ID);
        }
        int index = Util.random(list_mob.size());
        p.quest_daily[0] = list_mob.get(index);
        p.quest_daily[1] = select;
        p.quest_daily[2] = 0;
        p.quest_daily[4] -= 1;
        switch (select) {
            case 0: {
                p.quest_daily[3] = Util.random(20, 50);
                break;
            }
            case 1: {
                p.quest_daily[3] = Util.random(50, 70);
                break;
            }
            case 2: {
                p.quest_daily[3] = Util.random(70, 100);
                break;
            }
            case 3: {
                p.quest_daily[3] = Util.random(80, 200);
                break;
            }
        }
        Mob mob_info = Mob.entry.get(p.quest_daily[0]);
        QuestService.receiveQuest(p.conn, (short) 1000, (byte) 1, new short[][]{{(short) p.quest_daily[0], 0, (short) p.quest_daily[3]}});
        Service.send_notice_box(p.conn, String.format("Nhiệm vụ hiện tại:\nTiêu diệt %s.\nMap : %s\nHôm nay còn %s lượt.",
                (p.quest_daily[2] + " / " + p.quest_daily[3] + " " + mob_info.name), mob_info.map.name, p.quest_daily[4]));
    }

    private static boolean checkmob(int i) {
        for (Map[] map : Map.entrys) {
            for (Mob_in_map m_temp : map[0].mobs) {
                if (m_temp == null) {
                    continue;
                }
                if (m_temp.template.mob_id == i) {
                    Mob.entry.get(i).map = map[0];
                    return true;
                }
            }
        }
        return false;
    }

    public static void remove_quest(Player p) throws IOException {
        if (p.quest_daily[0] == -1) {
            Service.send_notice_box(p.conn, "Hiện tại không nhận nhiệm vụ nào!");
        } else {
            p.quest_daily[0] = -1;
            p.quest_daily[1] = -1;
            p.quest_daily[2] = 0;
            p.quest_daily[3] = 0;
            Service.send_notice_box(p.conn, "Hủy nhiệm vụ thành công, nào rảnh quay lại nhận tiếp nhá!");
        }
    }

    public static String info_quest(Player p) {
        if (p.quest_daily[0] == -1) {
            return String.format("Bạn chưa nhận nhiệm vụ.\nHôm nay còn %s lượt.", p.quest_daily[4]);
        } else {
            checkmob(p.quest_daily[0]);
            Mob mob_info = Mob.entry.get(p.quest_daily[0]);
            return String.format("Nhiệm vụ hiện tại:\nTiêu diệt %s.\nMap : %s\nHôm nay còn %s lượt.",
                    (p.quest_daily[2] + " / " + p.quest_daily[3] + " " + mob_info.name), mob_info.map.name, p.quest_daily[4]);
        }
    }

    public static void finish_quest(Player p) throws IOException {
        if (p.quest_daily[0] == -1) {
            Service.send_notice_box(p.conn, "Hiện tại không nhận nhiệm vụ nào!");
        } else if (p.quest_daily[2] == p.quest_daily[3]) {
            short id_blue = (short) Util.random(216, 226);
            short id_yellow = (short) Util.random(226, 236);
            short id_violet = (short) Util.random(236, 246);
            int vang = Util.random(70, 120) * (p.quest_daily[1] + 1) * p.quest_daily[2];
            int ngoc = p.quest_daily[1] == 3 ? Util.random(80, 200)
                    : (p.quest_daily[1] == 2 ? Util.random(50, 70)
                    : (p.quest_daily[1] == 1 ? Util.random(20, 50) : Util.random(20, 50)));
            int exp = Util.random(50, 100) * (p.quest_daily[1] + 1) * p.quest_daily[2];

            p.update_vang(vang, "Nhận %s vàng từ nhiệm vụ hàng ngày");
            p.update_ngoc(ngoc);
            p.update_Exp(exp, false);
            if (p.quest_daily[1] == 1) {
                if (((p.item.get_inventory_able() > 0) || (p.item.total_item_by_id(7, id_blue) > 0))) {
                    Item47 itbag = new Item47();
                    itbag.id = id_blue;
                    itbag.quantity = (short) Util.random(0, 2);
                    itbag.category = 7;
                    p.item.add_item_inventory47(7, itbag);
                }
            } else if (p.quest_daily[1] == 2) {
                if (((p.item.get_inventory_able() > 0) || (p.item.total_item_by_id(7, id_yellow) > 0))) {
                    Item47 itbag = new Item47();
                    itbag.id = id_yellow;
                    itbag.quantity = (short) Util.random(0, 2);
                    itbag.category = 7;
                    p.item.add_item_inventory47(7, itbag);
                }
            } else if (p.quest_daily[1] == 3) {
                if (((p.item.get_inventory_able() > 0) || (p.item.total_item_by_id(7, id_violet) > 0))) {
                    Item47 itbag = new Item47();
                    itbag.id = id_violet;
                    itbag.quantity = (short) Util.random(0, 2);
                    itbag.category = 7;
                    p.item.add_item_inventory47(7, itbag);
                }
            }

            Service.send_notice_box(p.conn,
                    "Trả thành công, nhận được " + vang + " vàng, " + ngoc + " ngọc và " + exp + " kinh nghiệm!");
            p.quest_daily[0] = -1;
            p.quest_daily[1] = -1;
            p.quest_daily[2] = 0;
            p.quest_daily[3] = 0;
        } else {
            Service.send_notice_box(p.conn, "Chưa hoàn thành được nhiệm vụ!");
        }
    }
}

