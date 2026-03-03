// Trong file: org/example/flyora_backend/repository/InventoryRepository.java
package org.example.flyora_backend.repository;

import org.example.flyora_backend.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    void deleteAllByProductId(Integer productId);

    /**
     * Xóa các bản ghi Inventory dựa trên account_id của SalesStaff.
     * Sử dụng Native Query vì model Inventory.java không có liên kết tới SalesStaff.
     * Câu lệnh này tìm ra id của SalesStaff từ account_id, sau đó xóa các inventory có staff_id đó.
     * @param accountId ID của tài khoản (Account) liên kết với SalesStaff.
     */
    @Modifying // Bắt buộc phải có cho các câu lệnh UPDATE/DELETE
    @Query(
        value = "DELETE FROM Inventory WHERE staff_id IN (SELECT id FROM SalesStaff WHERE account_id = :accountId)",
        nativeQuery = true // Đánh dấu đây là câu lệnh SQL thuần
    )
    void deleteBySalesStaffAccountId(@Param("accountId") Integer accountId);
}