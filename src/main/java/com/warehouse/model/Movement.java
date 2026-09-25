package com.warehouse.model;

import java.time.LocalDateTime;

public class Movement {
    private Long id;
    private String docNumber;
    private LocalDateTime docDate;
    private String movementType; // "in", "out", "transfer"
    private Long warehouseId;
    private Long contractorId;

    public Movement() {}

    public Movement(Long id, String docNumber, LocalDateTime docDate, String movementType,
                    Long warehouseId, Long contractorId) {
        this.id = id;
        this.docNumber = docNumber;
        this.docDate = docDate;
        this.movementType = movementType;
        this.warehouseId = warehouseId;
        this.contractorId = contractorId;
    }

    // Getters и Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }
    public LocalDateTime getDocDate() { return docDate; }
    public void setDocDate(LocalDateTime docDate) { this.docDate = docDate; }
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getContractorId() { return contractorId; }
    public void setContractorId(Long contractorId) { this.contractorId = contractorId; }

    @Override
    public String toString() {
        return docNumber + " (" + movementType + ")";
    }
}