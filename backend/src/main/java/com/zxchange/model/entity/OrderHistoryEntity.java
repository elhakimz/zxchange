package com.zxchange.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
public class OrderHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "alpaca_order_id", nullable = false, unique = true)
    private String alpacaOrderId;
    @Column(name = "client_order_id")
    private String clientOrderId;
    @Column(nullable = false)
    private String symbol;
    @Column(nullable = false)
    private String side;
    @Column(nullable = false)
    private String type;
    @Column(name = "time_in_force", nullable = false)
    private String timeInForce;
    private Double qty;
    private Double notional;
    @Column(name = "limit_price")
    private Double limitPrice;
    @Column(name = "stop_price")
    private Double stopPrice;
    @Column(name = "filled_qty")
    private Double filledQty;
    @Column(name = "filled_avg_price")
    private Double filledAvgPrice;
    @Column(nullable = false)
    private String status;
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    @Column(name = "filled_at")
    private LocalDateTime filledAt;
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
    @Lob
    @Column(name = "raw_json")
    private String rawJson;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public OrderHistoryEntity() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAlpacaOrderId() { return alpacaOrderId; }
    public void setAlpacaOrderId(String alpacaOrderId) { this.alpacaOrderId = alpacaOrderId; }
    public String getClientOrderId() { return clientOrderId; }
    public void setClientOrderId(String clientOrderId) { this.clientOrderId = clientOrderId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTimeInForce() { return timeInForce; }
    public void setTimeInForce(String timeInForce) { this.timeInForce = timeInForce; }
    public Double getQty() { return qty; }
    public void setQty(Double qty) { this.qty = qty; }
    public Double getNotional() { return notional; }
    public void setNotional(Double notional) { this.notional = notional; }
    public Double getLimitPrice() { return limitPrice; }
    public void setLimitPrice(Double limitPrice) { this.limitPrice = limitPrice; }
    public Double getStopPrice() { return stopPrice; }
    public void setStopPrice(Double stopPrice) { this.stopPrice = stopPrice; }
    public Double getFilledQty() { return filledQty; }
    public void setFilledQty(Double filledQty) { this.filledQty = filledQty; }
    public Double getFilledAvgPrice() { return filledAvgPrice; }
    public void setFilledAvgPrice(Double filledAvgPrice) { this.filledAvgPrice = filledAvgPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getFilledAt() { return filledAt; }
    public void setFilledAt(LocalDateTime filledAt) { this.filledAt = filledAt; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(LocalDateTime canceledAt) { this.canceledAt = canceledAt; }
    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
