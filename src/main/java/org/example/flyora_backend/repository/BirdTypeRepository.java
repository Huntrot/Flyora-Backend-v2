package org.example.flyora_backend.repository;

import org.example.flyora_backend.model.BirdType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BirdTypeRepository extends JpaRepository<BirdType, Integer>{
    
}
