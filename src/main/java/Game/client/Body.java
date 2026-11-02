package Game.client;

import Game.activities.Maze;
import Game.core.Manager;
import Game.core.Util;
import Game.activities.ChiemThanhManager;
import Game.io.Message;
import Game.map.MapService;
import Game.template.*;
import Game.core.Service;

import java.io.IOException;
import java.util.ArrayList;

import Game.map.Map;

public class Body extends MainObject {

    private Player p;

    protected void SetPlayer(Player p) {
        if (this.p != null) {
            return;
        }
        this.p = p;
        kham = new Kham_template();
        MainEff = new ArrayList<>();
        Eff_me_kham = new ArrayList<>();
        Eff_Tinh_Tu = new ArrayList<>();
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    private int get_point(int i) {
        int point = 0;
        switch (i) {
            case 1: {
                point += p.point1 + get_plus_point(23);
                break;
            }
            case 2: {
                point += p.point2 + get_plus_point(24);
                break;
            }
            case 3: {
                point += p.point3 + get_plus_point(25);
                break;
            }
            case 4: {
                point += p.point4 + get_plus_point(26);
                break;
            }
        }
        return point;
    }

    public Pet get_pet_follow() {
        for (Pet pett : p.mypet) {
            if (pett.is_follow) {
                return pett;
            }
        }
        return null;
    }

    public int get_point_pet(int i) {
        Pet my_pet = get_pet_follow();
        if (my_pet != null && my_pet.grown > 0) {
            my_pet.setPoint();
            for (int i2 = 0; i2 < my_pet.op.size(); i2++) {
                OptionPet temp = my_pet.op.get(i2);
                if (temp.id == i) {
                    return temp.param;
                }
            }
        }
        return 0;
    }

    public int get_plus_point(int i) {
        int param = 0;
        switch (i) {
            case 23: {
                param += total_item_param(i);
                EffTemplate ef = p.get_EffDefault(23);
                if (ef != null) {
                    param += (p.point1 * (ef.param / 100)) / 100;
                }
                break;
            }
            case 24, 26, 25: {
                param += total_item_param(i);
                break;
            }
        }
        return param + get_point_pet(i);
    }

    @Override
    public int total_item_param(int id) {
        int param = 0;
        if (id == 112) {
            return p.point_king_cup;
        } else if (id == -75) {
            return p.getDiemNap();
        } else {
            for (int i = 0; i < p.item.wear.length; i++) {
                Item3 temp = p.item.wear[i];
                if (temp != null) {
                    if (p.level < temp.level) {
                        continue;
                    }
                    for (Option op : temp.op) {
                        if (op.id == id) {
                            param += op.getParam(temp.tier);
                        }
                    }
                }
            }
            return param;
        }
    }

    public int getPercentExpUp() {
        int percent = 0;
        EffTemplate ef = p.get_EffDefault(-125);
        if (ef != null) {
            percent += 50;
        }
        if (p.type_use_horse == Horse.NGUA_DEN || p.type_use_horse == Horse.SOI_GIO_TUYET || p.type_use_horse == Horse.CON_LAN) {
            percent += 5;
        } else if (p.type_use_horse == Horse.SOI_BAO_LUA) {
            percent += 7;
        } else if (p.type_use_horse == Horse.SOI_BONG_MA) {
            percent += 10;
        }
        if (Manager.gI().ty_phu.contains(p.name)) {
            percent += 20;
        } else if (Manager.gI().trieu_phu.contains(p.name)) {
            percent += 15;
        } else if (Manager.gI().dai_gia.contains(p.name)) {
            percent += 10;
        }
        if (p.myclan != null) { // Sách bang
            EffTemplate eff_clan = p.myclan.getEffect(Clan.TIME_SACH);
            if (eff_clan != null) {
                percent += 15;
            }
        }
        if (p.map.zone_id == 1 && !Map.is_map_not_zone2(p.map.map_id)) { // Khu 2
            percent += 5;
        }
        // Lua thieng
        if (p.myclan != null && p.map.luathieng != null
                && p.map.luathieng.time > System.currentTimeMillis()
                && p.map.luathieng.isPointInCircle(p.x, p.y)
                && p.map.luathieng.clan.name_clan.equals(p.myclan.name_clan)) {
            percent += p.map.luathieng.getPercentExp();
        }
        return percent;
    }

    public int getPercentBuffHP() {
        int percent = 0;
        percent = p.body.total_skill_param(29) + p.body.total_item_param(29);
        if (p.type_use_horse == Horse.NGUA_DEN || p.type_use_horse == Horse.SOI_GIO_TUYET || p.type_use_horse == Horse.CON_LAN) {
            percent += 5;
        } else if (p.type_use_horse == Horse.SOI_BAO_LUA) {
            percent += 7;
        } else if (p.type_use_horse == Horse.SOI_BONG_MA) {
            percent += 10;
        }
        return percent;
    }

    @Override
    public int get_HpMax() {
        long hpm = 0;
        switch (p.clazz) {
            case 0: {
                hpm += 1400 + 700 * (p.level + 4) + (get_point(3) - 5) * 360L;
                break;
            }
            case 1: { // sat thu
                hpm += 1200 + 250 * (p.level + 4) + (get_point(3) - 5) * 300L;
                break;
            }
            case 2: { // phap su
                hpm += 1400 + 700 * (p.level + 4) + (get_point(3) - 5) * 360L;
                break;
            }
            case 3: { // xa thu
                hpm += 1000 + 315 * (p.level + 4) + (get_point(3) - 5) * 300L;
                break;
            }
        }
        int percent = total_item_param(27);
        if (p.skill_point[9] > 0) {
            for (Option op : p.skills[9].mLvSkill[p.skill_point[9] - 1].minfo) {
                if (op.id == 27) {
                    percent += op.getParam(0);
                    break;
                }
            }
        }
        if (p.type_use_horse == Horse.HEO_RUNG || p.type_use_horse == Horse.CON_LAN || p.type_use_horse == Horse.SKELETON
                || p.type_use_horse == Horse.CHUOT_TUYET || p.type_use_horse == Horse.VOI_MA_MUT
                || p.type_use_horse == Horse.MA_TOC_DO || p.type_use_horse == Horse.RONG_BANG) {
            percent += 1000;
        }

        // Apply % from items/skills/horses
        hpm += ((hpm * (percent / 100)) / 100);

        // NEW: +10% HPMax riêng cho Pháp sư (clazz == 2)
        if (p.clazz == 2) {
            hpm += (hpm * 10) / 100; // +10%
        }

        // Debuff: hiệu ứng id=2 giảm 20% HPMax
        EffTemplate ef = p.get_EffDefault(2);
        if (ef != null) {
            hpm = (hpm * 8) / 10;
        }
        if (hpm > 2_000_000_000) {
            hpm = 2_000_000_000;
        }
        if (get_EffDefault(StrucEff.NOI_TAI_BANG) != null) {
            hpm = hpm / 5 * 4;
            if (hp > hpm) {
                hp = (int) hpm;
            }
        }
        if (get_EffDefault(StrucEff.TE_CONG) != null) {
            hpm = Math.max(1, hpm * 80 / 100);
            if (hp > hpm) {
                hp = (int) hpm;
            }
        }
        return (int) hpm;
    }

    @Override
    public int get_MpMax() {
        long mpm = 0;
        switch (p.clazz) {
            case 0:
            case 1: {
                mpm += 200 + 10 * (p.level + 4) + (get_point(4) - 5) * 10L;
                break;
            }
            case 2: {
                mpm += 160 + 20 * (p.level + 4) + (get_point(4) - 5) * 11L;
                break;
            }
            case 3: {
                mpm += 200 + 11 * (p.level + 4) + (get_point(4) - 5) * 11L;
                break;
            }
        }
        int percent = total_item_param(28);
        if (p.skill_point[10] > 0) {
            for (Option op : p.skills[10].mLvSkill[p.skill_point[10] - 1].minfo) {
                if (op.id == 28) {
                    percent += op.getParam(0);
                    break;
                }
            }
        }
        mpm += ((mpm * (percent / 100)) / 100);
        EffTemplate ef = p.get_EffDefault(2);
        if (ef != null) {
            mpm = (mpm * 8) / 10;
        }
        if (mpm > 2_000_000_000) {
            mpm = 2_000_000_000;
        }
        return (int) mpm;
    }

    @Override
    public int total_skill_param(int id) {
        int param = 0;
        for (int i = 0; i < p.skill_point.length; i++) {
            if (p.skill_point[i] > 0) {
                Option[] temp = p.skills[i].mLvSkill[get_skill_point(i) - 1].minfo;
                for (Option op : temp) {
                    if (op.id == id) {
                        param += op.getParam(0);
                    }
                }
            }
        }
        return param;
    }

    @Override
    public int getPierce() {
        int pie = total_item_param(36);
        pie += get_point(4) * 2;
        EffTemplate ef = get_EffDefault(36);
        if (ef != null) {
            pie += ef.param;
        }
        EffTemplate eff_ve_binh = getEffTinhTu(EffTemplate.GIAP_VE_BINH);
        if (eff_ve_binh != null) {
            pie = pie / 10;
        }
        return pie;
    }

    @Override
    public int get_PhanDame() {
        int param = 2 * get_point(3);
        param += total_item_param(35);
        EffTemplate ef = get_EffDefault(35);
        if (ef != null) {
            param += ef.param;
        }
        EffTemplate eff_thien_su = getEffTinhTu(EffTemplate.GIAP_THIEN_SU);
        if (eff_thien_su != null) {
            param = param / 10;
        }
        return param;
    }

    @Override
    public int get_Miss(boolean giam_ne) {
        int param = 2 * get_point(2);
        param += total_item_param(34);
        EffTemplate ef = get_EffDefault(34);
        if (ef != null) {
            param += ef.param;
        }
        if (giam_ne) {
            param = param / 10 * 9;
        }
        return param;
    }

    @Override
    public int getCrit() {
        int crit = total_item_param(33);
        crit += get_point(1) * 2;
        EffTemplate ef = get_EffDefault(33);
        if (ef != null) {
            crit += ef.param;
        }
        EffTemplate eff_bach_kim = getEffTinhTu(EffTemplate.GIAP_BACH_KIM);
        if (eff_bach_kim != null) {
            crit = crit / 10;
        }
        return crit;
    }

    public int get_skill_point(int i) {
        if (p.skill_point[i] > 0) {
            int par_ = p.skill_point[i] + get_skill_point_plus(i);
            return (par_ > 15 ? 15 : par_);
        }
        return 0;
    }

    @Override
    public int get_PercentDefBase() {
        int def = total_item_param(15);
        def += get_point(2) * 10;
        if (get_skill_point(15) > 0) {
            for (Option op : p.skills[15].mLvSkill[get_skill_point(15) - 1].minfo) {
                if (op.id == 15) {
                    def += op.getParam(0);
                    break;
                }
            }
        }
        EffTemplate ef = p.get_EffDefault(24);
        if (ef != null) {
            def += ef.param;
        }
        if (p.type_use_horse == Horse.NGUA_CHIEN_GIAP || p.type_use_horse == Horse.VOI_MA_MUT) {
            def += 2000;
        } else if (p.type_use_horse == Horse.NGUA_XICH_THO || p.type_use_horse == Horse.TUAN_LOC
                || p.type_use_horse == Horse.CAN_DAU_VAN || p.type_use_horse == Horse.XE_TRUOT_TUYET
                || p.type_use_horse == Horse.CA_CHEP) {
            def += 1000;
        } else if (p.type_use_horse == Horse.MA_TOC_DO || p.type_use_horse == Horse.HOA_KY_LAN
                || p.type_use_horse == Horse.PHUONG_HOANG_LUA) {
            def += 1500;
        } else if (p.type_use_horse == Horse.TRAU_RUNG) {
            def += 3000;
        }
        EffTemplate temp2 = p.get_EffDefault(StrucEff.PowerWing);
        if (temp2 != null) {
            def += 3000;
        }
        if (p.get_EffDefault(StrucEff.NOI_TAI_LUA) != null) {
            def -= 2000;
        }
        return def;
    }

    @Override
    public int get_DefBase() {
        int def = total_item_param(14);
        switch (p.clazz) {
            case 0, 2: {
                def += get_point(2) * 20;
                break;
            }
            case 1, 3: {
                def += get_point(2) * 22;
                break;
            }
        }
        def += ((def * (get_PercentDefBase() / 100)) / 100);
        EffTemplate ef = p.get_EffDefault(0);
        if (ef != null) {
            def = (def * 8) / 10;
        }
        ef = p.get_EffDefault(15);
        if (ef != null) {
            def += (def * (ef.param / 100)) / 100;
        }

        // >>> NEW: +1000% giáp (x11 DEF) cho thành viên bang đang giữ đá trong map 87
        try {
            if (ChiemThanhManager.isOwnerClanBuffActive(p)) {
                long boosted = (long) def * 11L; // +1000% = x11
                if (boosted > 2_000_000_000L) boosted = 2_000_000_000L;
                def = (int) boosted;
            }
        } catch (Exception ignore) {}

        return def;
    }

    @Override
    public int get_PercentDameProp(int type) {
        if (type == 0) {
            int percent = total_item_param(7);
            switch (p.clazz) {
                case 0:
                case 1: {
                    percent += get_point(1) * 20;
                    break;
                }
                case 2:
                case 3: {
                    percent += get_point(1) * 20 + get_point(4) * 18;
                    break;
                }
            }
            if (get_skill_point(11) > 0) {
                for (Option op : p.skills[11].mLvSkill[get_skill_point(11) - 1].minfo) {
                    if (op.id == 7) {
                        percent += op.getParam(0);
                        break;
                    }
                }
            }
            EffTemplate eff = get_EffDefault(StrucEff.BuffSTVL);
            if (eff != null) {
                percent += eff.param;
            }
            EffTemplate temp2 = p.get_EffDefault(StrucEff.PowerWing);
            if (temp2 != null) {
                percent += 3000;
            }
            // BONUS +15% DMG VẬT LÝ cho 3 class: 0, 1, 3
            if (p.clazz == 0 || p.clazz == 1 || p.clazz == 3) {
                percent += 1500; // +15%
            }
            // ngựa...
            if (p.type_use_horse == Horse.NGUA_XICH_THO) {
                percent += 2000;
            } else if (p.type_use_horse == Horse.TUAN_LOC) {
                percent += 4000;
            } else if (p.type_use_horse == Horse.HEO_RUNG || p.type_use_horse == Horse.CON_LAN || p.type_use_horse == Horse.CA_CHEP) {
                percent += 1000;
            } else if (p.type_use_horse == Horse.TRAU_RUNG || p.type_use_horse == Horse.MA_TOC_DO
                    || p.type_use_horse == Horse.PHUONG_HOANG_LUA) {
                percent += 1500;
            } else if (p.type_use_horse == Horse.HOA_KY_LAN) {
                percent += 3500;
            }
            return percent;
        }
        int perct = 0;
        switch (p.clazz) {
            case 0: {
                if (type == 9 || type == 2) {
                    perct += get_point(1) * 20;
                    perct += total_item_param(9);
                    if (get_skill_point(12) > 0) {
                        for (Option op : p.skills[12].mLvSkill[get_skill_point(12) - 1].minfo) {
                            if (op.id == 9) {
                                perct += op.getParam(0);
                                break;
                            }
                        }
                    }
                    // +15% Lửa cho class 0
                    if (type == 2) {
                        perct += 1500;
                    }
                }
                EffTemplate eff = get_EffDefault(StrucEff.BuffSTLua);
                if (eff != null) {
                    perct += eff.param;
                }
                break;
            }
            case 1: {
                if (type == 11 || type == 4) {
                    perct += get_point(1) * 20;
                    perct += total_item_param(11);
                    if (get_skill_point(12) > 0) {
                        for (Option op : p.skills[12].mLvSkill[get_skill_point(12) - 1].minfo) {
                            if (op.id == 11) {
                                perct += op.getParam(0);
                                break;
                            }
                        }
                    }
                    // +15% Độc cho class 1
                    if (type == 4) {
                        perct += 1500;
                    }
                }
                EffTemplate eff = get_EffDefault(StrucEff.BuffSTDoc);
                if (eff != null) {
                    perct += eff.param;
                }
                break;
            }
            case 2: {
                if (type == 8 || type == 1) {
                    perct += get_point(1) * 20 + get_point(4) * 18;
                    perct += total_item_param(8);
                    if (get_skill_point(12) > 0) {
                        for (Option op : p.skills[12].mLvSkill[get_skill_point(12) - 1].minfo) {
                            if (op.id == 8) {
                                perct += op.getParam(0);
                                break;
                            }
                        }
                    }
                }
                EffTemplate eff = get_EffDefault(StrucEff.BuffSTBang);
                if (eff != null) {
                    perct += eff.param;
                }
                break;
            }
            case 3: {
                if (type == 10 || type == 3) {
                    perct += get_point(1) * 20 + get_point(4) * 18;
                    perct += total_item_param(10);
                    if (get_skill_point(12) > 0) {
                        for (Option op : p.skills[12].mLvSkill[get_skill_point(12) - 1].minfo) {
                            if (op.id == 10) {
                                perct += op.getParam(0);
                                break;
                            }
                        }
                    }
                    // +15% Điện cho class 3
                    if (type == 3) {
                        perct += 1500;
                    }
                }
                EffTemplate eff = get_EffDefault(StrucEff.BuffSTDien);
                if (eff != null) {
                    perct += eff.param;
                }
                break;
            }
        }

        // +15% GLOBAL cho Lửa (type == 2) cho tất cả class
        if (type == 2) {
            perct += 1500;
        }

        EffTemplate temp2 = p.get_EffDefault(StrucEff.PowerWing);
        if (temp2 != null) {
            perct += temp2.param;
        }
        // ngựa...
        if (p.type_use_horse == Horse.NGUA_XICH_THO) {
            perct += 2000;
        } else if (p.type_use_horse == Horse.TUAN_LOC) {
            perct += 4000;
        } else if (p.type_use_horse == Horse.HEO_RUNG || p.type_use_horse == Horse.CON_LAN || p.type_use_horse == Horse.CA_CHEP) {
            perct += 1000;
        } else if (p.type_use_horse == Horse.TRAU_RUNG || p.type_use_horse == Horse.MA_TOC_DO
                || p.type_use_horse == Horse.PHUONG_HOANG_LUA) {
            perct += 1500;
        } else if (p.type_use_horse == Horse.HOA_KY_LAN) {
            perct += 3500;
        }
        return perct;
    }

    @Override
    public int get_DameBase() {
        return get_param_view_in4(40);
    }

    @Override
    public int get_DameProp(int type) {
        if (type == 0) {
            long dame = total_item_param(0);
            switch (p.clazz) {
                case 0, 1: {
                    dame += get_point(1) * 6;
                    break;
                }
                case 2, 3: {
                    dame += get_point(4) * 6;
                    if (p.clazz == 3) {
                        dame += get_point(4) * 1; // nhỏ tăng base riêng cho Xạ thủ
                    }
                    break;
                }
            }
            dame += ((dame * (get_PercentDameProp(0) / 100)) / 100);
            if (dame > 2_000_000_000) {
                dame = 2_000_000_000;
            }
            return (int) dame;
        }
        long dprop = 0;
        switch (p.clazz) {
            case 0: {
                if (type == 2) {
                    dprop += get_point(1) * 6;
                    dprop += total_item_param(2);
                }
                break;
            }
            case 1: {
                if (type == 4) {
                    dprop += get_point(1) * 6; // tăng từ *4 lên *6
                    dprop += total_item_param(4);
                }
                break;
            }
            case 2: {
                if (type == 1) {
                    dprop += get_point(4) * 4;
                    dprop += total_item_param(1);
                }
                break;
            }
            case 3: {
                if (type == 3) {
                    dprop += get_point(4) * 6;
                    dprop += total_item_param(3);
                }
                break;
            }
        }
        dprop += ((dprop * (get_PercentDameProp(type) / 100)) / 100);
        if (dprop > 2_000_000_000) {
            dprop = 2_000_000_000;
        }
        return (int) dprop;
    }

    public int get_skill_point_plus(int i) {
        int par = 0;
        if (i >= 1 && i <= 8 || i == 19 || i == 20 || i == 17) {
            par = total_item_param(37);
        }
        if ((i >= 9 && i <= 16) || i == 18) {
            par = total_item_param(38);
        }
        return (par > 5) ? 5 : par;
    }

    @Override
    public int get_PercentDefProp(int type) {
        int param = total_item_param(type) + total_skill_param(type);
        EffTemplate ef = p.get_EffDefault(4);
        if (ef != null) {
            param -= 1000;
        }
        if (param < 0) {
            param = 0;
        }
        if (p.get_EffDefault(StrucEff.NOI_TAI_VAT_LY) != null) {
            param -= 1000;
        }
        return param;
    }

    @Override
    public void SetDie(Map map, MainObject mainAtk) throws IOException {
        if (isdie) {
            return;
        }
        if (map.map_id == 87) {
            ChiemThanhManager.PlayerDie(p);
        }
        if (map.map_id == 102 && map.kingCupMap != null && map.kingCupMap.timeWait < System.currentTimeMillis()) {
            Player p0 = (Player) mainAtk;
            map.kingCupMap.end_round();
            p0.countWin++;
            map.kingCupMap.send_notify(String.format("%s đã chiến thắng %s trong hiệp thi đấu thứ %s", p0.name, p.name, map.kingCupMap.countMatch));
        }
        p.time_die = System.currentTimeMillis();
        p.dame_affect_special_sk = 0;
        p.hp = 0;
        p.isdie = true;
        if (p.point_activity > 1000) {
            p.point_activity -= 10;
        }
        if (!Horse.isHorseClan(p.type_use_horse)) {
            p.type_use_horse = -1;
            map.send_horse(p);
        }
        if (p.isLiveSquire) {
            Squire.squireLeaveMap(p);
            p.isLiveSquire = false;
        }
        Player pATK = mainAtk.isPlayer() ? (Player) mainAtk : null;
        if (pATK != null) {
            if (pATK.typepk == 0) {
                pATK.hieuchien += 200;
            }
            if (map.isMapLangPhuSuong()) {
                pATK.langPhuSuong();
                p.langPhuSuong();
            }
            if (pATK.list_enemies.contains(this.name)) {
                pATK.list_enemies.remove(this.name);
            } else if (p.typepk == -1) {
                if (!p.list_enemies.contains(pATK.name)) {
                    p.list_enemies.add(pATK.name);
                    if (p.list_enemies.size() > 20) {
                        p.list_enemies.remove(0);
                    }
                }
            }
        }
    }

    public boolean mienST(byte type_dame) {
        return this.total_item_param(-123 + type_dame) > Util.random(10000);
    }

    public boolean isEffTinhTu(int id) {
        return this.total_item_param(id) > Util.random(10000);
    }

    public int get_id_eff_skill(int type) {
        int id = 0;
        switch (this.clazz) {
            case 0:
                if (type == 19) {
                    id = 56;
                } else if (type == 20) {
                    id = 53;
                }
                break;
            case 1:
                if (type == 19) {
                    id = 59;
                } else if (type == 20) {
                    id = 76;
                }
                break;
            case 2:
                if (type == 19) {
                    id = 49;
                } else if (type == 20) {
                    id = 56;
                }
                break;
            case 3:
                if (type == 19) {
                    id = 77;
                } else if (type == 20) {
                    id = 52;
                }
                break;
        }
        return id;
    }

    public int getLightDame(Player target) {
        int dame = this.total_item_param(6);
        dame = dame + (dame / 100 * this.total_item_param(13));
        dame = dame - (dame / 100 * target.total_item_param(22));
        return Math.min(0, dame); // (giữ nguyên theo base của bạn)
    }

    public int getDarkDame(Player target) {
        int dame = this.total_item_param(5);
        dame = dame + (dame / 100 * this.total_item_param(12));
        dame = dame - (dame / 100 * target.total_item_param(21));
        return Math.min(0, dame); // (giữ nguyên theo base của bạn)
    }

    @Override
    public int get_TypeObj() {
        return 0;
    }

    @Override
    public void update(Map map) {
        try {
            if (isdie) {
                return;
            }

            long now = System.currentTimeMillis();

            // <auto +hp, mp>
            EffTemplate vet_thuong_sau = p.get_EffDefault(StrucEff.VET_THUONG_SAU);
            int par = 0;

            // AUTO +HP
            if (get_pet_follow() != null) {
                par = get_pet_follow().getParam((byte) 47);
            }
            int percent_hp = getPercentBuffHP() + par;
            if (percent_hp > 0
                    && p.time_buff_hp < now
                    && vet_thuong_sau == null
                    && p.canRestoreHp()) {
                p.time_buff_hp = now + 5000L;
                if (p.hp < p.body.get_HpMax()) {
                    long param = (((long) p.body.get_HpMax()) * (percent_hp / 100)) / 100;
                    Service.usepotion(p, 0, param);
                }
            }

            EffTemplate eff_tan_phe = p.getEffTinhTu(EffTemplate.TAN_PHE);
            par = 0;
            if (get_pet_follow() != null) {
                par = get_pet_follow().getParam((byte) 44);
            }
            int percent_mp = p.body.total_skill_param(30) + p.body.total_item_param(30) + par;
            if (percent_mp > 0
                    && p.time_buff_mp < now
                    && eff_tan_phe == null) {
                p.time_buff_mp = now + 5000L;
                if (p.mp < p.body.get_MpMax()) {
                    long param = (((long) p.body.get_MpMax()) * (percent_mp / 100)) / 100;
                    Service.usepotion(p, 1, param);
                }
            }
            // </auto +hp, mp>

            // <eff Player>
            long _time = now;
            if (get_EffMe_Kham(StrucEff.BongLua) != null && !p.isdie) {
                Service.usepotion(p, 0, (int) -(p.hp * Util.random(5, 10) * 0.01));
                if (p.hp <= 0) {
                    p.hp = 1;
                }
            }
            if (get_EffMe_Kham(StrucEff.BongLanh) != null) {
                Service.usepotion(p, 1, (int) -(p.mp * Util.random(5, 10) * 0.01));
            }
            if (getEffTinhTu(EffTemplate.THIEU_CHAY) != null) {
                Service.usepotion(p, 0, -1500);
                if (p.hp <= 0) {
                    p.hp = 1;
                }
            }
            EffTemplate eff_doc = get_EffDefault(StrucEff.NOI_TAI_DOC);
            if (eff_doc != null) {
                Service.usepotion(p, 0, -eff_doc.param);
            }
            if (p.hp <= 0 && !p.isdie) {
                p.hp = 0;
                p.isdie = true;
            }
            // </eff Player>

            // Khu 2 tự đẩy ra
            if (map.zone_id == 1 && !Map.is_map_not_zone2(map.map_id) && !p.isSquire) {
                EffTemplate eff = get_EffDefault(-127);
                if (eff == null) {
                    Map m = Map.get_map_by_id(map.map_id)[0];
                    MapService.leave(map, p);
                    p.map = m;
                    MapService.enter(p.map, p);
                }
            }
            // Làng Phù Sương tự đẩy ra
            if (map.isMapLangPhuSuong() && !p.isSquire) {
                EffTemplate eff = get_EffDefault(-128);
                if (eff == null) {
                    p.langPhuSuong();
                }
            }

            Maze.updatePlayer(p, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
