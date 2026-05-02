package com.zxchange.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BarDto(
    @JsonProperty("S") String symbol,
    @JsonProperty("t") String timestamp,
    @JsonProperty("o") double open,
    @JsonProperty("h") double high,
    @JsonProperty("l") double low,
    @JsonProperty("c") double close,
    @JsonProperty("v") long volume,
    @JsonProperty("vw") double vwap,
    @JsonProperty("n") int tradeCount
) {}
