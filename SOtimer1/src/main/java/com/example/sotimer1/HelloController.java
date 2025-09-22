package com.example.sotimer1;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HelloController {

    @FXML
    private Label labelTimer1;
    @FXML
    private Label labelTimer2;
    @FXML
    private Label labelTimer3;
    @FXML
    private Label labelTimer4;

    @FXML
    private TextField timeInput;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private ScheduledFuture<?> task1;
    private ScheduledFuture<?> task2;
    private ScheduledFuture<?> task3;
    private ScheduledFuture<?> task4;

    @FXML
    public void startTimer1() {
        if (task1 == null || task1.isCancelled() || task1.isDone()) {
            task1 = scheduler.scheduleAtFixedRate(() ->
                    Platform.runLater(() ->
                            labelTimer1.setText("Timer 1 rulează: " + System.currentTimeMillis() / 1000 + "s")),
                    0, 1, TimeUnit.SECONDS);
        } else {
            task1.cancel(true);
            Platform.runLater(() -> labelTimer1.setText("Timer 1 oprit"));
        }
    }

    @FXML
    public void startTimer2() {
        if (task2 == null || task2.isCancelled() || task2.isDone()) {
            final int[] counter = {0};
            task2 = scheduler.scheduleAtFixedRate(() ->
                    Platform.runLater(() -> {
                        counter[0] = (counter[0] % 3) + 1;
                        labelTimer2.setText("Timer 2 rulează" + ".".repeat(counter[0]));
                    }),
                    0, 1, TimeUnit.SECONDS);

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
        String input = timeInput.getText().trim();

        if (input.isEmpty()) {
            labelTimer3.setText("Introdu o oră (HH:mm, ex: 18:30)");
            return;
        }

        try {
            long delay = getDelayUntil(input);

            if (task3 == null || task3.isCancelled() || task3.isDone()) {
                task3 = scheduler.schedule(() ->
                        Platform.runLater(() -> labelTimer3.setText("A sosit ora " + input + "!")),
                        delay, TimeUnit.MILLISECONDS);

                labelTimer3.setText("Timer programat pentru " + input);
            } else {
                task3.cancel(true);
                Platform.runLater(() -> labelTimer3.setText("Timer oprit"));
            }
        } catch (Exception e) {
            labelTimer3.setText("Format invalid! Folosește HH:mm (ex: 07:45)");
        }
    }

    private long getDelayUntil(String hhmm) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime targetTime = LocalTime.parse(hhmm, formatter);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDateTime = now.withHour(targetTime.getHour())
                .withMinute(targetTime.getMinute())
                .withSecond(0)
                .withNano(0);

        if (targetDateTime.isBefore(now)) {
            // Dacă ora a trecut → așteaptă până mâine
            targetDateTime = targetDateTime.plusDays(1);
        }

        return Duration.between(now, targetDateTime).toMillis();
    }

    @FXML
    public void startTimer4() {
        if (task4 == null || task4.isCancelled() || task4.isDone()) {
            task4 = scheduler.scheduleAtFixedRate(() ->
                    Platform.runLater(() ->
                            labelTimer4.setText("Timer 4 rulează: " + System.currentTimeMillis() / 1000 + "s")),
                    2, 3, TimeUnit.SECONDS);
        } else {
            task4.cancel(true);
            Platform.runLater(() -> labelTimer4.setText("Timer 4 oprit"));
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
