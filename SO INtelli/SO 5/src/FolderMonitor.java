import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FolderMonitor implements Runnable {

    @Override
    public void run() {
        Logger.log("FolderMonitor started for: " + Config.MONITORED_FOLDER);
        try {
            watchFolder();
        } catch (IOException | InterruptedException e) {
            Logger.log("FolderMonitor error: " + e.getMessage());
        }
    }

    private void watchFolder() throws IOException, InterruptedException {
        Path path = Paths.get(Config.MONITORED_FOLDER);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        while (true) {
            WatchKey key = watchService.take(); // blocant
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path context = (Path) event.context();
                String fullPath = path.resolve(context).toString();

                String msg = "Folder event: " + kind.name() + " -> " + fullPath;
                Logger.log(msg);
                Notifier.notifyAllChats(msg);

            }
            boolean valid = key.reset();
            if (!valid) {
                Logger.log("WatchKey no longer valid, stopping FolderMonitor.");
                break;
            }
        }
    }
}
