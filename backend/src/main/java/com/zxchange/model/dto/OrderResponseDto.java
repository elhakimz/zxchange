package com.zxchange.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponseDto {
    @JsonProperty("id") private String id;
    @JsonProperty("client_order_id") private String clientOrderId;
    @JsonProperty("symbol") private String symbol;
    @JsonProperty("side") private String side;
    @JsonProperty("type") private String type;
    @JsonProperty("time_in_force") private String timeInForce;
    @JsonProperty("qty") private Double qty;
    @JsonProperty("filled_qty") private Integer filledQty;
    @JsonProperty("filled_avg_price") private Double filledAvgPrice;
    @JsonProperty("filled_at") private String filledAt;
    @JsonProperty("expired_at") private String expiredAt;
    @JsonProperty("canceled_at") private String canceledAt;
    @JsonProperty("submitted_at") private String submittedAt;
    @JsonProperty("limit_price") private Double limitPrice;
    @JsonProperty("stop_price") private Double stopPrice;
    @JsonProperty("status") private String status;
    @JsonProperty("created_at") private String createdAt;
    @JsonProperty("updated_at") private String updatedAt;

    public OrderResponseDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public Integer getFilledQty() { return filledQty; }
    public void setFilledQty(Integer filledQty) { this.filledQty = filledQty; }
    public Double getFilledAvgPrice() { return filledAvgPrice; }
    public void setFilledAvgPrice(Double filledAvgPrice) { this.filledAvgPrice = filledAvgPrice; }
    public String getFilledAt() { return filledAt; }
    public void setFilledAt(String filledAt) { this.filledAt = filledAt; }
    public String getExpiredAt() { return expiredAt; }
    public void setExpiredAt(String expiredAt) { this.expiredAt = expiredAt; }
    public String getCanceledAt() { return canceledAt; }
    public void setCanceledAt(String canceledAt) { this.canceledAt = canceledAt; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public Double getLimitPrice() { return limitPrice; }
    public void setLimitPrice(Double limitPrice) { this.limitPrice = limitPrice; }
    public Double getStopPrice() { return stopPrice; }
    public void setStopPrice(Double stopPrice) { this.stopPrice = stopPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}