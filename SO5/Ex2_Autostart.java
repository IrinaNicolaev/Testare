import java.io.*;
import java.nio.file.*;
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
            System.out.println("✅ SUCCES: Cheie registry creată!");
        } else {
            System.err.println("❌ EȘUAT. Cod: " + exitCode);
        }
    }
    
    /**
     * Implementare LINUX: Creare fișier .service în /etc/systemd/system/
     */
    private static void addToStartupLinux() throws IOException, InterruptedException {
        System.out.println("\n=== [EX2 LINUX] Creare fișier systemd service ===");
        
        // Conținut service file
        String content = String.format(
            "[Unit]%n" +
            "Description=SO5 Lab Application%n" +
            "After=network.target%n%n" +
            "[Service]%n" +
            "Type=simple%n" +
            "User=%s%n" +
            "WorkingDirectory=%s%n" +
            "ExecStart=/usr/bin/java -cp \"%s\" Ex2_Autostart%n" +
            "Restart=on-failure%n%n" +
            "[Install]%n" +
            "WantedBy=multi-user.target%n",
            System.getProperty("user.name"),
            System.getProperty("user.dir"),
            System.getProperty("user.dir")
        );
        
        // Salvare temporar în directorul curent (pentru demonstrație)
        String serviceFileName = APP_NAME + ".service";
        Path tempPath = Paths.get(System.getProperty("user.dir"), serviceFileName);
        Files.write(tempPath, content.getBytes());
        tempPath.toFile().setReadable(true);
        
        System.out.println("✅ Fișier creat: " + tempPath);
        System.out.println("\nPentru instalare PERMANENTĂ în /etc/systemd/system/:");
        System.out.println("  sudo cp " + tempPath + " /etc/systemd/system/");
        System.out.println("  sudo systemctl daemon-reload");
        System.out.println("  sudo systemctl enable " + APP_NAME);
    }
    
    /**
     * Verificare implementare - platformă specifică
     */
    public static void verifyImplementation(boolean isWindows) throws Exception {
        System.out.println("\n=== VERIFICARE Ex2 - AUTOSTART ===");
        
        List<String> cmd;
        if (isWindows) {
            cmd = Arrays.asList(
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                "/v", APP_NAME
            );
        } else {
            // Verificare fișier .service în directorul curent
            String servicePath = System.getProperty("user.dir") + "/" + APP_NAME + ".service";
            cmd = Arrays.asList("ls", "-l", servicePath);
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