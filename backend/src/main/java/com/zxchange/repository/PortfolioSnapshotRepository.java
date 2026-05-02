package com.zxchange.repository;

import com.zxchange.model.entity.PortfolioSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshotEntity, Long> {
    
    @Query("SELECT p FROM PortfolioSnapshotEntity p ORDER BY p.recordedAt DESC")
    List<PortfolioSnapshotEntity> findLatestSnapshots();
}
