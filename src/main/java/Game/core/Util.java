package Game.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.List;

public class Util {

    private static final Random random = new Random();
    public static final SimpleDateFormat fmt_save_log = new SimpleDateFormat("dd_MM_yyyy");
    private static final SimpleDateFormat fmt_get_time_now = new SimpleDateFormat("hh:mm:ss a");
    private static final SimpleDateFormat fmt_is_same_day = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    private static final NumberFormat en = NumberFormat.getInstance(new Locale("vi"));
     private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public synchronized static byte[] loadfile(String url) throws IOException {
        try ( FileInputStream fis = new FileInputStream(url)) {
            byte[] ab = new byte[fis.available()];
            fis.read(ab, 0, ab.length);
            return ab;
        }
    }

    public static int log_2(int a) {
        int i;
        for (i = 0; i < a; i++) {
            if (Math.pow(2, i) >= a) {
                break;
            }
        }
        return i;
    }

    public static void logconsole(String s, int type, byte cmd) {
        if (Manager.gI().debug && cmd != 4 && cmd != 5 && cmd != 7 && cmd != -52) {
            switch (type) {
                case 0: {
                    System.out.println(s);
                    break;
                }
                case 1: {
                    System.err.println(s);
                    break;
                }
            }
        }
    }
    public static boolean random_ratio(double ratio){
        if(ratio >= 100)return true;
        return ratio > random.nextDouble( 100);
    }
    public static Integer random(List<Integer> array, List<Integer> arraynext){
        if(array == null || arraynext == null)
            return null;
        array.removeAll(arraynext);
        Random rand = new Random();
        if(array == null || array.size()<=0)
            return null;
        int randomIndex = rand.nextInt(array.size());
        return array.get(randomIndex);
    }
    public static int random(int[] array) {
        Random rand = new Random();
        int randomIndex = rand.nextInt(array.length);
        return array[randomIndex];
    }
    public static int nextInt(int x1, int x2) {
        int to = x2;
        int from = x1;
        if (x2 < x1) {
            to = x1;
            from = x2;
        }
        return  (from + Util.random.nextInt( (to + 1 - from)));
    }

 public static String toDateString(Date date) {
        try {
            String a = Util.dateFormat.format(date);
            return a;
        } catch (Exception e) {
            return "2021-01-01 01:01:00";
        }
    }

    public static int nextInt(int max) {
        return Util.random.nextInt( max);
    }
    public static int randomNext(int a1, int a2, int anext) {
        int ae = anext;
        while (ae == anext) {            
            ae = random.nextInt(a1, a2);
        }
        return ae;
    }
    
    public static int random(int a1, int a2) {
        if(a1>=a2)
            return a1;
        return random.nextInt(a1, a2);
    }

    public static int random(int a2) {
        return random.nextInt(0, a2);
    }

    public static String get_now_by_time() {
        return Util.fmt_get_time_now.format(Date.from(Instant.now()));
    }

    public synchronized static Date getDate(String day) {
        try {
            Date parsedDate = sdf.parse(day);
            return parsedDate;
        } catch (ParseException e) {
            return Date.from(Instant.now());
        }
    }

    public static boolean is_same_day(Date date1, Date date2) {
        return Util.fmt_is_same_day.format(date1).equals(Util.fmt_is_same_day.format(date2));
    }

    public static String getTime(int n) {
        int h, p, s;
        h = (n / 3600);
        p = ((n - (h * 3600)) / 60);
        s = n - (h * 3600 + p * 60);
        return String.format("%dh:%dp:%ds", h, p, s);
    }

    public static boolean isnumber(String txt) {
        try {
            Integer.valueOf(txt);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String number_format(long num) {
        return en.format(num);
    }
    public static String format_time(long time) {
        Date currentDate = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(currentDate);
    }
}
