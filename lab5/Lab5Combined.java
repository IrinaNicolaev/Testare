import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class Lab5Combined {

    private static final int DAYS_OLD = 30;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Laborator 5 - Program combinat ===");
        System.out.println("1) Curatare fisiere vechi dintr-un folder");
        System.out.println("2) Citire loguri Windows (Event Log)");
        System.out.print("Alege optiunea (1/2): ");

        String opt = scanner.nextLine().trim();

        if (opt.equals("1")) {
            runFileCleanup(scanner);
        } else if (opt.equals("2")) {
            runWindowsEventLogReader();
        } else {
            System.out.println("Optiune invalida.");
        }
    }

    private static void runFileCleanup(Scanner scanner) {
        System.out.println("=== Curatare folder (fisiere mai vechi de " + DAYS_OLD + " zile) ===");
        System.out.print("Introdu calea folderului (ENTER pentru Downloads implicit): ");
        String inputPath = scanner.nextLine().trim();

        File folder;
        if (inputPath.isEmpty()) {
            String userHome = System.getProperty("user.home");
            folder = new File(userHome, "Downloads");
            System.out.println("Se foloseste folderul implicit: " + folder.getAbsolutePath());
        } else {
            folder = new File(inputPath);
        }

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("EROARE: Calea nu exista sau nu este folder: " + folder.getAbsolutePath());
            return;
        }

        long now = System.currentTimeMillis();
        long thresholdMillis = DAYS_OLD * 24L * 60 * 60 * 1000;

        File[] allFiles = folder.listFiles();
        if (allFiles == null) {
            System.out.println("Nu pot lista fisierele din folder.");
            return;
        }

        File[] oldFiles = Arrays.stream(allFiles)
                .filter(File::isFile)
                .filter(f -> now - f.lastModified() > thresholdMillis)
                .toArray(File[]::new);

        if (oldFiles.length == 0) {
            System.out.println("Nu s-au gasit fisiere mai vechi de " + DAYS_OLD + " zile.");
            return;
        }

        Arrays.sort(oldFiles, Comparator
                .comparing((File f) -> getExtension(f.getName()))
                .thenComparingLong(File::length)
                .reversed()
        );

        System.out.println("\n=== Fisiere gasite ===");
        long totalBytes = 0;
        for (File f : oldFiles) {
            long size = f.length();
            totalBytes += size;
            System.out.printf("Tip: %-10s | Dim: %8.2f MB | Nume: %s%n",
                    getExtension(f.getName()),
                    bytesToMb(size),
                    f.getName());
        }
        System.out.printf("Total: %.2f MB%n", bytesToMb(totalBytes));

        System.out.print("\nVrei sa stergi toate aceste fisiere? (da/nu): ");
        String answer = scanner.nextLine().trim().toLowerCase();

        if (!answer.equals("da")) {
            System.out.println("Nu s-a sters nimic.");
            return;
        }

        int success = 0, fail = 0;
        for (File f : oldFiles) {
            if (f.delete()) {
                System.out.println("Sters: " + f.getAbsolutePath());
                success++;
            } else {
                System.out.println("NU am putut sterge: " + f.getAbsolutePath());
                fail++;
            }
        }

        System.out.printf("Rezumat: %d sterse, %d esuate.%n", success, fail);
    }

    private static String getExtension(String name) {
        int idx = name.lastIndexOf('.');
        if (idx == -1 || idx == name.length() - 1)
            return "fara_ext";
        return name.substring(idx + 1).toLowerCase();
    }

    private static double bytesToMb(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    private static void runWindowsEventLogReader() {
        System.out.println("=== Citire loguri Windows (ultimele 20 intrari din System) ===");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "wevtutil", "qe", "System", "/c:20", "/f:text");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = p.waitFor();
            System.out.println("\nComanda wevtutil s-a terminat cu codul: " + exitCode);
        } catch (Exception e) {
            System.out.println("Eroare la citirea logurilor Windows: " + e.getMessage());
        }
    }
}
