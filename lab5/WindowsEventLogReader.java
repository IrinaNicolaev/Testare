import java.io.IOException;
import java.util.Scanner;

public class WindowsEventLogReader {

    public static void readSystemLog() {
        try {
            String command = "wevtutil qe System /f:text /rd:true";


            Process process = Runtime.getRuntime().exec(command);

            System.out.println("Toate înregistrările din jurnalul SYSTEM");

            // Citire output
            Scanner sc = new Scanner(process.getInputStream());
            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
            sc.close();

            // Citire eventuale erori
            Scanner err = new Scanner(process.getErrorStream());
            while (err.hasNextLine()) {
                System.err.println(err.nextLine());
            }
            err.close();

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        readSystemLog();
    }
}