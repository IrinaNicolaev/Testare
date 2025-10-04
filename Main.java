import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;
import java.util.Timer;
import java.util.Calendar;
import java.util.TimerTask;
import java.util.ArrayList;

// --- Partea 1: Modificarea clasei TaskExecutor pentru a gestiona numărul de execuții ---
class TaskExecutor extends TimerTask {
    private String taskName;
    private int executions;
    private int maxExecutions;
    private Timer timer;
    private TimerGUI gui; // Singura adăugare pentru GUI

    public TaskExecutor(String taskName, int maxExecutions, Timer timer) {
        this(taskName, maxExecutions, timer, null);
    }

    public TaskExecutor(String taskName, int maxExecutions, Timer timer, TimerGUI gui) {
        this.taskName = taskName;
        this.executions = 0;
        this.maxExecutions = maxExecutions;
        this.timer = timer;
        this.gui = gui;
    }

    @Override
    public void run() {
        if (executions < maxExecutions) {
            executions++;
            String message = "S-a executat sarcina: " + taskName + " (Execuția " + executions + ") la: " + new java.util.Date();
            System.out.println(message);
            if (gui != null) {
                SwingUtilities.invokeLater(() -> {
                    gui.addLogMessage(message);
                    gui.notifyTimerFinished();
                });
            }
            this.cancel(); // Anulează doar această sarcină
            timer.cancel(); // Anulează și temporizatorul
        }
    }
}

// --- Stopwatch Executor (runs indefinitely) ---
class StopwatchExecutor extends TimerTask {
    private String taskName;
    private Timer timer;
    private TimerGUI gui;
    private long startTime;

    public StopwatchExecutor(String taskName, Timer timer, TimerGUI gui) {
        this.taskName = taskName;
        this.timer = timer;
        this.gui = gui;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        // Just keep running - will be canceled manually
    }
}

// --- Partea 2: Noua clasă TaskManager ---
class TaskManager {
    public static void scheduleOneShotTimer(long delay) {
        scheduleOneShotTimer(delay, null);
    }

    public static Timer scheduleOneShotTimer(long delay, TimerGUI gui) {
        Timer timer = new Timer();
        StopwatchExecutor task = new StopwatchExecutor("Temporizator cronometru", timer, gui);
        String message = "Cronometrul a pornit.";
        System.out.println(message);
        if (gui != null) gui.addLogMessage(message);
        timer.scheduleAtFixedRate(task, 0, 1000); // Update every second
        return timer;
    }

    public static void scheduleSpecificTimeTimer(int secondsFromNow) {
        scheduleSpecificTimeTimer(secondsFromNow, null);
    }

    public static Timer scheduleSpecificTimeTimer(int secondsFromNow, TimerGUI gui) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + secondsFromNow);
        Date specificTime = calendar.getTime();
        TaskExecutor task = new TaskExecutor("Temporizator cu dată specifică", 1, timer, gui);
        String message = "Temporizatorul cu dată specifică va porni la: " + specificTime;
        System.out.println(message);
        if (gui != null) gui.addLogMessage(message);
        timer.schedule(task, specificTime);
        return timer;
    }

    public static void scheduleRepetitiveTimer(long initialDelay, long period, int maxExecutions) {
        scheduleRepetitiveTimer(initialDelay, period, maxExecutions, null);
    }

    public static Timer scheduleRepetitiveTimer(long initialDelay, long period, int maxExecutions, TimerGUI gui) {
        Timer timer = new Timer();
        RepetitiveTaskExecutor task = new RepetitiveTaskExecutor("Temporizator repetitiv", maxExecutions, timer, gui, period);
        String message = "Temporizatorul repetitiv va porni și se va repeta la fiecare " + period / 1000 + " secunde, de " + maxExecutions + " ori.";
        System.out.println(message);
        if (gui != null) gui.addLogMessage(message);
        timer.scheduleAtFixedRate(task, period, period); // Start after first period, not immediately
        return timer;
    }
}

// --- Special executor for repetitive timers ---
class RepetitiveTaskExecutor extends TimerTask {
    private String taskName;
    private int executions;
    private int maxExecutions;
    private Timer timer;
    private TimerGUI gui;
    private long period;

    public RepetitiveTaskExecutor(String taskName, int maxExecutions, Timer timer, TimerGUI gui, long period) {
        this.taskName = taskName;
        this.executions = 0;
        this.maxExecutions = maxExecutions;
        this.timer = timer;
        this.gui = gui;
        this.period = period;
    }

    @Override
    public void run() {
        executions++;
        String message = "S-a executat sarcina: " + taskName + " (Execuția " + executions + "/" + maxExecutions + ") la: " + new Date();
        System.out.println(message);
        
        if (gui != null) {
            int remaining = maxExecutions - executions;
            SwingUtilities.invokeLater(() -> {
                gui.addLogMessage(message);
            });
        }
        
        if (executions >= maxExecutions) {
            // All iterations complete - show success
            String finishMessage = "Sarcina '" + taskName + "' a atins numărul maxim de execuții (" + maxExecutions + ") și va fi anulată.";
            System.out.println(finishMessage);
            if (gui != null) {
                SwingUtilities.invokeLater(() -> {
                    gui.addLogMessage(finishMessage);
                    gui.notifyTimerFinished();
                });
            }
            this.cancel();
            timer.cancel();
        } else {
            // More iterations remaining - show waiting state
            if (gui != null) {
                int remaining = maxExecutions - executions;
                SwingUtilities.invokeLater(() -> {
                    gui.notifyTimerIteration(remaining);
                });
            }
        }
    }
}

// --- Clock Animation Panel ---
class ClockPanel extends JPanel {
    private long startTime;
    private long duration;
    private boolean isRunning;
    private boolean isFinished;
    private boolean isStopwatch;
    private boolean isWaiting;
    private Timer animationTimer;
    private int pulsePhase;
    private int remainingIterations;
    private long iterationDuration;

    public ClockPanel() {
        setPreferredSize(new Dimension(250, 250));
        setBorder(new TitledBorder("Vizualizare Temporizator"));
        isRunning = false;
        isFinished = false;
        isStopwatch = false;
        isWaiting = false;
        pulsePhase = 0;
        remainingIterations = 0;
        
        animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isRunning || isFinished || isStopwatch) {
                    pulsePhase = (pulsePhase + 1) % 60;
                    repaint();
                }
            }
        }, 0, 50);
    }

    public void startStopwatch() {
        this.startTime = System.currentTimeMillis();
        this.isStopwatch = true;
        this.isRunning = false;
        this.isFinished = false;
        this.isWaiting = false;
        repaint();
    }

    public void startTimer(long durationMs) {
        this.startTime = System.currentTimeMillis();
        this.duration = durationMs;
        this.isRunning = true;
        this.isFinished = false;
        this.isStopwatch = false;
        this.isWaiting = false;
        this.remainingIterations = 0;
        repaint();
    }

    public void startRepetitiveTimer(long iterationDurationMs, int totalIterations) {
        this.startTime = System.currentTimeMillis();
        this.iterationDuration = iterationDurationMs;
        this.duration = iterationDurationMs;
        this.isRunning = true;
        this.isFinished = false;
        this.isStopwatch = false;
        this.isWaiting = false;
        this.remainingIterations = totalIterations;
        repaint();
    }

    public void notifyIteration(int remaining) {
        this.remainingIterations = remaining;
        if (remaining > 0) {
            this.isWaiting = true;
            this.isRunning = false;
            repaint();
            
            // Wait briefly then restart the running clock for next iteration
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        isWaiting = false;
                        isRunning = true;
                        startTime = System.currentTimeMillis();
                        duration = iterationDuration;
                        repaint();
                    });
                }
            }, 1000); // Wait 1 second between iterations
        }
    }

    public void finishTimer() {
        this.isRunning = false;
        this.isStopwatch = false;
        this.isFinished = true;
        this.isWaiting = false;
        repaint();
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                isFinished = false;
                SwingUtilities.invokeLater(() -> repaint());
            }
        }, 3000);
    }

    public void resetTimer() {
        this.isRunning = false;
        this.isFinished = false;
        this.isStopwatch = false;
        this.isWaiting = false;
        this.remainingIterations = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 30;

        if (isFinished) {
            drawFinishedAnimation(g2d, centerX, centerY, radius);
        } else if (isStopwatch) {
            drawStopwatch(g2d, centerX, centerY, radius);
        } else if (isWaiting) {
            drawWaitingState(g2d, centerX, centerY, radius);
        } else if (isRunning) {
            drawRunningClock(g2d, centerX, centerY, radius);
        } else {
            drawIdleClock(g2d, centerX, centerY, radius);
        }
    }

    private void drawStopwatch(Graphics2D g2d, int centerX, int centerY, int radius) {
        long elapsed = System.currentTimeMillis() - startTime;
        
        g2d.setColor(new Color(100, 200, 255));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int x1 = centerX + (int) ((radius - 10) * Math.cos(angle));
            int y1 = centerY + (int) ((radius - 10) * Math.sin(angle));
            int x2 = centerX + (int) ((radius - 20) * Math.cos(angle));
            int y2 = centerY + (int) ((radius - 20) * Math.sin(angle));
            g2d.setColor(new Color(80, 160, 255));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x1, y1, x2, y2);
        }

        double handAngle = Math.toRadians((elapsed / 1000.0 * 6) - 90);
        int handLength = radius - 30;
        int handX = centerX + (int) (handLength * Math.cos(handAngle));
        int handY = centerY + (int) (handLength * Math.sin(handAngle));
        g2d.setColor(new Color(255, 150, 50));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, handX, handY);

        g2d.setColor(new Color(255, 150, 50));
        g2d.fillOval(centerX - 6, centerY - 6, 12, 12);

        long elapsedSec = elapsed / 1000;
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(50, 50, 50));
        String timeText = String.format("%d:%02d", elapsedSec / 60, elapsedSec % 60);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeText);
        g2d.drawString(timeText, centerX - textWidth / 2, centerY + 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String statusText = "Cronometru activ...";
        textWidth = g2d.getFontMetrics().stringWidth(statusText);
        g2d.drawString(statusText, centerX - textWidth / 2, centerY + radius + 25);
    }

    private void drawWaitingState(Graphics2D g2d, int centerX, int centerY, int radius) {
        float pulse = (float) Math.sin(pulsePhase * 0.3) * 0.5f + 0.5f;
        
        g2d.setColor(new Color(255, 200, 100, (int) (100 * pulse)));
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        g2d.setColor(new Color(255, 180, 50));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int x1 = centerX + (int) ((radius - 10) * Math.cos(angle));
            int y1 = centerY + (int) ((radius - 10) * Math.sin(angle));
            int x2 = centerX + (int) ((radius - 20) * Math.cos(angle));
            int y2 = centerY + (int) ((radius - 20) * Math.sin(angle));
            g2d.setColor(new Color(200, 150, 50));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(new Color(200, 120, 0));
        String text = "Așteptare...";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, centerX - textWidth / 2, centerY);

        if (remainingIterations > 0) {
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String iterText = remainingIterations + " cicluri rămase";
            textWidth = g2d.getFontMetrics().stringWidth(iterText);
            g2d.drawString(iterText, centerX - textWidth / 2, centerY + 25);
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String statusText = "Interval între cicluri";
        textWidth = g2d.getFontMetrics().stringWidth(statusText);
        g2d.drawString(statusText, centerX - textWidth / 2, centerY + radius + 25);
    }

    private void drawIdleClock(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        g2d.setColor(new Color(150, 150, 150));
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int x1 = centerX + (int) ((radius - 10) * Math.cos(angle));
            int y1 = centerY + (int) ((radius - 10) * Math.sin(angle));
            int x2 = centerX + (int) ((radius - 20) * Math.cos(angle));
            int y2 = centerY + (int) ((radius - 20) * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(100, 100, 100));
        String text = "Așteptare...";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, centerX - textWidth / 2, centerY + radius + 25);
    }

    private void drawRunningClock(Graphics2D g2d, int centerX, int centerY, int radius) {
        long elapsed = System.currentTimeMillis() - startTime;
        double progress = Math.min(1.0, (double) elapsed / duration);

        g2d.setColor(new Color(100, 150, 255));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        g2d.setColor(new Color(100, 200, 100, 100));
        int arcAngle = (int) (360 * progress);
        g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -arcAngle);

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int x1 = centerX + (int) ((radius - 10) * Math.cos(angle));
            int y1 = centerY + (int) ((radius - 10) * Math.sin(angle));
            int x2 = centerX + (int) ((radius - 20) * Math.cos(angle));
            int y2 = centerY + (int) ((radius - 20) * Math.sin(angle));
            g2d.setColor(new Color(80, 120, 200));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x1, y1, x2, y2);
        }

        double handAngle = Math.toRadians(360 * progress - 90);
        int handLength = radius - 30;
        int handX = centerX + (int) (handLength * Math.cos(handAngle));
        int handY = centerY + (int) (handLength * Math.sin(handAngle));
        g2d.setColor(new Color(255, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, handX, handY);

        g2d.setColor(new Color(255, 100, 100));
        g2d.fillOval(centerX - 6, centerY - 6, 12, 12);

        long remainingMs = Math.max(0, duration - elapsed);
        long remainingSec = remainingMs / 1000;
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(50, 50, 50));
        String timeText = String.format("%d:%02d", remainingSec / 60, remainingSec % 60);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeText);
        g2d.drawString(timeText, centerX - textWidth / 2, centerY + 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String statusText = "În execuție...";
        textWidth = g2d.getFontMetrics().stringWidth(statusText);
        g2d.drawString(statusText, centerX - textWidth / 2, centerY + radius + 25);
    }

    private void drawFinishedAnimation(Graphics2D g2d, int centerX, int centerY, int radius) {
        float pulse = (float) Math.sin(pulsePhase * 0.2) * 0.5f + 0.5f;
        int pulseRadius = radius + (int) (pulse * 20);

        g2d.setColor(new Color(100, 255, 100, (int) (150 * pulse)));
        g2d.fillOval(centerX - pulseRadius, centerY - pulseRadius, pulseRadius * 2, pulseRadius * 2);

        g2d.setColor(new Color(50, 200, 50));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        int checkSize = radius / 2;
        g2d.setColor(new Color(50, 200, 50));
        g2d.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int[] xPoints = {centerX - checkSize / 2, centerX - checkSize / 6, centerX + checkSize / 2};
        int[] yPoints = {centerY, centerY + checkSize / 2, centerY - checkSize / 2};
        for (int i = 0; i < xPoints.length - 1; i++) {
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(50, 150, 50));
        String text = "Finalizat!";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, centerX - textWidth / 2, centerY + radius + 25);
    }
}

// --- GUI Class ---
class TimerGUI extends JFrame {
    private JTextArea logArea;
    private ArrayList<Timer> activeTimers;
    private ClockPanel clockPanel;

    public TimerGUI() {
        activeTimers = new ArrayList<>();
        setTitle("Aplicație Timer");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 1, 10, 10));
        controlPanel.setBorder(new TitledBorder("Tipuri de Temporizatoare"));

        JButton oneShotBtn = new JButton("Cronometru (Stopwatch)");
        oneShotBtn.addActionListener(e -> createOneShotTimer());
        controlPanel.add(oneShotBtn);

        JButton specificTimeBtn = new JButton("Temporizator Simplu");
        specificTimeBtn.addActionListener(e -> createSpecificTimeTimer());
        controlPanel.add(specificTimeBtn);

        JButton repetitiveBtn = new JButton("Temporizator Repetitiv");
        repetitiveBtn.addActionListener(e -> createRepetitiveTimer());
        controlPanel.add(repetitiveBtn);

        leftPanel.add(controlPanel, BorderLayout.NORTH);

        clockPanel = new ClockPanel();
        leftPanel.add(clockPanel, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Jurnal de Activitate"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearBtn = new JButton("Șterge Jurnal");
        clearBtn.addActionListener(e -> logArea.setText(""));
        JButton cancelAllBtn = new JButton("Anulează Toate Temporizatoarele");
        cancelAllBtn.addActionListener(e -> cancelAllTimers());
        bottomPanel.add(clearBtn);
        bottomPanel.add(cancelAllBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        addLogMessage("Aplicația a pornit. Selectați un tip de temporizator pentru a începe.");
    }

    private void createOneShotTimer() {
        int result = JOptionPane.showConfirmDialog(this, 
            "Pornește cronometrul?\n(Va măsura timpul până la oprire)", 
            "Cronometru", 
            JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            clockPanel.startStopwatch();
            Timer timer = TaskManager.scheduleOneShotTimer(0, this);
            activeTimers.add(timer);
        }
    }

    private void createSpecificTimeTimer() {
        String input = JOptionPane.showInputDialog(this, "Introduceți timpul (secunde de acum):", "10");
        if (input != null) {
            try {
                int seconds = Integer.parseInt(input);
                clockPanel.startTimer(seconds * 1000);
                Timer timer = TaskManager.scheduleSpecificTimeTimer(seconds, this);
                activeTimers.add(timer);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vă rugăm introduceți un număr valid.");
            }
        }
    }

    private void createRepetitiveTimer() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField periodField = new JTextField("5");
        JTextField executionsField = new JTextField("3");
        
        panel.add(new JLabel("Durată ciclu (secunde):"));
        panel.add(periodField);
        panel.add(new JLabel("Număr de cicluri:"));
        panel.add(executionsField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Temporizator Repetitiv", 
                                                   JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                long period = Long.parseLong(periodField.getText()) * 1000;
                int executions = Integer.parseInt(executionsField.getText());
                clockPanel.startRepetitiveTimer(period, executions);
                Timer timer = TaskManager.scheduleRepetitiveTimer(0, period, executions, this);
                activeTimers.add(timer);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vă rugăm introduceți numere valide.");
            }
        }
    }

    private void cancelAllTimers() {
        for (Timer timer : activeTimers) {
            timer.cancel();
        }
        activeTimers.clear();
        clockPanel.resetTimer();
        addLogMessage("Toate temporizatoarele active au fost anulate.");
    }

    public void addLogMessage(String message) {
        logArea.append("[" + new Date() + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void notifyTimerFinished() {
        SwingUtilities.invokeLater(() -> clockPanel.finishTimer());
    }

    public void notifyTimerIteration(int remainingIterations) {
        SwingUtilities.invokeLater(() -> clockPanel.notifyIteration(remainingIterations));
    }
}

// --- Clasa Main actualizată (o versiune mai curată) ---
public class Main {
    public static void main(String[] args) {
        // Verifică dacă se rulează în modul GUI sau CLI
        if (args.length > 0 && args[0].equals("--cli")) {
            // Apelurile către managerul de sarcini (modul original CLI)
            TaskManager.scheduleOneShotTimer(5000); // Pornire peste 5 secunde
            TaskManager.scheduleSpecificTimeTimer(10); // Pornire peste 10 secunde
            TaskManager.scheduleRepetitiveTimer(15000, 3000, 3); // Pornire peste 15 secunde, repetare la 3s, de 3 ori
        } else {
            // Lansează GUI
            SwingUtilities.invokeLater(() -> {
                TimerGUI gui = new TimerGUI();
                gui.setVisible(true);
            });
        }
    }
}