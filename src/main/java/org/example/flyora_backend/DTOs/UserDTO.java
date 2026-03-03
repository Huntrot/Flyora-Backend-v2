package org.example.flyora_backend.DTOs;

import org.example.flyora_backend.model.Account;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String phone;
    private boolean isActive;
    private boolean isApproved;
    private String role;

    public UserDTO(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.phone = account.getPhone();
        this.isActive = account.getIsActive();
        this.isApproved = account.getIsApproved();
        this.role = account.getRole() != null ? account.getRole().getName() : null;
    }
}