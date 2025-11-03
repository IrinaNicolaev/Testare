package Testare;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Date;

public class TimerSimplu {
    public static void main(String[] args) {

        System.out.println("Start.");

        
        final Timer tInterval = new Timer();
        tInterval.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                System.out.println("@@@la fiecare 2s");
            }
        }, 0L, 2000L); 

        
        final Timer tAtTime = new Timer();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1); 
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date exactMoment = cal.getTime();
        tAtTime.schedule(new TimerTask() {
            @Override public void run() {
                System.out.println("$$$La timp exact a venit momentul exact");
            }
        }, exactMoment); 

        
        final Timer tPeriodic = new Timer();
        TimerTask periodicTask = new TimerTask() {
            int count = 0;
            @Override public void run() {
                count++;
                System.out.println("Periodic exec #" + count);
                if (count >= 4) {
                    System.out.println("!!!Periodic stop după 4 execuții ");
                    tPeriodic.cancel(); 
                }
            }
        };
        tPeriodic.schedule(periodicTask, 3000L, 1500L); 

        
        final Timer killer = new Timer();
        killer.schedule(new TimerTask() {
            @Override public void run() {
                System.out.println("demo, cancel pe toate și System.exit(0)");
                tInterval.cancel();
                tAtTime.cancel();
                tPeriodic.cancel();
                killer.cancel();
                System.exit(0);
            }
        }, 20000L);
    }
}
