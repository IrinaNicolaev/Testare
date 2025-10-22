package com.example.lab1so;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class TimerFixedRateSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimerFixedRateSwing::createAndShowUI);
    }

    private static void createAndShowUI() {
        JFrame frame = new JFrame("Timer FixedRate Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel label = new JLabel("Ticks: 0", SwingConstants.CENTER);
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(0);
        progress.setStringPainted(true);

        JButton startBtn = new JButton("Start");
        JButton stopBtn = new JButton("Stop");
        stopBtn.setEnabled(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel controls = new JPanel(new FlowLayout());
        controls.add(startBtn);
        controls.add(stopBtn);
        panel.add(label, BorderLayout.NORTH);
        panel.add(progress, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setSize(360, 180);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Timer[] timerRef = new Timer[1];

        startBtn.addActionListener(e -> {
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);

            timerRef[0] = new Timer("fixed-rate", true);
            final long delayMs = 1000L;
            final long periodMs = 500L;

            TimerTask task = new TimerTask() {
                int ticks = 0;
                @Override
                public void run() {
                    ticks++;
                    // Simulează ocazional lucru mai lung pentru a vedea compensarea
                    if (ticks % 5 == 0) {
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    }
                    final int v = Math.min(ticks, 100);
                    SwingUtilities.invokeLater(() -> {
                        label.setText("Ticks: " + v);
                        progress.setValue(Math.min(progress.getValue() + 1, 100));
                    });
                    if (v >= 100 || progress.getValue() >= 100) {
                        System.out.println("[FixedRateUI] Reached 100 -> cancel");
                        cancel();
                        timerRef[0].cancel();
                        SwingUtilities.invokeLater(() -> {
                            startBtn.setEnabled(true);
                            stopBtn.setEnabled(false);
                        });
                    }
                }
            };

            // Rată fixă: încearcă să mențină period-ul relativ la ceas
            timerRef[0].scheduleAtFixedRate(task, delayMs, periodMs);
        });

        stopBtn.addActionListener(e -> {
            if (timerRef[0] != null) {
                timerRef[0].cancel();
                timerRef[0] = null;
            }
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
    }
}
