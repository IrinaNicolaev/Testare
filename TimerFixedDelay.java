package com.example.lab1so;

import java.util.Timer;
import java.util.TimerTask;

public class TimerFixedDelay {
    public static void main(String[] args) {
        Timer timer = new Timer("fixed-delay", true);

        final long delayMs = 2000L;   // start după 2s
        final long periodMs = 1000L;  // rulează la fiecare 1s
        final int maxRuns = 8;

        TimerTask task = new TimerTask() {
            int runs = 0;
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                runs++;
                System.out.println("[FixedDelay] Run " + runs + " at " + start);

                // Simulează lucru mai lung (1.3s) pentru a arăta întârzierea cumulată
                try { Thread.sleep(1300L); } catch (InterruptedException ignored) {}

                long end = System.currentTimeMillis();
                System.out.println("[FixedDelay] Finished run " + runs + " at " + end);

                if (runs >= maxRuns) {
                    System.out.println("[FixedDelay] Cancel timer");
                    cancel();       // oprește task-ul
                    timer.cancel(); // oprește timerul
                }
            }
        };

        timer.schedule(task, delayMs, periodMs);

        // ține procesul deschis până la final
        try { Thread.sleep(delayMs + periodMs * (maxRuns + 2)); } catch (InterruptedException ignored) {}
        System.out.println("[FixedDelay] Done.");
    }
}
