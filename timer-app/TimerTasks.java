// Файл: TimerTasks.java 

import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

// Класс для звука и обновления статуса
class SoundPlayer implements Runnable {
    private JLabel statusLabel;

    public SoundPlayer(JLabel label) {
        this.statusLabel = label;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            final int count = i;
            Toolkit.getDefaultToolkit().beep();
            System.out.println("Thread 1: Sound played, count: " + count);

            // !!! БЕЗОПАСНОЕ ОБНОВЛЕНИЕ GUI !!!
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Thread 1 (Sound): Playing... (" + count + "/5)");
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ...
            }
        }
        SwingUtilities.invokeLater(() -> statusLabel.setText("Thread 1 (Sound): FINISHED."));
    }
}

// Класс для сообщения и обновления статуса
class Message implements Runnable {
    private String msg;
    private JLabel statusLabel;

    public Message(String msg, JLabel label) {
        this.msg = msg;
        this.statusLabel = label;
    }

    @Override
    public void run() {
        System.out.println("Thread 2: " + msg);
        SwingUtilities.invokeLater(() -> statusLabel.setText("Thread 2 (Message): " + msg));
    }
}