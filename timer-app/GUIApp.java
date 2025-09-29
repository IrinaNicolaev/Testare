import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.sound.sampled.*;
import javax.swing.*;

public class GUIApp {

    static final JLabel statusLabel1 = new JLabel("Status 1: Idle");
    static final JLabel statusLabel2 = new JLabel("Status 2: Idle");
    static final JLabel statusLabel3 = new JLabel("Status 3: Idle");

    public static void main(String[] args) {
        JFrame frame = new JFrame("Thread and Timer Status");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel(new GridLayout(5, 1));
        JButton startButton = new JButton("Start All Threads");

        panel.add(startButton);
        panel.add(statusLabel1);
        panel.add(statusLabel2);
        panel.add(statusLabel3);

        startButton.addActionListener((ActionEvent e) -> {
            startButton.setEnabled(false);

            // Поток для воспроизведения звука
            Thread soundThread = new Thread(() -> playSound("sound.wav"));

            // Поток Message
            Thread messageThread = new Thread(new Message("Message received!"));

            // Третий поток (анонимный)
            Thread thirdThread = new Thread(() -> {
                SwingUtilities.invokeLater(() -> statusLabel3.setText("Thread 3: Working..."));
                try { Thread.sleep(3000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                SwingUtilities.invokeLater(() -> statusLabel3.setText("Thread 3: Finished."));
            });

            soundThread.start();
            messageThread.start();
            thirdThread.start();
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    // Метод для воспроизведения звука
    private static void playSound(String fileName) {
        SwingUtilities.invokeLater(() -> statusLabel1.setText("Thread 1: Playing sound..."));
        try {
            File soundFile = new File(fileName); // укажите путь к вашему .wav файлу
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

            // Ждем, пока звук не закончится
            Thread.sleep(clip.getMicrosecondLength() / 1000);

        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> statusLabel1.setText("Thread 1: Error: " + ex.getMessage()));
        }
        SwingUtilities.invokeLater(() -> statusLabel1.setText("Thread 1: Finished."));
    }
}

// Класс Message для второго потока
class Message implements Runnable {
    private final String message;
    public Message(String message) { this.message = message; }
    @Override
    public void run() {
        System.out.println("Message thread: " + message);
        SwingUtilities.invokeLater(() -> GUIApp.statusLabel2.setText("Thread 2: " + message));
    }
}
