import java.io.*;
import java.util.*;

/**
 * OSManagerMain - Meniu interactiv pentru exercițiile 2 și 8
 * Implementare exclusiv pentru LINUX
 */
public class OSManagerMain {
    
    public static void main(String[] args) {
        printHeader();
        
        Scanner scanner = new Scanner(System.in);
        
        // Buclă meniu principal
        while (true) {
            printMenu();
            
            int choice = getMenuChoice(scanner);
            
            // Ieșire din program
            if (choice == 0) {
                System.out.println("\n✅ Program terminat cu succes!");
                System.out.println("👋 La revedere!");
                break;
            }
            
            // Executare opțiune selectată
            executeMenuOption(choice);
            
            // Pauză după fiecare operație
            waitForUser();
        }
        
        scanner.close();
    }
    
    /**
     * Afișare header aplicație
     */
    private static void printHeader() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║      LAB SO5 - EXERCIȚIILE 2 ȘI 8 (Interactive Menu)        ║");
        System.out.println("║                    Platformă: LINUX                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("📅 Data: " + new java.util.Date());
        System.out.println("🖥️  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("👤 User: " + System.getProperty("user.name"));
    }
    
    /**
     * Afișare meniu principal
     */
    private static void printMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                        MENIU PRINCIPAL                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  1. ⚙️  Execută Exercițiul 2 - Pornire automată");
        System.out.println("  2. 🔥 Execută Exercițiul 8 - Configurare firewall");
        System.out.println("  3. ✅ Verificare implementare Ex2");
        System.out.println("  4. ✅ Verificare implementare Ex8");
        System.out.println("  5. 🔍 Testare port 8080");
        System.out.println("  0. 🚪 Ieșire");
        System.out.println();
        System.out.println("─────────────────────────────────────────────────────────────────");
    }
    
    /**
     * Citește opțiunea din meniu
     */
    private static int getMenuChoice(Scanner scanner) {
        System.out.print("Selectați opțiunea [0-5]: ");
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumă newline rămas
            return choice;
        } catch (InputMismatchException e) {
            scanner.nextLine(); // Curăță input invalid
            return -1;
        }
    }
    
    /**
     * Execută opțiunea selectată din meniu
     */
    private static void executeMenuOption(int choice) {
        try {
            switch (choice) {
                case 1:
                    Ex2_Autostart.executeForCurrentOS();
                    break;
                    
                case 2:
                    Ex8_Firewall.executeForCurrentOS();
                    break;
                    
                case 3:
                    Ex2_Autostart.verifyImplementation();
                    break;
                    
                case 4:
                    Ex8_Firewall.verifyImplementation();
                    break;
                    
                case 5:
                    testPort8080();
                    break;
                    
                default:
                    System.out.println("\n❌ Opțiune invalidă! Vă rugăm selectați 0-5.");
            }
        } catch (Exception e) {
            System.err.println("\n❌ Eroare în execuție: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Testare port 8080
     */
    private static void testPort8080() throws Exception {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  TESTARE PORT 8080                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        // Verificare dacă portul este deschis
        System.out.println("\n🔍 Căutare servicii pe portul 8080...");
        
        if (isCommandAvailable("ss")) {
            System.out.println("\n📊 Porturi deschise care conțin 8080 (ss):");
            System.out.println("─────────────────────────────────────────");
            executeCommandFiltered(Arrays.asList("ss", "-tuln"), "8080");
        } else if (isCommandAvailable("netstat")) {
            System.out.println("\n📊 Porturi deschise care conțin 8080 (netstat):");
            System.out.println("─────────────────────────────────────────");
            executeCommandFiltered(Arrays.asList("netstat", "-tuln"), "8080");
        }
        
        // Verificare regulă firewall
        System.out.println("\n🔥 Verificare regulă firewall pentru portul 8080:");
        System.out.println("─────────────────────────────────────────");
        executeCommandFiltered(Arrays.asList("sudo", "iptables", "-L", "INPUT", "-n"), "8080");
        
        // Sugestii pentru testare
        System.out.println("\n💡 Pentru a testa portul 8080:");
        System.out.println("─────────────────────────────────────────");
        System.out.println("1. Porniți un server web:");
        System.out.println("   python3 -m http.server 8080");
        System.out.println("   SAU");
        System.out.println("   php -S localhost:8080");
        System.out.println();
        System.out.println("2. Verificați din alt terminal:");
        System.out.println("   curl http://localhost:8080");
        System.out.println("   SAU");
        System.out.println("   wget http://localhost:8080");
        System.out.println();
        System.out.println("3. Testați de la distanță (dacă este permis):");
        System.out.println("   telnet <ip-ul-tau> 8080");
        System.out.println("   SAU");
        System.out.println("   nc -zv <ip-ul-tau> 8080");
    }
    
    /**
     * Așteaptă input de la utilizator
     */
    private static void waitForUser() {
        System.out.println("\n⏸️  Apăsați Enter pentru a continua...");
        try {
            System.in.read();
            // Consumă caractere rămase
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (IOException e) {
            // Ignoră eroarea
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
     * Executare comandă cu filtrare output
     */
    private static void executeCommandFiltered(List<String> command, String filter) 
            throws IOException, InterruptedException {
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        boolean foundMatch = false;
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(filter)) {
                    System.out.println("   " + line);
                    foundMatch = true;
                }
            }
        }
        
        process.waitFor();
        
        if (!foundMatch) {
            System.out.println("   ⚠️  Nu s-au găsit rezultate pentru: " + filter);
        }
    }
    
    /**
     * Helper: Executare comandă și afișare output
     */
    private static int executeCommand(List<String> command, boolean showOutput) 
            throws IOException, InterruptedException {
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        int lineCount = 0;
        if (showOutput) {
            try (BufferedReader reader = new BufferedReader(
                     new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null && lineCount < 100) {
                    System.out.println("   " + line);
                    lineCount++;
                }
                if (lineCount >= 100) {
                    System.out.println("   ... (output trunchiat)");
                }
            }
        }
        
        int exitCode = process.waitFor();
        
        if (showOutput && exitCode != 0) {
            System.out.println("   ⚠️  Cod ieșire: " + exitCode);
        }
        
        return exitCode;
    }
}