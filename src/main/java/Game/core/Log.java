package Game.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import Game.template.LogTemplate;

public class Log implements Runnable {

    private static Log instance;
    private final BlockingQueue<LogTemplate> list;
    private final BlockingQueue<LogTemplate> log_gold;
    private final BlockingQueue<LogTemplate> log_gems;
    private final BlockingQueue<LogTemplate> Logs_Server;
    private final BlockingQueue<LogTemplate> Logs_Trade;
    private final Thread mythread;
    private boolean running;

    public Log() {
        list = new LinkedBlockingDeque<>();
        log_gold = new LinkedBlockingDeque<>();
        log_gems = new LinkedBlockingDeque<>();
        Logs_Server = new LinkedBlockingDeque<>();
        Logs_Trade = new LinkedBlockingDeque<>();
        mythread = new Thread(this);
    }

    public synchronized static Log gI() {
        if (instance == null) {
            instance = new Log();
        }
        return instance;
    }

    @Override
    public void run() {
        while (this.running) {
            LogTemplate temp = list.poll();
            if (temp != null) {
                try {
                    this.save_log(temp.name, temp.text);
                } catch (IOException e) {
                    System.err.println("save log err at " + temp.name + " !");
                }
            }
            temp = log_gold.poll();
            if (temp != null) {
                try {
                    this.save_log_gold(temp.name, temp.text);
                } catch (IOException e) {
                    System.err.println("save log err at " + temp.name + " !");
                }
            }
            temp = log_gems.poll();
            if (temp != null) {
                try {
                    this.save_log_gems(temp.name, temp.text);
                } catch (IOException e) {
                    System.err.println("save log err at " + temp.name + " !");
                }
            }
            temp = Logs_Server.poll();
            if (temp != null) {
                try {
                    this.save_log_Server(temp.name, temp.text);
                } catch (IOException e) {
                    System.err.println("save log err at " + temp.name + " !");
                }
            }
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void save_log_gems(String name, String text) throws IOException {
        String path = "log_gems/" + Util.fmt_save_log.format(Date.from(Instant.now())) + "/" + name + ".txt";
        File f = new File(path);
        f.getParentFile().mkdirs();
        if (!f.exists()) {
            if (!f.createNewFile()) {
                System.out.println("Tạo file " + name + ".txt xảy ra lỗi");
                return;
            }
        }
        try (FileWriter fwt = new FileWriter(f, true)) {
            fwt.write((text + "\n"));
        }
    }

    private void save_log_gold(String name, String text) throws IOException {
        String path = "log_gold/" + Util.fmt_save_log.format(Date.from(Instant.now())) + "/" + name + ".txt";
        File f = new File(path);
        f.getParentFile().mkdirs();
        if (!f.exists()) {
            if (!f.createNewFile()) {
                System.out.println("Tạo file " + name + ".txt xảy ra lỗi");
                return;
            }
        }
        try (FileWriter fwt = new FileWriter(f, true)) {
            fwt.write((text + "\n"));
        }
    }

    private void save_log(String name, String text) throws IOException {
        String path = "log/" + Util.fmt_save_log.format(Date.from(Instant.now())) + "/" + name + ".txt";
        File f = new File(path);
        f.getParentFile().mkdirs();
        if (!f.exists()) {
            if (!f.createNewFile()) {
                System.out.println("Tạo file " + name + ".txt xảy ra lỗi");
                return;
            }
        }
        try (FileWriter fwt = new FileWriter(f, true)) {
            fwt.write((text + "\n"));
        }
    }
    private void save_log_Server(String name, String text) throws IOException {
        String path = "log_server/" + Util.fmt_save_log.format(Date.from(Instant.now())) + "/" + name + ".txt";
        File f = new File(path);
        f.getParentFile().mkdirs();
        if (!f.exists()) {
            if (!f.createNewFile()) {
                System.out.println("Tạo file " + name + ".txt xảy ra lỗi");
                return;
            }
        }
        try (FileWriter fwt = new FileWriter(f, true)) {
            fwt.write((text + "\n"));
        }
    }

    public void start_log() {
        this.running = true;
        this.mythread.start();
    }

    public void close_log() {
        this.running = false;
        this.mythread.interrupt();
    }

    public void add_log(String name, String txt) {
        String time = "[" + Util.get_now_by_time() + "]  ";
        this.list.add(new LogTemplate(name, (time + txt)));
    }
    public void add_log_gems(String name, String txt) {
        String time = "[" + Util.get_now_by_time() + "]  ";
        this.log_gems.add(new LogTemplate(name, (time + txt)));
    }
    public void add_log_gold(String name, String txt) {
        String time = "[" + Util.get_now_by_time() + "]  ";
        this.log_gold.add(new LogTemplate(name, (time + txt)));
    }
    public void add_Log_Server(String name, String txt)
    {
        String time = "[" + Util.get_now_by_time() + "]  ";
        this.Logs_Server.add(new LogTemplate(name, (time + txt)));
    }
    
    public void add_Log_Trade(String name, String txt)
    {
        String time = "[" + Util.get_now_by_time() + "]  ";
        this.Logs_Trade.add(new LogTemplate(name, (time + txt)));
    }
}
