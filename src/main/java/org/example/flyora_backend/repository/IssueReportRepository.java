package org.example.flyora_backend.repository;

import org.example.flyora_backend.model.IssueReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueReportRepository extends JpaRepository<IssueReport, Integer> {
    void deleteByCustomerId(Integer customerId);

}
