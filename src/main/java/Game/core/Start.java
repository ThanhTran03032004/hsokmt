package Game.core;

public class Start {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (SQL.is_connected) {
                    Manager.gI().close();
                    SQL.gI().close();
                    System.out.println("SERVER STOPPED!");
                }
            }
        }));
        ServerManager.gI().init();
        ServerManager.gI().running();
    }
}
