package com.zxchange.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountDto(
    String id,
    @JsonProperty("account_number")
    String accountNumber,
    String status,
    String currency,
    @JsonProperty("buying_power")
    String buyingPower,
    String cash,
    @JsonProperty("portfolio_value")
    String portfolioValue,
    String equity,
    @JsonProperty("long_market_value")
    String longMarketValue,
    @JsonProperty("short_market_value")
    String shortMarketValue,
    @JsonProperty("initial_margin")
    String initialMargin,
    @JsonProperty("maintenance_margin")
    String maintenanceMargin,
    @JsonProperty("last_equity")
    String lastEquity,
    @JsonProperty("daytop_pnl")
    String daytopPnl
) {}
