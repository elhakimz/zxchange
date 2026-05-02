package com.zxchange.repository;

import com.zxchange.model.entity.MockAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockAccountRepository extends JpaRepository<MockAccountEntity, String> {
}
