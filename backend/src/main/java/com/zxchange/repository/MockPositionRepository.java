package com.zxchange.repository;

import com.zxchange.model.entity.MockPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MockPositionRepository extends JpaRepository<MockPositionEntity, Long> {
    Optional<MockPositionEntity> findBySymbol(String symbol);
}
