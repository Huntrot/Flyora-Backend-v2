package org.example.flyora_backend.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.example.flyora_backend.DTOs.EmailDTO;
import org.example.flyora_backend.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    //mail sending method
    @Override
    public void sendEmail(EmailDTO emailDTO) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("captainparrot13042005@gmail.com");
            helper.setTo(emailDTO.getTo());
            helper.setSubject(emailDTO.getSubject());
            helper.setText(emailDTO.getContent(), true);
            mailSender.send(message);
        }
        catch (Exception e) {
            throw new RuntimeException("Không thể gửi email", e);
        }
    }

    @Override
    public void sendOTPEmail(String to, String otp) {
        String subject = "Mã OTP của bạn";
        String content = "<p>Chào bạn,</p>"
                + "<p>Mã OTP của bạn là: <b>" + otp + "</b></p>"
                + "<p>Vui lòng không chia sẻ mã này với bất kỳ ai.</p>"
                + "<br>"
                + "<p>Trân trọng,</p>"
                + "<p>Đội ngũ Flyora</p>";
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTo(to);
        emailDTO.setSubject(subject);
        emailDTO.setContent(content);
        sendEmail(emailDTO);
    }
    
    // OTP generation method
    private record OtpEntry(String otp, long expiredAt) {}
    
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    @Override
    public String createAndStoreOtp(String key) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        long expiredAt = System.currentTimeMillis() + 5 * 60 * 1000; // 5 phút
        otpStore.put(key, new OtpEntry(otp, expiredAt));
        return otp;
    }

    @Override
    public boolean verifyOtp(String key, String otp) {
        OtpEntry entry = otpStore.get(key);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expiredAt()) {
            otpStore.remove(key);
            return false;
        }
        boolean isCorrect = entry.otp().equals(otp);
        if (isCorrect) {
            otpStore.remove(key);
        }
        return isCorrect;
    }

    @Override
    public void sendOrderConfirmationEmail(Order order) {

        String email = order.getCustomer().getAccount().getEmail();

        String subject = "Flyora - Xác nhận đơn hàng #" + order.getOrderCode();

        String content = """
            <h2>Cảm ơn bạn đã mua hàng tại Flyora 🐦</h2>
            <p>Mã đơn hàng: <b>%s</b></p>
            <p>Trạng thái hiện tại: <b>%s</b></p>
            """.formatted(order.getOrderCode(), order.getStatus());

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTo(email);
        emailDTO.setSubject(subject);
        emailDTO.setContent(content);

        sendEmail(emailDTO);
    }

    @Async
    @Override
    public void sendStatusUpdateEmail(Order order) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            String customerEmail =
                    order.getCustomer().getEmail();

            helper.setTo(customerEmail);
            helper.setSubject("Cập nhật trạng thái đơn hàng #" 
                            + order.getOrderCode());

            String htmlContent = """
                <h3>Xin chào %s,</h3>
                <p>Đơn hàng <b>#%s</b> của bạn đã được cập nhật trạng thái:</p>
                <h2 style="color:blue;">%s</h2>
                <p>Cảm ơn bạn đã mua sắm tại Flyora 💙</p>
                """.formatted(
                    order.getCustomer().getName(),
                    order.getOrderCode(),
                    order.getStatus()
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            System.out.println("Send mail error: " + e.getMessage());
        }
    }
}
    