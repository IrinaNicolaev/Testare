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

    private static final Path ORIG = Paths.get(
            "C:/Users/nasty/OneDrive/Desktop/Laboratorul_5/system_original");
    private static final Path BACK = Paths.get(
            "C:/Users/nasty/OneDrive/Desktop/Laboratorul_5/system_backup");

    // Fișierul de log pentru înregistrarea acțiunilor
    private static final Path LOG  = Paths.get(
            "C:/Users/nasty/OneDrive/Desktop/Laboratorul_5/integrity_log.txt");

    // Executor pentru programarea închiderii automate a browserului
    private static final ScheduledExecutorService S =
            Executors.newScheduledThreadPool(2);
    private static ScheduledFuture<?> autoClose;

    private static ScheduledFuture<?> monitorTask;

    public static void main(String[] a) {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.println("""
                1. Pornește Edge (3 taburi, auto 10 min)
                2. Oprește Edge
                3. Pornește monitorizarea
                4. Oprește monitorizarea
                5. Backup manual
                0. Ieșire
            """);

            System.out.print("Alege opțiunea: ");
            String opt = in.nextLine().trim();

            switch (opt) {
                case "1" -> startBrowser();
                case "2" -> stopBrowser("manual");
                case "3" -> startMonitoring();   
                case "4" -> stopMonitoring();    
                case "5" -> backup();            
                case "0" -> {
                    stopMonitoring(); 
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

        // MONITORIZARE
        private static void startMonitoring() {
        if (monitorTask != null && !monitorTask.isDone()) {
            System.out.println("Monitorizarea este deja activă.");
            return;
        }
        try {
            Files.createDirectories(ORIG);
            Files.createDirectories(BACK);
            backup();

            monitorTask = S.scheduleAtFixedRate(() -> {
                try {
                    check();
                    backup();
                } catch (Exception e) {
                    log("Eroare la monitorizare: " + e.getMessage());
                }
            }, 0, 30, TimeUnit.SECONDS);

            System.out.println("Monitorizarea a început.");
        } catch (IOException e) {
            log("Eroare init monitorizare: " + e.getMessage());
        }
    }

        private static void stopMonitoring() {
        if (monitorTask != null && !monitorTask.isDone()) {
            monitorTask.cancel(true);
            System.out.println("Monitorizarea a fost oprită.");
        }
    }

    // BACKUP & RESTAURARE
        private static void backup() {
        try {
            Files.walk(ORIG)
                    .filter(Files::isRegularFile)
                    .forEach(f -> {
                        Path d = BACK.resolve(ORIG.relativize(f));
                        try {
                            Files.createDirectories(d.getParent());
                            Files.copy(f, d, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ignored) {}
                    });
            log("Backup realizat.");
        } catch (IOException e) {
            log("Eroare backup: " + e.getMessage());
        }
    }

    private static void check() throws IOException {
        // fișiere modificate
        Files.walk(ORIG)
                .filter(Files::isRegularFile)
                .forEach(o -> {
                    Path b = BACK.resolve(ORIG.relativize(o));
                    if (!Files.exists(b)) return;
                    try {
                        if (Files.mismatch(o, b) != -1) {
                            Files.copy(b, o, StandardCopyOption.REPLACE_EXISTING);
                            log("Restaurat (modificat): " + o);
                        }
                    } catch (IOException ignored) {}
                });

        // fișiere șterse din ORIG, dar prezente în BACK
        Files.walk(BACK)
                .filter(Files::isRegularFile)
                .forEach(b -> {
                    Path o = ORIG.resolve(BACK.relativize(b));
                    if (!Files.exists(o)) {
                        try {
                            Files.createDirectories(o.getParent());
                            Files.copy(b, o, StandardCopyOption.REPLACE_EXISTING);
                            log("Recreat (șters): " + o);
                        } catch (IOException ignored) {}
                    }
                });
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