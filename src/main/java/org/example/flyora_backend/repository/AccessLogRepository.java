package org.example.flyora_backend.repository;

import org.example.flyora_backend.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Integer> {
    void deleteByAccountId(Integer accountId);
}
