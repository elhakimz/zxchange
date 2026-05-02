package com.zxchange.repository;

import com.zxchange.model.entity.WatchlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistEntity, Long> {
    @Query("SELECT DISTINCT w FROM WatchlistEntity w LEFT JOIN FETCH w.symbols")
    List<WatchlistEntity> findAllWithSymbols();
}
