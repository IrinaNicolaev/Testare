// Файл: MyProgram.java
import java.awt.Toolkit;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Класс, который воспроизводит звук.
 */
class SoundPlayer extends TimerTask {
    @Override
    public void run() {
        Toolkit.getDefaultToolkit().beep();
        System.out.println("Sound played");
    }
}

/**
 * Класс, который выводит сообщение.
 */
class Message extends TimerTask {
    String msg;

    public Message(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        System.out.println(msg);
    }
}

public class MyProgram {

    public static void main(String[] args) {
        // Создание и запуск таймеров
        
        // Таймер для звука (каждые 2 секунды)
        Timer soundTimer = new Timer();
        TimerTask soundPlayer = new SoundPlayer();
        soundTimer.scheduleAtFixedRate(soundPlayer, 0, 2000); 

        // Таймер, который отменяет первый таймер через 5 секунд
        Timer messageTimer = new Timer();
        messageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("5 seconds have passed. Cancelling sound.");
                soundTimer.cancel(); 
            }
        }, 5000); 

        // Таймер для сообщения в определенное время
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18); 
        calendar.set(Calendar.MINUTE, 24); 
        calendar.set(Calendar.SECOND, 0); 
        Date date = calendar.getTime();

        Timer clock = new Timer();
        TimerTask message = new Message("Salut, Salut, Salut!!!");
        clock.schedule(message, date);

        System.out.println("Start");
    }
}