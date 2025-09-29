import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    private TextArea logArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Daily Reminders");

        Color backgroundColor = Color.web("#FFC1DA");
        Font labelFont = Font.font("Cal Sans", 18);
        logArea.setEditable(false);
        logArea.setPrefWidth(300);
        logArea.setStyle("-fx-control-inner-background: #FFD6E7; -fx-font-size: 14;");

        //QuickReminder
        VBox quickBox = new VBox(10);
        quickBox.setPadding(new Insets(10));
        quickBox.setPrefWidth(180);
        quickBox.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

        Label quickLabel = new Label("Quick Reminder");
        quickLabel.setFont(Font.font("Cal Sans", FontWeight.BOLD, 18));

        TextField quickInput = new TextField();
        quickInput.setPromptText("Task");
        TextField quickTime = new TextField();
        quickTime.setPromptText("Secunde");

        Button startQuick = new Button("Start");
        styleStartButton(startQuick);
        Button stopQuick = new Button("Stop");
        styleStopButton(stopQuick);

        quickBox.getChildren().addAll(quickLabel, quickInput, quickTime, startQuick, stopQuick);

        //HourReminder
        VBox hourBox = new VBox(10);
        hourBox.setPadding(new Insets(10));
        hourBox.setPrefWidth(180);
        hourBox.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

        Label hourLabel = new Label("Hour Reminder");
        hourLabel.setFont(Font.font("Cal Sans", FontWeight.BOLD, 18));

        TextField hourInput = new TextField();
        hourInput.setPromptText("Task");
        TextField hourTime = new TextField();
        hourTime.setPromptText("ora(HH):minut(mm)");

        Button startHour = new Button("Start");
        styleStartButton(startHour);
        Button stopHour = new Button("Stop");
        styleStopButton(stopHour);

        hourBox.getChildren().addAll(hourLabel, hourInput, hourTime, startHour, stopHour);

        //Pomodoro
        VBox pomodoroBox = new VBox(10);
        pomodoroBox.setPadding(new Insets(10));
        pomodoroBox.setPrefWidth(200);
        pomodoroBox.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

        Label pomodoroLabel = new Label("Pomodoro Timer");
        pomodoroLabel.setFont(Font.font("Cal Sans", FontWeight.BOLD, 18));

        TextField workInput = new TextField();
        workInput.setPromptText("Task");
        TextField breakInput = new TextField();
        breakInput.setPromptText("Mesaj pauză");
        TextField workTime = new TextField();
        workTime.setPromptText("Minute pentru task");
        TextField breakTime = new TextField();
        breakTime.setPromptText("Minute pentru pauză");
        TextField cyclesInput = new TextField();
        cyclesInput.setPromptText("Număr cicluri");

        Button startPomodoro = new Button("Start");
        styleStartButton(startPomodoro);
        Button stopPomodoro = new Button("Stop");
        styleStopButton(stopPomodoro);

        pomodoroBox.getChildren().addAll(pomodoroLabel, workInput, breakInput, workTime, breakTime, cyclesInput, startPomodoro, stopPomodoro);

        //logica
        QuickReminder quickReminder = new QuickReminder(logArea);
        HourReminder hourReminder = new HourReminder(logArea);
        PomodoroTimer pomodoroTimer = new PomodoroTimer(logArea);

        //buton - quickReminder
        startQuick.setOnAction(e -> {
            try {
                int sec = Integer.parseInt(quickTime.getText());
                String msg = quickInput.getText();
                quickReminder.start(sec, msg);
            } catch (NumberFormatException ex) {
                logArea.appendText("Introduceți un număr valid pentru secunde!\n");
            }
        });
        stopQuick.setOnAction(e -> quickReminder.stop());

        //buton - hourReminder
        startHour.setOnAction(e -> {
            String ora = hourTime.getText();
            String msg = hourInput.getText();
            hourReminder.start(ora, msg);
        });
        stopHour.setOnAction(e -> hourReminder.stop());

        //buton - pomodoro
        startPomodoro.setOnAction(e -> {
            try {
                int work = Integer.parseInt(workTime.getText());
                int brk = Integer.parseInt(breakTime.getText());
                int cycles = Integer.parseInt(cyclesInput.getText());
                String workMsg = workInput.getText();
                String breakMsg = breakInput.getText();
                pomodoroTimer.start(work, brk, cycles, workMsg, breakMsg);
            } catch (NumberFormatException ex) {
                logArea.appendText("Introduceți valori numerice valide pentru minute și cicluri!\n");
            }
        });
        stopPomodoro.setOnAction(e -> pomodoroTimer.stop());

        //Styiling
        HBox mainLayout = new HBox(20, quickBox, hourBox, pomodoroBox, logArea);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(mainLayout, 900, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    private void styleStartButton(Button btn) {
        btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
    }
    private void styleStopButton(Button btn) {
        btn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
