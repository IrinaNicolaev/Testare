import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WindowsRegistry {

    public static String read(String path, String key) {
        try {
            String cmd = "reg query \"" + path + "\" /v " + key;
            System.out.println("EXEC: " + cmd);

            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("REG: " + line);

                if (line.contains(key)) {
                    String[] parts = line.trim().split("\\s+");
                    return parts[parts.length - 1];
                }
            }
        } catch (Exception e) {
            System.out.println("Registry read error: " + e.getMessage());
        }
        return null;
    }

    public static void write(String path, String key, String value) {
        try {
            String cmd = "reg add \"" + path + "\" /v " + key + " /t REG_SZ /d \"" + value + "\" /f";
            System.out.println("EXEC: " + cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            System.out.println("Registry write error: " + e.getMessage());
        }
    }
    
    public static void delete(String path, String key) {
    try {
        String cmd = "reg delete \"" + path + "\" /v " + key + " /f";
        System.out.println("EXEC: " + cmd);
        Runtime.getRuntime().exec(cmd);
    } catch (Exception e) {
        System.out.println("Registry delete error: " + e.getMessage());
    }
}

}
