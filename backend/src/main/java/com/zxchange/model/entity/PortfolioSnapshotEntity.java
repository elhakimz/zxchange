package com.zxchange.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_snapshots")
public class PortfolioSnapshotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double equity;
    @Column(nullable = false)
    private Double cash;
    @Column(name = "pl_day")
    private Double plDay;
    @CreationTimestamp
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    public PortfolioSnapshotEntity() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getEquity() { return equity; }
    public void setEquity(Double equity) { this.equity = equity; }
    public Double getCash() { return cash; }
    public void setCash(Double cash) { this.cash = cash; }
    public Double getPlDay() { return plDay; }
    public void setPlDay(Double plDay) { this.plDay = plDay; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
