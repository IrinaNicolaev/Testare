import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public class GUIApp {

    private static JLabel sumLabel = new JLabel("Sum: ?");
    private static AtomicInteger result1 = new AtomicInteger(0);
    private static AtomicInteger result2 = new AtomicInteger(0);
    private static AtomicInteger result3 = new AtomicInteger(0);

    public static void main(String[] args) {
        JFrame frame = new JFrame("User Input Sum Beep");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        JTextField input1 = new JTextField();
        JTextField input2 = new JTextField();
        JTextField input3 = new JTextField();

        JButton calculateButton = new JButton("Calculate Sum");
        JButton resetButton = new JButton("Reset");

        panel.add(new JLabel("Enter number for Thread 1:"));
        panel.add(input1);
        panel.add(new JLabel("Enter number for Thread 2:"));
        panel.add(input2);
        panel.add(new JLabel("Enter number for Thread 3:"));
        panel.add(input3);

        panel.add(calculateButton);
        panel.add(resetButton);

        panel.add(sumLabel);
        panel.add(new JLabel()); // пустая ячейка

        // Кнопка расчёта суммы
        calculateButton.addActionListener(e -> {
            calculateButton.setEnabled(false);
            int num1 = parseInput(input1.getText());
            int num2 = parseInput(input2.getText());
            int num3 = parseInput(input3.getText());

            // Поток 1
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                result1.set(num1);
                checkAndShowSum();
            }).start();

            // Поток 2
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                result2.set(num2);
                checkAndShowSum();
            }).start();

            // Поток 3
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                result3.set(num3);
                checkAndShowSum();
            }).start();
        });

        // Кнопка сброса
        resetButton.addActionListener(e -> {
            input1.setText("");
            input2.setText("");
            input3.setText("");
            sumLabel.setText("Sum: ?");
            result1.set(0);
            result2.set(0);
            result3.set(0);
            calculateButton.setEnabled(true);
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static int parseInput(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void checkAndShowSum() {
        SwingUtilities.invokeLater(() -> {
            int sum = result1.get() + result2.get() + result3.get();
            sumLabel.setText("Sum: " + sum);
            if (sum == 9) {
                Toolkit.getDefaultToolkit().beep();
                System.out.println("Beep! Sum = 9");
            }
        });
    }
}
