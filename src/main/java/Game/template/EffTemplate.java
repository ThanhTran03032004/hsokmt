package Game.template;

import Game.client.Player;

public class EffTemplate {
    // 0 -> 4 : special skill clazz, 4: vat ly
    // 52 : + 10 %vang
    // 53 : + 1% hoi hp
    // -121 : troi 4 he clazz 0
    // -122 : .... 
    // -123 : ....
    // -124 : ....
    // 23 : + suc manh skill buff
    // 24 : + phong thu skill buff
    // -126 : chong pk
    // -125 : x2 time
    // -128 : Time Lang Phu Suong

    // = option item = param this option
    public static final int GIAP_BAO_HO = 38;
    public static final int HUNG_TAN = 34;
    public static final int GIAP_BACH_KIM = 41;
    public static final int GIAP_THIEN_SU = 42;
    public static final int GIAP_VE_BINH = 43;
    public static final int NGU_DAN = 44;
    public static final int BAT_TU = 48;
    public static final int MU_MAT = 35;
    public static final int THIEU_CHAY = 37;
    public static final int TAN_PHE = 40;
    public static final int LINH_CANH = 67;
    public static final int HIEP_SI_TAP_SU = 68;
    public static final int HIEP_SI_NHO = 69;
    public static final int HIEP_SI = 70;
    public static final int DAI_HIEP_SI = 71;

    public int id;
    public short param;
    public short param2;
    public long time;
    public MainObject o_atk;

    public EffTemplate(int id, int param, long time) {
        this.id = id;
        this.param = (short)param;
        this.time = time;
    }
    public EffTemplate(int id, int param, int para2, long time) {
        this.id = id;
        this.param = (short)param;
        this.param2 = (short)para2;
        this.time = time;
    }
    public EffTemplate(int id, int param, int para2, long time, Player p) {
        this.id = id;
        this.param = (short)param;
        this.param2 = (short)para2;
        this.time = time;
        this.o_atk = p;
    }
}
