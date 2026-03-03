package org.example.flyora_backend.service;

import java.time.Instant;

import org.example.flyora_backend.model.AccessLog;
import org.example.flyora_backend.model.Account;
import org.example.flyora_backend.repository.AccessLogRepository;
import org.example.flyora_backend.repository.AccountRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;
    private final AccountRepository accountRepository;

    public void logAction(Integer accountId, String action) {
        Account acc = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Integer newId = accessLogRepository.findAll().stream()
                .mapToInt(log -> log.getId() != null ? log.getId() : 0)
                .max()
                .orElse(0) + 1;

        AccessLog log = new AccessLog();
        log.setId(newId);
        log.setAccount(acc);
        log.setAction(action);
        log.setTimestamp(Instant.now());

        accessLogRepository.save(log);
    }
}

