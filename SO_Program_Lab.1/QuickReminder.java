import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.util.Timer;
import java.util.TimerTask;

//Crearea clasei
public class QuickReminder {
    // Crearea variabilelor de tip timer si text input
    private Timer timer;
    private TextArea logArea;

    // Crearea constructorului pentru text input
    public QuickReminder(TextArea logArea) {
        this.logArea = logArea;
    }
    // Crearea de timer
    public void start(int intervalSec, String mesaj) {
        stop(); // Oprește timer-ul existent (dacă există) înainte de a porni unul nou
        timer = new Timer(); // Creează un nou timer
        // Creează task-ul
        TimerTask task = new TimerTask() {
            @Override
            // Actualizare de UI
            public void run() {
                Platform.runLater(() -> logArea.appendText(mesaj + "\n"));
            }
        };
        // Programeaza timerul si afiseaza un mesaj
        timer.scheduleAtFixedRate(task, 0, intervalSec * 1000L);
        Platform.runLater(() -> logArea.appendText("Quick Reminder pornit: " + mesaj + "\n"));
    }
    // Functie de oprire a timerului
    public void stop() {
        if (timer != null) { // Verifica
            timer.cancel(); // Anuleaza
            timer = null; // Elibereaza referinta
            Platform.runLater(() -> logArea.appendText("Quick Reminder oprit!\n"));
        }
    }
}
