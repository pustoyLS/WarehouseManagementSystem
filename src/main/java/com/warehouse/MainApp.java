package com.warehouse;

import com.warehouse.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            MainWindow mainWindow = new MainWindow(primaryStage);
            mainWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка запуска приложения: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}