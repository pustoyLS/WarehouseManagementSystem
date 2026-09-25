package com.warehouse.ui;

import com.warehouse.dao.ProductDao;
import com.warehouse.model.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class ProductsView {
    private TableView<Product> tableView;
    private ObservableList<Product> data = FXCollections.observableArrayList();
    private ProductDao productDao = new ProductDao();

    public ProductsView() {
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        tableView = new TableView<>();
        tableView.setItems(data);

        TableColumn<Product, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getId().toString()));
        idCol.setPrefWidth(50);

        TableColumn<Product, String> skuCol = new TableColumn<>("Артикул");
        skuCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSku()));
        skuCol.setPrefWidth(100);

        TableColumn<Product, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<Product, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory()));
        categoryCol.setPrefWidth(150);

        TableColumn<Product, String> minLevelCol = new TableColumn<>("Мин. уровень");
        minLevelCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMinStockLevel().toString()));
        minLevelCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, skuCol, nameCol, categoryCol, minLevelCol);
    }

    private void loadData() {
        try {
            data.setAll(productDao.getAll());
        } catch (SQLException e) {
            showError("Ошибка загрузки данных", e.getMessage());
        }
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label title = new Label("Справочник товаров");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button addBtn = new Button("➕ Добавить");
        addBtn.setOnAction(e -> showAddEditDialog(null));

        Button editBtn = new Button("✏ Редактировать");
        editBtn.setOnAction(e -> {
            Product selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddEditDialog(selected);
            } else {
                showWarning("Выберите товар для редактирования");
            }
        });

        Button deleteBtn = new Button("🗑 Удалить");
        deleteBtn.setOnAction(e -> deleteProduct());

        Button refreshBtn = new Button("🔄 Обновить");
        refreshBtn.setOnAction(e -> loadData());

        HBox controls = new HBox(10, title, addBtn, editBtn, deleteBtn, refreshBtn);
        controls.setPadding(new Insets(0, 0, 10, 0));

        vbox.getChildren().addAll(controls, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        return vbox;
    }

    private void showAddEditDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Добавить товар" : "Редактировать товар");
        dialog.setHeaderText(product == null ? "Введите данные нового товара" : "Измените данные товара");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField skuField = new TextField();
        skuField.setPromptText("Артикул (SKU)");
        if (product != null) skuField.setText(product.getSku());

        TextField nameField = new TextField();
        nameField.setPromptText("Название товара");
        if (product != null) nameField.setText(product.getName());

        TextField categoryField = new TextField();
        categoryField.setPromptText("Категория");
        if (product != null) categoryField.setText(product.getCategory());

        TextField minLevelField = new TextField();
        minLevelField.setPromptText("Минимальный уровень");
        if (product != null) minLevelField.setText(product.getMinStockLevel().toString());

        grid.add(new Label("Артикул:"), 0, 0);
        grid.add(skuField, 1, 0);
        grid.add(new Label("Название:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Категория:"), 0, 2);
        grid.add(categoryField, 1, 2);
        grid.add(new Label("Мин. уровень:"), 0, 3);
        grid.add(minLevelField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Product result = product == null ? new Product() : product;
                result.setSku(skuField.getText());
                result.setName(nameField.getText());
                result.setCategory(categoryField.getText());
                try {
                    result.setMinStockLevel(new BigDecimal(minLevelField.getText()));
                } catch (NumberFormatException e) {
                    result.setMinStockLevel(BigDecimal.ZERO);
                }
                return result;
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(prod -> {
            try {
                if (product == null) {
                    productDao.save(prod);
                    showInfo("Товар успешно добавлен");
                } else {
                    productDao.update(prod);
                    showInfo("Товар успешно обновлён");
                }
                loadData();
            } catch (SQLException e) {
                showError("Ошибка сохранения", e.getMessage());
            }
        });
    }

    private void deleteProduct() {
        Product selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите товар для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Вы уверены, что хотите удалить товар?");
        confirm.setContentText("Товар: " + selected.getName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productDao.delete(selected.getId());
                showInfo("Товар успешно удалён");
                loadData();
            } catch (SQLException e) {
                showError("Ошибка удаления",
                        "Невозможно удалить товар, так как он используется в других записях.\n" + e.getMessage());
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