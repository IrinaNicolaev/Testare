import java.time.LocalDate;
import java.time.LocalTime;

public class Alert {

    public enum AlertType {
        TIME,       
        INTERVAL,   
        COUNTDOWN   
    }

    private AlertType type;
    private String name;

    private LocalTime time;
    private boolean repeatDaily;
    private LocalDate lastTriggeredDate;
    private int intervalSeconds;
    private int repeatTimes; 

    private int countdownTotalSeconds;   

    private int remainingSeconds;     

    private String soundPath;
    private String actionPath;
    private boolean enabled = true;

    public Alert(AlertType type,
                 String name,
                 LocalTime time,
                 int intervalSeconds,
                 int countdownTotalSeconds,
                 String soundPath,
                 String actionPath,
                 boolean repeatDaily,
                 int repeatTimes,
                 boolean enabled,
                 int remainingSeconds,
                 LocalDate lastTriggeredDate) {

        this.type = type;
        this.name = name;
        this.time = time;
        this.intervalSeconds = intervalSeconds;
        this.countdownTotalSeconds = countdownTotalSeconds;
        this.soundPath = soundPath;
        this.actionPath = actionPath;
        this.repeatDaily = repeatDaily;
        this.repeatTimes = repeatTimes;
        this.enabled = enabled;
        this.remainingSeconds = remainingSeconds;
        this.lastTriggeredDate = lastTriggeredDate;
    }

    public static Alert createTimeAlert(String name,
                                        LocalTime time,
                                        String soundPath,
                                        String actionPath,
                                        boolean repeatDaily) {
        return new Alert(
                AlertType.TIME,
                name,
                time,
                0,
                0,
                soundPath,
                actionPath,
                repeatDaily,
                -1,
                true,
                0,
                null
        );
    }

    public static Alert createIntervalAlert(String name,
                                            int intervalSeconds,
                                            String soundPath,
                                            String actionPath,
                                            int repeatTimes) {
        if (intervalSeconds <= 0) {
            intervalSeconds = 1;
        }
        int remaining = intervalSeconds;
        return new Alert(
                AlertType.INTERVAL,
                name,
                null,
                intervalSeconds,
                0,
                soundPath,
                actionPath,
                false,
                repeatTimes,
                true,
                remaining,
                null
        );
    }

    public static Alert createCountdownAlert(String name,
                                             int countdownSeconds,
                                             String soundPath,
                                             String actionPath) {
        if (countdownSeconds <= 0) {
            countdownSeconds = 1;
        }
        return new Alert(
                AlertType.COUNTDOWN,
                name,
                null,
                0,
                countdownSeconds,
                soundPath,
                actionPath,
                false,
                0,
                true,
                countdownSeconds,
                null
        );
    }

    public AlertType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean isRepeatDaily() {
        return repeatDaily;
    }

    public void setRepeatDaily(boolean repeatDaily) {
        this.repeatDaily = repeatDaily;
    }

    public LocalDate getLastTriggeredDate() {
        return lastTriggeredDate;
    }

    public void setLastTriggeredDate(LocalDate lastTriggeredDate) {
        this.lastTriggeredDate = lastTriggeredDate;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public int getCountdownTotalSeconds() {
        return countdownTotalSeconds;
    }

    public String getSoundPath() {
        return soundPath;
    }

    public String getActionPath() {
        return actionPath;
    }

    public int getRepeatTimes() {
        return repeatTimes;
    }

    public void setRepeatTimes(int repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
