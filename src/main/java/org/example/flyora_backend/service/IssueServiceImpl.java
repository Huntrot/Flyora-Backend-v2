package org.example.flyora_backend.service;

import java.util.Map;

import org.example.flyora_backend.DTOs.IssueReportDTO;
import org.example.flyora_backend.model.Customer;
import org.example.flyora_backend.model.IssueReport;
import org.example.flyora_backend.model.Order;
import org.example.flyora_backend.repository.IssueReportRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueReportRepository issueRepository;

    @Override
    public Map<String, Object> submitIssue(IssueReportDTO dto) {
        IssueReport issue = new IssueReport();

        Customer customer = new Customer();
        customer.setId(dto.getCustomerId());
        issue.setCustomer(customer);

        Order order = new Order();
        order.setId(dto.getOrderId());
        issue.setOrder(order);

        issue.setContent(dto.getContent());

        issueRepository.save(issue);

        return Map.of("message", "Gửi báo lỗi thành công");
    }
}
