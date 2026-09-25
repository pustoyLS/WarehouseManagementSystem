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

public class CriticalInventoryView {
    private TableView<Inventory> tableView;
    private ObservableList<Inventory> data = FXCollections.observableArrayList();
    private InventoryDao inventoryDao = new InventoryDao();

    public CriticalInventoryView() {
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        tableView = new TableView<>();
        tableView.setItems(data);

        // Колонки (те же, что в InventoryView)
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

        TableColumn<Inventory, String> quantityCol = new TableColumn<>("Текущий остаток");
        quantityCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantity().toString()));
        quantityCol.setPrefWidth(120);

        TableColumn<Inventory, String> minLevelCol = new TableColumn<>("Мин. уровень");
        minLevelCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMinStockLevel().toString()));
        minLevelCol.setPrefWidth(120);

        TableColumn<Inventory, String> deficitCol = new TableColumn<>("Дефицит");
        deficitCol.setCellValueFactory(cellData -> {
            Inventory inv = cellData.getValue();
            double deficit = inv.getMinStockLevel().doubleValue() - inv.getQuantity().doubleValue();
            return new SimpleStringProperty(String.format("%.3f", Math.max(0, deficit)));
        });
        deficitCol.setPrefWidth(100);

        tableView.getColumns().addAll(warehouseCol, skuCol, productCol, quantityCol, minLevelCol, deficitCol);

        // Все строки красные (все критические)
        tableView.setRowFactory(tv -> new TableRow<Inventory>() {
            @Override
            protected void updateItem(Inventory item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });
    }

    private void loadData() {
        try {
            data.setAll(inventoryDao.getBelowMinLevel());
        } catch (SQLException e) {
            showError("Ошибка загрузки данных", e.getMessage());
        }
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label title = new Label("⚠ Товары с критическим уровнем остатков");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");

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