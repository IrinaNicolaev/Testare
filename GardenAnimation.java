import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class GardenAnimation extends JFrame {

    private GardenPanel gardenPanel;
    private Timer growthTimer;

    public GardenAnimation() {
        setTitle("🌱 Garden Animation");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gardenPanel = new GardenPanel();
        add(gardenPanel, BorderLayout.CENTER);

        // Panou de control
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(245, 245, 245));

        JButton startButton = new JButton("▶️ Start");
        JButton stopButton = new JButton("⏸️ Stop");

        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));

        controlPanel.add(new JLabel("Control Timer:"));
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        add(controlPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
    }

    private void startSimulation() {
        stopSimulation();

        // 1️⃣ Interval fix → planta crește la fiecare 5 secunde
        growthTimer = new Timer();
        growthTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gardenPanel.growPlant();
            }
        }, 0, 5000);
    }

    private void stopSimulation() {
        if (growthTimer != null) growthTimer.cancel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GardenAnimation().setVisible(true));
    }
}

// Panoul grafic (plantă + floare originală)
class GardenPanel extends JPanel {
    private int plantHeight = 60;

    public void growPlant() {
        plantHeight += 10;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Cer cu gradient
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint sky = new GradientPaint(0, 0, new Color(135, 206, 250), 0, getHeight(), Color.WHITE);
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Pământ
        g.setColor(new Color(110, 70, 40));
        g.fillRect(0, getHeight() - 120, getWidth(), 120);

        // Tulpină
        g.setColor(new Color(0, 160, 0));
        g.fillRect(getWidth() / 2 - 5, getHeight() - 120 - plantHeight, 10, plantHeight);

        // Frunze
        g.setColor(new Color(0, 200, 0));
        g.fillOval(getWidth() / 2 - 40, getHeight() - 150 - plantHeight, 40, 30); // stânga
        g.fillOval(getWidth() / 2, getHeight() - 150 - plantHeight, 40, 30);     // dreapta

        // Floare originală: petale + centru
        g.setColor(Color.PINK);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int x = getWidth() / 2 + (int)(25 * Math.cos(angle));
            int y = getHeight() - 160 - plantHeight + (int)(25 * Math.sin(angle));
            g.fillOval(x - 15, y - 15, 30, 30);
        }
        g.setColor(Color.YELLOW);
        g.fillOval(getWidth() / 2 - 15, getHeight() - 160 - plantHeight - 15, 30, 30);
    }
}
