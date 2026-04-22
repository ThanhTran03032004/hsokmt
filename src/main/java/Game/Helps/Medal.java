
package Game.Helps;

import Game.core.Util;
import Game.template.Item3;
import Game.template.Option;

import java.util.ArrayList;
import java.util.List;


public class Medal {
    public static List<Option> CreateMedal(byte countOP, byte color, short idItem) {
        List<Integer> id_percent_dame = new ArrayList<>(java.util.Arrays.asList(7, 8, 9, 10, 11));
        List<Integer> id_other = new ArrayList<>(java.util.Arrays.asList(14, 15, 16, 17, 18, 19, 20, 23, 24, 25, 26, 27, 28, 33, 34, 35, 36, 37, 38));
        List<Integer> id_special = new ArrayList<>(java.util.Arrays.asList(77, 79, 82, 86, 88, 80));
        List<Integer> ops = new ArrayList<>();
        List<Option> op = new ArrayList<>();

        int count = 0;
        while (count++ < countOP) {
            Integer id_add = null;
            int r = Util.random(0, 101);
            if (r <= 10) // cs vip
            {
                id_add = Util.random(id_special, ops);
            } else if (r <= 25) // %st
            {
                id_add = Util.random(id_percent_dame, ops);
            } else // cs default
            {
                id_add = Util.random(id_other, ops);
            }
            if (id_add == null) {
                count--;
                continue;
            }

            int param_add = 1;
            int param_add2 = 1;
            if (id_add == 38 || id_add == 37)
                param_add = 1;
            else if (id_add >= 23 && id_add <= 26) {
                param_add = Util.random(18, 31);
            } else if (id_special.contains(id_add)) {
                if (color == 0) {
                    param_add = Util.random(600, 800);
                    param_add2 = Util.random(600, 800);
                } else if (color == 1) {
                    param_add = Util.random(700, 900);
                    param_add2 = Util.random(700, 900);
                } else if (color == 2) {
                    param_add = Util.random(800, 1100);
                    param_add2 = Util.random(800, 1100);
                } else if (color == 3) {
                    param_add = Util.random(1000, 1350);
                    param_add2 = Util.random(1000, 1350);
                } else if (color == 4) {
                    param_add = Util.random(1200, 1500);
                    param_add2 = Util.random(1200, 1500);
                }
            } else if (id_add >= 7 && id_add <= 13) {
                if(color ==0)
                    param_add = Util.random(114, 160);
                else if(color ==1)
                    param_add = Util.random(160, 207);
                else if(color ==2)
                    param_add = Util.random(207, 254);
                else if(color ==3)
                    param_add = Util.random(253, 300);
                else if(color ==4)
                    param_add = Util.random(300, 347);
            } else {
                param_add = Util.random(50, 200);
            }
            op.add(new Option(id_add, param_add, idItem));

            ops.add(id_add);
            if (id_add == 77 || id_add == 79 || id_add == 82 || id_add == 86 || id_add == 88) {
                op.add(new Option(id_add - 1, param_add2, idItem));
            }
        }
        return op;
    }

    public static Item3 UpgradeMedal(Item3 tem) {
        List<Integer> id_percent_dame = new ArrayList<>(java.util.Arrays.asList(7, 8, 9, 10, 11));
        List<Integer> id_other = new ArrayList<>(java.util.Arrays.asList(14, 15, 16, 17, 18, 19, 20, 23, 24, 25, 26, 27, 28, 33, 34, 35, 36, 37, 38));
        List<Integer> id_special = new ArrayList<>(java.util.Arrays.asList(77, 79, 82, 86, 88, 80));
        List<Integer> ops = new ArrayList<>();
        byte countOP = 0;
        int count = 0;
        boolean isnext = false;
        tem.op.removeIf(op -> op != null && op.id >= 29 && op.id <= 32);
        for (int i = 0; i < tem.op.size(); i++) {
            Option op = tem.op.get(i);
            if (op == null) continue;
            ops.add((int) op.id);
            if (op.id >= 0 && op.id <= 4) continue;
            if ((op.id >= 58 && op.id <= 60) || (op.id >= 100 && op.id <= 107)) {
                continue;
            }
            if (op.id == 96) {
                countOP = (byte) op.getParam(0);
                continue;
            }
            if (op.id == 77 || op.id == 79 || op.id == 82 || op.id == 86 || op.id == 88) {
                isnext = true;
            }
            if (!isnext)
                count++;
            else
                isnext = false;
        }
        if (count < countOP) {
            Integer id_add = null;
            int r = Util.random(0, 101);
            if (r <= 10) // cs vip
            {
                id_add = Util.random(id_special, ops);
            } else if (r <= 25) // %st
            {
                id_add = Util.random(id_percent_dame, ops);
            } else // cs default
            {
                id_add = Util.random(id_other, ops);
            }
            if (id_add == null) {
                return tem;
            }

            int param_add = 1;
            int param_add2 = 1;
            if (id_add == 38 || id_add == 37)
                param_add = 1;
            else if (id_add >= 23 && id_add <= 26) {
                param_add = Util.random(18, 31);
            } else if (id_special.contains(id_add)) {
                if (tem.color == 0) {
                    param_add = Util.random(600, 800);
                    param_add2 = Util.random(600, 800);
                } else if (tem.color == 1) {
                    param_add = Util.random(700, 900);
                    param_add2 = Util.random(700, 900);
                } else if (tem.color == 2) {
                    param_add = Util.random(800, 1100);
                    param_add2 = Util.random(800, 1100);
                } else if (tem.color == 3) {
                    param_add = Util.random(1000, 1350);
                    param_add2 = Util.random(1000, 1350);
                } else if (tem.color == 4) {
                    param_add = Util.random(1200, 1500);
                    param_add2 = Util.random(1200, 1500);
                }
            } else if (id_add >= 7 && id_add <= 13) {
                if(tem.color ==0)
                    param_add = Util.random(114, 160);
                else if(tem.color == 1)
                    param_add = Util.random(160, 207);
                else if(tem.color == 2)
                    param_add = Util.random(207, 254);
                else if(tem.color == 3)
                    param_add = Util.random(253, 300);
                else if(tem.color == 4)
                    param_add = Util.random(300, 347);
            } else {
                param_add = Util.random(50, 200);
            }
            tem.op.add(new Option(id_add, param_add, tem.id));

            ops.add(id_add);
            if (id_add == 77 || id_add == 79 || id_add == 82 || id_add == 86 || id_add == 88) {
                tem.op.add(new Option(id_add - 1, param_add2, tem.id));
            }
        }
        if (countOP <= count) {
            for (int i = 0; i < tem.op.size(); i++) {
                Option op = tem.op.get(i);
                if (op == null) continue;
                if (op.id == 96) {
                    tem.op.remove(op);
                    break;
                }
            }
        }
        return tem;
    }
}
