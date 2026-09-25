package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.Inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {

    public List<Inventory> getAll() throws SQLException {
        List<Inventory> inventoryList = new ArrayList<>();

        // Универсальный SQL для обеих СУБД
        String sql = """
            SELECT 
                i.warehouse_id,
                i.product_id,
                w.name AS warehouse_name,
                p.name AS product_name,
                p.sku AS product_sku,
                i.quantity,
                p.min_stock_level
            FROM inventory i
            JOIN warehouses w ON i.warehouse_id = w.id
            JOIN products p ON i.product_id = p.id
            ORDER BY w.name, p.name
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setWarehouseId(rs.getLong("warehouse_id"));
                inv.setProductId(rs.getLong("product_id"));
                inv.setWarehouseName(rs.getString("warehouse_name"));
                inv.setProductName(rs.getString("product_name"));
                inv.setProductSku(rs.getString("product_sku"));
                inv.setQuantity(rs.getBigDecimal("quantity"));
                inv.setMinStockLevel(rs.getBigDecimal("min_stock_level"));
                inventoryList.add(inv);
            }
        }
        return inventoryList;
    }

    public List<Inventory> getBelowMinLevel() throws SQLException {
        List<Inventory> inventoryList = new ArrayList<>();

        String sql = """
            SELECT 
                i.warehouse_id,
                i.product_id,
                w.name AS warehouse_name,
                p.name AS product_name,
                p.sku AS product_sku,
                i.quantity,
                p.min_stock_level
            FROM inventory i
            JOIN warehouses w ON i.warehouse_id = w.id
            JOIN products p ON i.product_id = p.id
            WHERE i.quantity <= p.min_stock_level
            ORDER BY w.name, p.name
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Inventory inv = new Inventory();
                inv.setWarehouseId(rs.getLong("warehouse_id"));
                inv.setProductId(rs.getLong("product_id"));
                inv.setWarehouseName(rs.getString("warehouse_name"));
                inv.setProductName(rs.getString("product_name"));
                inv.setProductSku(rs.getString("product_sku"));
                inv.setQuantity(rs.getBigDecimal("quantity"));
                inv.setMinStockLevel(rs.getBigDecimal("min_stock_level"));
                inventoryList.add(inv);
            }
        }
        return inventoryList;
    }
}