package com.codegym.Quiz.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            // Email và tên người gửi
            helper.setFrom(fromEmail, "Quiz App Team");

            helper.setTo(toEmail);
            helper.setSubject("Quiz App - Mã xác thực OTP đặt lại mật khẩu");

            helper.setText(
                    "Xin chào,\n\n" +
                            "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản Quiz App của bạn.\n\n" +
                            "Mã xác thực OTP của bạn là: " + otp + "\n\n" +
                            "Mã OTP này có hiệu lực trong vòng 5 phút.\n" +
                            "Vui lòng không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn cho tài khoản của bạn.\n\n" +
                            "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email. Mật khẩu của bạn sẽ không bị thay đổi.\n\n" +
                            "Trân trọng,\n" +
                            "Quiz App Team",
                    false
            );

            mailSender.send(message);
            logger.info("Đã gửi thành công email OTP tới: {}", toEmail);

        } catch (Exception e) {
            logger.error("Lỗi khi gửi email tới {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng kiểm tra lại cấu hình hệ thống!");
        }
    }
}