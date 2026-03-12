package org.example.flyora_backend.controller;

import java.util.ArrayList;
import java.util.List;

import org.example.flyora_backend.DTOs.CartItemDTO;
import org.example.flyora_backend.DTOs.CartRequestDTO;
import org.example.flyora_backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart Services", description = "API cho thao tác giỏ hàng (không lưu vào database)")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @Operation(
        summary = "Xem giỏ hàng (dùng GET)",
        description = """
            Nhận danh sách sản phẩm từ client thông qua query params.

            Áp dụng khi dữ liệu đơn giản và số lượng ít.

            Tham số dạng:
            - productId: Danh sách id sản phẩm (VD: `productId=1&productId=2`)
            - quantity: Danh sách số lượng tương ứng (VD: `quantity=2&quantity=1`)

            Trả về: Danh sách CartItemDTO gồm productId, name, imageUrl, quantity.
        """
    )
    public ResponseEntity<List<CartItemDTO>> getCart(
            @RequestParam List<Integer> productId,
            @RequestParam List<Integer> quantity) {
        List<CartRequestDTO> requests = new ArrayList<>();
        for (int i = 0; i < productId.size(); i++) {
            requests.add(new CartRequestDTO(productId.get(i), quantity.get(i)));
        }
        return ResponseEntity.ok(cartService.getCartItems(requests));
    }

    @PostMapping
    @Operation(
        summary = "Xem giỏ hàng (dùng POST)",
        description = """
            Nhận danh sách sản phẩm từ client thông qua body JSON.

            Body (List<CartRequestDTO>):
            - productId (Integer)
            - quantity (Integer)

            Ưu điểm so với GET:
            - Gửi được nhiều sản phẩm hơn, không giới hạn độ dài URL.
            - Dữ liệu rõ ràng và dễ mở rộng.

            Trả về: Danh sách CartItemDTO gồm productId, name, imageUrl, quantity.
        """
    )
    public ResponseEntity<List<CartItemDTO>> viewCart(@RequestBody List<CartRequestDTO> requests) {
        return ResponseEntity.ok(cartService.getCartItems(requests));
    }

    @PutMapping("/{productId}")
    @Operation(
        summary = "Cập nhật số lượng sản phẩm trong giỏ hàng",
        description = """
            Đường dẫn:
            - productId: id sản phẩm muốn cập nhật

            Query param:
            - quantity (Integer): số lượng mới

            Trả về: CartItemDTO đã cập nhật số lượng.
        """
    )
    public ResponseEntity<CartItemDTO> updateQuantity(
            @PathVariable Integer productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItem(productId, quantity));
    }

    @DeleteMapping("/{productId}")
    @Operation(
        summary = "Xoá 1 sản phẩm khỏi giỏ hàng",
        description = """
            Đường dẫn:
            - productId: id sản phẩm muốn xoá

            Trả về: Thông báo xoá thành công.
        """
    )
    public ResponseEntity<String> deleteItem(@PathVariable Integer productId) {
        return ResponseEntity.ok("Deleted product " + productId + " from cart.");
    }

    @DeleteMapping("/clear")
    @Operation(
        summary = "Xoá toàn bộ giỏ hàng",
        description = """
            Xoá tất cả sản phẩm đang có trong giỏ hàng phía client.

            Trả về: Thông báo đã xoá giỏ hàng.
        """
    )
    public ResponseEntity<String> clearCart() {
        return ResponseEntity.ok("Cart cleared.");
    }
}