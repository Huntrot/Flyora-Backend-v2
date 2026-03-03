package org.example.flyora_backend.repository;

import java.util.Optional;

import org.example.flyora_backend.model.Account; // Giả sử dùng package model (đồng nhất với main)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    boolean existsByUsername(String username);

    Optional<Account> findByUsernameAndPassword(String username, String password);

    Optional<Account> findByUsername(String username);

    @Query("SELECT MAX(a.id) FROM Account a")
    Optional<Integer> findMaxId(); // 👉 để tạo id thủ công
}