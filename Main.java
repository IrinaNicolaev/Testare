import java.util.Date;
import java.util.Timer;
import java.util.Calendar;
import java.util.TimerTask;

// --- Partea 1: Modificarea clasei TaskExecutor pentru a gestiona numărul de execuții ---
class TaskExecutor extends TimerTask {
    private String taskName;
    private int executions;
    private int maxExecutions;
    private Timer timer;

    public TaskExecutor(String taskName, int maxExecutions, Timer timer) {
        this.taskName = taskName;
        this.executions = 0;
        this.maxExecutions = maxExecutions;
        this.timer = timer;
    }

    @Override
    public void run() {
        if (executions < maxExecutions) {
            System.out.println("S-a executat sarcina: " + taskName + " (Execuția " + (executions + 1) + ") la: " + new java.util.Date());
            executions++;
        } else {
            System.out.println("Sarcina '" + taskName + "' a atins numărul maxim de execuții (" + maxExecutions + ") și va fi anulată.");
            this.cancel(); // Anulează doar această sarcină
            timer.cancel(); // Anulează și temporizatorul
        }
    }
}

// --- Partea 2: Noua clasă TaskManager ---
class TaskManager {
    public static void scheduleOneShotTimer(long delay) {
        Timer timer = new Timer();
        TaskExecutor task = new TaskExecutor("Temporizator cu întârziere (un singur foc)", 1, timer);
        System.out.println("Temporizatorul cu o singură execuție va porni peste " + delay / 1000 + " secunde.");
        timer.schedule(task, delay);
    }

    public static void scheduleSpecificTimeTimer(int secondsFromNow) {
        Timer timer = new Timer();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + secondsFromNow);
        Date specificTime = calendar.getTime();
        TaskExecutor task = new TaskExecutor("Temporizator cu dată specifică", 1, timer);
        System.out.println("Temporizatorul cu dată specifică va porni la: " + specificTime);
        timer.schedule(task, specificTime);
    }

    public static void scheduleRepetitiveTimer(long initialDelay, long period, int maxExecutions) {
        Timer timer = new Timer();
        TaskExecutor task = new TaskExecutor("Temporizator repetitiv", maxExecutions, timer);
        System.out.println("Temporizatorul repetitiv va porni peste " + initialDelay / 1000 + " secunde și se va repeta la fiecare " + period / 1000 + " secunde, de " + maxExecutions + " ori.");
        timer.scheduleAtFixedRate(task, initialDelay, period);
    }
}

// --- Clasa Main actualizată (o versiune mai curată) ---
public class Main {
    public static void main(String[] args) {
        // Apelurile către managerul de sarcini
        TaskManager.scheduleOneShotTimer(5000); // Pornire peste 5 secunde
        TaskManager.scheduleSpecificTimeTimer(10); // Pornire peste 10 secunde
        TaskManager.scheduleRepetitiveTimer(15000, 3000, 3); // Pornire peste 15 secunde, repetare la 3s, de 3 ori
    }
}
