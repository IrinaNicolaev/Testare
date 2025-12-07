public class Main {

    public static void main(String[] args) {
        Logger.log("Application start.");

        // Adăugăm la startup (Windows registry)
        AutostartManager.ensureAutostart();

        // Thread proces monitor (punctul 14)
        Thread processThread = new Thread(new ProcessMonitor(), "ProcessMonitor");
        processThread.setDaemon(true);
        processThread.start();

        // Thread folder monitor (punctul 20)
        Thread folderThread = new Thread(new FolderMonitor(), "FolderMonitor");
        folderThread.setDaemon(true);
        folderThread.start();

        // Thread Telegram update listener (pentru comenzi /unblock_steam)
        TelegramUpdateListener telegramListener = new TelegramUpdateListener();
        Thread telegramThread = new Thread(telegramListener, "TelegramUpdateListener");
        telegramThread.setDaemon(true);
        telegramThread.start();

        // menținem aplicația vie
        try {
            while (true) {
                Thread.sleep(60_000);
            }
        } catch (InterruptedException e) {
            Logger.log("Main interrupted: " + e.getMessage());
        }
    }
}
