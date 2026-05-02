package com.zxchange.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "watchlists")
public class WatchlistEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WatchlistSymbolEntity> symbols = new ArrayList<>();

    public WatchlistEntity() {}
    public WatchlistEntity(Long id, String name, LocalDateTime createdAt, List<WatchlistSymbolEntity> symbols) {
        this.id = id; this.name = name; this.createdAt = createdAt; this.symbols = symbols != null ? symbols : new ArrayList<>();
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<WatchlistSymbolEntity> getSymbols() { return symbols; }
    public void setSymbols(List<WatchlistSymbolEntity> symbols) { this.symbols = symbols; }
}
