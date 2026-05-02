package com.zxchange.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BarsResponseDto(
    String symbol,
    List<BarDto> bars,
    @JsonProperty("next_page_token")
    String nextPageToken
) {}
