import java.util.TimerTask;

public class TaskExecutor extends TimerTask {
    private String taskName;

    public TaskExecutor(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public void run() {
        System.out.println("S-a executat sarcina: " + taskName + " la momentul: " + new java.util.Date());
    }
}