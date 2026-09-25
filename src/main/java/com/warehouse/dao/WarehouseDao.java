package com.warehouse.dao;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.model.Warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDao {

    public List<Warehouse> getAll() throws SQLException {
        List<Warehouse> warehouses = new ArrayList<>();
        String sql = "SELECT id, name, address FROM warehouses ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Warehouse warehouse = new Warehouse(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("address")
                );
                warehouses.add(warehouse);
            }
        }
        return warehouses;
    }

    public Warehouse getById(Long id) throws SQLException {
        String sql = "SELECT id, name, address FROM warehouses WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Warehouse(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("address")
                    );
                }
            }
        }
        return null;
    }

    public void save(Warehouse warehouse) throws SQLException {
        String sql = "INSERT INTO warehouses (name, address) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, warehouse.getName());
            stmt.setString(2, warehouse.getAddress());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    warehouse.setId(keys.getLong(1));
                }
            }
        }
    }

    public void update(Warehouse warehouse) throws SQLException {
        String sql = "UPDATE warehouses SET name = ?, address = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, warehouse.getName());
            stmt.setString(2, warehouse.getAddress());
            stmt.setLong(3, warehouse.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM warehouses WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}