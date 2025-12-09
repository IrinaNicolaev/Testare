import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.*;

public class AddAlertDialog extends JDialog {

    private final JTextField nameField = new JTextField(20);

    private final JComboBox<Alert.AlertType> typeCombo =
            new JComboBox<>(Alert.AlertType.values());

    private final JSpinner timeSpinner;
    private final JCheckBox repeatCheck =
            new JCheckBox("Повторять каждый день (для TIME)", true);

    private final JSpinner intervalSecondsSpinner;
    private final JSpinner repeatTimesSpinner;

    private final JSpinner countdownSecondsSpinner;

    private final JTextField soundField = new JTextField(20);
    private final JTextField actionField = new JTextField(20);

    private Alert result;

    public AddAlertDialog(Frame owner) {
        super(owner, "Новое напоминание", true);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());

        SpinnerNumberModel intervalModel = new SpinnerNumberModel(30, 1, 86400, 1);
        intervalSecondsSpinner = new JSpinner(intervalModel);

        SpinnerNumberModel repeatTimesModel = new SpinnerNumberModel(0, 0, 1000, 1);
        repeatTimesSpinner = new JSpinner(repeatTimesModel);

        SpinnerNumberModel countdownModel = new SpinnerNumberModel(60, 1, 86400, 1);
        countdownSecondsSpinner = new JSpinner(countdownModel);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Тип напоминания:"), gbc);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Название:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Время (HH:mm, для TIME):"), gbc);
        gbc.gridx = 1;
        panel.add(timeSpinner, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(repeatCheck, gbc);
        gbc.gridwidth = 1;

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Интервал (секунды, для INTERVAL):"), gbc);
        gbc.gridx = 1;
        panel.add(intervalSecondsSpinner, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Повторить N раз (0 = бесконечно):"), gbc);
        gbc.gridx = 1;
        panel.add(repeatTimesSpinner, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Обратный отсчёт (секунды, для COUNTDOWN):"), gbc);
        gbc.gridx = 1;
        panel.add(countdownSecondsSpinner, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Файл звука (.wav, опционально):"), gbc);

        JPanel soundPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        soundPanel.add(soundField);
        JButton browseSound = new JButton("...");
        browseSound.addActionListener(e -> chooseFile(soundField));
        soundPanel.add(browseSound);

        gbc.gridx = 1;
        panel.add(soundPanel, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Файл/папка для открытия (опционально):"), gbc);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionPanel.add(actionField);
        JButton browseAction = new JButton("...");
        browseAction.addActionListener(e -> chooseFile(actionField));
        actionPanel.add(browseAction);

        gbc.gridx = 1;
        panel.add(actionPanel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Отмена");

        okButton.addActionListener(e -> onOk());
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void chooseFile(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onOk() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Введите название напоминания.",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Alert.AlertType type = (Alert.AlertType) typeCombo.getSelectedItem();
        if (type == null) {
            type = Alert.AlertType.TIME;
        }

        String soundPath = soundField.getText().trim();
        String actionPath = actionField.getText().trim();
        if (soundPath.isEmpty()) soundPath = null;
        if (actionPath.isEmpty()) actionPath = null;

        Alert created = null;

        switch (type) {
            case TIME: {
                Date date = (Date) timeSpinner.getValue();
                LocalTime time = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .withSecond(0)
                        .withNano(0);

                boolean repeatDaily = repeatCheck.isSelected();
                created = Alert.createTimeAlert(name, time, soundPath, actionPath, repeatDaily);
                break;
            }
            case INTERVAL: {
                int seconds = (Integer) intervalSecondsSpinner.getValue();
                if (seconds <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Интервал должен быть больше 0 секунд.",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int rt = (Integer) repeatTimesSpinner.getValue();
                int repeatTimes = (rt == 0) ? -1 : rt; // 0 = бесконечно
                created = Alert.createIntervalAlert(name, seconds, soundPath, actionPath, repeatTimes);
                break;
            }
            case COUNTDOWN: {
                int seconds = (Integer) countdownSecondsSpinner.getValue();
                if (seconds <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Обратный таймер должен быть больше 0 секунд.",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                created = Alert.createCountdownAlert(name, seconds, soundPath, actionPath);
                break;
            }
        }

        result = created;
        dispose();
    }

    private void onCancel() {
        result = null;
        dispose();
    }

    public Alert getResult() {
        return result;
    }
}
