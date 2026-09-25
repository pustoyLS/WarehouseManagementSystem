package com.warehouse.ui;

import com.warehouse.dao.InventoryDao;
import com.warehouse.model.Inventory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;

public class InventoryView {
    private TableView<Inventory> tableView;
    private ObservableList<Inventory> data = FXCollections.observableArrayList();
    private InventoryDao inventoryDao = new InventoryDao();

    public InventoryView() {
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        tableView = new TableView<>();
        tableView.setItems(data);

        // Колонки
        TableColumn<Inventory, String> warehouseCol = new TableColumn<>("Склад");
        warehouseCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWarehouseName()));
        warehouseCol.setPrefWidth(200);

        TableColumn<Inventory, String> skuCol = new TableColumn<>("Артикул");
        skuCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductSku()));
        skuCol.setPrefWidth(100);

        TableColumn<Inventory, String> productCol = new TableColumn<>("Товар");
        productCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));
        productCol.setPrefWidth(250);

        TableColumn<Inventory, String> quantityCol = new TableColumn<>("Количество");
        quantityCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantity().toString()));
        quantityCol.setPrefWidth(100);

        TableColumn<Inventory, String> minLevelCol = new TableColumn<>("Мин. уровень");
        minLevelCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMinStockLevel().toString()));
        minLevelCol.setPrefWidth(100);

        TableColumn<Inventory, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cellData -> {
            Inventory inv = cellData.getValue();
            String status = inv.isBelowMinLevel() ? "⚠ КРИТИЧЕСКИЙ" : "✓ Норма";
            return new SimpleStringProperty(status);
        });
        statusCol.setPrefWidth(120);

        tableView.getColumns().addAll(warehouseCol, skuCol, productCol, quantityCol, minLevelCol, statusCol);

        // Подсветка критических остатков
        tableView.setRowFactory(tv -> new TableRow<Inventory>() {
            @Override
            protected void updateItem(Inventory item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isBelowMinLevel()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadData() {
        try {
            data.setAll(inventoryDao.getAll());
        } catch (SQLException e) {
            showError("Ошибка загрузки данных", e.getMessage());
        }
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label title = new Label("Остатки товаров на складах");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> loadData());

        HBox controls = new HBox(10, title, refreshBtn);
        controls.setPadding(new Insets(0, 0, 10, 0));

        vbox.getChildren().addAll(controls, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        return vbox;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}