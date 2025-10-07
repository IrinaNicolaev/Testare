// Файл: TimerApp.java

public class TimerApp {
    public static void main(String[] args) {
        System.out.println("Main Thread started. Starting 3 new threads.");

        // 1. Создаем первый поток для звука
        Thread soundThread = new Thread(new SoundPlayer());

        // 2. Создаем второй поток для сообщения
        Thread messageThread = new Thread(new Message("This is Thread 2's scheduled message!"));
        // 3. Создаем третий, анонимный поток для демонстрации третьего потока
      
        Thread thirdThread = new Thread(() -> 
            System.out.println("Thread 3 (Anonymous): I am the third thread, running in parallel.")
        );

        // Запускаем все три потока
        soundThread.start();
        messageThread.start();
        thirdThread.start();

        System.out.println("All threads started. Main thread finished.");
        // Задачи будут выполняться параллельно
    }
    
    // SoundPlayer class implementing Runnable
    static class SoundPlayer implements Runnable {
        public SoundPlayer() {
            // No-argument constructor
        }
    
        @Override
        public void run() {
            System.out.println("Thread 1 (SoundPlayer): Playing sound (simulated).");
        }
    }
}

// Message class implementing Runnable
class Message implements Runnable {
    private String message;

    public Message(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println(message);
    }
}
