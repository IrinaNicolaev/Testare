import java.io.*;
import java.util.*;

/**
 * Ex8_Firewall - Configurare firewall + verificări de rețea
 * WINDOWS: netsh + ipconfig + ping + tracert
 * LINUX: iptables + ip + ping + traceroute
 */
public class Ex8_Firewall {
    
    private static final String FIREWALL_RULE_NAME = "SO5_Port_8080";
    private static final int PORT = 8080;
    
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
            configureFirewallWindows();
        } else {
            configureFirewallLinux();
        }
    }
    
    /**
     * Implementare WINDOWS: netsh + ipconfig + ping + tracert
     */
    private static void configureFirewallWindows() throws IOException, InterruptedException {
        System.out.println("\n=== [EX8 WINDOWS] Configurare netsh firewall ===");
        
        // Șterge regula veche dacă există
        executeCommand(Arrays.asList(
            "netsh", "advfirewall", "firewall", "delete", "rule",
            "name=" + FIREWALL_RULE_NAME
        ), false);
        
        List<String> cmd = Arrays.asList(
            "netsh", "advfirewall", "firewall", "add", "rule",
            "name=" + FIREWALL_RULE_NAME,
            "dir=in", "action=allow", "protocol=TCP",
            "localport=" + PORT, "enable=yes", "profile=any"
        );
        
        System.out.println("Executăm: netsh advfirewall ...");
        int exitCode = executeCommand(cmd, true);
        
        if (exitCode == 0) {
            System.out.println("SUCCES: Regulă firewall creată pentru portul " + PORT);
        } else {
            System.err.println("EȘUAT. Rulează ca Administrator!");
        }
        
        // Verificări rețea obligatorii conform cerinței
        System.out.println("\n--- Verificări de rețea Windows ---");
        
        System.out.println("\n1. Configurare IP (ipconfig):");
        executeCommand(Arrays.asList("ipconfig", "/all"), true);
        
        System.out.println("\n2. Test ping localhost:");
        executeCommand(Arrays.asList("ping", "127.0.0.1", "-n", "3"), true);
        
        System.out.println("\n3. Test tracert către 8.8.8.8:");
        executeCommand(Arrays.asList("tracert", "-d", "8.8.8.8"), true);
    }
    
    /**
     * Implementare LINUX: iptables + ip + ping + traceroute
     */
    private static void configureFirewallLinux() throws IOException, InterruptedException {
        System.out.println("\n=== [EX8 LINUX] Configurare iptables + verificări ===");
        
        // Verificare sudo
        int sudoCheck = executeCommand(Arrays.asList("sudo", "-v"), false);
        if (sudoCheck != 0) {
            System.err.println("EROARE: Rulează cu sudo (sudo java Ex8_Firewall)");
            return;
        }
        
        // Adăugare regulă iptables
        List<String> cmd = Arrays.asList(
            "sudo", "iptables", "-A", "INPUT",
            "-p", "tcp", "--dport", String.valueOf(PORT),
            "-j", "ACCEPT",
            "-m", "comment", "--comment", FIREWALL_RULE_NAME
        );
        
        System.out.println("Executăm: sudo iptables ...");
        int exitCode = executeCommand(cmd, true);
        
        if (exitCode == 0) {
            System.out.println("SUCCES: Regulă iptables creată pentru portul " + PORT);
            
            // Salvare permanentă
            try {
                executeCommand(Arrays.asList("sudo", "iptables-save"), true);
                System.out.println("Regulă salvată permanent.");
            } catch (Exception e) {
                System.out.println("Salvează manual: sudo iptables-save > /etc/iptables/rules.v4");
            }
        } else {
            System.err.println("EȘUAT. Verifică iptables.");
        }
        
        // Verificări rețea obligatorii
        System.out.println("\n--- Verificări de rețea Linux ---");
        
        System.out.println("\n1. Interfețe de rețea (ip addr):");
        executeCommand(Arrays.asList("ip", "addr", "show"), true);
        
        System.out.println("\n2. Test ping localhost:");
        executeCommand(Arrays.asList("ping", "-c", "3", "127.0.0.1"), true);
        
        System.out.println("\n3. Test traceroute către 8.8.8.8:");
        executeCommand(Arrays.asList("traceroute", "-n", "8.8.8.8"), true);
        
        // Verificare regulă specifică
        System.out.println("\n4. Verificare regulă iptables:");
        executeCommand(Arrays.asList("sudo", "iptables", "-L", "-n", "--line-numbers"), true);
    }
    
    /**
     * Verificare implementare - platformă specifică
     */
    public static void verifyImplementation(boolean isWindows) throws Exception {
        System.out.println("\n=== VERIFICARE Ex8 - FIREWALL ȘI REȚEA ===");
        
        // Verificare regulă firewall
        List<String> fwCmd;
        if (isWindows) {
            fwCmd = Arrays.asList(
                "netsh", "advfirewall", "firewall", "show", "rule",
                "name=" + FIREWALL_RULE_NAME
            );
        } else {
            fwCmd = Arrays.asList("sudo", "iptables", "-L", "-n", "|", "grep", String.valueOf(PORT));
        }
        
        System.out.println("1. Regulă firewall:");
        executeCommand(fwCmd, true);
        
        // Comandă de rețea (ipconfig/ip addr)
        List<String> ipCmd = isWindows ? 
            Arrays.asList("ipconfig") : 
            Arrays.asList("ip", "addr", "show");
        
        System.out.println("\n2. Configurare rețea:");
        executeCommand(ipCmd, true);
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