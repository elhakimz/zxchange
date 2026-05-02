package com.zxchange.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mock_positions")
public class MockPositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private Double qty = 0.0;

    @Column(name = "avg_entry_price", nullable = false)
    private Double avgEntryPrice = 0.0;

    public MockPositionEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getQty() { return qty; }
    public void setQty(Double qty) { this.qty = qty; }

    public Double getAvgEntryPrice() { return avgEntryPrice; }
    public void setAvgEntryPrice(Double avgEntryPrice) { this.avgEntryPrice = avgEntryPrice; }
}
