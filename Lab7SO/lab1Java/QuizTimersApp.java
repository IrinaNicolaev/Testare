import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class QuizTimersApp {

    // ---- UI ----
    private JFrame frame;
    private JLabel timeLabel;
    private JTextArea questionArea;
    private JLabel indexLabel;
    private JButton answerBtn;
    private JButton nextBtn;
    private JLabel scoreLabel;
    private JButton startBtn;
    private JButton stopBtn;

    // ---- Model / stare ----
    private final List<String> questions = Arrays.asList(
            "1) Ce este o structură de date și dă un exemplu?",
            "2) Diferența dintre thread și proces?",
            "3) Ce face colectorul de gunoi (GC) în Java?",
            "4) Descrie MVP vs MVVM pe scurt.",
            "5) Ce este imutabilitatea și de ce e utilă?"
    );
    private int qIndex = -1;
    private int score = 0;

    // Setări test
    private int testDurationSeconds = 60;  // Timp total de test (ex. 1 minut)
    private int questionPeriodMs = 10_000; // Întrebare nouă la fiecare 10 secunde

    // ---- Timere ----
    private javax.swing.Timer secondCountdownTimer; // (1) Interval fix 1s
    private Timer questionTimer;                    // (2) Perioadă indicată

    // ---- Util ----
    private String formatSeconds(int total) {
        int m = total / 60, s = total % 60;
        return String.format("%02d:%02d", m, s);
    }

    public QuizTimersApp() {
        buildUI();
        wireActions();
    }

    private void buildUI() {
        frame = new JFrame("Quiz Timers — Întrebări + Timp limită");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.weightx = 1.0;

        timeLabel = new JLabel("Timp rămas: —");
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.BOLD, 18f));
        frame.add(timeLabel, c);

        c.gridy++;
        indexLabel = new JLabel("Întrebare: —/—");
        frame.add(indexLabel, c);

        c.gridy++;
        questionArea = new JTextArea(5, 40);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setEditable(false);
        questionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        frame.add(new JScrollPane(questionArea), c);

        c.gridy++;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        answerBtn = new JButton("Răspuns corect (+1)");
        nextBtn = new JButton("Următoarea întrebare");
        btns.add(answerBtn);
        btns.add(nextBtn);
        frame.add(btns, c);

        c.gridy++;
        scoreLabel = new JLabel("Scor: 0");
        frame.add(scoreLabel, c);

        c.gridy++;
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        startBtn = new JButton("Start test");
        stopBtn  = new JButton("Stop test");
        stopBtn.setEnabled(false);
        bottom.add(startBtn);
        bottom.add(stopBtn);
        frame.add(bottom, c);

        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                cleanupTimers();
                frame.dispose();
                System.exit(0);
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        setControlsEnabled(false);
    }

    private void wireActions() {
        startBtn.addActionListener(e -> startTest());
        stopBtn.addActionListener(e -> endTest("Test oprit de utilizator."));
        nextBtn.addActionListener(e -> showNextQuestion());
        answerBtn.addActionListener(e -> {
            score++;
            scoreLabel.setText("Scor: " + score);
        });
    }

    private void setControlsEnabled(boolean running) {
        answerBtn.setEnabled(running);
        nextBtn.setEnabled(running);
        stopBtn.setEnabled(running);
        startBtn.setEnabled(!running);
    }

    private void startTest() {
        // Reset stare
        score = 0;
        qIndex = -1;
        scoreLabel.setText("Scor: 0");
        questionArea.setText("Se încarcă prima întrebare...");
        indexLabel.setText("Întrebare: 0/" + questions.size());

        // Timer 1 — contor per secundă (interval fix)
        int[] timeLeft = {testDurationSeconds};
        if (secondCountdownTimer != null && secondCountdownTimer.isRunning()) {
            secondCountdownTimer.stop();
        }
        secondCountdownTimer = new javax.swing.Timer(1000, ev -> {
            timeLeft[0] = Math.max(0, timeLeft[0] - 1);
            timeLabel.setText("Timp rămas: " + formatSeconds(timeLeft[0]));
            if (timeLeft[0] == 0) {
                endTest("Timpul a expirat. Test încheiat automat.");
            }
        });
        secondCountdownTimer.setInitialDelay(0);
        secondCountdownTimer.start();

        // Timer 2 — întrebare nouă la fiecare 10 secunde (perioadă indicată)
        if (questionTimer != null) questionTimer.cancel();
        questionTimer = new Timer("question-rotator", /*daemon*/ true);
        questionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> showNextQuestion());
            }
        }, 0, questionPeriodMs);

        setControlsEnabled(true);
    }

    private void showNextQuestion() {
        if (questions.isEmpty()) return;
        qIndex = (qIndex + 1) % questions.size();
        questionArea.setText(questions.get(qIndex));
        indexLabel.setText("Întrebare: " + (qIndex + 1) + "/" + questions.size());
    }

    private void endTest(String reason) {
        setControlsEnabled(false);
        cleanupTimers();
        questionArea.append("\n\n" + reason + "\nScor final: " + score);
    }

    private void cleanupTimers() {
        if (secondCountdownTimer != null && secondCountdownTimer.isRunning()) {
            secondCountdownTimer.stop();
        }
        if (questionTimer != null) {
            questionTimer.cancel();
            questionTimer = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizTimersApp::new);
    }
}