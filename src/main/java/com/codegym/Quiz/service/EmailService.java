package com.codegym.Quiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async // Gửi email chạy ngầm giúp trang web phản hồi tức thì
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[Quiz App] Mã OTP đặt lại mật khẩu");
            message.setText("Chào bạn,\n\nMã OTP để đặt lại mật khẩu của bạn là: " + otp +
                    "\nMã này có hiệu lực trong vòng 5 phút.\n\nTrân trọng!");

            mailSender.send(message);
            logger.info("Đã gửi thành công email OTP tới: {}", toEmail);
        } catch (MailException e) {
            logger.error("Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng kiểm tra lại cấu hình hệ thống!");
        }
    }
}