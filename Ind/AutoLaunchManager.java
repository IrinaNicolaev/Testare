public class AutoLaunchManager {

    private static final String RUN =
        "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    private static final String KEY = "AlertScheduler";

    public static boolean enable(String pathToJar) {
        WindowsRegistry.write(RUN, KEY, pathToJar);
        return true; 
    }

    public static boolean disable() {
        WindowsRegistry.delete(RUN, KEY);
        return true;
    }

    public static boolean isEnabled() {
        return WindowsRegistry.read(RUN, KEY) != null;
    }
}
