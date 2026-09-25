package com.warehouse.model;

import java.math.BigDecimal;

public class MovementItem {
    private Long movementId;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;

    public MovementItem() {}

    public MovementItem(Long movementId, Long productId, String productName,
                        String productSku, BigDecimal quantity) {
        this.movementId = movementId;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.quantity = quantity;
    }

    // Getters и Setters
    public Long getMovementId() { return movementId; }
    public void setMovementId(Long movementId) { this.movementId = movementId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}