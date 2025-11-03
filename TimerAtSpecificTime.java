package com.example.lab1so;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerAtSpecificTime {
    public static void main(String[] args) {
        Timer timer = new Timer("specific-time", true);

        // Setează ora: peste 15 secunde de acum
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 15);
        Date runAt = cal.getTime();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[SpecificTime] Execut la: " + new Date());
            }
        };

        // Task de oprire după 5 secunde de la execuția primului task
        TimerTask stopTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("[SpecificTime] Oprire timer la: " + new Date());
                timer.cancel();
            }
        };

        timer.schedule(task, runAt);
        // programează oprirea la 20 sec de acum (15s până la task + 5s)
        Timer stopScheduler = new Timer("stopper", true);
        stopScheduler.schedule(stopTask, new Date(System.currentTimeMillis() + 20000L));

        // menține aplicația vie până când se încheie
        try { Thread.sleep(22000L); } catch (InterruptedException ignored) {}
        stopScheduler.cancel();
        System.out.println("[SpecificTime] Done.");
    }
}   
