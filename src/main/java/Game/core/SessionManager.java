
package Game.core;

import Game.io.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SessionManager {
    public static final ConcurrentHashMap<String, Integer> BandWidthSizes = new ConcurrentHashMap<>();
    public static synchronized void AddBandWidth(String ipAddress, int size) {
        int errorCount = BandWidthSizes.getOrDefault(ipAddress, 0);
        if(errorCount>2_000_000_000)
        {
            CheckDDOS.blockIP(ipAddress,"Max Size BandWidth");
            return;
        }
        long currentTime = System.currentTimeMillis();
        BandWidthSizes.put(ipAddress, size);
    }
    public static void CheckBandWidth()
    {
        try {
            String ip="";
            int size=0;
            Map<String, Integer> map = BandWidthSizes;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if(key!=null && value!=null && value>size)
                {
                    ip = key;
                    size = value;
                }
            }
            map.clear();
            BandWidthSizes.clear();
            if(size>0)
            {
                double n = size; // ví dụ: giá trị đếm băng thông là 1,5 MB
                String result;

                if (n < 1024) {
                   result = n + " b"; // nếu giá trị đếm băng thông nhỏ hơn 1024 thì sử dụng đơn vị bytes
                } else if (n < 1048576) {
                   double kb = n / 1024;
                   result = String.format("%.2f", kb) + " KB"; // nếu giá trị đếm băng thông nhỏ hơn 1048576 (1024^2) thì chuyển sang đơn vị KB
                } else if (n < 1073741824) {
                   double mb = n / 1048576;
                   result = String.format("%.2f", mb) + " MB"; // nếu giá trị đếm băng thông nhỏ hơn 1073741824 (1024^3) thì chuyển sang đơn vị MB
                } else {
                   double gb = n / 1073741824;
                   result = String.format("%.2f", gb) + " GB"; // nếu giá trị đếm băng thông lớn hơn hoặc bằng 1073741824 thì chuyển sang đơn vị GB
                }
                Log.gI().add_Log_Server("BandWidth", "IP : ["+ip+"] "+result);
            }
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void RemoveClient(){
        long time = System.currentTimeMillis();
        for(int i = Session.client_entry.size() -1; i>=0; i--){
            try{
                Session s = Session.client_entry.get(i);
                if(s==null)continue;
                if(time - s.timeConnect > Manager.timeRemoveClient && !s.get_in4)
                    s.close();
            }catch(Exception e){}
        }
    }

    public static int countLoginSlotsByIp(String ipAddress, Session current) {
        if (ipAddress == null) {
            return 0;
        }
        int count = 0;
        synchronized (Session.client_entry) {
            for (Session session : Session.client_entry) {
                if (session == null || session == current || !ipAddress.equals(session.ip)) {
                    continue;
                }
                if (session.connected && session.get_in4 && session.user != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public static int countOnlinePlayers() {
        int count = 0;
        synchronized (Session.client_entry) {
            for (Session session : Session.client_entry) {
                if (session != null && session.connected && session.p != null && session.isLogin) {
                    count++;
                }
            }
        }
        return count;
    }

    public static Map<String, Integer> countConnectionsByIp() {
        Map<String, Integer> result = new HashMap<>();
        synchronized (Session.client_entry) {
            for (Session session : Session.client_entry) {
                if (session == null || session.ip == null) {
                    continue;
                }
                result.put(session.ip, result.getOrDefault(session.ip, 0) + 1);
            }
        }
        return result;
    }
    
}
