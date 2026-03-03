package org.example.flyora_backend.service;

import org.example.flyora_backend.DTOs.ChangePasswordDTO;
import org.example.flyora_backend.DTOs.ProfileDTO;
import org.example.flyora_backend.DTOs.UpdateProfileDTO;
import org.example.flyora_backend.model.Account;

public interface ProfileService {
    ProfileDTO getProfile(Account account);

    void updateProfile(Account account, UpdateProfileDTO request);

    void changePassword(Account account, ChangePasswordDTO request);
}
