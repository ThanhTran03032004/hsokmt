
package Game.Helps;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;


public class _Time {

    public static long timeDay = GetTime();

    public static long GetTime() {
        Date currentDate = new Date();
        long currentLong = currentDate.getTime();

        return currentLong;
    }

    public static long GetTimeNextDay() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 1);
        Date nextDate = calendar.getTime();
        return nextDate.getTime();
    }
    
    public static String ConvertTime(Long time){
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM");
        return currentDateTime.format(formatter);
    }
}
