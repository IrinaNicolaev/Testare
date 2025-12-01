import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WindowsEventLogReader {

    public static void readEventLog(String logName, int numberOfRecords) {
        try {
            // Construim comanda wevtutil
            String command = String.format(
                "wevtutil qe %s /c:%d /f:text /rd:true",
                logName, numberOfRecords
            );

            // Pornim procesul
            Process process = Runtime.getRuntime().exec(command);

            // Citim ieșirea standard
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            // Citim eroarea standard (în caz că există)
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            String line;

            System.out.println("=== Ultimele înregistrări din jurnalul " + logName + " ===");

            // Afișăm ieșirea comenzii
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Afișăm eventualele erori
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Citiți 20 de intrări din jurnalul System
        readEventLog("System", 20);

        // Exemplu — citiți 10 intrări din Application
        // readEventLog("Application", 10);
    }
}