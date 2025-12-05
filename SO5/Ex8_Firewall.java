import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Ex8_Firewall - Configurare firewall + verificări de rețea (LINUX ONLY)
 * Folosește: iptables + ip + ping + traceroute
 */
public class Ex8_Firewall {
    
    private static final String FIREWALL_RULE_NAME = "SO5_Port_8080";
    private static final int PORT = 8080;
    
    /**
     * Metodă publică statică - EXECUTĂ configurare firewall
     */
    public static void executeForCurrentOS() throws Exception {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         EXERCIȚIUL 8 - CONFIGURARE FIREWALL (LINUX)         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        configureFirewallLinux();
    }
    
    /**
     * Implementare LINUX: iptables + verificări de rețea
     */
    private static void configureFirewallLinux() throws IOException, InterruptedException {
        System.out.println("\n=== Configurare iptables ===");
        
        // Verificare dacă rulează cu sudo
        if (!hasSudoAccess()) {
            System.err.println("❌ EROARE: Acest exercițiu necesită privilegii root!");
            System.err.println("💡 Rulați programul cu: sudo java -cp out OSManagerMain");
            return;
        }
        
        // Verificare dacă iptables există
        if (!isCommandAvailable("iptables")) {
            System.err.println("❌ EROARE: iptables nu este instalat!");
            System.err.println("💡 Instalați cu: sudo apt install iptables");
            return;
        }
        
        // Verificare dacă regula există deja
        if (ruleExists()) {
            System.out.println("⚠️  Regula pentru portul " + PORT + " există deja.");
            System.out.print("Doriți să o înlocuiți? (y/n): ");
            
            try {
                int ch = System.in.read();
                if (ch != 'y' && ch != 'Y') {
                    System.out.println("❌ Operație anulată.");
                    return;
                }
                // Consumă restul liniei
                while (System.in.available() > 0) {
                    System.in.read();
                }
            } catch (Exception e) {
                // Continuă dacă nu poate citi input
            }
            
            // Șterge regula existentă
            deleteExistingRule();
        }
        
        // Adăugare regulă nouă
        addFirewallRule();
        
        // Salvare permanentă
        saveFirewallRules();
        
        // Verificări de rețea
        performNetworkChecks();
    }
    
    /**
     * Verifică dacă avem acces sudo/root
     */
    private static boolean hasSudoAccess() {
        try {
            Process p = new ProcessBuilder("sudo", "-n", "true")
                .redirectErrorStream(true)
                .start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifică dacă o comandă este disponibilă
     */
    private static boolean isCommandAvailable(String command) {
        try {
            Process p = new ProcessBuilder("which", command)
                .redirectErrorStream(true)
                .start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifică dacă regula există deja
     */
    private static boolean ruleExists() throws IOException, InterruptedException {
        List<String> cmd = Arrays.asList("sudo", "iptables", "-L", "INPUT", "-n");
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("dpt:" + PORT) || line.contains(FIREWALL_RULE_NAME)) {
                    found = true;
                    break;
                }
            }
        }
        
        process.waitFor();
        return found;
    }
    
    /**
     * Șterge regula existentă
     */
    private static void deleteExistingRule() throws IOException, InterruptedException {
        System.out.println("🗑️  Ștergere regulă existentă...");
        
        // Găsește numărul liniei regulii
        List<String> listCmd = Arrays.asList("sudo", "iptables", "-L", "INPUT", "-n", "--line-numbers");
        ProcessBuilder pb = new ProcessBuilder(listCmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        int lineNumber = -1;
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("^\\d+.*") && (line.contains("dpt:" + PORT) || line.contains(FIREWALL_RULE_NAME))) {
                    String[] parts = line.trim().split("\\s+");
                    lineNumber = Integer.parseInt(parts[0]);
                    break;
                }
            }
        }
        process.waitFor();
        
        if (lineNumber > 0) {
            List<String> delCmd = Arrays.asList("sudo", "iptables", "-D", "INPUT", String.valueOf(lineNumber));
            executeCommand(delCmd, false);
            System.out.println("✅ Regulă ștearsă.");
        }
    }
    
    /**
     * Adaugă regula firewall pentru portul 8080
     */
    private static void addFirewallRule() throws IOException, InterruptedException {
        System.out.println("\n📝 Adăugare regulă iptables pentru portul " + PORT + "...");
        
        List<String> cmd = Arrays.asList(
            "sudo", "iptables", "-A", "INPUT",
            "-p", "tcp", "--dport", String.valueOf(PORT),
            "-j", "ACCEPT",
            "-m", "comment", "--comment", FIREWALL_RULE_NAME
        );
        
        System.out.println("🚀 Comandă: sudo iptables -A INPUT -p tcp --dport " + PORT + " -j ACCEPT");
        int exitCode = executeCommand(cmd, false);
        
        if (exitCode == 0) {
            System.out.println("✅ SUCCES: Regulă iptables creată pentru portul " + PORT);
        } else {
            System.err.println("❌ EȘUAT: Nu s-a putut crea regula. Verificați iptables.");
        }
    }
    
    /**
     * Salvare permanentă a regulilor firewall
     */
    private static void saveFirewallRules() {
        System.out.println("\n💾 Salvare permanentă a regulilor...");
        
        try {
            // Încercăm iptables-save cu diverse metode
            if (isCommandAvailable("netfilter-persistent")) {
                executeCommand(Arrays.asList("sudo", "netfilter-persistent", "save"), false);
                System.out.println("✅ Reguli salvate cu netfilter-persistent");
            } else if (isCommandAvailable("iptables-save")) {
                // Salvare manuală
                System.out.println("💡 Pentru salvare permanentă, rulați manual:");
                System.out.println("   sudo iptables-save | sudo tee /etc/iptables/rules.v4");
                System.out.println("   SAU instalați: sudo apt install iptables-persistent");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Nu s-a putut salva automat. Salvați manual cu:");
            System.out.println("   sudo iptables-save | sudo tee /etc/iptables/rules.v4");
        }
    }
    
    /**
     * Efectuează verificări de rețea obligatorii
     */
    private static void performNetworkChecks() throws IOException, InterruptedException {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              VERIFICĂRI DE REȚEA - LINUX                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        // 1. Interfețe de rețea
        System.out.println("\n1️⃣  Interfețe de rețea (ip addr):");
        System.out.println("─────────────────────────────────────────");
        if (isCommandAvailable("ip")) {
            executeCommand(Arrays.asList("ip", "addr", "show"), true);
        } else {
            System.out.println("⚠️  Comanda 'ip' nu este disponibilă");
        }
        
        // 2. Test ping localhost
        System.out.println("\n2️⃣  Test conectivitate localhost (ping):");
        System.out.println("─────────────────────────────────────────");
        if (isCommandAvailable("ping")) {
            executeCommand(Arrays.asList("ping", "-c", "3", "127.0.0.1"), true);
        } else {
            System.out.println("⚠️  Comanda 'ping' nu este disponibilă");
        }
        
        // 3. Test traceroute
        System.out.println("\n3️⃣  Traseu către 8.8.8.8 (traceroute):");
        System.out.println("─────────────────────────────────────────");
        if (isCommandAvailable("traceroute")) {
            executeCommand(Arrays.asList("traceroute", "-n", "-m", "10", "8.8.8.8"), true);
        } else {
            System.out.println("⚠️  Comanda 'traceroute' nu este instalată");
            System.out.println("💡 Instalați cu: sudo apt install traceroute");
        }
        
        // 4. Verificare reguli iptables
        System.out.println("\n4️⃣  Reguli iptables curente:");
        System.out.println("─────────────────────────────────────────");
        executeCommand(Arrays.asList("sudo", "iptables", "-L", "INPUT", "-n", "--line-numbers"), true);
    }
    
    /**
     * Verificare implementare
     */
    public static void verifyImplementation() throws Exception {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          VERIFICARE EXERCIȚIUL 8 - FIREWALL                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        // Verificare reguli firewall
        System.out.println("\n1️⃣  Reguli firewall pentru portul " + PORT + ":");
        System.out.println("─────────────────────────────────────────");
        
        List<String> fwCmd = Arrays.asList("sudo", "iptables", "-L", "INPUT", "-n", "-v");
        ProcessBuilder pb = new ProcessBuilder(fwCmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("   " + line);
                if (line.contains("dpt:" + PORT) || line.contains(FIREWALL_RULE_NAME)) {
                    found = true;
                }
            }
        }
        process.waitFor();
        
        if (found) {
            System.out.println("\n✅ Regula pentru portul " + PORT + " este activă!");
        } else {
            System.out.println("\n❌ Regula pentru portul " + PORT + " NU a fost găsită!");
        }
        
        // Configurare rețea
        System.out.println("\n2️⃣  Configurare rețea actuală:");
        System.out.println("─────────────────────────────────────────");
        if (isCommandAvailable("ip")) {
            executeCommand(Arrays.asList("ip", "-brief", "addr", "show"), true);
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
                    System.out.println("   " + line);
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
                verifyImplementation();
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