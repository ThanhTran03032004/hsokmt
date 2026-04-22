package Game.template;

import Game.Helps.CheckItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Item3 {

    public short id;
    public byte clazz;
    public byte type;
    public short level;
    public short icon;
    public byte color;
    public byte part;
    public boolean islock;
    public String name;
    public byte tier;
    public byte tierStar;
    public List<Option> op;
    public List<Option> opMedal;
    public long time_use;
    public long expiry_date;
    public short[] item_medal;
    public short index;

    public Item3() {
    }

    public Item3(Item3 Origin) {
        this.id = Origin.id;
        this.clazz = Origin.clazz;
        this.type = Origin.type;
        this.level = Origin.level;
        this.icon = Origin.icon;
        this.color = Origin.color;
        this.part = Origin.part;
        this.islock = Origin.islock;
        this.name = Origin.name;
        this.tier = Origin.tier;
        this.tierStar = Origin.tierStar;
        this.op = new ArrayList<>();
        if (Origin.op != null) {
            this.op.addAll(Origin.op);
        }
        this.opMedal = new ArrayList<>();
        if (Origin.opMedal != null) {
            this.opMedal.addAll(Origin.opMedal);
        }
        this.time_use = Origin.time_use;
        this.expiry_date = Origin.expiry_date;
        this.item_medal = Origin.item_medal;
        sanitizeMedalOptions();
    }
    public static JSONArray convertToJson(Item3 temp) {
        temp.sanitizeMedalOptions();
        JSONArray jsar2 = new JSONArray();
        jsar2.add(temp.id);
        jsar2.add(temp.clazz);
        jsar2.add(temp.type);
        jsar2.add(temp.level);
        jsar2.add(temp.icon);
        jsar2.add(temp.color);
        jsar2.add(temp.part);
        jsar2.add(temp.islock ? 1 : 0);
        jsar2.add(temp.tier);
        JSONArray jsar3 = new JSONArray();
        for (int j = 0; j < temp.op.size(); j++) {
            JSONArray jsar4 = new JSONArray();
            jsar4.add(temp.op.get(j).id);
            jsar4.add(temp.op.get(j).getParam(0));
            jsar3.add(jsar4);
        }
        jsar2.add(jsar3);
        jsar2.add(temp.time_use);
        jsar2.add(temp.tierStar);
        jsar2.add(temp.expiry_date);
        JSONArray jsar4 = new JSONArray();
        if (CheckItem.isMeDay(temp.id)) {
            if (temp.item_medal == null) {temp.item_medal = new short[5];};
            for (int j = 0; j < temp.item_medal.length; j++) {
                jsar4.add(temp.item_medal[j]);
            }
            jsar2.add(jsar4);
        }
        return jsar2;
    }
    public static Item3 parseJSON(JSONArray jsar2) {
        try {
            Item3 temp = new Item3();
            temp.id = Short.parseShort(jsar2.get(0).toString());
            temp.clazz = Byte.parseByte(jsar2.get(1).toString());
            temp.type = Byte.parseByte(jsar2.get(2).toString());
            temp.level = Short.parseShort(jsar2.get(3).toString());
            temp.icon = Short.parseShort(jsar2.get(4).toString());
            temp.color = Byte.parseByte(jsar2.get(5).toString());
            temp.part = Byte.parseByte(jsar2.get(6).toString());
            temp.islock = Byte.parseByte(jsar2.get(7).toString()) == 1;
            temp.name = ItemTemplate3.item.get(temp.id).getName();
            if (temp.islock) {
                temp.name += " [Khóa]";
            }
            temp.tier = Byte.parseByte(jsar2.get(8).toString());
            JSONArray jsar3 = (JSONArray) JSONValue.parse(jsar2.get(9).toString());
            temp.op = new ArrayList<>();
            for (Object o : jsar3) {
                JSONArray jsar4 = (JSONArray) JSONValue.parse(o.toString());
                temp.op.add(
                        new Option(Byte.parseByte(jsar4.get(0).toString()), Integer.parseInt(jsar4.get(1).toString()), temp.id));
            }
            temp.time_use = 0;
            int index = -1;
            if (jsar2.size() >= 11) {
                index = 10;
                temp.time_use = Long.parseLong(jsar2.get(10).toString());
            }
            if (jsar2.size() >= 12) {
                index = 12;
                temp.tierStar = Byte.parseByte(jsar2.get(11).toString());
            }
            if (jsar2.size() >= 13) {
                index = 13;
                temp.expiry_date = Long.parseLong(jsar2.get(12).toString());
            }
            if (CheckItem.isMeDay(temp.id) && jsar2.size() >= index) {
                temp.item_medal = new short[5];
                JSONArray jsar4 = (JSONArray) JSONValue.parse(jsar2.get(index).toString());
                for (int j = 0; j < 5; j++) {
                    temp.item_medal[j] = Short.parseShort(jsar4.get(j).toString());
                }
            }
            temp.sanitizeMedalOptions();
            temp.UpdateName();
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sanitizeMedalOptions() {
        if (!CheckItem.isMeDay(id) || op == null) {
            return;
        }
        op.removeIf(option -> option != null && option.id >= 29 && option.id <= 32);
    }
    public Option GetNextOPMedal() {
        if (this.opMedal == null || this.opMedal.size() < 1) {
            this.opMedal = null;
            return null;
        }
        return this.opMedal.remove(0);
    }

    public void UpdateName() {
        name = ItemTemplate3.item.get(id).getName();
        if (islock) {
            name += " [Khóa]";
        }
        if (tierStar > 0) {
            name += " [Cấp " + tierStar + "]";
        }
    }

    public boolean isTT() {
        return (id >= 3732 && id <= 3736) || id >= 3807 && id <= 3811 || id >= 3897 && id <= 3901 || id >= 4656 && id <= 4675;
    }
    public boolean isWingClan() {
        return (id >= 4642 && id <= 4648);
    }
    public void UpdateOption() {
        int[] opAo = {-123, -122, -121, -120, -119};
        int[] opNon = {-114, -125, -117};
        int[] opVK = {-128, -125, 99, -85, -83, -81};
        int[] opNhan = {-90, -88, -116, 99, -85, -83, -81};
        int[] opDayChuyen = {-88, -117, -115, -92};
        int[] opGang = {-90, -115, -92};
        int[] opGiay = {-116, -115, -92};

        Integer[] opAoOld = {-111, -110, -109, -108, -107};
        Integer[] opNonOld = {-102, -113, -105};
        Integer[] opVKOld = {-101, -113, -86, -84, -82, -80};
        Integer[] opNhanOld = {-89, -87, -104, -86, -84, -82, -80};
        Integer[] opDayChuyenOld = {-87, -105, -103, -91};
        Integer[] opGangOld = {-89, -103, -91};
        Integer[] opGiayOld = {-104, -103, -91};

        if (type == 0 || type == 1) {
            List<Integer> list = Arrays.asList(opAoOld);
            for (Option option : op) {
                if (list.contains((int) option.id)) {
                    option.id = (byte) opAo[list.indexOf((int) option.id)];
                }
            }
        } else if (type == 2) {
            List<Integer> list = Arrays.asList(opNonOld);
            for (Option option : op) {
                if (list.contains((int) option.id)) {
                    option.id = (byte) opNon[list.indexOf((int) option.id)];
                }
            }
        } else if (type == 3) {
            List<Integer> list = Arrays.asList(opGangOld);
            for (Option option : op) {
                if (list.contains((int) option.id)) {
                    option.id = (byte) opGang[list.indexOf((int) option.id)];
                }
            }
        } else if (type == 4) {
            List<Integer> list = Arrays.asList(opNhanOld);
            for (Option option : op) {
                if (list.contains((int) option.id)) {
                    option.id = (byte) opNhan[list.indexOf((int) option.id)];
                }
            }
        } else if (type == 5) {
            List<Integer> list = Arrays.asList(opDayChuyenOld);
            for (Option option : op) {
                if (list.contains((int) option.id)) {
                    option.id = (byte) opDayChuyen[list.indexOf((int) option.id)];
                }
            }
        } else if (type == 6) {
            List<Integer> list = Arrays.asList(opGiayOld);
            for (Option option : op) {
                if (list.contains((int) option.id)) {
                    option.id = (byte) opGiay[list.indexOf((int) option.id)];
                }
            }
        } else if (type > 7) {
            List<Integer> list = Arrays.asList(opVKOld);
            for (Option option : op) {
                if (list.contains((int) option.id)) {
                    option.id = (byte) opVK[list.indexOf((int) option.id)];
                }
            }
        }

    }

    public void ReUpdateOption() {
        Integer[] opAoOld = {-123, -122, -121, -120, -119};
        Integer[] opNonOld = {-114, -125, -117};
        Integer[] opVKOld = {-128, -125, 99, -85, -83, -81};
        Integer[] opNhanOld = {-90, -88, -116, 99, -85, -83, -81};
        Integer[] opDayChuyenOld = {-88, -117, -115, -92};
        Integer[] opGangOld = {-90, -115, -92};
        Integer[] opGiayOld = {-116, -115, -92};

        int[] opAo = {-111, -110, -109, -108, -107};
        int[] opNon = {-102, -113, -105};
        int[] opVK = {-101, -113, -86, -84, -82, -80};
        int[] opNhan = {-89, -87, -104, -86, -84, -82, -80};
        int[] opDayChuyen = {-87, -105, -103, -91};
        int[] opGang = {-89, -103, -91};
        int[] opGiay = {-104, -103, -91};

        if (type == 0 || type == 1) {
            List<Integer> list = Arrays.asList(opAoOld);
            for (int i = 0; i < op.size(); i++) {
                if (list.contains((int) op.get(i).id)) {
                    op.get(i).id = (byte) opAo[list.indexOf((int) op.get(i).id)];
                }
            }
        } else if (type == 2) {
            List<Integer> list = Arrays.asList(opNonOld);
            for (int i = 0; i < op.size(); i++) {
                if (list.contains((int) op.get(i).id)) {
                    op.get(i).id = (byte) opNon[list.indexOf((int) op.get(i).id)];
                }
            }
        } else if (type == 3) {
            List<Integer> list = Arrays.asList(opGangOld);
            for (int i = 0; i < op.size(); i++) {
                if (list.contains((int) op.get(i).id)) {
                    op.get(i).id = (byte) opGang[list.indexOf((int) op.get(i).id)];
                }
            }
        } else if (type == 4) {
            List<Integer> list = Arrays.asList(opNhanOld);
            for (int i = 0; i < op.size(); i++) {
                if (list.contains((int) op.get(i).id)) {
                    op.get(i).id = (byte) opNhan[list.indexOf((int) op.get(i).id)];
                }
            }
        } else if (type == 5) {
            List<Integer> list = Arrays.asList(opDayChuyenOld);
            for (int i = 0; i < op.size(); i++) {
                if (list.contains((int) op.get(i).id)) {
                    op.get(i).id = (byte) opDayChuyen[list.indexOf((int) op.get(i).id)];
                }
            }
        } else if (type == 6) {
            List<Integer> list = Arrays.asList(opGiayOld);
            for (int i = 0; i < op.size(); i++) {
                if (list.contains((int) op.get(i).id)) {
                    op.get(i).id = (byte) opGiay[list.indexOf((int) op.get(i).id)];
                }
            }
        } else if (type > 7) {
            List<Integer> list = Arrays.asList(opVKOld);
            for (int i = 0; i < op.size(); i++) {
                if (list.contains((int) op.get(i).id)) {
                    op.get(i).id = (byte) opVK[list.indexOf((int) op.get(i).id)];
                }
            }
        }
    }
    public boolean hasOp(int id) {
        for (Option option : op) {
            if (option.id == id) {
                return true;
            }
        }
        return false;
    }
    public boolean hasOpPercentDame() {
        for (Option option : op) {
            if (option.id >= 7 && option.id <= 11) {
                return true;
            }
        }
        return false;
    }
    public boolean isTrangBi() {
        return this.type >= 0 && this.type <= 4 || this.type >= 6 && this.type <= 9;
    }
    public static boolean isBook(int id) {
        return 4577 <= id && id <= 4584;
    }

    public int getParamOption(int id) {
        for (Option o : op) {
            if (o.id == id) {
                return o.param;
            }
        }
        return 0;
    }
}
