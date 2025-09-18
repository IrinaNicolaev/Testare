import java.util.Date;
import java.util.Timer;
import java.util.Calendar;

public class Main {
    public static void main(String[] args) {

        // --- 1. Să reacționeze la un anumit interval de timp (după o întârziere) ---
        Timer timer1 = new Timer();
        long delay = 5000; // 5 secunde
        TaskExecutor task1 = new TaskExecutor("Temporizator cu întârziere");
        System.out.println("Temporizatorul 1 va porni peste " + delay/1000 + " secunde.");
        timer1.schedule(task1, delay);

        // --- 2. Să reacționeze la un anumit timp (la o dată specifică) ---
        Timer timer2 = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 10); // Setează-l să pornească peste 10 secunde
        Date specificTime = calendar.getTime();
        TaskExecutor task2 = new TaskExecutor("Temporizator cu dată specifică");
        System.out.println("Temporizatorul 2 va porni la o oră specifică: " + specificTime);
        timer2.schedule(task2, specificTime);

        // --- 3. Să reacționeze cu o perioadă indicată (repetitiv) ---
        Timer timer3 = new Timer();
        long initialDelay = 15000; // 15 secunde întârziere inițială
        long period = 3000; // Se va repeta la fiecare 3 secunde
        TaskExecutor task3 = new TaskExecutor("Temporizator repetitiv");
        System.out.println("Temporizatorul 3 va porni peste " + initialDelay/1000 + " secunde și se va repeta la fiecare " + period/1000 + " secunde.");
        timer3.scheduleAtFixedRate(task3, initialDelay, period);
    }
}