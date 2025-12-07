import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Object LOCK = new Object();
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String line = "[" + timestamp + "] " + message;
        System.out.println(line);
        synchronized (LOCK) {
            try (FileWriter fw = new FileWriter(Config.LOG_FILE, true)) {
                fw.write(line + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
