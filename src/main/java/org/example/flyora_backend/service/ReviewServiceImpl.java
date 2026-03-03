package org.example.flyora_backend.service;

import java.util.List;
import java.util.Map;
import org.example.flyora_backend.DTOs.ProductReviewDTO;
import org.example.flyora_backend.model.Customer;
import org.example.flyora_backend.model.Product;
import org.example.flyora_backend.model.ProductReview;
import org.example.flyora_backend.repository.CustomerRepository;
import org.example.flyora_backend.repository.ProductRepository;
import org.example.flyora_backend.repository.ProductReviewRepository;
import org.example.flyora_backend.Utils.IdGeneratorUtil;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository; // Cần repo này để tránh lỗi
    private final CustomerRepository customerRepository;
    private final IdGeneratorUtil idGeneratorUtil;

    @Override
    public Map<String, Object> submitReview(ProductReviewDTO dto) {
        ProductReview review = new ProductReview();
        review.setId(idGeneratorUtil.generateProductReviewId());

        // Load đầy đủ các đối tượng từ DB để đảm bảo tính nhất quán
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        
        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(dto.getRating());
        review.setReview(dto.getComment());

        reviewRepository.save(review);

        return Map.of("message", "Đánh giá sản phẩm thành công");
    }

    @Override
    public List<ProductReviewDTO> getReviewsByProduct(Integer productId) {
        // Sử dụng một phương thức truy vấn tối ưu hơn để tránh N+1 Select
        List<ProductReview> reviews = reviewRepository.findByProductIdWithCustomer(productId);
        
        return reviews.stream().map(review -> new ProductReviewDTO(
            review.getCustomer().getId(),
            review.getProduct().getId(),
            review.getRating(),
            review.getReview(),
            review.getCustomer().getName() // **QUAN TRỌNG: Thêm tên khách hàng vào đây**
        )).toList();
    }
}
