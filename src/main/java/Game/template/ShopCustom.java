package Game.template;

import Game.client.Player;
import Game.core.Admin;
import Game.core.Service;

import java.io.IOException;
import java.util.HashMap;

public class ShopCustom {

    public static final HashMap<Integer, Long> items_gems = new HashMap<>();

//    public static void setShopGems() {
////        items_gems.put(4587, 20000000L);
////        items_gems.put(4588, 2000000L);
////        items_gems.put(4589, 2000000L);            
////        items_gems.put(4590, 2000000L);
//        items_gems.put(4632, 20000L);
//        items_gems.put(4633, 20000L);
//        items_gems.put(4634, 20000L);
//        items_gems.put(4635, 20000L);
//        items_gems.put(4636, 20000L);
//        items_gems.put(4716, 20000L);
//        items_gems.put(4717, 20000L);
//        items_gems.put(4718, 20000L);
//        items_gems.put(4719, 20000L);
//        items_gems.put(4799, 20000L);
//        items_gems.put(4690, 20900L);
//        for (int i = 4720; i < 4728; i++) {
//            items_gems.put(i, 20000L);
//        }
//        for (int i = 4766; i < 4768; i++) {
//            items_gems.put(i, 20000L);
//        }
//        for (int i = 4775; i < 4784; i++) {
//            items_gems.put(i, 20000L);
//        }
//        items_gems.put(4802, 20000L);
//        items_gems.put(4759, 20000L);
//        items_gems.put(4760, 20000L);
//
//        items_gems.put(4617, 20000L);
////        items_gems.put(4626, 15000000L);
////        items_gems.put(3269, 15000000L);
//        items_gems.put(4631, 20000L);
//        
//        items_gems.put(4587, 20000L);
//        items_gems.put(4588, 20000L);
//        items_gems.put(4589, 20000L);            
//        items_gems.put(4590, 20000L);
//        items_gems.put(4591, 20000L);
//        items_gems.put(4592, 20000L);
//        items_gems.put(4593, 20000L);
//        items_gems.put(4594, 20000L);
//        items_gems.put(4632, 20000L);
//        items_gems.put(4633, 20000L);
//        items_gems.put(4634, 20000L);
//        items_gems.put(4635, 20000L);
//        items_gems.put(4636, 20000L);
//        items_gems.put(4716, 20000L);
//        items_gems.put(4717, 20000L);
//        items_gems.put(4718, 20000L);
//        items_gems.put(4719, 20000L);
//        items_gems.put(4799, 20000L);
//        items_gems.put(4690, 20000L);
//    
//        items_gems.put(4759, 20000L);
//        items_gems.put(4802, 26000000L);
//        items_gems.put(4760, 26000000L);
//
//        items_gems.put(4585, 20000L);
//        items_gems.put(4586, 20000L);
//        items_gems.put(4616, 20000L);
//        items_gems.put(4617, 20000L);
//        items_gems.put(4618, 20000L);
//        items_gems.put(4619, 20000L);
//        
//        items_gems.put(4620, 20000L);
//        items_gems.put(4627, 20000L);
//        items_gems.put(4628, 20000L);
//        items_gems.put(4629, 20000L);
//        items_gems.put(4630, 20000L);
//        items_gems.put(4637, 20000L);
//        items_gems.put(4654, 20000L);
//        
//        items_gems.put(4655, 35000L);
//        items_gems.put(4676, 36000L);
//        items_gems.put(4691, 36000L);
//        items_gems.put(4692, 35000L);
//        items_gems.put(4693, 35000L);
//        items_gems.put(4694, 35000L);
//        items_gems.put(4696, 35000L);
//        items_gems.put(4697, 35000L);
//        items_gems.put(4698, 35000L);
//        items_gems.put(4699, 35000L);
//        items_gems.put(4700, 36000L);
//        items_gems.put(4703, 35000L);
//        items_gems.put(4704, 15000L);
//        items_gems.put(4705, 15000L);
//        
//        items_gems.put(4706, 26000L);
//        items_gems.put(4707, 15090L);
//        items_gems.put(4709, 25000L);
//        items_gems.put(4710, 26000L);
//        items_gems.put(4711, 26000L);
//        items_gems.put(4712, 20000L);
//        items_gems.put(4713, 26000L);
//        items_gems.put(4715, 10000L);
//        items_gems.put(4721, 26000L);
//        items_gems.put(4722, 15000L);
//        items_gems.put(4725, 15000L);
//        
//        items_gems.put(4724, 26000L);
//        items_gems.put(4725, 15000L);
//        items_gems.put(4726, 10000L);
//        items_gems.put(4727, 25000L);
//        items_gems.put(4730, 26000L);
//        items_gems.put(4732, 15000L);
//        items_gems.put(4733, 15000L);
//        items_gems.put(4734, 25000L);
//        items_gems.put(4735, 26000L);
//        items_gems.put(4736, 26000L);
//        items_gems.put(4737, 26000L);
//        items_gems.put(4738, 26000L);
//        items_gems.put(4739, 15000L);
//        items_gems.put(4740, 15000L);
//       
//        items_gems.put(4741, 26000L);
//        items_gems.put(4742, 15000L);
//        items_gems.put(4743, 15000L);
//        items_gems.put(4744, 25000L);
//        items_gems.put(4745, 26000L);
//        items_gems.put(4746, 26000L);
//        items_gems.put(4747, 26000L);
//        items_gems.put(4748, 26000L);
//        items_gems.put(4749, 15000L);
//        items_gems.put(4750, 15000L);
//        items_gems.put(4751, 25000L);
//        items_gems.put(4752, 26000L);
//        items_gems.put(4753, 26000L);
//        items_gems.put(4754, 26000L);
//        items_gems.put(4755, 30000L);
//        items_gems.put(4756, 30000L);
//        items_gems.put(4757, 30000L);
//          
//        items_gems.put(4758, 20000L);
//        items_gems.put(4761, 20000L);
//        items_gems.put(4762, 20000L);
//        items_gems.put(4763, 30000L);
//        items_gems.put(4764, 30000L);
//        items_gems.put(4765, 30000L);
//        items_gems.put(4767, 30000L);
//        items_gems.put(4769, 30000L);
//        items_gems.put(4770, 30000L);
//        items_gems.put(4771, 30000L);
//        items_gems.put(4772, 30000L);
//        items_gems.put(4773, 30000L);
//        items_gems.put(4774, 40000L);
//        
//        items_gems.put(4776, 40000L);
//        items_gems.put(4777, 40000L);
//        items_gems.put(4778, 40000L);
//        items_gems.put(4779, 40000L);
//        items_gems.put(4780, 40000L);
//        items_gems.put(4781, 40000L);
//        items_gems.put(4782, 40000L);
//        items_gems.put(4783, 40000L);
//        items_gems.put(4784, 40000L);
//        items_gems.put(4786, 40000L);
//        items_gems.put(4787, 40000L);
//        items_gems.put(4788, 40000L);
//        items_gems.put(4789, 40000L);
//        items_gems.put(4790, 40000L);
//        items_gems.put(4791, 50000L);
//        
//        items_gems.put(4792, 50000L);
//        items_gems.put(4793, 50000L);
//        items_gems.put(4794, 50000L);
//        items_gems.put(4795, 50000L);
//        items_gems.put(4796, 50000L);
//        items_gems.put(4797, 50000L);
//        items_gems.put(4798, 50000L);
//        items_gems.put(4800, 50000L);
//        items_gems.put(4801, 50000L);
//        items_gems.put(4803, 50000L);
//       
//        items_gems.put(4810, 50000L);
//        items_gems.put(4811, 50000L);
//        items_gems.put(4812, 50000L);
//        items_gems.put(4813, 50000L);
//    }
    
    
    public static void setShopGems() {
    // reset lại map
    items_gems.clear();

    // danh sách id item
    int[] ids = {
        4632,4633,4634,4635,4636,4716,4717,4718,4719,4799,4690,
        4720,4721,4722,4723,4724,4725,4726,4727,
        4766,4767,
        4775,4776,4777,4778,4779,4780,4781,4782,4783,
        4802,4759,4760,4617,4631,4587,4588,4589,4590,
        4591,4592,4593,4594,
        4585,4586,4616,4617,4618,4619,4620,4627,4628,4629,4630,
        4637,4654,4655,4676,4691,4692,4693,4694,4696,4697,4698,4699,
        4700,4703,4704,4705,4706,4707,4709,4710,4711,4712,4713,4715,
        4721,4722,4725,4724,4725,4726,4727,4730,4732,4733,4734,4735,
        4736,4737,4738,4739,4740,4741,4742,4743,4744,4745,4746,4747,
        4748,4749,4750,4751,4752,4753,4754,4755,4756,4757,4758,
        4761,4762,4763,4764,4765,4767,4769,4770,4771,4772,4773,4774,
        4776,4777,4778,4779,4780,4781,4782,4783,4784,4786,4787,4788,
        4789,4790,4791,4792,4793,4794,4795,4796,4797,4798,4800,4801,
        4803,4810,4811,4812,4813
    };

    long price = 1L;
    for (int id : ids) {
        items_gems.put(id, (long)1000000);
    }
}

//    public static void buy(Player p, int idbuy) throws IOException {
//        if (p.get_ngoc() < items_gems.get(idbuy)) {
//            Service.send_notice_box(p.conn, "Không đủ ngọc");
//            return;
//        }
//        p.update_ngoc(-items_gems.get(idbuy));
//        if (ItemTemplate3.item.get(idbuy).getType() == 16) {
//            Admin.randomMedal(p, (byte) 4, (byte) 0, true);
//        } else {
//            Item3 itbag = new Item3();
//            itbag.id = (short) idbuy;
//            itbag.clazz = ItemTemplate3.item.get(idbuy).getClazz();
//            itbag.type = ItemTemplate3.item.get(idbuy).getType();
//            itbag.level = ItemTemplate3.item.get(idbuy).getLevel();
//            itbag.icon = ItemTemplate3.item.get(idbuy).getIcon();
//            itbag.color = 5;
//            itbag.part = ItemTemplate3.item.get(idbuy).getPart();
//            itbag.islock = true;
//            itbag.name = ItemTemplate3.item.get(idbuy).getName();
//            itbag.tier = 0;
//            itbag.op = ItemTemplate3.item.get(idbuy).getOp();
//            itbag.time_use = 0;
//            p.item.add_item_inventory3(itbag);
//        }
//        Service.send_notice_box(p.conn, "Đã mua thành công");
//    }
    public static void buy(Player p, int idbuy) throws IOException {
        if (ItemTemplate3.item.get(idbuy).getType() == 16) {
            Admin.randomMedal(p, (byte) 4, (byte) 0, true);
        } else {
            Item3 itbag = new Item3();
            itbag.id = (short) idbuy;
            itbag.clazz = ItemTemplate3.item.get(idbuy).getClazz();
            itbag.type = ItemTemplate3.item.get(idbuy).getType();
            itbag.level = ItemTemplate3.item.get(idbuy).getLevel();
            itbag.icon = ItemTemplate3.item.get(idbuy).getIcon();
            itbag.color = 5;
            itbag.part = ItemTemplate3.item.get(idbuy).getPart();
            itbag.islock = true;
            itbag.name = ItemTemplate3.item.get(idbuy).getName();
            itbag.tier = 0;
            itbag.op = ItemTemplate3.item.get(idbuy).getOp();
            itbag.time_use = 0;
            p.item.add_item_inventory3(itbag);
        }
        Service.send_notice_box(p.conn, "Đã mua thành công");
    }
}
