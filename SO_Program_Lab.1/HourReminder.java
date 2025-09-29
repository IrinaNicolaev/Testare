import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;

public class HourReminder {
    private Timer timer;
    private TextArea logArea;

    public HourReminder(TextArea logArea) {
        this.logArea = logArea;
    }

    // Timer pentru o ora anumita
    public void start(String oraHHMM, String mesaj) {
        stop();
        String[] parts = oraHHMM.split(":"); // Separa ora de minute
        int hour = Integer.parseInt(parts[0]); // Converteste stringurile
        int minute = Integer.parseInt(parts[1]); //

        Calendar calendar = Calendar.getInstance(); // Crearea obiectului calendar
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Convertește Calendar-ul în obiect Date
        Date triggerTime = calendar.getTime();
        // Verifică dacă ora setată a trecut deja astăzi, daca da se seteaza pe ziua urmatoare
        if (triggerTime.before(new Date())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            triggerTime = calendar.getTime();
        }

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> logArea.appendText(mesaj + "\n"));
            }
        };
        timer.schedule(task, triggerTime);
        Platform.runLater(() -> logArea.appendText("Hour Reminder setat la " + oraHHMM + "\n"));
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            Platform.runLater(() -> logArea.appendText("Hour Reminder oprit!\n"));
        }
    }
}
