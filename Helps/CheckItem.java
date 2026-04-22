
package Game.Helps;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class CheckItem {
    public static boolean isMeDay(short id) {
        return id >= 4587 && id <= 4590;
    }
    public static boolean isWing(short id) {
        return id >= 2880 && id <= 2935;
    }
    public static boolean isTinhTu(short id) {
        return (id >= 3732 && id <= 3736) || id >= 3807 && id <= 3811 || id >= 3897 && id <= 3901 || id >= 4656 && id <= 4675;
    }
    public static boolean item4CanTrade(short id) {
        List<Short> cantrade = new ArrayList<>(java.util.Arrays.asList((short) 101, (short) 205, (short) 206, (short) 207, (short) 224, (short) 249, (short) 273, (short) 274));
        boolean sknoel = (id >= 113 && id <= 123) || id == 183;
        boolean vpsk = (id >= 28 && id <= 47) || (id >= 89 && id <= 130 && id != 124 && id != 125) || (id >= 137 && id <= 145 && id != 142 && id != 143) || (id >= 148 && id <= 158) || id == 162
                || id == 172 || (id >= 183 && id <= 195) || (id >= 199 && id <= 204) || (id >= 254 && id <= 258) || (id >= 303 && id <= 308) || (id >= 48 && id <= 51);
        return sknoel || vpsk || cantrade.contains(id);
    }

    public static boolean isBuyItemCoin(short id) {
        List<Short> itemcoin = new ArrayList<>(java.util.Arrays.asList((short) 4587, (short) 4588, (short) 4589, (short) 4590, (short) 4656, (short) 4660, (short) 4664, (short) 4668, (short) 4669, (short) 4670, (short) 4671, (short) 4672, (short) 4658, (short) 4662, (short) 4666, (short) 4675, (short) 4657, (short) 4661, (short) 4665, (short) 4673, (short) 4659, (short) 4663, (short) 4667, (short) 4674, (short) 4718, (short) 4779));
        return itemcoin.contains(id);
    }

    public static boolean isDaKham(short id) {
        return (id >= 352 && id <= 356) || (id >= 382 && id <= 386) || //hỗn nguyễn
                (id >= 357 && id <= 361) || (id >= 387 && id <= 391) || //khải hoàn
                (id >= 362 && id <= 366) || (id >= 392 && id <= 396) || //lục bảo
                (id >= 367 && id <= 371) || (id >= 397 && id <= 401) || //phong ma
                (id >= 372 && id <= 376) || (id >= 402 && id <= 406) || //sinh mệnh
                (id >= 377 && id <= 381) || (id >= 407 && id <= 411); //tâm linh
    }

    public static boolean isArmor(short idItem) {
        return 4784 <= idItem && idItem <= 4787;
    }
}
