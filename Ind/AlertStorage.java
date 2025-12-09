import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AlertStorage {

    private static final Path FILE_PATH = Paths.get("alerts.txt");

    public static void save(List<Alert> alerts) {
        List<String> lines = new ArrayList<>();

        for (Alert a : alerts) {
            String line = String.join(";",
                    a.getType().name(),
                    safe(a.getName()),
                    a.getTime() != null ? a.getTime().toString() : "",
                    String.valueOf(a.getIntervalSeconds()),
                    String.valueOf(a.getCountdownTotalSeconds()),
                    safe(a.getSoundPath()),
                    safe(a.getActionPath()),
                    String.valueOf(a.isRepeatDaily()),
                    String.valueOf(a.getRepeatTimes()),
                    String.valueOf(a.isEnabled()),
                    String.valueOf(a.getRemainingSeconds()),
                    a.getLastTriggeredDate() != null ? a.getLastTriggeredDate().toString() : ""
            );
            lines.add(line);
        }

        try {
            Files.write(FILE_PATH, lines, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public static List<Alert> load() {
        List<Alert> result = new ArrayList<>();

        if (!Files.exists(FILE_PATH)) {
            return result;
        }

        try {
            List<String> lines = Files.readAllLines(FILE_PATH);

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(";", -1);
                if (p.length < 12) continue;

                Alert.AlertType type = Alert.AlertType.valueOf(p[0]);
                String name = p[1];

                LocalTime time = p[2].isEmpty() ? null : LocalTime.parse(p[2]);
                int intervalSec = Integer.parseInt(p[3]);
                int countdownSec = Integer.parseInt(p[4]);

                String sound = p[5].isEmpty() ? null : p[5];
                String action = p[6].isEmpty() ? null : p[6];

                boolean repeatDaily = Boolean.parseBoolean(p[7]);
                int repeatTimes = Integer.parseInt(p[8]);
                boolean enabled = Boolean.parseBoolean(p[9]);
                int remaining = Integer.parseInt(p[10]);

                LocalDate last = p[11].isEmpty() ? null : LocalDate.parse(p[11]);

                Alert alert = new Alert(
                        type,
                        name,
                        time,
                        intervalSec,
                        countdownSec,
                        sound,
                        action,
                        repeatDaily,
                        repeatTimes,
                        enabled,
                        remaining,
                        last
                );

                result.add(alert);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
