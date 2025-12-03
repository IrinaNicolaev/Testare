import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.*;

public class Lab5App {

    private static final String EDGE = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";

    private static final String[] URLS = {
            "https://www.google.com",
            "https://utm.md",
            "https://educatieonline.md"
    };

    // Fișierul de log pentru înregistrarea acțiunilor
    private static final Path LOG  = Paths.get(
            "C:/Users/nasty/OneDrive/Desktop/Laboratorul_5/integrity_log.txt");

    // Executor pentru programarea închiderii automate a browserului
    private static final ScheduledExecutorService S =
            Executors.newScheduledThreadPool(2);
    private static ScheduledFuture<?> autoClose;

    public static void main(String[] a) {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.println("""
                1. Pornește Edge (3 taburi, auto 10 min)
                2. Oprește Edge
                0. Ieșire
            """);

            System.out.print("Alege opțiunea: ");
            String opt = in.nextLine().trim();

            switch (opt) {
                case "1" -> startBrowser();
                case "2" -> stopBrowser("manual");
                case "0" -> {
                    stopBrowser("exit");
                    S.shutdownNow();
                    return;
                }
                default -> System.out.println("Opțiune invalidă.");
            }
        }
    }

    //  BROWSER
    private static void startBrowser() {
        try {
            ProcessBuilder p = new ProcessBuilder();

            p.command().add(EDGE);
            for (String u : URLS) p.command().add(u);

            p.start();
            log("Edge pornit");

            // Resetează timerul dacă era deja programat
            if (autoClose != null && !autoClose.isDone()) {
                autoClose.cancel(true);
            }

            // Închidere automată după 10 minute
            autoClose = S.schedule(
                    () -> stopBrowser("automat 10 min"),
                    10, TimeUnit.MINUTES
            );

        } catch (IOException e) {
            log("Eroare browser: " + e.getMessage());
        }
    }

    private static void stopBrowser(String r) {
        try {
            // Oprește timerul dacă mai era activ
            if (autoClose != null && !autoClose.isDone()) {
                autoClose.cancel(true);
            }

            // Închide toate procesele Edge
            new ProcessBuilder("cmd", "/c", "taskkill /IM msedge.exe /F /T")
                    .inheritIO()
                    .start();

            log("Edge oprit (" + r + ")");

        } catch (IOException e) {
            log("Eroare stop browser: " + e.getMessage());
        }
    }

    // LOG
    private static void log(String m) {
        // Creează linia de log cu timestamp
        String x = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " - " + m;

        System.out.println(x);

        try {
            Files.createDirectories(LOG.getParent()); // asigură existența folderului
            Files.writeString(LOG, x + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException ignored) {}  
    }
}