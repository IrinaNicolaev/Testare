import java.io.*;
import java.util.*;
import java.util.Scanner;

/**
 * SO5_Main - Meniu interactiv pentru exercitiul 8 (Firewall)
 * Detecteaza platforma si delega catre implementarea specifica
 * 
 */
public class OSManagerMain {
    
    public static void main(String[] args) {
        // Detectare automata sistem de operare
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");
        
        // Antet
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         LAB SO5 - EXERCITIUL 8 (Firewall - Multi-OS)         ║");
        System.out.println("║   Platforma detectata: " + (isWindows ? "WINDOWS" : "LINUX") + 
                           "                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        Scanner scanner = new Scanner(System.in);
        
        // Bucla meniu principal
        while (true) {
            // Afisare optiuni
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                        MENIU PRINCIPAL                       ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            System.out.println("1. Executa Exercitiul 8 - Configurare firewall");
            System.out.println("2. Verificare implementare Ex8 (firewall + retea)");
            System.out.println("3. Testare port 8080");
            System.out.println("0. Iesire");
            System.out.print("\nSelectati optiunea: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consuma newline ramas
            
            // Iesire din program
            if (choice == 0) {
                System.out.println("\nProgram terminat cu succes!");
                break;
            }
            
            // Executare optiune selectata
            try {
                switch (choice) {
                    case 1:
                        // Executa configurarea firewall
                        Ex8_Firewall.executeForCurrentOS();
                        break;
                        
                    case 2:
                        // Verificare firewall + comenzi retea
                        Ex8_Firewall.verifyImplementation(isWindows);
                        break;
                        
                    case 3:
                        // Testare port 8080
                        testPort(isWindows);
                        break;
                        
                    default:
                        System.out.println("\nOptiune invalida! Reincercati.");
                }
            } catch (Exception e) {
                System.err.println("\nEroare in executie: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * Testare port 8080
     */
    private static void testPort(boolean isWindows) throws Exception {
        System.out.println("\n=== TESTARE PORT 8080 ===");
        System.out.println("Porniti un server pe port 8080 (ex: python -m http.server 8080)");
        System.out.println("Apoi apasati Enter pentru a verifica...");
        System.in.read();
        
        List<String> cmd;
        if (isWindows) {
            cmd = Arrays.asList("cmd.exe", "/c", "netstat -an | findstr :8080");
        } else {
            cmd = Arrays.asList("bash", "-c", "netstat -tuln | grep :8080");
        }
        
        System.out.println("Comanda: " + String.join(" ", cmd));
        executeCommand(cmd, true);
    }
    
    /**
     * Helper: Executare comanda si afisare output
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
        System.out.println("   Cod iesire: " + exitCode);
        return exitCode;
    }
}