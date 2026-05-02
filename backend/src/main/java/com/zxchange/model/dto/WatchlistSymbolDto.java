package com.zxchange.model.dto;

import java.time.LocalDateTime;

public record WatchlistSymbolDto(
    Long id,
    String symbol,
    int positionIndex,
    LocalDateTime addedAt
) {}
