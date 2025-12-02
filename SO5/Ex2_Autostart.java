import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/**
 * Ex2_Autostart - Adăugare la pornirea automată
 * WINDOWS: Registry 
 * LINUX: .service în /etc/systemd/system/
 */
public class Ex2_Autostart {
    
    private static final String APP_NAME = "SO5_LabApp";
    
    /**
     * Detectare automată sistem de operare
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    /**
     * Metodă publică statică - EXECUTĂ pentru OS curent
     */
    public static void executeForCurrentOS() throws Exception {
        if (isWindows()) {
            addToStartupWindows();
        } else {
            addToStartupLinux();
        }
    }
    
    /**
     * Implementare WINDOWS: Adăugare în Registry
     */
    private static void addToStartupWindows() throws IOException, InterruptedException {
        System.out.println("\n=== [EX2 WINDOWS] Adăugare în Registry ===");
        
        String appPath = getCurrentPath();
        System.out.println("Cale aplicație: " + appPath);
        
        List<String> cmd = Arrays.asList(
            "reg", "add",
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
            "/v", APP_NAME,
            "/t", "REG_SZ",
            "/d", appPath,
            "/f"
        );
        
        System.out.println("Executăm: reg add ...");
        int exitCode = executeCommand(cmd, true);
        
        if (exitCode == 0) {
            System.out.println("SUCCES: Cheie registry creată.");
        } else {
            System.err.println("EȘUAT. Cod: " + exitCode);
        }
    }
    
    /**
     * Implementare LINUX complet automată
     */
    private static void addToStartupLinux() throws IOException, InterruptedException {
        System.out.println("\n=== [EX2 LINUX] Creare + instalare systemd service ===");

        // verificăm că rulează ca root
        if (!System.getProperty("user.name").equals("root")) {
            System.err.println("Această operație necesită root.");
            System.err.println("Rulează: sudo java -jar aplicatia.jar");
            return;
        }

        String realUser = System.getenv("SUDO_USER");
        if (realUser == null || realUser.isEmpty()) realUser = "root";

        String workingDir = System.getProperty("user.dir");
        String executablePath = getCurrentPath(); 

        // Detectăm dacă este .jar
        boolean isJar = executablePath.endsWith(".jar");

        String execStart = isJar
            ? "/usr/bin/java -jar \"" + executablePath + "\""
            : "/usr/bin/java -cp \"" + executablePath + "\" Ex2_Autostart";

        String content =
            "[Unit]\n" +
            "Description=SO5 Lab Application\n" +
            "After=network.target\n\n" +
            "[Service]\n" +
            "Type=simple\n" +
            "User=" + realUser + "\n" +
            "WorkingDirectory=" + workingDir + "\n" +
            "ExecStart=" + execStart + "\n" +
            "Restart=on-failure\n\n" +
            "[Install]\n" +
            "WantedBy=multi-user.target\n";

        Path servicePath = Paths.get("/etc/systemd/system/" + APP_NAME + ".service");

        Files.write(servicePath, content.getBytes());
        Files.setPosixFilePermissions(servicePath,
            PosixFilePermissions.fromString("rw-r--r--"));

        System.out.println("Fișier service creat: " + servicePath);

        executeCommand(Arrays.asList("systemctl", "daemon-reload"), true);
        executeCommand(Arrays.asList("systemctl", "enable", APP_NAME), true);
        executeCommand(Arrays.asList("systemctl", "start", APP_NAME), true);

        System.out.println("Serviciul a fost instalat și pornește automat la boot.");
    }
    
    /**
     * Verificare implementare - platformă specifică
     */
    public static void verifyImplementation(boolean isWin) throws Exception {
        System.out.println("\n=== VERIFICARE Ex2 - AUTOSTART ===");
        
        List<String> cmd;
        if (isWin) {
            cmd = Arrays.asList(
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                "/v", APP_NAME
            );
        } else {
            cmd = Arrays.asList("systemctl", "status", APP_NAME);
        }
        
        System.out.println("Comandă: " + String.join(" ", cmd));
        executeCommand(cmd, true);
    }
    
    /**
     * Helper: Calea curentă a aplicației
     */
    private static String getCurrentPath() throws IOException {
        try {
            String path = Ex2_Autostart.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();
            return new File(java.net.URLDecoder.decode(path, "UTF-8")).getAbsolutePath();
        } catch (Exception e) {
            return new File(System.getProperty("user.dir")).getAbsolutePath();
        }
    }
    
    /**
     * Helper: Executare comandă
     */
    private static int executeCommand(List<String> command, boolean showOutput) 
            throws IOException, InterruptedException {
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        if (showOutput) {
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("   > " + line);
                }
            }
        }
        
        return process.waitFor();
    }
}
