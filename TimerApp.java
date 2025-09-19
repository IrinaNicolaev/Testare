// Файл: TimerApp.java

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

// Запустит задачи, определённые в другом файле.
public class TimerApp {
    public static void main(String[] args) {
        // Задача 1: Запуск с указанным периодом (каждые 2 секунды).
        // Использует класс SoundPlayer из первого файла.
        SoundPlayer soundPlayer = new SoundPlayer();
        Timer soundTimer = new Timer();
        soundTimer.scheduleAtFixedRate(soundPlayer, 0, 2000);

        // Задача 2: Реагировать на определённый интервал времени (через 5 секунд).
        // Отменяет первый таймер.
        Timer messageTimer = new Timer();
        messageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("5 seconds have passed. Cancelling the sound timer.");
                soundTimer.cancel();
            }
        }, 5000);

        // Задача 3: Реагировать на определённое время (в 18:24).
        // Использует класс Message из первого файла.
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 24);
        calendar.set(Calendar.SECOND, 0);
        Date date = calendar.getTime();

        Message message = new Message("Salut, Salut, Salut!!!");
        Timer clock = new Timer();
        clock.schedule(message, date);

        System.out.println("Application started.");
    }
}