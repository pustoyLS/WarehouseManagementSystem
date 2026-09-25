package com.warehouse.ui;

import com.warehouse.dao.MovementDao;
import com.warehouse.dao.ProductDao;
import com.warehouse.dao.WarehouseDao;
import com.warehouse.model.Movement;
import com.warehouse.model.MovementItem;
import com.warehouse.model.Product;
import com.warehouse.model.Warehouse;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovementsView {
    private TableView<Movement> tableView;
    private ObservableList<Movement> data = FXCollections.observableArrayList();
    private MovementDao movementDao = new MovementDao();
    private WarehouseDao warehouseDao = new WarehouseDao();
    private ProductDao productDao = new ProductDao();

    public MovementsView() {
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        tableView = new TableView<>();
        tableView.setItems(data);

        TableColumn<Movement, String> docNumCol = new TableColumn<>("Номер документа");
        docNumCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDocNumber()));
        docNumCol.setPrefWidth(150);

        TableColumn<Movement, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDocDate().toString()));
        dateCol.setPrefWidth(180);

        TableColumn<Movement, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getMovementType();
            String display = switch (type) {
                case "in" -> "📥 Приход";
                case "out" -> "📤 Расход";
                case "transfer" -> "🔄 Перемещение";
                default -> type;
            };
            return new SimpleStringProperty(display);
        });
        typeCol.setPrefWidth(150);

        tableView.getColumns().addAll(docNumCol, dateCol, typeCol);
    }

    private void loadData() {
        try {
            data.setAll(movementDao.getAll());
        } catch (SQLException e) {
            showError("Ошибка загрузки данных", e.getMessage());
        }
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label title = new Label("Документы движения");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button addBtn = new Button("➕ Создать документ");
        addBtn.setOnAction(e -> showCreateDialog());

        Button refreshBtn = new Button("🔄 Обновить");
        refreshBtn.setOnAction(e -> loadData());

        HBox controls = new HBox(10, title, addBtn, refreshBtn);
        controls.setPadding(new Insets(0, 0, 10, 0));

        vbox.getChildren().addAll(controls, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        return vbox;
    }

    private void showCreateDialog() {
        Dialog<Movement> dialog = new Dialog<>();
        dialog.setTitle("Создать документ движения");
        dialog.setHeaderText("Введите данные документа");

        ButtonType saveButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField docNumberField = new TextField();
        docNumberField.setPromptText("Номер документа");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("in - Приход", "out - Расход", "transfer - Перемещение");

        ComboBox<Warehouse> warehouseCombo = new ComboBox<>();
        try {
            warehouseCombo.getItems().setAll(warehouseDao.getAll());
        } catch (SQLException e) {
            showError("Ошибка", "Не удалось загрузить склады");
        }

        grid.add(new Label("Номер документа:"), 0, 0);
        grid.add(docNumberField, 1, 0);
        grid.add(new Label("Тип движения:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Склад:"), 0, 2);
        grid.add(warehouseCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Movement movement = new Movement();
                movement.setDocNumber(docNumberField.getText());
                movement.setDocDate(LocalDateTime.now());
                String typeStr = typeCombo.getValue();
                if (typeStr != null) {
                    movement.setMovementType(typeStr.split(" ")[0]);
                }
                Warehouse wh = warehouseCombo.getValue();
                if (wh != null) {
                    movement.setWarehouseId(wh.getId());
                }
                return movement;
            }
            return null;
        });

        Optional<Movement> result = dialog.showAndWait();
        result.ifPresent(movement -> {
            try {
                List<MovementItem> items = createMovementItems();
                if (!items.isEmpty()) {
                    movementDao.save(movement, items);
                    showInfo("Документ успешно создан");
                    loadData();
                }
            } catch (SQLException e) {
                showError("Ошибка создания документа", e.getMessage());
            }
        });
    }

    private List<MovementItem> createMovementItems() {
        List<MovementItem> items = new ArrayList<>();

        Dialog<List<MovementItem>> dialog = new Dialog<>();
        dialog.setTitle("Добавить позиции");
        dialog.setHeaderText("Добавьте товары в документ");

        ButtonType doneButtonType = new ButtonType("Готово", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(doneButtonType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TableView<MovementItem> itemsTable = new TableView<>();
        ObservableList<MovementItem> itemsData = FXCollections.observableArrayList();
        itemsTable.setItems(itemsData);

        TableColumn<MovementItem, String> skuCol = new TableColumn<>("Артикул");
        skuCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductSku()));
        skuCol.setPrefWidth(100);

        TableColumn<MovementItem, String> nameCol = new TableColumn<>("Товар");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        nameCol.setPrefWidth(200);

        TableColumn<MovementItem, String> qtyCol = new TableColumn<>("Количество");
        qtyCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getQuantity().toString()));
        qtyCol.setPrefWidth(100);

        itemsTable.getColumns().addAll(skuCol, nameCol, qtyCol);

        Button addItemBtn = new Button("Добавить товар");
        addItemBtn.setOnAction(e -> {
            try {
                List<Product> products = productDao.getAll();
                ChoiceDialog<Product> productDialog = new ChoiceDialog<>(products.get(0), products);
                productDialog.setTitle("Выбор товара");
                productDialog.setHeaderText("Выберите товар");

                Optional<Product> result = productDialog.showAndWait();
                result.ifPresent(product -> {
                    TextInputDialog qtyDialog = new TextInputDialog("1");
                    qtyDialog.setTitle("Количество");
                    qtyDialog.setHeaderText("Введите количество для товара: " + product.getName());

                    Optional<String> qtyResult = qtyDialog.showAndWait();
                    qtyResult.ifPresent(qtyStr -> {
                        try {
                            BigDecimal qty = new BigDecimal(qtyStr);
                            MovementItem item = new MovementItem(null, product.getId(),
                                    product.getName(), product.getSku(), qty);
                            itemsData.add(item);
                        } catch (NumberFormatException ex) {
                            showError("Ошибка", "Неверный формат количества");
                        }
                    });
                });
            } catch (SQLException ex) {
                showError("Ошибка", "Не удалось загрузить товары");
            }
        });

        content.getChildren().addAll(itemsTable, addItemBtn);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == doneButtonType) {
                return itemsData;
            }
            return null;
        });

        Optional<List<MovementItem>> result = dialog.showAndWait();
        return result.orElse(new ArrayList<>());
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
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