import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class MiniAppUI extends JFrame {
    private static final String GIF_PATH = "src/lama.gif"; // lasă GIF-ul în src/
    private static final int ICON_W = 120, ICON_H = 120;

    private final AnimationEngine engine;
    private final JTextArea log = new JTextArea(8, 40);

    // Timer #3 (periodic) — ținut în UI
    private Timer timer3;

    public MiniAppUI() {
        super("Timers Lab (Split: Engine + UI)");

        engine = new AnimationEngine(GIF_PATH, ICON_W, ICON_H);

        JButton bStartStop = new JButton("Start/Stop");
        JButton bReset     = new JButton("Reset");
        JButton bDelay     = new JButton("Delay 2s");         // Timer #1
        JButton bAt        = new JButton("La timp");    // Timer #2
        JButton bFix       = new JButton("Periodic (5 pași)"); // Timer #3
        JButton bStopAll   = new JButton("Stop ALL timers");

        JPanel controls = new JPanel();
        controls.add(bStartStop);
        controls.add(bReset);
        controls.add(bDelay);
        controls.add(bAt);
        controls.add(bFix);
        controls.add(bStopAll);

        log.setEditable(false);

        setLayout(new BorderLayout());
        add(engine, BorderLayout.CENTER);
        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(log), BorderLayout.SOUTH);

        // Acțiuni
        bStartStop.addActionListener(e -> {
            if (!engine.isRunning()) {
                engine.startAnim();
                append("Start anim.\n");
            } else {
                engine.stopAnim();
                append("Stop anim.\n");
            }
        });

        bReset.addActionListener(e -> {
            engine.reset();
            append("Reset.\n");
        });

        // Timer #1: Delay 2s (e în Engine)
        bDelay.addActionListener(e -> {
            engine.scheduleDelay(2000);
            append("Programat Delay +2000ms (Timer #1).\n");
        });

        // Timer #2: La timp +5s (e în Engine)
        bAt.addActionListener(e -> {
            engine.scheduleAtDateTime(2025, 9, 16, 00, 23, 5);
            append("Programat La timp: (Timer #2 )\n");

        });

        // Timer #3: Periodic — 5 pași (start în 3s, apoi la fiecare 1s)
        bFix.addActionListener(e -> {
            ensureTimer3();
            timer3.scheduleAtFixedRate(new TimerTask() {
                int steps = 0;
                @Override public void run() {
                    SwingUtilities.invokeLater(() -> {
                        steps++;
                        if (steps == 1) engine.startAnim(); // pornește animația la primul pas
                        engine.moveBy(20);                  // deplasare discretă
                        if (steps >= 5) {
                            engine.stopAnim();
                            cancel();
                            append("Periodic complet (5 pași) — Timer #3.\n");
                        } else {
                            append("Pas #" + steps + " — Timer #3.\n");
                        }
                    });
                }
            }, 3000, 1000);
            append("Periodic programat: start 3000ms, apoi 1000ms × 5 (Timer #3).\n");
        });

        bStopAll.addActionListener(e -> {
            stopTimer3();
            engine.stopTimers12();
            engine.stopAnim();
            append("STOP ALL timers (#1, #2, #3) + animație.\n");
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void ensureTimer3() {
        if (timer3 == null) timer3 = new Timer("Timer3", true);
    }
    private void stopTimer3() {
        if (timer3 != null) { timer3.cancel(); timer3 = null; }
    }

    private void append(String s) {
        log.append(s);
        log.setCaretPosition(log.getDocument().getLength());
        System.out.print(s);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MiniAppUI().setVisible(true));
    }
}