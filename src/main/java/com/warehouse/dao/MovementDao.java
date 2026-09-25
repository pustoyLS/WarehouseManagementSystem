package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.Movement;
import com.warehouse.model.MovementItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovementDao {

    public List<Movement> getAll() throws SQLException {
        List<Movement> movements = new ArrayList<>();
        String sql = "SELECT id, doc_number, doc_date, movement_type, warehouse_id, contractor_id " +
                "FROM movements ORDER BY doc_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movement movement = new Movement(
                        rs.getLong("id"),
                        rs.getString("doc_number"),
                        rs.getTimestamp("doc_date").toLocalDateTime(),
                        rs.getString("movement_type"),
                        rs.getLong("warehouse_id"),
                        rs.getObject("contractor_id") != null ? rs.getLong("contractor_id") : null
                );
                movements.add(movement);
            }
        }
        return movements;
    }

    public Long save(Movement movement, List<MovementItem> items) throws SQLException {
        Connection conn = DatabaseConfig.getConnection();
        try {
            conn.setAutoCommit(false);

            // Сохраняем шапку документа
            String sqlMovement = "INSERT INTO movements (doc_number, doc_date, movement_type, warehouse_id, contractor_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
            Long movementId;
            try (PreparedStatement stmt = conn.prepareStatement(sqlMovement, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, movement.getDocNumber());
                stmt.setTimestamp(2, Timestamp.valueOf(movement.getDocDate()));
                stmt.setString(3, movement.getMovementType());
                stmt.setLong(4, movement.getWarehouseId());
                if (movement.getContractorId() != null) {
                    stmt.setLong(5, movement.getContractorId());
                } else {
                    stmt.setNull(5, Types.BIGINT);
                }
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        movementId = keys.getLong(1);
                    } else {
                        throw new SQLException("Не удалось получить ID документа");
                    }
                }
            }

            // Сохраняем позиции
            String sqlItem = "INSERT INTO movement_items (movement_id, product_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlItem)) {
                for (MovementItem item : items) {
                    stmt.setLong(1, movementId);
                    stmt.setLong(2, item.getProductId());
                    stmt.setBigDecimal(3, item.getQuantity());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            // Обновляем остатки
            updateInventory(conn, movement.getMovementType(), movement.getWarehouseId(), items);

            conn.commit();
            return movementId;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void updateInventory(Connection conn, String movementType, Long warehouseId,
                                 List<MovementItem> items) throws SQLException {
        for (MovementItem item : items) {
            String checkSql = "SELECT quantity FROM inventory WHERE warehouse_id = ? AND product_id = ?";
            BigDecimal currentQty = BigDecimal.ZERO;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setLong(1, warehouseId);
                checkStmt.setLong(2, item.getProductId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        currentQty = rs.getBigDecimal("quantity");
                    }
                }
            }

            BigDecimal newQty;
            if (movementType.equals("in")) {
                newQty = currentQty.add(item.getQuantity());
            } else if (movementType.equals("out")) {
                newQty = currentQty.subtract(item.getQuantity());
                if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("Недостаточно товара на складе для расхода");
                }
            } else {
                newQty = currentQty; // Для transfer логика сложнее, упрощаем
            }

            if (currentQty.compareTo(BigDecimal.ZERO) == 0) {
                // Вставляем новую запись
                String insertSql = "INSERT INTO inventory (warehouse_id, product_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setLong(1, warehouseId);
                    insertStmt.setLong(2, item.getProductId());
                    insertStmt.setBigDecimal(3, newQty);
                    insertStmt.executeUpdate();
                }
            } else {
                // Обновляем существующую
                String updateSql = "UPDATE inventory SET quantity = ? WHERE warehouse_id = ? AND product_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setBigDecimal(1, newQty);
                    updateStmt.setLong(2, warehouseId);
                    updateStmt.setLong(3, item.getProductId());
                    updateStmt.executeUpdate();
                }
            }
        }
    }
}