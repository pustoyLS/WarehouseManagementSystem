package com.warehouse.ui;

import com.warehouse.config.DatabaseConfig;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow {
    private Stage primaryStage;
    private BorderPane root;
    private TabPane tabPane;
    private Label statusLabel;

    public MainWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeUI();
    }

    private void initializeUI() {
        root = new BorderPane();
        root.setPadding(new Insets(10));

        // Меню
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Заголовок
        Label title = new Label("Система управления складом (WMS)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(title);
        titleBox.setPadding(new Insets(10));

        VBox topBox = new VBox(menuBar, titleBox);
        root.setTop(topBox);

        // Вкладки
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Вкладка "Остатки"
        Tab inventoryTab = new Tab("Остатки");
        inventoryTab.setContent(new InventoryView().getView());
        tabPane.getTabs().add(inventoryTab);

        // Вкладка "Склады"
        Tab warehousesTab = new Tab("Склады");
        warehousesTab.setContent(new WarehousesView().getView());
        tabPane.getTabs().add(warehousesTab);

        // Вкладка "Товары"
        Tab productsTab = new Tab("Товары");
        productsTab.setContent(new ProductsView().getView());
        tabPane.getTabs().add(productsTab);

        // Вкладка "Документы"
        Tab movementsTab = new Tab("Документы");
        movementsTab.setContent(new MovementsView().getView());
        tabPane.getTabs().add(movementsTab);

        // Вкладка "Критические остатки"
        Tab criticalTab = new Tab("⚠ Критические остатки");
        criticalTab.setContent(new CriticalInventoryView().getView());
        tabPane.getTabs().add(criticalTab);

        root.setCenter(tabPane);

        // Статусная строка
        statusLabel = new Label("Активная СУБД: " + DatabaseConfig.getDatabaseDisplayName());
        statusLabel.setStyle("-fx-font-weight: bold;");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        root.setBottom(statusBar);

        // Создание сцены
        Scene scene = new Scene(root, 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Система управления складом");
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Меню "Файл"
        Menu fileMenu = new Menu("Файл");
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().add(exitItem);

        // Меню "База данных"
        Menu dbMenu = new Menu("База данных");

        MenuItem postgresItem = new MenuItem("PostgreSQL");
        postgresItem.setOnAction(e -> switchDatabase("postgres"));

        MenuItem mssqlItem = new MenuItem("MS SQL Server");
        mssqlItem.setOnAction(e -> switchDatabase("mssql"));

        MenuItem testConnectionItem = new MenuItem("Тест подключения");
        testConnectionItem.setOnAction(e -> testConnection());

        dbMenu.getItems().addAll(postgresItem, mssqlItem, new SeparatorMenuItem(), testConnectionItem);

        // Меню "Вид"
        Menu viewMenu = new Menu("Вид");
        MenuItem refreshItem = new MenuItem("Обновить все вкладки");
        refreshItem.setOnAction(e -> refreshAllTabs());
        viewMenu.getItems().add(refreshItem);

        menuBar.getMenus().addAll(fileMenu, dbMenu, viewMenu);
        return menuBar;
    }

    private void switchDatabase(String dbType) {
        if (dbType.equals(DatabaseConfig.getActiveDatabase())) {
            showInfo("Информация", "Уже используется " + DatabaseConfig.getDatabaseDisplayName());
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Переключение базы данных");
        confirm.setHeaderText("Переключиться на " + getDbDisplayName(dbType) + "?");
        confirm.setContentText("Все несохраненные данные могут быть потеряны.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    DatabaseConfig.setActiveDatabase(dbType);
                    statusLabel.setText("Активная СУБД: " + DatabaseConfig.getDatabaseDisplayName());
                    showInfo("Успех", "Переключено на " + DatabaseConfig.getDatabaseDisplayName());
                    refreshAllTabs();
                } catch (IOException e) {
                    showError("Ошибка", "Не удалось переключить базу данных: " + e.getMessage());
                }
            }
        });
    }

    private void testConnection() {
        boolean connected = DatabaseConfig.testConnection();
        if (connected) {
            showInfo("Тест подключения",
                    "Подключение к " + DatabaseConfig.getDatabaseDisplayName() + " успешно!");
        } else {
            showError("Тест подключения",
                    "Не удалось подключиться к " + DatabaseConfig.getDatabaseDisplayName() +
                            "\nПроверьте настройки в config.properties");
        }
    }

    private void refreshAllTabs() {
        // Перезагружаем данные во всех вкладках
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getContent() instanceof VBox vbox) {
                // Находим кнопку "Обновить" и нажимаем её
                vbox.getChildren().stream()
                        .filter(node -> node instanceof HBox)
                        .flatMap(node -> ((HBox) node).getChildren().stream())
                        .filter(node -> node instanceof Button && ((Button) node).getText().contains("Обновить"))
                        .forEach(node -> ((Button) node).fire());
            }
        }
    }

    private String getDbDisplayName(String dbType) {
        return switch (dbType) {
            case "postgres" -> "PostgreSQL";
            case "mssql" -> "MS SQL Server";
            default -> dbType;
        };
    }

    public void show() {
        primaryStage.show();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}