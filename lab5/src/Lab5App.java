import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Lab5App extends JFrame {

    // Calea către Edge
    private static final String EDGE_PATH =
            "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";

    // URL-urile pentru Edge
    private static final String[] URLS = {
            "https://www.google.com",
            "https://utm.md",
            "https://educatieonline.md"
    };

    // == COMPONENTE GUI ==
    private JTextArea logArea;

    // Browser
    private JButton startBrowserBtn;
    private JButton stopBrowserBtn;
    private JLabel browserStatusLabel;
    private JLabel timerLabel;
    private Timer browserTimer; // Swing Timer (1 sec)
    private int secondsRemaining; // 10 minute = 600 secunde

    public Lab5App() {
        super("Lab 5 - Funcții de nivel înalt ale SO");

        initGui();
        initWindowListener();

        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initGui() {
        setLayout(new BorderLayout());

        // == PANOU DE SUS: doar browser ==
        JPanel topPanel = new JPanel(new GridLayout(1, 1, 10, 10));

        // -- Panel browser --
        JPanel browserPanel = new JPanel();
        browserPanel.setBorder(BorderFactory.createTitledBorder("Control browser (Edge)"));
        browserPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(5, 5, 5, 5);
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.gridx = 0; gbc1.gridy = 0; gbc1.gridwidth = 2;

        startBrowserBtn = new JButton("Pornește Edge (10 min)");
        stopBrowserBtn = new JButton("Oprește Edge");
        stopBrowserBtn.setEnabled(false);

        browserStatusLabel = new JLabel("Status: oprit");
        timerLabel = new JLabel("Timer: 00:00");

        // Butoane
        browserPanel.add(startBrowserBtn, gbc1);
        gbc1.gridy++;
        browserPanel.add(stopBrowserBtn, gbc1);
        gbc1.gridy++;

        // Status + timer
        gbc1.gridwidth = 1;
        browserPanel.add(browserStatusLabel, gbc1);
        gbc1.gridx = 1;
        browserPanel.add(timerLabel, gbc1);

        topPanel.add(browserPanel);

        add(topPanel, BorderLayout.NORTH);

        // == ZONA DE LOG ==
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Log aplicație"));

        add(scrollPane, BorderLayout.CENTER);

        // == ASOCIERE BUTOANE ==
        attachActions();
    }

    private void initWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Oprim timer-ul browserului înainte de închidere
                if (browserTimer != null && browserTimer.isRunning()) {
                    browserTimer.stop();
                }
            }
        });
    }

    private void attachActions() {
        // == BROWSER ==
        startBrowserBtn.addActionListener(e -> startBrowserWithTimer());
        stopBrowserBtn.addActionListener(e -> stopBrowserManual());
    }

    // == LOGICĂ BROWSER ==
    private void startBrowserWithTimer() {
        try {
            // pornește Edge cu tab-uri
            ProcessBuilder pb = new ProcessBuilder();
            pb.command().add(EDGE_PATH);
            for (String url : URLS) {
                pb.command().add(url);
            }
            pb.start();

            log("Browser Edge a fost pornit.");
            browserStatusLabel.setText("Status: pornit");

            startBrowserBtn.setEnabled(false);
            stopBrowserBtn.setEnabled(true);

            // 10 minute = 600 secunde
            secondsRemaining = 10 * 60;
            updateTimerLabel();

            browserTimer = new Timer(1000, e -> {
                secondsRemaining--;
                updateTimerLabel();
                if (secondsRemaining <= 0) {
                    ((Timer) e.getSource()).stop();
                    stopBrowserAuto();
                }
            });
            browserTimer.start();

        } catch (IOException ex) {
            log("Eroare la pornirea browserului: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateTimerLabel() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;
        timerLabel.setText(String.format("Timer: %02d:%02d", minutes, seconds));
    }

    private void stopBrowserManual() {
        if (browserTimer != null && browserTimer.isRunning()) {
            browserTimer.stop();
        }
        stopBrowserProcess("manual");
    }

    private void stopBrowserAuto() {
        stopBrowserProcess("automat (10 minute)");
    }

    private void stopBrowserProcess(String reason) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd", "/c", "taskkill /IM msedge.exe /F /T"
            );
            pb.inheritIO();
            pb.start();

            log("Browser Edge a fost închis " + reason + ".");
            browserStatusLabel.setText("Status: oprit");
            startBrowserBtn.setEnabled(true);
            stopBrowserBtn.setEnabled(false);

        } catch (IOException ex) {
            log("Eroare la închiderea browserului: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // == LOG GENERAL (doar în GUI) ==
    private synchronized void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // == MAIN ==
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Lab5App app = new Lab5App();
            app.setVisible(true);
        });
    }
}