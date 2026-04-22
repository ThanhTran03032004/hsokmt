
package Game.core;

import Game.io.Session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class CheckDDOS {
    static private final Map<String, Byte> ipErrorCount = new HashMap<>();
    static private final Map<String, Long> ipLastAccessTime = new HashMap<>();
    private static volatile Set<String> ips = new HashSet<>();
    private static final Lock lock = new ReentrantLock();

    public static void blockIP(String ipAddress, String logger) {
        lock.lock();
        try {
            if (!ips.contains(ipAddress)) {
                ips.add(ipAddress);
                DisconnectIP(ipAddress);
                System.out.println("============block ip: " + ipAddress + "==========" + logger);
                Log.gI().add_Log_Server("block_ip","["+ ipAddress + "] => " + logger);
            }
        } finally {
            lock.unlock();
        }
    }

    public static void DisconnectIP(String ipAddress) {
        try {
            for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                Session s = Session.client_entry.get(i);
                if (s != null && s.ip.equals(ipAddress)) {
                    s.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean removeIP(String playerName) {
        lock.lock();
        try {
            return ips.remove(playerName);
        } finally {
            lock.unlock();
        }
    }

    public static boolean isIPExist(String playerName) {
        lock.lock();
        try {
            return ips.contains(playerName);
        } finally {
            lock.unlock();
        }

    }

    public static synchronized boolean canAccess(String ipAddress) {
        int errorCount = ipErrorCount.getOrDefault(ipAddress, (byte) 0);
        long lastAccessTime = ipLastAccessTime.getOrDefault(ipAddress, 0L);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAccessTime > 2000) {
            ipErrorCount.put(ipAddress, (byte) 0);
            ipLastAccessTime.put(ipAddress, currentTime);
            return true;
        } else if (errorCount >= 20) {
            //System.out.println("core.CheckDDOS.canAccess()-----"+errorCount+"---------"+(currentTime - lastAccessTime));
            blockIP(ipAddress, "" + (currentTime - lastAccessTime));

            ipErrorCount.remove(ipAddress);
            ipLastAccessTime.remove(ipAddress);
            return false;
        }
        return true;
    }

    public static synchronized void NextError(String ipAddress) {
        int errorCount = ipErrorCount.getOrDefault(ipAddress, (byte) 0);
        long currentTime = System.currentTimeMillis();
        ipErrorCount.put(ipAddress, (byte) (errorCount + 1));
        ipLastAccessTime.put(ipAddress, currentTime);
        //System.out.println("core.CheckDDOS.NextError()========");
    }

    static private Map<String, Byte> countIp = new HashMap<>();
    private static final Lock lock1 = new ReentrantLock();

    public static boolean checkCountIP(String ip) {
        lock1.lock();
        try {
            return countIp.getOrDefault(ip, (byte) 0) < 7;
        } finally {
            lock1.unlock();
        }
    }

    public static void removeIp(String ip) {
        if (ip == null) {
            return;
        }
        lock1.lock();
        try {
            if (countIp.containsKey(ip)) {
                byte count = countIp.getOrDefault(ip, (byte) 0);
                count -= 1;
                if (count <= 0) {
                    countIp.remove(ip);
                } else {
                    countIp.replace(ip, count);
                }
            }
        } finally {
            lock1.unlock();
        }
    }

    public static void addIp(String ip) {
        if (ip == null) {
            return;
        }
        lock1.lock();
        try {
            if (countIp.containsKey(ip)) {
                byte count = countIp.getOrDefault(ip, (byte) 0);
                count += 1;
                countIp.replace(ip, count);
            } else {
                countIp.put(ip, (byte) 1);
            }
        } finally {
            lock1.unlock();
        }
    }

    public static void ClearRam() {
        try {
            long time = System.currentTimeMillis();
            Map<String, Long> map = ipLastAccessTime;
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                try {
                    String key = entry.getKey();
                    Long value = entry.getValue();
                    if (value != null && time - value > 10000) {
                        ipLastAccessTime.remove(key);
                        ipErrorCount.remove(key);
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception ee) {
        }
    }
}
