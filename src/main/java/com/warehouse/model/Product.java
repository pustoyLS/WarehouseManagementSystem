package com.warehouse.model;

import java.math.BigDecimal;

public class Product {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal minStockLevel;

    public Product() {}

    public Product(Long id, String sku, String name, String category, BigDecimal minStockLevel) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.minStockLevel = minStockLevel;
    }

    // Getters и Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(BigDecimal minStockLevel) { this.minStockLevel = minStockLevel; }

    @Override
    public String toString() {
        return sku + " - " + name;
    }
}