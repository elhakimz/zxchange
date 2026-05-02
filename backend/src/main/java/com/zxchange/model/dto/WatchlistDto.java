package com.zxchange.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WatchlistDto(
    Long id,
    String name,
    LocalDateTime createdAt,
    List<WatchlistSymbolDto> symbols
) {}
