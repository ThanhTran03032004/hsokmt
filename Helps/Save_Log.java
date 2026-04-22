
package Game.Helps;

import java.io.File;
import java.io.FileOutputStream;


public class Save_Log {
    public static synchronized void process(String filename, String txt){
        try{
            File f = new File("LogCheck/"+filename);
            f.getParentFile().mkdirs();
            if (!f.exists()) {
                if (!f.createNewFile()) {
                    System.out.println("Tạo file " + "LogCheck/"+filename+" xảy ra lỗi");
                    return;
                }
            }
            // Lưu mảng byte vào file
            FileOutputStream fileOutputStream = new FileOutputStream("LogCheck/"+filename);
            fileOutputStream.write(txt.getBytes("utf-8"));
            fileOutputStream.close();
            System.out.println("Đã lưu mảng byte vào file LogCheck/"+filename);
        }catch(Exception e){e.printStackTrace();}
    }
}
