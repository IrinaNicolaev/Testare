import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AnimationEngine extends JPanel {
    private final String gifPath;
    private final int iconW, iconH;

    private final JLabel sprite = new JLabel();
    private javax.swing.Timer anim;        // animația de deplasare (Swing)
    private Timer timer12;                 // timerele #1 și #2 (java.util.Timer)

    private int x = 50;
    private final int y = 100;
    private int dx = 5;
    private boolean running = false;

    public AnimationEngine(String gifPath, int iconW, int iconH) {
        this.gifPath = gifPath;
        this.iconW = iconW;
        this.iconH = iconH;

        setLayout(null);
        setPreferredSize(new Dimension(600, 300));
        setBackground(new Color(230, 240, 255));

        sprite.setBounds(x, y, iconW, iconH);
        sprite.setHorizontalAlignment(SwingConstants.CENTER);
        sprite.setVerticalAlignment(SwingConstants.CENTER);
        setStaticIcon();
        add(sprite);

        // animația de deplasare (40 ms)
        anim = new javax.swing.Timer(40, e -> {
            int max = Math.max(0, getWidth() - sprite.getWidth());
            if (x + dx < max) {
                x += dx;
                sprite.setLocation(x, y);
            } else {
                stopAnim(); // la capăt → oprește + revine la static
            }
        });
    }

    /* ===== API pentru UI ===== */
    public void startAnim() {
        if (!running) {
            setGifIcon();
            anim.start();
            running = true;
        }
    }

    public void stopAnim() {
        if (running) {
            anim.stop();
            running = false;
        }
        setStaticIcon();
    }

    public void reset() {
        anim.stop();
        running = false;
        x = 50;
        sprite.setLocation(x, y);
        setStaticIcon();
    }

    public boolean isRunning() { return running; }
    public void setSpeed(int pxPerTick) { dx = pxPerTick; }

    /** Pentru Timer #3 (periodic) — mișcare în pași discreți controlată din UI */
    public void moveBy(int px) {
        x = Math.min(x + px, Math.max(0, getWidth() - sprite.getWidth()));
        sprite.setLocation(x, y);
    }

    /* ===== Timer #1: Delay (ms) ===== */
    public void scheduleDelay(long ms) {
        ensureTimer12();
        timer12.schedule(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(AnimationEngine.this::startAnim);
            }
        }, ms);
    }

    /* ===== Timer #2: La timp (ABSOLUT: yyyy-MM-dd HH:mm:ss, în tz local) ===== */
    public void scheduleAtDateTime(int year, int month, int day, int hour, int minute, int second) {
        ensureTimer12();
        // month este 1-12 (nu 0-11). Se folosește fusul orar local al sistemului.
        ZonedDateTime zdt = LocalDateTime.of(year, month, day, hour, minute, second)
                .atZone(ZoneId.systemDefault());
        Date when = Date.from(zdt.toInstant());
        timer12.schedule(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(AnimationEngine.this::startAnim);
            }
        }, when);
    }

    /** Variantă convenabilă: primește direct un java.util.Date */
    public void scheduleAt(Date when) {
        ensureTimer12();
        timer12.schedule(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(AnimationEngine.this::startAnim);
            }
        }, when);
    }

    /** (Opțional) Păstrată pentru compatibilitate: +N secunde de acum */
    public void scheduleAtSeconds(int secsFromNow) {
        ensureTimer12();
        LocalDateTime ldt = LocalDateTime.now().plusSeconds(secsFromNow);
        scheduleAtDateTime(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(),
                ldt.getHour(), ldt.getMinute(), ldt.getSecond());
    }

    public void stopTimers12() {
        if (timer12 != null) { timer12.cancel(); timer12 = null; }
    }

    /* ===== Helpers ===== */
    private void ensureTimer12() {
        if (timer12 == null) timer12 = new Timer("Timers12", true);
    }

    // GIF animat, forțat la iconW×iconH folosind HTML în JLabel (rămâne animat, nu se mărește)
    private void setGifIcon() {
        String url = new File(gifPath).toURI().toString(); // file:///...
        sprite.setIcon(null);
        sprite.setText("<html><img src='" + url + "' width='" + iconW + "' height='" + iconH + "'></html>");
        sprite.setSize(iconW, iconH);
    }

    // „PNG” din primul cadru al GIF-ului (scalat la iconW×iconH)
    private void setStaticIcon() {
        sprite.setText(null);
        sprite.setIcon(staticFromGif(gifPath, iconW, iconH));
        sprite.setSize(iconW, iconH);
    }

    private static ImageIcon staticFromGif(String path, int w, int h) {
        try {
            BufferedImage frame1 = ImageIO.read(new File(path)); // primul cadru
            Image scaled = frame1.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            ImageIcon ic = new ImageIcon(path);
            Image scaled = ic.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
    }
}

