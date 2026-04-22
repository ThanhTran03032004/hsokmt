
package Game.Helps;

import java.util.ArrayList;
import java.util.List;

import Game.template.Item3;
import Game.template.Option;

public class Kham_Item {

    public static boolean CheckNgocTinhLuyen(short id) {
        // hỗn nguyên tinh luyện
        return (id >= 382 && id <= 386) || (id >= 387 && id <= 391) || (id >= 392 && id <= 396) || (id >= 397 && id <= 401) || (id >= 402 && id <= 406) || (id >= 407 && id <= 411);
    }

    public static boolean KhamNgoc(short idNgoc, Item3 tem) {
        Option ops = GetOps(idNgoc, tem.id);
        if(ops == null)
            return false;
        for(Option o2 : tem.op)
        {
            if(ops.id == o2.id)
            {
                if(ops.id == 100 || ops.id == 101)
                    return false;
                o2.setParam(o2.getParam(0) + ops.getParam(0));
                return true;
            }
        }
        tem.op.add(ops);
        if((idNgoc >= 367 && idNgoc <= 371)||(idNgoc >= 397 && idNgoc <= 401)) //phong ma
        {
            tem.op.addAll(GetOps_PhongMa(idNgoc,tem.id));
        }
        return true;
    }

    private static Option GetOps(short id, short idItem) {
        Option re = null;
        if (id >= 352 && id <= 356)// hỗn nguyên
        {
            int pr = (357 - id) * 5;
            re = new Option(100, pr, idItem);
            //re.add(new Option(100, pr, idItem));
        } 
        else if (id >= 382 && id <= 386)// hỗn nguyên tinh luyện
        {
            int pr = (387 - id) * 5;
            re = new Option(100, pr, idItem);
            //re.add(new Option(100, pr, idItem));
        }
        //
        else if(id >= 357 && id <= 361) // khải hoàn
        {
            int pr = (362 - id) * 5;
            re = new Option(101, pr, idItem);
            //re.add(new Option(101, pr, idItem));
        }
        else if(id >= 387 && id <= 391) // khải hoàn tinh luyện
        {
            int pr = (392 - id) * 5;
            re = new Option(101, pr, idItem);
//            re.add(new Option(101, pr, idItem));
        }
        //
        else if(id >= 362 && id <= 366) // lục bảo
        {
            int idx = (367 - id);
            int pr = idx == 1 ? 350 : (idx == 2 ? 250 : (idx == 3 ? 150 : (idx == 4 ? 100 : 50)));
            re = new Option(102, pr, idItem);
//            re.add(new Option(102, pr, idItem));
        }
        else if(id >= 392 && id <= 396) // lục bảo tinh luyện
        {
            int idx = (397 - id);
            int pr = idx == 1 ? 350 : (idx == 2 ? 250 : (idx == 3 ? 150 : (idx == 4 ? 100 : 50)));
            re = new Option(102, pr, idItem);
//            re.add(new Option(102, pr, idItem));
        }
        //
        else if(id >= 367 && id <= 371) // phong ma
        {
            int idx = (372 - id);
            int pr1 = idx == 1 ? 350 : (idx == 2 ? 250 : (idx == 3 ? 150 : (idx == 4 ? 100 : 50)));
            int pr2 = idx == 1 ? 70 : (idx == 2 ? 50 : (idx == 3 ? 40 : (idx == 4 ? 30 : 20)));
            int pr3 = idx == 1 ? 10 : (idx == 2 ? 7 : (idx == 3 ? 5 : (idx == 4 ? 2 : 1)));
            re = new Option(103, pr1, idItem);
//            re.add(new Option(103, pr1, idItem));
//            re.add(new Option(104, pr2, idItem));
//            re.add(new Option(105, pr3, idItem));
        }
        else if(id >= 397 && id <= 401) // phong ma tinh luyện
        {
            int idx = (402 - id);
            int pr1 = idx == 1 ? 450 : (idx == 2 ? 350 : (idx == 3 ? 250 : (idx == 4 ? 200 : 150)));
            int pr2 = idx == 1 ? 70 : (idx == 2 ? 50 : (idx == 3 ? 40 : (idx == 4 ? 30 : 20)));
            int pr3 = idx == 1 ? 10 : (idx == 2 ? 7 : (idx == 3 ? 5 : (idx == 4 ? 2 : 1)));
            re = new Option(103, pr1, idItem);
//            re.add(new Option(103, pr1, idItem));
//            re.add(new Option(104, pr2, idItem));
//            re.add(new Option(105, pr3, idItem));
        }
        //
        else if(id >= 372 && id <= 376) // sinh mệnh
        {
            int idx = (377 - id);
            int pr = idx == 1 ? 450 : (idx == 2 ? 350 : (idx == 3 ? 300 : (idx == 4 ? 250 : 200)));
            re = new Option(106, pr, idItem);
//            re.add(new Option(106, pr, idItem));
        }
        else if(id >= 402 && id <= 406) // sinh mệnh tinh luyện
        {
            int idx = (407 - id);
            int pr = idx == 1 ? 450 : (idx == 2 ? 350 : (idx == 3 ? 300 : (idx == 4 ? 250 : 200)));
            re = new Option(106, pr, idItem);
//            re.add(new Option(106, pr, idItem));
        }
        
        //
        else if(id >= 377 && id <= 381) // Tâm linh
        {
            int idx = (382 - id);
            int pr = idx == 1 ? 1400 : (idx == 2 ? 900 : (idx == 3 ? 700 : (idx == 4 ? 500 : 300)));
            re = new Option(107, pr, idItem);
//            re.add(new Option(107, pr, idItem));
        }
        else if(id >= 407 && id <= 411) // Tâm linh tinh luyện
        {
            int idx = (412 - id);
            int pr = idx == 1 ? 1400 : (idx == 2 ? 900 : (idx == 3 ? 700 : (idx == 4 ? 500 : 300)));
            re = new Option(107, pr, idItem);
//            re.add(new Option(107, pr, idItem));
        }
        return re;
    }
    public static List<Option> GetOps_PhongMa(short id, short idItem)
    {
        List<Option> re = new ArrayList<>();
        if((id >= 367 && id <= 371)||(id >= 397 && id <= 401)) //phong ma
        {
            if(id >= 367 && id <= 371) // phong ma
            {
                int idx = (372 - id);
                int pr1 = idx == 1 ? 350 : (idx == 2 ? 250 : (idx == 3 ? 150 : (idx == 4 ? 100 : 50)));
                int pr2 = idx == 1 ? 700 : (idx == 2 ? 500 : (idx == 3 ? 400 : (idx == 4 ? 300 : 200)));
                int pr3 = idx == 1 ? 100 : (idx == 2 ? 70 : (idx == 3 ? 50 : (idx == 4 ? 20 : 10)));
    //            re.add(new Option(103, pr1, idItem));
                re.add(new Option(104, pr2, idItem));
                re.add(new Option(105, pr3, idItem));
            }
            else if(id >= 397 && id <= 401) // phong ma tinh luyện
            {
                int idx = (402 - id);
                int pr1 = idx == 1 ? 450 : (idx == 2 ? 350 : (idx == 3 ? 250 : (idx == 4 ? 200 : 150)));
                int pr2 = idx == 1 ? 7000 : (idx == 2 ? 5000 : (idx == 3 ? 4000 : (idx == 4 ? 3000 : 2000)));
                int pr3 = idx == 1 ? 1000 : (idx == 2 ? 700 : (idx == 3 ? 500 : (idx == 4 ? 200 : 100)));
    //            re.add(new Option(103, pr1, idItem));
                re.add(new Option(104, pr2, idItem));
                re.add(new Option(105, pr3, idItem));
            }
        }
        return re;
    }
}
