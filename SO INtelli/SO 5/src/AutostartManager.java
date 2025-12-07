import java.io.File;

public class AutostartManager {

    public static void ensureAutostart() {
        try {
            String jarPath = getJarPath();
            if (jarPath == null) {
                Logger.log("Cannot determine JAR path, skipping autostart setup.");
                return;
            }

            String regCmd = "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\" "
                    + "/v ProcessAndFolderMonitor "
                    + "/t REG_SZ "
                    + "/d \"" + jarPath + "\" "
                    + "/f";

            Logger.log("Setting autostart with command: " + regCmd);
            Process p = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", regCmd});
            p.waitFor();
            Logger.log("Autostart registry entry attempted (check registry to confirm).");

        } catch (Exception e) {
            Logger.log("ensureAutostart error: " + e.getMessage());
        }
    }

    private static String getJarPath() {
        try {
            String path = AutostartManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();
            File f = new File(path);
            return f.getAbsolutePath();
        } catch (Exception e) {
            Logger.log("getJarPath error: " + e.getMessage());
            return null;
        }
    }
}
