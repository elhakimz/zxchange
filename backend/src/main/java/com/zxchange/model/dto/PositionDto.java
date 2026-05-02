package com.zxchange.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PositionDto(
    @JsonProperty("symbol") String symbol,
    @JsonProperty("qty") Double qty,
    @JsonProperty("side") String side,
    @JsonProperty("avg_entry_price") Double avgEntryPrice,
    @JsonProperty("current_price") Double currentPrice,
    @JsonProperty("market_value") Double marketValue,
    @JsonProperty("cost_basis") Double costBasis,
    @JsonProperty("unrealized_pl") Double unrealizedPl,
    @JsonProperty("unrealized_plpc") Double unrealizedPlpc
) {}