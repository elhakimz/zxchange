package com.zxchange.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderRequestDto(
    @JsonProperty("symbol") String symbol,
    @JsonProperty("qty") Double qty,
    @JsonProperty("side") String side,
    @JsonProperty("type") String type,
    @JsonProperty("time_in_force") String timeInForce,
    @JsonProperty("limit_price") Double limitPrice,
    @JsonProperty("stop_price") Double stopPrice,
    @JsonProperty("client_order_id") String clientOrderId
) {}