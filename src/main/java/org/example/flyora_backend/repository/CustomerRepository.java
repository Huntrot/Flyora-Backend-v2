package org.example.flyora_backend.repository;

import java.util.Optional;
import org.example.flyora_backend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByAccountId(Integer accountId);

    @Query("SELECT MAX(c.id) FROM Customer c")
    Optional<Integer> findMaxId(); // 👉 để tạo id thủ công

    void deleteByAccountId(Integer accountId);

}