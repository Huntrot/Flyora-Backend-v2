package org.example.flyora_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.flyora_backend.DTOs.PromotionDTO;
import org.example.flyora_backend.model.Promotion;
import org.example.flyora_backend.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public List<PromotionDTO> getAllPromotions(Integer customerId) {
        List<Promotion> promotions = (customerId != null)
            ? promotionRepository.findByCustomer_Id(customerId)
            : promotionRepository.findAll();

        return promotions.stream()
            .map(p -> new PromotionDTO(
                p.getId(),
                p.getCode(),
                p.getDiscount(),
                p.getProduct() != null ? p.getProduct().getId() : null,
                p.getCustomer() != null ? p.getCustomer().getId() : null
            ))
            .collect(Collectors.toList());
    }
}