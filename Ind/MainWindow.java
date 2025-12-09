import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import javax.sound.sampled.*;
import javax.swing.*;

public class MainWindow extends JFrame {

    private final AlertTableModel alertTableModel = new AlertTableModel();
    private final JTable alertTable = new JTable(alertTableModel);
    private final JTextArea logArea = new JTextArea(8, 40);
    

    private Timer timer;
    private boolean schedulerRunning = false;
    private TrayIcon trayIcon;

    private static final String REG_PATH = "HKCU\\Software\\AlertScheduler";

    public MainWindow() {
        super("Sound Alert Scheduler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);

        loadWindowPosition();

        ThemeUtils.apply(this);

        initTrayIcon();
        loadAlertsFromStorage();
        initUi();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindowPosition();
            }
        });
    }

    private void initUi() {
        setLayout(new BorderLayout());

        // Добавляем меню с автозапуском и темой
        setJMenuBar(createMenuBar());

        alertTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(alertTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton startButton = new JButton("Старт");
        JButton stopButton = new JButton("Стоп");

        addButton.addActionListener(this::onAddAlert);
        removeButton.addActionListener(this::onRemoveAlert);
        startButton.addActionListener(e -> startScheduler());
        stopButton.addActionListener(e -> stopScheduler());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
logArea.setEditable(false);
logArea.setBorder(BorderFactory.createTitledBorder("Лог"));

logArea.setOpaque(true);
logArea.setBackground(new Color(30, 30, 30));
logArea.setForeground(new Color(220, 220, 220));

        add(buttonPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(logArea, BorderLayout.SOUTH);
    }

    // Меню с настройками
    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu settings = new JMenu("Настройки");

        // автозапуск
        JCheckBoxMenuItem auto = new JCheckBoxMenuItem("Запускать с Windows");
        auto.setState(AutoLaunchManager.isEnabled());

        auto.addActionListener(e -> {
            if (auto.getState()) {
                AutoLaunchManager.enable(System.getProperty("java.class.path"));
                appendLog("Автозапуск включён");
            } else {
                AutoLaunchManager.disable();
                appendLog("Автозапуск выключён");
            }
        });

        // ручное обновление темы
        JMenuItem themeItem = new JMenuItem("Обновить тему Windows");
        themeItem.addActionListener(e -> ThemeUtils.apply(this));

        settings.add(auto);
        settings.add(themeItem);

        bar.add(settings);
        return bar;
    }

    private void initTrayIcon() {
        if (!SystemTray.isSupported()) {
            appendLog("SystemTray не поддерживается на этой системе.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.BLUE);
        g.fillOval(2, 2, 12, 12);
        g.dispose();

        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("Открыть окно");
        showItem.addActionListener(e -> {
            setVisible(true);
            setState(JFrame.NORMAL);
            toFront();
        });
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.addActionListener(e -> {
            stopScheduler();
            saveWindowPosition(); // сохраняем при выходе
            System.exit(0);
        });

        popup.add(showItem);
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Sound Alert Scheduler", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            appendLog("Не удалось добавить иконку в трей: " + e.getMessage());
        }
    }

    private void showTrayMessage(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    private void startScheduler() {
        if (schedulerRunning) {
            appendLog("Планировщик уже запущен.");
            return;
        }

        timer = new Timer("AlertTimer", true);
        timer.scheduleAtFixedRate(
                new AlertTask(alertTableModel, this::onAlertTriggered),
                0,
                1000
        );

        schedulerRunning = true;
        appendLog("Планировщик запущен.");
    }

    private void stopScheduler() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        schedulerRunning = false;
        appendLog("Планировщик остановлен.");
    }

    private void onAlertTriggered(Alert alert) {
        appendLog("Сработало напоминание: " + alert.getName() +
                " [тип: " + alert.getType() + "]");

        if (alert.getSoundPath() != null && !alert.getSoundPath().isEmpty()) {
            playSound(alert.getSoundPath());
        } else {
            Toolkit.getDefaultToolkit().beep();
        }

        if (alert.getActionPath() != null && !alert.getActionPath().isEmpty()) {
            openPath(alert.getActionPath());
        }

        String details;
        switch (alert.getType()) {
            case TIME:
                details = "Время: " + (alert.getTime() != null ? alert.getTime().toString() : "");
                break;
            case INTERVAL:
                details = "Интервал: каждые " + alert.getIntervalSeconds() + " сек";
                break;
            case COUNTDOWN:
                details = "Таймер на " + alert.getCountdownTotalSeconds() + " сек завершён";
                break;
            default:
                details = "";
        }

        showTrayMessage("Напоминание: " + alert.getName(), details);
        saveAlertsToStorage();
    }

    private void onAddAlert(ActionEvent e) {
        AddAlertDialog dialog = new AddAlertDialog(this);
        dialog.setVisible(true);

        Alert alert = dialog.getResult();
        if (alert != null) {
            alertTableModel.addAlert(alert);
            appendLog("Добавлено напоминание: " + alert.getName() +
                    " [тип: " + alert.getType() + "]");
            saveAlertsToStorage();
        }
    }

    private void onRemoveAlert(ActionEvent e) {
        int row = alertTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите напоминание для удаления.",
                    "Нет выбора",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Alert alert = alertTableModel.getAlertAt(row);
        alertTableModel.removeAlert(row);
        appendLog("Удалено напоминание: " + alert.getName());
        saveAlertsToStorage();
    }

    private void appendLog(String text) {
        logArea.append("[" + LocalDateTime.now().withSecond(0).withNano(0) + "] " + text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void playSound(String path) {
        new Thread(() -> {
            try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(path))) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        appendLog("Ошибка проигрывания звука: " + ex.getMessage()));
            }
        }, "SoundPlayerThread").start();
    }

    private void openPath(String pathStr) {
        try {
            Desktop desktop = Desktop.getDesktop();
            File file = new File(pathStr);
            if (!file.exists()) {
                appendLog("Файл/папка не существует: " + pathStr);
                return;
            }
            desktop.open(file);
        } catch (IOException ex) {
            appendLog("Ошибка открытия пути: " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            appendLog("Desktop.open не поддерживается: " + ex.getMessage());
        }
    }

    private void loadAlertsFromStorage() {
        List<Alert> loaded = AlertStorage.load();
        for (Alert a : loaded) {
            alertTableModel.addAlert(a);
        }
        if (!loaded.isEmpty()) {
            appendLog("Загружено напоминаний из файла: " + loaded.size());
        }
    }

    private void saveAlertsToStorage() {
        AlertStorage.save(alertTableModel.getAlerts());
    }

    // сохранение/загрузка позиции окна

    private void loadWindowPosition() {
        String xs = WindowsRegistry.read(REG_PATH, "x");
        String ys = WindowsRegistry.read(REG_PATH, "y");

        if (xs != null && ys != null) {
            try {
                setLocation(Integer.parseInt(xs), Integer.parseInt(ys));
            } catch (Exception ignored) {}
        } else {
            setLocationRelativeTo(null);
        }
    }

    private void saveWindowPosition() {
        WindowsRegistry.write(REG_PATH, "x", "" + getX());
        WindowsRegistry.write(REG_PATH, "y", "" + getY());
    }
}
