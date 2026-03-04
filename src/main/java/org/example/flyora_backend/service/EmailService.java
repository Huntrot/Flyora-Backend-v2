package org.example.flyora_backend.service;

import org.example.flyora_backend.DTOs.EmailDTO;
import org.example.flyora_backend.model.Order;

public interface EmailService {
    void sendEmail(EmailDTO emailDTO);

    void sendOTPEmail(String to, String otp); 

    String createAndStoreOtp(String key);

    boolean verifyOtp(String key, String otp);

    void sendOrderConfirmationEmail(Order order);
    
    void sendStatusUpdateEmail(Order order);
}
