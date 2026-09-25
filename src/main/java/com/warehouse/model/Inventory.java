package com.warehouse.model;

import java.math.BigDecimal;

public class Inventory {
    private Long warehouseId;
    private Long productId;
    private String warehouseName;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private BigDecimal minStockLevel;

    public Inventory() {}

    // Getters и Setters
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(BigDecimal minStockLevel) { this.minStockLevel = minStockLevel; }

    public boolean isBelowMinLevel() {
        return quantity.compareTo(minStockLevel) <= 0;
    }
}