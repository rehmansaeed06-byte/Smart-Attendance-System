package launcher;

import server.ServerMain;

public class ServerNoGUI {
    public static void main(String[] args) {
        ServerMain.startServer();

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                break;
            }
        }
    }
}
