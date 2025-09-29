import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.util.Timer;
import java.util.TimerTask;

public class PomodoroTimer {
    private Timer timer;
    private TextArea logArea;

    public PomodoroTimer(TextArea logArea) {
        this.logArea = logArea;
    }

    // Declarare de parametri
    public void start(int workMinutes, int breakMinutes, int cycles, String workMsg, String breakMsg) {
        stop();
        timer = new Timer();
        Platform.runLater(() -> logArea.appendText("Pomodoro pornit: " + workMinutes + " min work, " + breakMinutes + " min pauză, " + cycles + " cicluri\n"));
        // Începe ciclul 1
        scheduleCycle(1, workMinutes, breakMinutes, cycles, workMsg, breakMsg);
    }

    // Se autoapelează pentru a trece la ciclul următor
    private void scheduleCycle(int currentCycle, int workMinutes, int breakMinutes, int totalCycles, String workMsg, String breakMsg) {
        // Condiție de oprire: dacă am trecut de toate ciclurile
        if (currentCycle > totalCycles) {
            Platform.runLater(() -> logArea.appendText("Toate ciclurile Pomodoro completate!\n"));
            return;
        }

        // Work
        TimerTask workTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> logArea.appendText("Ciclul " + currentCycle + " WORK: " + workMsg + "\n"));

                // Break
                TimerTask breakTask = new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> logArea.appendText("Ciclul " + currentCycle + " BREAK: " + breakMsg + "\n"));
                        scheduleCycle(currentCycle + 1, workMinutes, breakMinutes, totalCycles, workMsg, breakMsg);
                    }
                };
                timer.schedule(breakTask, breakMinutes * 60 * 1000L);
            }
        };

        timer.schedule(workTask, 0);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            Platform.runLater(() -> logArea.appendText("Pomodoro Timer oprit!\n"));
        }
    }
}
