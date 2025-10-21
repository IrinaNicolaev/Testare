package com.example.sotimer1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

//Test
public class HelloApplication extends Application {
    
    private HelloController controller;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Scene scene = new Scene(loader.load(), 700, 750);
        
        // Salvăm referința la controller pentru cleanup
        controller = loader.getController();

        primaryStage.setTitle(" Aplicație cu 3 Timere - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        
        // Cleanup la închidere
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("🔴 Aplicația se închide...");
            if (controller != null) {
                controller.shutdown();
            }
        });
        
        primaryStage.show();
        System.out.println("✅ Aplicația a pornit cu succes!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
