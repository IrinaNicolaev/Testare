package com.example.sotimer1;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

    @FXML
    private ImageView caruselView;

    @FXML
    private ImageView fireworksView;


    private Timer timer1;
    private Timer timer2;
    private Timer timer3;
    private Timer timer4;

    // ===== Timer 1 – Carusel GIF =====
    @FXML
    public void startTimer1() {
        if (timer1 == null) {
            // Pornim GIF
            Image gif = new Image(getClass().getResourceAsStream("carusel.gif"));
            caruselView.setImage(gif);

            timer1 = new Timer();
            timer1.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() ->
                            labelTimer1.setText("Timp de funcționare " + (System.currentTimeMillis() / 1000) + "s"));
                }
            }, 0, 1000);
        } else {
            // Oprire
            timer1.cancel();
            timer1 = null;
            Platform.runLater(() -> labelTimer1.setText("S-a oprit"));

            Image staticFrame = new Image(getClass().getResourceAsStream("static.png"));
            caruselView.setImage(staticFrame);
        }
    }

// ===== Timer 2 – Carusel GIF =====
@FXML
public void startTimer2() {
    if (timer2 == null) {
        // Ascunde artificiile la început
        fireworksView.setVisible(false);
        
        timer2 = new Timer();
        final int[] counter = {0};

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    counter[0] = (counter[0] % 3) + 1;
                    labelTimer2.setText("Runda a început" + ".".repeat(counter[0]));
                });
            }
        };

        timer2.scheduleAtFixedRate(task, 0, 1000);

        // Timer separat pentru oprire după 5 secunde și afișare artificii
        Timer stopTimer = new Timer();
        stopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Oprește timer2
                if (timer2 != null) {
                    timer2.cancel();
                    timer2 = null;
                }
                
                Platform.runLater(() -> {
                    labelTimer2.setText("5 secunde au trecut!");
                    
                    try {
                        // Încearcă să încarci GIF-ul artificiilor
                        Image fireworksGif = new Image(getClass().getResourceAsStream("Fireworks.gif"));
                        
                        if (fireworksGif.isError()) {
                            System.err.println("Eroare la încărcarea fireworks.gif");
                            labelTimer2.setText("Eroare: fireworks.gif nu a fost găsit!");
                        } else {
                            // Setează și afișează artificiile
                            fireworksView.setImage(fireworksGif);
                            fireworksView.setVisible(true);
                            
                            // Debug - confirmă că artificiile sunt afișate
                            System.out.println("Artificiile sunt afișate!");
                            
                            // Ascunde artificiile după 3 secunde
                            Timer hideTimer = new Timer();
                            hideTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Platform.runLater(() -> {
                                        fireworksView.setVisible(false);
                                        System.out.println("Artificiile sunt ascunse!");
                                    });
                                }
                            }, 3000);
                        }
                    } catch (Exception e) {
                        System.err.println("Excepție la încărcarea artificiilor: " + e.getMessage());
                        labelTimer2.setText("Eroare la încărcarea artificiilor!");
                    }
                });
            }
        }, 5000);

    } else {
        // Oprire manuală
        timer2.cancel();
        timer2 = null;
        Platform.runLater(() -> {
            labelTimer2.setText("Timer 2 oprit");
            fireworksView.setVisible(false);
        });
    }
}




    // ===== Timer 3 – La o oră specificată =====
    @FXML
    public void startTimer3() {
        String input = timeInput.getText().trim();

        if (input.isEmpty()) {
            labelTimer3.setText("Introdu o oră (HH:mm, ex: 18:30)");
            return;
        }

        try {
            long delay = getDelayUntil(input);

            if (timer3 == null) {
                timer3 = new Timer();
                timer3.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            labelTimer3.setText("E seară deja " + input + "!");
                            Image nightGif = new Image(getClass().getResourceAsStream("Night.gif"));
                            caruselView.setImage(nightGif);
                            caruselView.setStyle("-fx-background-color: black;");
                        });
                    }
                }, delay);

                labelTimer3.setText("Se va însera la ora " + input);

            } else {
                timer3.cancel();
                timer3 = null;
                Platform.runLater(() -> {
                    labelTimer3.setText("Timer oprit");
                    Image staticImg = new Image(getClass().getResourceAsStream("static.png"));
                    caruselView.setImage(staticImg);
                    caruselView.setStyle("");
                });
            }

        } catch (Exception e) {
            labelTimer3.setText("Aceea nu este o oră! Folosește HH:mm");
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
            targetDateTime = targetDateTime.plusDays(1);
        }

        return Duration.between(now, targetDateTime).toMillis();
    }

    // ===== Timer 4 – Joules consumați =====
    @FXML
    public void startTimer4() {
        if (timer4 == null) {
            timer4 = new Timer();
            timer4.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() ->
                            labelTimer4.setText("Joules (J) consumați : " + (System.currentTimeMillis() / 1000) + "s"));
                }
            }, 2000, 3000);
        } else {
            timer4.cancel();
            timer4 = null;
            Platform.runLater(() -> labelTimer4.setText("A fost inutil?"));
        }
    }

    public void shutdown() {
        if (timer1 != null) timer1.cancel();
        if (timer2 != null) timer2.cancel();
        if (timer3 != null) timer3.cancel();
        if (timer4 != null) timer4.cancel();
    }
}
