module com.example.sotimer1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sotimer1 to javafx.fxml;
    exports com.example.sotimer1;
}