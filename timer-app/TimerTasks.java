import java.awt.Toolkit;
import java.util.TimerTask;

/**
 * Класс, который воспроизводит звуковой сигнал.
 * Он наследует возможности класса TimerTask.
 */
class SoundPlayer extends TimerTask {
    @Override
    public void run() {
        // Заставляет систему издать короткий звуковой сигнал
        Toolkit.getDefaultToolkit().beep();
        System.out.println("Sound played");
    }
}

/**
 * Класс, который выводит текстовое сообщение.
 * Он также наследует возможности класса TimerTask.
 */
class Message extends TimerTask {
    String msg;

    /**
     * Конструктор для класса Message.
     * @param msg Текст сообщения, который нужно вывести.
     */
    public Message(String msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        // Выводит сохранённое сообщение на консоль
        System.out.println(msg);
    }
}