package com.zxchange.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_symbols", uniqueConstraints = {@UniqueConstraint(columnNames = {"watchlist_id", "symbol"})})
public class WatchlistSymbolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_id", nullable = false)
    private WatchlistEntity watchlist;
    @Column(nullable = false)
    private String symbol;
    @Column(name = "position_index")
    private int positionIndex;
    @CreationTimestamp
    @Column(name = "added_at")
    private LocalDateTime addedAt;

    public WatchlistSymbolEntity() {}
    public WatchlistSymbolEntity(Long id, WatchlistEntity watchlist, String symbol, int positionIndex, LocalDateTime addedAt) {
        this.id = id; this.watchlist = watchlist; this.symbol = symbol; this.positionIndex = positionIndex; this.addedAt = addedAt;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public WatchlistEntity getWatchlist() { return watchlist; }
    public void setWatchlist(WatchlistEntity watchlist) { this.watchlist = watchlist; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public int getPositionIndex() { return positionIndex; }
    public void setPositionIndex(int positionIndex) { this.positionIndex = positionIndex; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
