package com.warehouse.ui;

import com.warehouse.dao.WarehouseDao;
import com.warehouse.model.Warehouse;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.Optional;

public class WarehousesView {
    private TableView<Warehouse> tableView;
    private ObservableList<Warehouse> data = FXCollections.observableArrayList();
    private WarehouseDao warehouseDao = new WarehouseDao();

    public WarehousesView() {
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        tableView = new TableView<>();
        tableView.setItems(data);

        TableColumn<Warehouse, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId().toString()));
        idCol.setPrefWidth(50);

        TableColumn<Warehouse, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        nameCol.setPrefWidth(300);

        TableColumn<Warehouse, String> addressCol = new TableColumn<>("Адрес");
        addressCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAddress()));
        addressCol.setPrefWidth(400);

        tableView.getColumns().addAll(idCol, nameCol, addressCol);
    }

    private void loadData() {
        try {
            data.setAll(warehouseDao.getAll());
        } catch (SQLException e) {
            showError("Ошибка загрузки данных", e.getMessage());
        }
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label title = new Label("Справочник складов");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button addBtn = new Button("➕ Добавить");
        addBtn.setOnAction(e -> showAddEditDialog(null));

        Button editBtn = new Button("✏ Редактировать");
        editBtn.setOnAction(e -> {
            Warehouse selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddEditDialog(selected);
            } else {
                showWarning("Выберите склад для редактирования");
            }
        });

        Button deleteBtn = new Button("🗑 Удалить");
        deleteBtn.setOnAction(e -> deleteWarehouse());

        Button refreshBtn = new Button("🔄 Обновить");
        refreshBtn.setOnAction(e -> loadData());

        HBox controls = new HBox(10, title, addBtn, editBtn, deleteBtn, refreshBtn);
        controls.setPadding(new Insets(0, 0, 10, 0));

        vbox.getChildren().addAll(controls, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        return vbox;
    }

    private void showAddEditDialog(Warehouse warehouse) {
        Dialog<Warehouse> dialog = new Dialog<>();
        dialog.setTitle(warehouse == null ? "Добавить склад" : "Редактировать склад");
        dialog.setHeaderText(warehouse == null ? "Введите данные нового склада" : "Измените данные склада");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Название склада");
        if (warehouse != null) nameField.setText(warehouse.getName());

        TextField addressField = new TextField();
        addressField.setPromptText("Адрес");
        if (warehouse != null) addressField.setText(warehouse.getAddress());

        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Адрес:"), 0, 1);
        grid.add(addressField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Warehouse result = warehouse == null ? new Warehouse() : warehouse;
                result.setName(nameField.getText());
                result.setAddress(addressField.getText());
                return result;
            }
            return null;
        });

        Optional<Warehouse> result = dialog.showAndWait();
        result.ifPresent(wh -> {
            try {
                if (warehouse == null) {
                    warehouseDao.save(wh);
                    showInfo("Склад успешно добавлен");
                } else {
                    warehouseDao.update(wh);
                    showInfo("Склад успешно обновлён");
                }
                loadData();
            } catch (SQLException e) {
                showError("Ошибка сохранения", e.getMessage());
            }
        });
    }

    private void deleteWarehouse() {
        Warehouse selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите склад для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Вы уверены, что хотите удалить склад?");
        confirm.setContentText("Склад: " + selected.getName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                warehouseDao.delete(selected.getId());
                showInfo("Склад успешно удалён");
                loadData();
            } catch (SQLException e) {
                showError("Ошибка удаления",
                        "Невозможно удалить склад, так как он используется в других записях.\n" + e.getMessage());
            }
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}