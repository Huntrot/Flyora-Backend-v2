package org.example.flyora_backend.repository;

import java.util.Optional;
import org.example.flyora_backend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByAccountId(Integer accountId);

    @Query("SELECT MAX(a.id) FROM Admin a")
    Optional<Integer> findMaxId();

    void deleteByAccountId(Integer accountId);
}
