import java.io.*;
import java.util.*;
import java.util.Scanner;

/**
 * SO5_Main - Meniu interactiv pentru exercițiile 2 și 8
 * Detectează platforma și delegă către implementările specifice
 */
public class OSManagerMain {
    
    public static void main(String[] args) {
        // Detectare automată sistem de operare
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");
        
        // Antet
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         LAB SO5 - EXERCITIILE 2 SI 8 (Interactive Menu)       ║");
        System.out.println("║   Platformă detectată: " + (isWindows ? "WINDOWS" : "LINUX") + 
                           "                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        Scanner scanner = new Scanner(System.in);
        
        // Buclă meniu principal
        while (true) {
            // Afișare opțiuni specifice platformei
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                        MENIU PRINCIPAL                       ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            System.out.println("1. Execută Exercițiul 2 - Pornire automată");
            System.out.println("2. Execută Exercițiul 8 - Configurare firewall");
            System.out.println("3. Verificare implementare Ex2");
            System.out.println("4. Verificare implementare Ex8");
            System.out.println("5. Testare port 8080");
            System.out.println("0. Ieșire");
            System.out.print("\nSelectați opțiunea: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumă newline rămas
            
            // Ieșire din program
            if (choice == 0) {
                System.out.println("\n✅ Program terminat cu succes!");
                break;
            }
            
            // Executare opțiune selectată
            try {
                switch (choice) {
                    case 1:
                        Ex2_Autostart.executeForCurrentOS();
                        break;
                        
                    case 2:
                        Ex8_Firewall.executeForCurrentOS();
                        break;
                        
                    case 3:
                        Ex2_Autostart.verifyImplementation(isWindows);
                        break;
                        
                    case 4:
                        Ex8_Firewall.verifyImplementation(isWindows);
                        break;
                        
                    case 5:
                        testPort(isWindows);
                        break;
                        
                    default:
                        System.out.println("\n❌ Opțiune invalidă! Reîncercați.");
                }
            } catch (Exception e) {
                System.err.println("\n❌ Eroare în execuție: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * Testare port 8080
     */
    private static void testPort(boolean isWindows) throws Exception {
        System.out.println("\n=== TESTARE PORT 8080 ===");
        System.out.println("Porniți un server pe port 8080 (ex: python -m http.server 8080)");
        System.out.println("Apoi apăsați Enter pentru a verifica...");
        System.in.read();
        
        List<String> cmd;
        if (isWindows) {
            cmd = Arrays.asList("cmd.exe", "/c", "netstat -an | findstr :8080");
        } else {
            cmd = Arrays.asList("bash", "-c", "netstat -tuln | grep :8080");
        }
        
        System.out.println("Comandă: " + String.join(" ", cmd));
        executeCommand(cmd, true);
    }
    
    /**
     * Helper: Executare comandă și afișare output
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
        
        int exitCode = process.waitFor();
        System.out.println("   Cod ieșire: " + exitCode);
        return exitCode;
    }
}