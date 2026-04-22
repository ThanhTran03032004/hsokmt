package Game.template;

public class Horse {
    public static final byte NGUA_NAU = 0;
    public static final byte NGUA_TRANG = 1;
    public static final byte NGUA_CHIEN_GIAP = 2;
    public static final byte NGUA_XICH_THO = 3;
    public static final byte NGUA_DEN = 4;
    public static final byte TUAN_LOC = 5;
    public static final byte SOI_XAM = 6;
    public static final byte SOI_GIO_TUYET = 7;
    public static final byte SOI_BAO_LUA = 8;
    public static final byte SOI_BONG_MA = 9;
    public static final byte SU_TU = 10;
    public static final byte HEO_RUNG = 11;
    public static final byte CON_LAN = 12;
    public static final byte SKELETON = 13;
    public static final byte CHUOT_TUYET = 14;
    public static final byte VOI_MA_MUT = 15;
    public static final byte TRAU_RUNG = 16;
    public static final byte CAN_DAU_VAN = 17;
    public static final byte MA_TOC_DO = 18;
    public static final byte XE_TRUOT_TUYET = 19;
    public static final byte HOA_KY_LAN = 20;
    public static final byte PHUONG_HOANG_LUA = 21;
    public static final byte RONG_BANG = 22;
    public static final byte CA_CHEP = 23;
    public static boolean isHorseClan(int id) {
        return id >= 6 && id <= 10;
    }
}
