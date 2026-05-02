package com.zxchange.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mock_account")
public class MockAccountEntity {
    @Id
    private String id = "PRIMARY";

    @Column(nullable = false)
    private Double cash = 100000.0;

    @Column(nullable = false)
    private Double startingEquity = 100000.0;

    public MockAccountEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Double getCash() { return cash; }
    public void setCash(Double cash) { this.cash = cash; }

    public Double getStartingEquity() { return startingEquity; }
    public void setStartingEquity(Double startingEquity) { this.startingEquity = startingEquity; }
}
