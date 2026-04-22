package Game.client;

import java.util.ArrayList;
import java.util.List;

import Game.core.Util;
import Game.template.Level;
import Game.template.OptionPet;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class Pet {

    public static String[] name_template = new String[]{"Cú", "Dơi", "Sói", "Đại Bàng", "Khỉ", "Rồng lửa", "Thỏ",
            "Phượng hoàng băng", "Zabivaka", "Bóng ma", "Dê con", "Yêu tinh", "Thiên thần", "Sao la", "Mèo"};
    static short[] id_template = new short[]{2944, 2943, 2939, 3269, 3616, 4617, 4622, 4626, 4631, 4699, 4708, 4761, 4762, 4768, 4788};
    public static byte[] type_template = new byte[]{41, 8, 13, 10, 11, 11, 11, 12, 12, 12, 9, 7, 7, 7, 6, 5, 5, 5, 4, 4, 4,
            3, 3, 3, 2, 2, 2, 1, 1, 1, 0, 0, 0};
    public static byte[] icon_template = new byte[]{26, 41, 32, 33, 34, 35, 36, 37, 38, 29, 21, 22, 23, 20, 15, 16, 17,
            12, 13, 14, 9, 10, 11, 6, 7, 8, 0, 1, 2, 3, 4, 5};
    public static byte ATTACK_OWL = 0;
    public static byte ATTACK_BAT = 1;
    public static byte ATTACK_MELEE = 2;
    public static byte ATTACK_POISON_NOVA = 3;
    public static byte ATTACK_ICE_NOVA = 4;
    public static byte ATTACK_FIRE_BLAST = 5;
    public static byte ATTACK_PET_EAGLE = 10;
    public static byte ATTACK_TOOL_1 = 11;
    public static byte ATTACK_TOOL_2 = 12;

    public List<OptionPet> op;
    public String name;
    public short level;
    public byte type;
    public byte icon;
    public byte nframe;
    public byte color;
    public short grown;
    public short maxgrown;
    public short sucmanh;
    public short kheoleo;
    public short theluc;
    public short tinhthan;
    public short maxpoint;
    public boolean is_follow;
    public boolean is_hatch;
    public long exp;
    public long time_born;
    public long time_eat;
    public long expiry_date;

    public void setup(JSONArray js) {
        level = Short.parseShort(js.get(0).toString());
        type = Byte.parseByte(js.get(1).toString());
        name = name_template[type];
        icon = Byte.parseByte(js.get(2).toString());
        nframe = Byte.parseByte(js.get(3).toString());
        color = Byte.parseByte(js.get(4).toString());
        grown = Short.parseShort(js.get(5).toString());
        maxgrown = Short.parseShort(js.get(6).toString());
        sucmanh = Short.parseShort(js.get(7).toString());
        kheoleo = Short.parseShort(js.get(8).toString());
        theluc = Short.parseShort(js.get(9).toString());
        tinhthan = Short.parseShort(js.get(10).toString());
        maxpoint = Short.parseShort(js.get(11).toString());
        maxpoint = 20000;
        exp = Long.parseLong(js.get(12).toString());
        is_follow = Byte.parseByte(js.get(13).toString()) == 1;
        is_hatch = Byte.parseByte(js.get(14).toString()) == 1;
        time_born = Long.parseLong(js.get(15).toString());
        op = new ArrayList<>();
        JSONArray js2 = (JSONArray) JSONValue.parse(js.get(16).toString());
        for (Object o : js2) {
            JSONArray js3 = (JSONArray) JSONValue.parse(o.toString());
            OptionPet temp = new OptionPet(Byte.parseByte(js3.get(0).toString()),
                    Integer.parseInt(js3.get(1).toString()), Integer.parseInt(js3.get(2).toString()));
            op.add(temp);
        }
        if (js.size() >= 18)
            expiry_date = Long.parseLong(js.get(17).toString());
    }

    public JSONArray parseToJSON() {
        JSONArray js1 = new JSONArray();
        js1.add(this.level);
        js1.add(this.type);
        js1.add(this.icon);
        js1.add(this.nframe);
        js1.add(this.color);
        js1.add(this.grown);
        js1.add(this.maxgrown);
        js1.add(this.sucmanh);
        js1.add(this.kheoleo);
        js1.add(this.theluc);
        js1.add(this.tinhthan);
        js1.add(this.maxpoint);
        js1.add(this.exp);
        js1.add(this.is_follow ? 1 : 0);
        js1.add(this.is_hatch ? 1 : 0);
        js1.add(this.time_born);
        JSONArray js2 = new JSONArray();
        for (int i2 = 0; i2 < this.op.size(); i2++) {
            JSONArray js3 = new JSONArray();
            js3.add(this.op.get(i2).id);
            js3.add(this.op.get(i2).param);
            js3.add(this.op.get(i2).maxdame);
            js2.add(js3);
        }
        js1.add(js2);
        js1.add(this.expiry_date);
        return js1;
    }

    public int getlevelpercent() {
        return (int) ((exp * 1000) / Level.entry.get(level - 1).exp);
    }

    public static Pet newPet(short id, long hsd) {
        Pet temp = new Pet();
        temp.level = 1;
        temp.nframe = 3;
        temp.color = 0;
        temp.grown = 0;
        temp.maxgrown = 300;
        temp.sucmanh = 0;
        temp.kheoleo = 0;
        temp.theluc = 0;
        temp.tinhthan = 0;
        temp.maxpoint = 20_000;
        temp.exp = 0;
        temp.is_follow = false;
        temp.is_hatch = true;
        temp.time_born = System.currentTimeMillis() + 1000L * 60;
        temp.op = new ArrayList<>();
        if (hsd > 0) {
            temp.expiry_date = hsd + System.currentTimeMillis();
        }
        switch (id) {
            case 2943: {//dơi
                temp.icon = 0;
                temp.type = 1;
                break;
            }
            case 2944: {//cú
                temp.icon = 3;
                temp.type = 0;
                break;
            }
            case 2939: {//sói
                temp.icon = 6;
                temp.type = 2;
                break;
            }
            case 3269: {//đại bàng
                temp.icon = 9;
                temp.type = 3;
                break;
            }
            case 3616: {//khỉ
                temp.icon = 12;
                temp.type = 4;
                break;
            }
            case 4617: {//rong lua
                temp.icon = 15;
                temp.type = 5;
                break;
            }
            case 4626: {//phb
                temp.icon = 21;
                temp.type = 7;
                break;
            }
            case 4631: {//zabivaka
                temp.icon = 24;
                temp.type = 8;
                break;
            }
            case 4699: {//ma
                temp.icon = 27;
                temp.type = 9;
                break;
            }
            case 4761: {//yeu tinh
                temp.icon = 33;
                temp.type = 11;
                break;
            }
            case 4762: {//thien than
                temp.icon = 36;
                temp.type = 12;
                break;
            }
            case 4768: {//sao la
                temp.icon = 39;
                temp.type = 13;
                break;
            }
            case 4708://dê
            {
                temp.icon = 30;
                temp.type = 10;
                break;
            }
            case 4788: {//mèo
                temp.icon = 40;
                temp.type = 14;
                break;
            }
            default:
                return null;
        }
        temp.name = name_template[temp.type];
        temp.setOption();
        return temp;
    }

    public void setOption() {
        this.op = new ArrayList<>();

        // Option default
        for (short i = 23; i < 27; i++) {
            OptionPet op = new OptionPet(i, 0, 0);
            this.op.add(op);
        }
        int dame_base = Util.random(900, 1200);
        OptionPet op;
        switch (this.get_id()) {
            case 2943: {
                // Dơi
                op = new OptionPet(4, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(47, 400, 0);
                this.op.add(op);
                break;
            }
            case 2944: {
                // Cú
                op = new OptionPet(1, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(44, 200, 0);
                this.op.add(op);
                op = new OptionPet(45, 3000, 0);
                this.op.add(op);
                break;
            }
            case 2939, 3616: {
                //Sói, Khỉ nâu
                op = new OptionPet(0, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(49, 100, 0);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                break;
            }
            case 3269: {
                // Đại bàng
                op = new OptionPet(3, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(46, 2000, 0);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                break;
            }
            case 4617: {
                // Rồng lửa
                op = new OptionPet(2, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                op = new OptionPet(97, Util.random(2300, 2400), 0);
                this.op.add(op);
                op = new OptionPet(98, 1500, 0);
                this.op.add(op);
                break;
            }
            case 4626: {
                // Phượng hoàng băng
                op = new OptionPet(1, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(67, 2350, 100000);
                this.op.add(op);
                op = new OptionPet(113, 500, 0);
                this.op.add(op);
                op = new OptionPet(114, 2, 0);
                this.op.add(op);
                break;
            }
            case 4631: {
                // Zabivaka
                op = new OptionPet(0, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                op = new OptionPet(80, 200, 0);
                this.op.add(op);
                op = new OptionPet(85, 150, 0);
                this.op.add(op);
                op = new OptionPet(86, 1500, 0);
                this.op.add(op);
                op = new OptionPet(114, 2, 0);
                this.op.add(op);
                break;
            }
            case 4699, 4768, 4788: {
                op = new OptionPet(1, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                op = new OptionPet(80, 200, 0);
                this.op.add(op);
                op = new OptionPet(85, 150, 0);
                this.op.add(op);
                op = new OptionPet(86, 1500, 0);
                this.op.add(op);
                op = new OptionPet(114, 2, 0);
                this.op.add(op);
                break;
            }
            case 4761: {
                // Yêu tinh
                op = new OptionPet(2, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                op = new OptionPet(80, 200, 0);
                this.op.add(op);
                op = new OptionPet(114, 2, 0);
                this.op.add(op);
                break;
            }
            case 4762: {
                // Thiên thần
                op = new OptionPet(3, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                op = new OptionPet(80, 200, 0);
                this.op.add(op);
                op = new OptionPet(114, 2, 0);
                this.op.add(op);
                break;
            }
            case 4708: {
                // Dê
                op = new OptionPet(0, dame_base, dame_base * 6 / 5);
                this.op.add(op);
                op = new OptionPet(49, 100, 0);
                this.op.add(op);
                op = new OptionPet(48, 1500, 0);
                this.op.add(op);
                op = new OptionPet(159, 1, 0);
                this.op.add(op);
                break;
            }
        }
        if (this.expiry_date > 0) {
            this.level = 35;
            this.sucmanh = theluc = tinhthan = kheoleo = 20000;
            this.grown = 300;
            setPoint();
        }
    }

    public short get_id() {
        if (type < id_template.length)
            return id_template[type];
        else
            return 2943;
    }

    public void update_exp(int i) {
        if (i <= 0) {
            return;
        }
        exp += i;
        setPoint();
        if ((this.level == 9 || this.level == 19 || this.level == 30) && exp >= Level.entry.get(level - 1).exp) {
            exp = Level.entry.get(level - 1).exp - 1;
        } else {
            while (exp >= Level.entry.get(level - 1).exp) {
                exp -= Level.entry.get(level - 1).exp;
                UpgradeLevel();
            }
        }
    }

    public void UpgradeLevel() {
        level++;
    }

    public void setPoint() {
        for (OptionPet o : op) {
            switch (o.id) {
                case 23:
                    o.param = (sucmanh / 140);
                    break;
                case 24:
                    o.param = (kheoleo / 140);
                    break;
                case 25:
                    o.param = (theluc / 140);
                    break;
                case 26:
                    o.param = (tinhthan / 140);
                    break;
            }
            if (o.param >= 128 && o.id >= 23 && o.id <= 26) {
                o.param = 128;
            }
        }
    }

    public int getParam(byte id) {
        for (OptionPet o : op) {
            if (o.id == id) {
                if (0 <= id && id <= 4) {
                    if (this.level >= 20) {
                        return o.param + o.param * this.level;
                    } else if (this.level >= 10) {
                        return o.param + o.param * this.level / 2;
                    } else {
                        return o.param + o.param * this.level / 3;
                    }
                } else if (id == 44 || id == 47) {
                    if (this.level >= 29) {
                        return o.param + 100;
                    } else {
                        return o.param;
                    }
                } else if (id == 85) {
                    if (this.level >= 29) {
                        return o.param + 300;
                    } else {
                        return o.param;
                    }
                } else if (id == 45) {
                    if (this.level >= 29) {
                        return o.param + 2000;
                    } else {
                        return o.param;
                    }
                } else if (id == 48 || id == 98 || id == 86) {
                    if (this.level >= 29) {
                        return o.param + 3000;
                    } else {
                        return o.param;
                    }
                } else {
                    return o.param;
                }
            }
        }
        return 0;
    }

    public int getMaxDame(byte id) {
        for (OptionPet o : op) {
            if (o.id == id) {
                if (0 <= id && id <= 4) {
                    if (this.level >= 20) {
                        return o.maxdame + o.maxdame * this.level;
                    } else if (this.level >= 10) {
                        return o.maxdame + o.maxdame * this.level / 2;
                    } else {
                        return o.maxdame + o.maxdame * this.level / 3;
                    }
                }
            }
        }
        return 0;
    }

    public int get_age() {
        long age = System.currentTimeMillis() - time_born;
        age /= 3_600_000;
        if (age < 0) {
            age = 0;
        } else if (age > Integer.MAX_VALUE) {
            age = Integer.MAX_VALUE;
        }
        return (int) age;
    }

    public boolean can_revolution() {
        if ((this.exp >= (Level.entry.get(this.level - 1).exp - 1)) && (this.level == 9 || this.level == 19)) {
            return true;
        }
        return false;
    }

    public void update_grown(short t) {
        if (this.expiry_date > 0) return;
        this.grown -= t;
        if (this.grown < 0) {
            this.grown = 0;
        }
    }

    public byte getSkillPet() {
        switch (this.type) {
            case 0 -> {
                return ATTACK_OWL;
            }
            case 1 -> {
                return ATTACK_BAT;
            }
            case 2 -> {
                return ATTACK_MELEE;
            }
            case 3 -> {
                return ATTACK_PET_EAGLE;
            }
            case 4 -> {
                return ATTACK_POISON_NOVA;
            } // hỏng thì xóa 4-8
            case 5 -> {
                return ATTACK_ICE_NOVA;
            }
            case 6 -> {
                return ATTACK_FIRE_BLAST;
            }
            case 8 -> {
                return ATTACK_TOOL_1;
            }
            case 7 -> {
                return 4;
            }
            default -> {
                return ATTACK_TOOL_2;
            }
        }
    }

    public byte getEff() {
        switch (this.type) {
            case 0 -> {
                return 6;
            }
            case 3 -> {
                return 7;
            }
            default -> {
                return -1;
            }
        }
    }
    public static boolean isEgg(short id) {
        for (short value : id_template) {
            if (value == id) {
                return true;
            }
        }
        return false;
    }
}
