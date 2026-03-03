package org.example.flyora_backend.repository;

import java.util.List;
import java.util.Optional;

import org.example.flyora_backend.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {

    // Sắp xếp các log theo thời gian mới nhất
    List<SystemLog> findAllByOrderByCreatedAtDesc();
    
    // Tìm kiếm các log của một admin cụ thể
    @Query("SELECT sl FROM SystemLog sl JOIN FETCH sl.admin WHERE sl.admin.id = :adminId ORDER BY sl.createdAt DESC")
    List<SystemLog> findByAdminIdWithAdmin(Integer adminId);

    @Query("SELECT MAX(f.id) FROM SystemLog f")
    Optional<Integer> findMaxId();

    void deleteByAdminAccountId(Integer accountId);

}