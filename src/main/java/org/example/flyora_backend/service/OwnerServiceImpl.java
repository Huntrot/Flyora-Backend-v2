
    package org.example.flyora_backend.service;

    import java.util.List;
    import org.example.flyora_backend.DTOs.CreateProductDTO;
    import org.example.flyora_backend.DTOs.OwnerProductListDTO;
    import org.example.flyora_backend.DTOs.TopProductDTO;
    import org.example.flyora_backend.model.BirdType;
    import org.example.flyora_backend.model.FoodDetail;
    import org.example.flyora_backend.model.FurnitureDetail;
    import org.example.flyora_backend.model.Product;
    import org.example.flyora_backend.model.ProductCategory;
    import org.example.flyora_backend.model.ShopOwner;
    import org.example.flyora_backend.model.ToyDetail;
    import org.example.flyora_backend.repository.BirdTypeRepository;
    import org.example.flyora_backend.repository.FoodDetailRepository;
    import org.example.flyora_backend.repository.FurnitureDetailRepository;
    import org.example.flyora_backend.repository.InventoryRepository;
    import org.example.flyora_backend.repository.OrderItemRepository;
    import org.example.flyora_backend.repository.ProductCategoryRepository;
    import org.example.flyora_backend.repository.ProductRepository;
    import org.example.flyora_backend.repository.ProductReviewRepository;
    import org.example.flyora_backend.repository.PromotionRepository;
    import org.example.flyora_backend.repository.ShopOwnerRepository;
    import org.example.flyora_backend.repository.ToyDetailRepository;
    import org.example.flyora_backend.Utils.IdGeneratorUtil;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import lombok.RequiredArgsConstructor;

    @RequiredArgsConstructor
    @Service
    public class OwnerServiceImpl implements OwnerService {

        @Autowired
        private ShopOwnerRepository shopOwnerRepository;
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private ProductCategoryRepository categoryRepository;
        @Autowired
        private BirdTypeRepository birdTypeRepository;
        @Autowired
        private FoodDetailRepository foodDetailRepository;
        @Autowired
        private ToyDetailRepository toyDetailRepository;
        @Autowired
        private FurnitureDetailRepository furnitureDetailRepository;
        @Autowired
        private IdGeneratorUtil idGeneratorUtil;
        @Autowired
        private InventoryRepository inventoryRepository;
        @Autowired
        private OrderItemRepository orderItemRepository;
        @Autowired
        private PromotionRepository promotionRepository;
        @Autowired
        private ProductReviewRepository productReviewRepository;
        
        @Override
        public List<TopProductDTO> getTopSellingProducts() {
            return productRepository.findTopSellingProductsByShopOwner();

        }

        @Override
        public Product createProduct(CreateProductDTO dto, Integer accountId) {
            ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Loại sản phẩm không hợp lệ"));

            BirdType birdType = birdTypeRepository.findById(dto.getBirdTypeId())
                    .orElseThrow(() -> new RuntimeException("Loại chim không hợp lệ"));

            ShopOwner shopOwner = shopOwnerRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new RuntimeException("ShopOwner không tồn tại"));
            Integer newProductId = idGeneratorUtil.generateProductId();

            Product product = Product.builder()
                    .id(newProductId)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .price(dto.getPrice())
                    .stock(dto.getStock())
                    .category(category)
                    .birdType(birdType)
                    .salesCount(0)
                    .status(true)
                    .shopOwner(shopOwner)
                    .build();

            productRepository.save(product);

            switch (category.getName().toUpperCase()) {
                case "FOODS" -> {
                    FoodDetail detail = new FoodDetail();
                    detail.setId(idGeneratorUtil.generateFoodDetailId());
                    detail.setProduct(product);
                    detail.setMaterial(dto.getMaterial());
                    detail.setOrigin(dto.getOrigin());
                    detail.setUsageTarget(dto.getUsageTarget());
                    detail.setWeight(dto.getWeight());
                    detail.setImageUrl(dto.getImageUrl());
                    foodDetailRepository.save(detail);
                }
                case "TOYS" -> {
                    ToyDetail detail = new ToyDetail();
                    detail.setId(idGeneratorUtil.generateToyDetailId());
                    detail.setProduct(product);
                    detail.setMaterial(dto.getMaterial());
                    detail.setOrigin(dto.getOrigin());
                    detail.setColor(dto.getColor());
                    detail.setDimensions(dto.getDimensions());
                    detail.setWeight(dto.getWeight());
                    detail.setImageUrl(dto.getImageUrl());
                    toyDetailRepository.save(detail);
                }
                case "FURNITURE" -> {
                    FurnitureDetail detail = new FurnitureDetail();
                    detail.setId(idGeneratorUtil.generateFurnitureDetailId());
                    detail.setProduct(product);
                    detail.setMaterial(dto.getMaterial());
                    detail.setOrigin(dto.getOrigin());
                    detail.setColor(dto.getColor());
                    detail.setDimensions(dto.getDimensions());
                    detail.setWeight(dto.getWeight());
                    detail.setImageUrl(dto.getImageUrl());
                    furnitureDetailRepository.save(detail);
                }
                default -> throw new RuntimeException("Loại sản phẩm không hỗ trợ");
            }

            return product;
        }

        @Override
        public List<OwnerProductListDTO> getAllProductsByOwner(int accountId) {
            ShopOwner owner = shopOwnerRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new RuntimeException("ShopOwner không tồn tại"));
            return productRepository.findAllByShopOwnerIdOrderByIdAsc(owner.getId());
        }

        @Override
        public Product updateProduct(Integer productId, CreateProductDTO dto, Integer accountId) {
            // 1. Lấy sản phẩm hiện có từ database
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

            // 2. Cập nhật các thuộc tính cơ bản của sản phẩm
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setStock(dto.getStock());

            // 3. Lấy và cập nhật các đối tượng liên quan (Category và BirdType)
            ProductCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Loại sản phẩm không hợp lệ"));
            BirdType birdType = birdTypeRepository.findById(dto.getBirdTypeId())
                    .orElseThrow(() -> new RuntimeException("Loại chim không hợp lệ"));

            product.setCategory(category);
            product.setBirdType(birdType);

            // 4. Xử lý logic cập nhật cho các bảng chi tiết (Detail) dựa trên danh mục
            // Đây là phần quan trọng đã được sửa lỗi cho tất cả các trường hợp
            switch (category.getName().toUpperCase()) {
                case "FOODS" -> {
                    FoodDetail detail = foodDetailRepository.findByProductId(productId).orElse(new FoodDetail());

                    // Gán ID nếu đây là đối tượng mới được tạo ra
                    if (detail.getId() == null) {
                        detail.setId(idGeneratorUtil.generateFoodDetailId());
                    }

                    detail.setProduct(product);
                    detail.setMaterial(dto.getMaterial());
                    detail.setOrigin(dto.getOrigin());
                    detail.setUsageTarget(dto.getUsageTarget());
                    detail.setWeight(dto.getWeight());
                    detail.setImageUrl(dto.getImageUrl());
                    foodDetailRepository.save(detail);
                }
                case "TOYS" -> {
                    // Sửa lỗi tương tự cho ToyDetail
                    ToyDetail detail = toyDetailRepository.findByProductId(productId).orElse(new ToyDetail());

                    // Gán ID nếu đây là đối tượng mới được tạo ra
                    if (detail.getId() == null) {
                        detail.setId(idGeneratorUtil.generateToyDetailId());
                    }

                    detail.setProduct(product);
                    detail.setMaterial(dto.getMaterial());
                    detail.setOrigin(dto.getOrigin());
                    detail.setColor(dto.getColor());
                    detail.setDimensions(dto.getDimensions());
                    detail.setWeight(dto.getWeight());
                    detail.setImageUrl(dto.getImageUrl());
                    toyDetailRepository.save(detail);
                }
                case "FURNITURE" -> {
                    // Sửa lỗi tương tự cho FurnitureDetail
                    FurnitureDetail detail = furnitureDetailRepository.findByProductId(productId)
                            .orElse(new FurnitureDetail());

                    // Gán ID nếu đây là đối tượng mới được tạo ra
                    if (detail.getId() == null) {
                        detail.setId(idGeneratorUtil.generateFurnitureDetailId());
                    }

                    detail.setProduct(product);
                    detail.setMaterial(dto.getMaterial());
                    detail.setOrigin(dto.getOrigin());
                    detail.setColor(dto.getColor());
                    detail.setDimensions(dto.getDimensions());
                    detail.setWeight(dto.getWeight());
                    detail.setImageUrl(dto.getImageUrl());
                    furnitureDetailRepository.save(detail);
                }
                default ->
                    throw new RuntimeException("Loại sản phẩm không hỗ trợ cập nhật chi tiết: " + category.getName());
            }

            // 5. Lưu lại sản phẩm đã được cập nhật và trả về
            return productRepository.save(product);
        }

        @Override
        @Transactional
        public void deleteProduct(Integer productId, Integer accountId) {
            ShopOwner owner = shopOwnerRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new RuntimeException("ShopOwner không tồn tại"));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Kiểm tra quyền sở hữu
            if (!product.getShopOwner().getId().equals(owner.getId())) {
                throw new RuntimeException("Sản phẩm không thuộc sở hữu của bạn");
            }

            // Kiểm tra xem sản phẩm đã từng bán chưa
            boolean isSold = orderItemRepository.existsByProductId(productId);
            if (isSold) {
                throw new RuntimeException("Không thể xóa sản phẩm đã được bán");
            }

            // Xóa chi tiết sản phẩm
            String category = product.getCategory().getName().toUpperCase();
            switch (category) {
                case "FOODS" -> foodDetailRepository.deleteById(productId);
                case "TOYS" -> toyDetailRepository.deleteById(productId);
                case "FURNITURE" -> furnitureDetailRepository.deleteById(productId);
                default -> throw new RuntimeException("Loại sản phẩm không hỗ trợ");
            }

            // Xóa các liên kết phụ
            inventoryRepository.deleteAllByProductId(productId);
            promotionRepository.deleteAllByProductId(productId);
            productReviewRepository.deleteAllByProductId(productId);

            // Cuối cùng xóa sản phẩm
            productRepository.delete(product);
        }

        @Override
        public List<OwnerProductListDTO> searchProductsByOwner(String keyword) {
            return productRepository.searchProductsByShopOwner(keyword);
        }
    }