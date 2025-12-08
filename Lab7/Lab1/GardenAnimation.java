
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class GardenAnimation extends JFrame {

    private final GardenPanel gardenPanel;
    private Timer growthTimer, sunTimer, rainTimer, rainCycleTimer;

    public GardenAnimation() {
        setTitle("Garden Simulation");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gardenPanel = new GardenPanel();
        add(gardenPanel, BorderLayout.CENTER);

        // Panou de control
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(245, 245, 245));

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");

        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));

        controlPanel.add(new JLabel("Control Timere:"));
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        add(controlPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startSimulation());
        stopButton.addActionListener(e -> stopSimulation());
    }

    private void startSimulation() {
        stopSimulation();

        // Interval fix → planta crește la fiecare 5 secunde
        growthTimer = new Timer();
        growthTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gardenPanel.growPlant();
            }
        }, 0, 5000);

        //Timp fix → soarele apare la următorul minut rotund
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        Date fixedTime = calendar.getTime();

        sunTimer = new Timer();
        sunTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                gardenPanel.showSun();
            }
        }, fixedTime);

        // Perioadă → ploaia apare ciclic: la fiecare 30 secunde timp de 5 execuții
        rainCycleTimer = new Timer();
        rainCycleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // pentru fiecare ciclu de 30s, creăm un nou timer de ploaie
                rainTimer = new Timer();
                rainTimer.scheduleAtFixedRate(new TimerTask() {
                    int counter = 0;
                    @Override
                    public void run() {
                        counter++;
                        gardenPanel.showRain(counter);
                        if (counter >= 5) {
                            this.cancel(); // oprește ploaia după 5 execuții
                        }
                    }
                }, 0, 2000); // ploaie la fiecare 2s
            }
        }, 0, 30000); // pornește o serie de ploaie la fiecare 30s
    }

    private void stopSimulation() {
        if (growthTimer != null) growthTimer.cancel();
        if (sunTimer != null) sunTimer.cancel();
        if (rainTimer != null) rainTimer.cancel();
        if (rainCycleTimer != null) rainCycleTimer.cancel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GardenAnimation().setVisible(true));
    }
}

// Panoul grafic
class GardenPanel extends JPanel {
    private int plantHeight = 60;
    private boolean sunVisible = false;
    private boolean raining = false;

    public void growPlant() {
        plantHeight += 10;
        repaint();
    }

    public void showSun() {
        sunVisible = true;
        repaint();
    }

    public void showRain(int step) {
        raining = true;
        repaint();
        if (step >= 5) {
            raining = false;
            repaint();
        }
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

        // Plantă
        g.setColor(new Color(0, 160, 0));
        g.fillRect(getWidth() / 2 - 5, getHeight() - 120 - plantHeight, 10, plantHeight); // tulpină

        // Frunze
        g.setColor(new Color(0, 200, 0));
        g.fillOval(getWidth() / 2 - 40, getHeight() - 150 - plantHeight, 40, 30);
        g.fillOval(getWidth() / 2, getHeight() - 150 - plantHeight, 40, 30);

        // Floare cu petale
        g.setColor(Color.PINK);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int x = getWidth() / 2 + (int)(25 * Math.cos(angle));
            int y = getHeight() - 160 - plantHeight + (int)(25 * Math.sin(angle));
            g.fillOval(x - 15, y - 15, 30, 30);
        }
        g.setColor(Color.YELLOW);
        g.fillOval(getWidth() / 2 - 15, getHeight() - 160 - plantHeight - 15, 30, 30);

        // Soarele
        if (sunVisible) {
            g.setColor(Color.YELLOW);
            g.fillOval(getWidth() - 150, 50, 100, 100);
            for (int i = 0; i < 12; i++) {
                double angle = Math.toRadians(i * 30);
                int x1 = getWidth() - 100 + (int)(70 * Math.cos(angle));
                int y1 = 100 + (int)(70 * Math.sin(angle));
                g.drawLine(getWidth() - 100, 100, x1, y1);
            }
        }

        // Nori + Ploaie
        if (raining) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillOval(150, 50, 120, 70);
            g.fillOval(200, 30, 150, 80);
            g.fillOval(270, 50, 120, 70);

            g.setColor(new Color(50, 150, 255));
            for (int i = 0; i < 20; i++) {
                int x = 180 + (int) (Math.random() * 120);
                int y = 120 + (int) (Math.random() * 250);
                g.fillRoundRect(x, y, 4, 12, 4, 8);
            }
        }
    }
}