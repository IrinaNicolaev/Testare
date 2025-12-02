import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/**
 * Ex2_Autostart - Adăugare la pornirea automată (fără reboot automat)
 * WINDOWS: Registry HKCU\...\Run
 * LINUX: .desktop file în ~/.config/autostart/ (universal, funcționează pe orice DE)
 */
public class Ex2_Autostart {
    
    private static final String APP_NAME = "SO5_LabApp";
    
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    public static void executeForCurrentOS() throws Exception {
        if (isWindows()) {
            addToStartupWindows();
            System.out.println("\n✅ Configurare completă!");
            System.out.println("💡 Pentru a activa: reporniți sistemul sau logout/login");
        } else {
            addToStartupLinux();
            System.out.println("\n✅ Configurare completă!");
            System.out.println("💡 Pentru a activa: logout și login din nou");
        }
    }
    
    /**
     * WINDOWS: Adăugare în Registry
     */
    private static void addToStartupWindows() throws IOException, InterruptedException {
        System.out.println("\n=== [EX2 WINDOWS] Adăugare în Registry ===");
        
        String workingDir = getRealWorkingDirectory();
        String command = "cmd /c start /d \"" + workingDir + "\" java -cp out OSManagerMain";
        
        System.out.println("📁 Directory: " + workingDir);
        System.out.println("🚀 Comandă: " + command);
        
        List<String> cmd = Arrays.asList(
            "reg", "add",
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
            "/v", APP_NAME,
            "/t", "REG_SZ",
            "/d", command,
            "/f"
        );
        
        int exitCode = executeCommand(cmd, true);
        
        if (exitCode != 0) {
            System.err.println("❌ EȘUAT! Cod: " + exitCode);
            throw new RuntimeException("Configurare Registry eșuată");
        }
        
        System.out.println("✅ Registry configurat cu succes!");
    }
    
    /**
     * LINUX: Creare .desktop autostart file (metodă UNIVERSALĂ)
     * Funcționează pe TOATE desktop environments: GNOME, KDE, XFCE, MATE, etc.
     */
    private static void addToStartupLinux() throws Exception {
        System.out.println("\n=== [EX2 LINUX] Creare .desktop autostart file ===");
        
        // Obține directorul de lucru corect
        String workingDir = getRealWorkingDirectory();
        System.out.println("📁 Directory: " + workingDir);
        
        // Verifică că directorul out/ există
        File outDir = new File(workingDir, "out");
        if (!outDir.exists() || !outDir.isDirectory()) {
            throw new RuntimeException("Directorul 'out' nu există în: " + workingDir);
        }
        
        // Detectare terminal disponibil
        String terminal = detectTerminal();
        System.out.println("🖥️  Terminal detectat: " + terminal);
        
        // Construiește comanda completă cu cale absolută
        String execCommand = buildDesktopCommand(terminal, workingDir);
        System.out.println("🚀 Comandă: " + execCommand);
        
        // Conținut .desktop file (standard FreeDesktop.org)
        String content =
            "[Desktop Entry]\n" +
            "Type=Application\n" +
            "Name=SO5 Lab Application\n" +
            "Comment=SO5 Lab - Exercițiul 2 - Autostart\n" +
            "Exec=" + execCommand + "\n" +
            "Icon=utilities-terminal\n" +
            "Terminal=false\n" +
            "Categories=Development;Education;\n" +
            "StartupNotify=false\n" +
            "X-GNOME-Autostart-enabled=true\n";
        
        // Obține home directory-ul utilizatorului real (chiar dacă rulăm cu sudo)
        String userHome;
        String realUser = System.getenv("SUDO_USER");
        if (realUser != null && !realUser.isEmpty()) {
            userHome = "/home/" + realUser;
        } else {
            userHome = System.getProperty("user.home");
        }
        
        // Creare director autostart dacă nu există
        Path autostartDir = Paths.get(userHome, ".config", "autostart");
        if (!Files.exists(autostartDir)) {
            Files.createDirectories(autostartDir);
            System.out.println("📁 Creat director: " + autostartDir);
        }
        
        // Scriere fișier .desktop
        Path desktopFile = autostartDir.resolve(APP_NAME + ".desktop");
        Files.write(desktopFile, content.getBytes());
        
        // Setare permisiuni executable
        try {
            Files.setPosixFilePermissions(desktopFile, 
                PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (Exception e) {
            // Fallback la chmod dacă setPosixFilePermissions eșuează
            executeCommand(Arrays.asList("chmod", "+x", desktopFile.toString()), false);
        }
        
        // Setare owner corect dacă rulăm ca root
        if (System.getProperty("user.name").equals("root") && realUser != null) {
            executeCommand(Arrays.asList("chown", "-R", realUser + ":" + realUser, 
                autostartDir.toString()), false);
        }
        
        System.out.println("✅ Autostart .desktop creat: " + desktopFile);
        System.out.println("\n📄 Conținut:");
        System.out.println(content);
    }
    
    /**
     * Construiește comanda pentru .desktop file (cu cale absolută către java)
     */
    private static String buildDesktopCommand(String terminal, String workingDir) {
        String javaPath = "/usr/bin/java";
        String classpath = workingDir + "/out";
        String javaCmd = javaPath + " -cp " + classpath + " OSManagerMain";
        
        // Construiește comanda în funcție de terminal
        switch (terminal) {
            case "gnome-terminal":
                return "/usr/bin/gnome-terminal --working-directory=" + workingDir + 
                       " -- bash -c '" + javaCmd + "; exec bash'";
                
            case "konsole":
                return "/usr/bin/konsole --workdir " + workingDir + 
                       " -e bash -c '" + javaCmd + "; exec bash'";
                
            case "xfce4-terminal":
                return "/usr/bin/xfce4-terminal --working-directory=" + workingDir + 
                       " -e \"bash -c '" + javaCmd + "; exec bash'\"";
                
            case "mate-terminal":
                return "/usr/bin/mate-terminal --working-directory=" + workingDir + 
                       " -- bash -c '" + javaCmd + "; exec bash'";
                
            case "xterm":
                return "/usr/bin/xterm -e bash -c 'cd " + workingDir + " && " + javaCmd + "; exec bash'";
                
            default:
                // Fallback universal
                return "sh -c 'cd " + workingDir + " && " + javaPath + " -cp out OSManagerMain'";
        }
    }
    
    /**
     * Detectare terminal disponibil în sistem
     */
    private static String detectTerminal() {
        String[] terminals = {
            "gnome-terminal",
            "konsole",
            "xfce4-terminal",
            "mate-terminal",
            "xterm"
        };
        
        for (String term : terminals) {
            try {
                Process p = new ProcessBuilder("which", term).start();
                if (p.waitFor() == 0) {
                    return term;
                }
            } catch (Exception e) {
                // Continuă căutarea
            }
        }
        
        return "xterm"; // Fallback (xterm e aproape universal)
    }
    
    /**
     * Obține directorul REAL unde se află proiectul (directorul care conține "out/")
     */
    private static String getRealWorkingDirectory() {
        try {
            // Metodă 1: Din CodeSource
            String path = Ex2_Autostart.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();
            path = java.net.URLDecoder.decode(path, "UTF-8");
            File file = new File(path);
            
            // Dacă e JAR, ia parent directory
            if (file.isFile()) {
                return file.getParentFile().getAbsolutePath();
            }
            
            // Dacă e director (clase compilate), urcă până găsești directorul cu "out"
            File current = file;
            while (current != null) {
                File outDir = new File(current, "out");
                if (outDir.exists() && outDir.isDirectory()) {
                    return current.getAbsolutePath();
                }
                current = current.getParentFile();
            }
            
            // Metodă 2: Verifică user.dir
            String userDir = System.getProperty("user.dir");
            File outInUserDir = new File(userDir, "out");
            if (outInUserDir.exists() && outInUserDir.isDirectory()) {
                return userDir;
            }
            
            // Metodă 3: Verifică directorul curent
            File outHere = new File("out");
            if (outHere.exists() && outHere.isDirectory()) {
                return new File(".").getCanonicalPath();
            }
            
            // Fallback
            System.err.println("⚠️  Nu s-a găsit directorul 'out'. Se folosește user.dir: " + userDir);
            return userDir;
            
        } catch (Exception e) {
            String userDir = System.getProperty("user.dir");
            System.err.println("⚠️  Eroare detectare director: " + e.getMessage());
            return userDir;
        }
    }
    
    /**
     * Verificare implementare
     */
    public static void verifyImplementation(boolean isWin) throws Exception {
        System.out.println("\n=== VERIFICARE Ex2 - AUTOSTART ===");
        
        if (isWin) {
            // Windows: verifică Registry
            List<String> cmd = Arrays.asList(
                "reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                "/v", APP_NAME
            );
            System.out.println("📋 Verificare Registry:");
            executeCommand(cmd, true);
            
        } else {
            // Linux: verifică .desktop file
            String userHome;
            String realUser = System.getenv("SUDO_USER");
            if (realUser != null && !realUser.isEmpty()) {
                userHome = "/home/" + realUser;
            } else {
                userHome = System.getProperty("user.home");
            }
            
            Path desktopFile = Paths.get(userHome, ".config", "autostart", APP_NAME + ".desktop");
            
            System.out.println("📋 Verificare .desktop file: " + desktopFile);
            
            if (Files.exists(desktopFile)) {
                System.out.println("✅ Fișier găsit!");
                System.out.println("\n📄 Conținut:");
                List<String> lines = Files.readAllLines(desktopFile);
                for (String line : lines) {
                    System.out.println("   > " + line);
                }
            } else {
                System.out.println("❌ Fișier NU a fost găsit!");
            }
        }
    }
    
    /**
     * Helper: Executare comandă sistem
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
    
    /**
     * Main pentru testare directă
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equals("--verify")) {
                verifyImplementation(isWindows());
            } else {
                executeForCurrentOS();
            }
        } catch (Exception e) {
            System.err.println("❌ EROARE: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}