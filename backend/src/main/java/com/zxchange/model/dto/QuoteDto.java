package com.zxchange.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuoteDto(
    String symbol,
    Double bidPrice,
    Integer bidSize,
    String bidExchange,
    Double askPrice,
    Integer askSize,
    String askExchange,
    String timestamp,
    Double change,
    Double changePercent
) {}
