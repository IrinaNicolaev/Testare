import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimerTask;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public class AlertTask extends TimerTask {

    private final AlertTableModel model;
    private final Consumer<Alert> alertCallback;

    public AlertTask(AlertTableModel model,
                     Consumer<Alert> alertCallback) {
        this.model = model;
        this.alertCallback = alertCallback;
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime().withSecond(0).withNano(0);
        LocalDate today = now.toLocalDate();

        for (Alert alert : model.getAlerts()) {
            if (!alert.isEnabled()) {
                continue;
            }

            switch (alert.getType()) {
                case TIME:
                    handleTimeAlert(alert, currentTime, today);
                    break;
                case INTERVAL:
                    handleIntervalAlert(alert);
                    break;
                case COUNTDOWN:
                    handleCountdownAlert(alert);
                    break;
            }
        }

        SwingUtilities.invokeLater(() -> model.refresh());
    }

    private void handleTimeAlert(Alert alert,
                                 LocalTime currentTime,
                                 LocalDate today) {
        LocalTime alertTime = alert.getTime();
        if (alertTime == null) return;

        if (alertTime.getHour() == currentTime.getHour()
                && alertTime.getMinute() == currentTime.getMinute()) {

            LocalDate lastDate = alert.getLastTriggeredDate();
            if (lastDate == null || !lastDate.equals(today)) {
                alert.setLastTriggeredDate(today);

                if (!alert.isRepeatDaily()) {
                    alert.setEnabled(false);
                }

                SwingUtilities.invokeLater(() -> alertCallback.accept(alert));
            }
        }
    }

    private void handleIntervalAlert(Alert alert) {
        int rem = alert.getRemainingSeconds();
        if (rem <= 0) {
            int rt = alert.getRepeatTimes();
            if (rt > 0) {
                rt--;
                alert.setRepeatTimes(rt);
                if (rt == 0) {
                    alert.setEnabled(false);
                }
            }
            if (alert.isEnabled()) {
                alert.setRemainingSeconds(alert.getIntervalSeconds());
            }
            SwingUtilities.invokeLater(() -> alertCallback.accept(alert));
        } else {
            alert.setRemainingSeconds(rem - 1);
        }
    }

    private void handleCountdownAlert(Alert alert) {
        int rem = alert.getRemainingSeconds();
        if (rem <= 0) {
            alert.setEnabled(false);
            SwingUtilities.invokeLater(() -> alertCallback.accept(alert));
        } else {
            alert.setRemainingSeconds(rem - 1);
        }
    }
}
