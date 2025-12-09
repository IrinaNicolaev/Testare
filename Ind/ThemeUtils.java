import java.awt.*;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

public class ThemeUtils {

    private static final String REG_PATH =
        "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";

    public static void apply(JFrame frame) {

        String raw = WindowsRegistry.read(REG_PATH, "AppsUseLightTheme");
        System.out.println("Theme registry value = " + raw);

        boolean light = raw == null || raw.equals("0x1");

        if (light) {
            applyColors(frame, Color.WHITE, Color.BLACK);
        } else {
            applyColors(frame, new Color(30,30,30), new Color(220,220,220));
        }

        SwingUtilities.updateComponentTreeUI(frame);
    }

    private static void applyColors(Component c, Color bg, Color fg) {
    try {
        if (!(c instanceof JTextComponent)) {
            c.setBackground(bg);
        }
        c.setForeground(fg);
    } catch (Exception ignored) {}

    if (c instanceof JScrollPane scroll) {
        scroll.setBackground(bg);
        scroll.getViewport().setBackground(bg);
        scroll.getViewport().setForeground(fg);
    }

    if (c instanceof JTable table) {
        table.setBackground(bg);
        table.setForeground(fg);
        table.setGridColor(fg);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setBackground(bg.darker());
            header.setForeground(fg);
        }
    }

    // Рекурсивная покраска вложенных компонентов
    if (c instanceof Container container) {
        for (Component child : container.getComponents()) {
            applyColors(child, bg, fg);
        }
    }
}

}
