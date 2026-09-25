package com.warehouse;

import com.warehouse.config.DatabaseConfig;
import com.warehouse.dao.WarehouseDao;
import com.warehouse.model.Warehouse;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Активная СУБД: " + DatabaseConfig.getActiveDatabase());

            WarehouseDao warehouseDao = new WarehouseDao();
            List<Warehouse> warehouses = warehouseDao.getAll();

            System.out.println("\nСписок складов:");
            for (Warehouse w : warehouses) {
                System.out.println(w.getId() + ": " + w.getName() + " - " + w.getAddress());
            }

            System.out.println("\nПодключение к БД работает!");

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}