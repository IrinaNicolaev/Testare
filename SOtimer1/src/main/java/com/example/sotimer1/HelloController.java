package com.example.sotimer1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.*;

public class HelloController {

    @FXML
    private Label labelTimer1; // Timer la interval fix
    @FXML
    private Label labelTimer2; // Timer cu mesaj la 5 secunde
    @FXML
    private Label labelTimer3; // Timer la un anumit timp
    @FXML
    private Label labelTimer4; // Timer cu perioadă indicată

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private ScheduledFuture<?> task1;
    private ScheduledFuture<?> task2;
    private ScheduledFuture<?> task3;
    private ScheduledFuture<?> task4;


    @FXML
    public void startTimer1() {
        if (task1 == null || task1.isCancelled()) {
            task1 = scheduler.scheduleAtFixedRate(() -> {
                Platform.runLater(() ->
                        labelTimer1.setText("Timer 1 rulează: " + System.currentTimeMillis() / 1000 + "s"));
            }, 0, 1, TimeUnit.SECONDS);
        } else {
            task1.cancel(true);
            Platform.runLater(() -> labelTimer1.setText("Timer 1 oprit"));
        }
    }

    @FXML
    public void startTimer2() {
        if (task2 == null || task2.isCancelled()) {
            final int[] counter = {0};
            task2 = scheduler.scheduleAtFixedRate(() -> {
                Platform.runLater(() -> {
                    counter[0] = (counter[0] % 3) + 1;
                    labelTimer2.setText("Timer 2 rulează" + ".".repeat(counter[0]));
                });
            }, 0, 1, TimeUnit.SECONDS);

            // Mesaj după 5 secunde
            scheduler.schedule(() -> {
                task2.cancel(true);
                Platform.runLater(() -> labelTimer2.setText("5 secunde au trecut!"));
            }, 5, TimeUnit.SECONDS);
        } else {
            task2.cancel(true);
            Platform.runLater(() -> labelTimer2.setText("Timer 2 oprit"));
        }
    }


    @FXML
    public void startTimer3() {
        if (task3 == null || task3.isCancelled()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            calendar.set(Calendar.MINUTE, 30);
            calendar.set(Calendar.SECOND, 0);
            Date targetTime = calendar.getTime();
            long delay = targetTime.getTime() - System.currentTimeMillis();

            if (delay < 0) delay = 0; // dacă timpul a trecut, rulează imediat

            task3 = scheduler.schedule(() -> {
                Platform.runLater(() -> labelTimer3.setText("Este ora 18:30!"));
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            task3.cancel(true);
            Platform.runLater(() -> labelTimer3.setText("Timer 3 oprit"));
        }
    }


    @FXML
    public void startTimer4() {
        if (task4 == null || task4.isCancelled()) {
            task4 = scheduler.scheduleAtFixedRate(() -> {
                Platform.runLater(() ->
                        labelTimer4.setText("Timer 4 rulează: " + System.currentTimeMillis() / 1000 + "s"));
            }, 2, 3, TimeUnit.SECONDS);
        } else {
            task4.cancel(true);
            Platform.runLater(() -> labelTimer4.setText("Timer 4 oprit"));
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
