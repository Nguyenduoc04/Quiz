package com.codegym.Quiz.authentication.validator;

import com.codegym.Quiz.dto.UserRegisterDTO;
import com.codegym.Quiz.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator riêng xử lý kiểm tra nghiệp vụ đăng ký tài khoản (Custom Spring Validator)
 */
@Component
public class RegisterValidator implements Validator {

    private final UserRepository userRepository;

    public RegisterValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRegisterDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserRegisterDTO dto = (UserRegisterDTO) target;

        // 1. Kiểm tra Username trùng lặp
        if (dto.getUsername() != null && userRepository.existsByUsername(dto.getUsername())) {
            errors.rejectValue("username", "duplicate.username", "Tên đăng nhập đã tồn tại!");
        }

        // 2. Kiểm tra Email trùng lặp
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            errors.rejectValue("email", "duplicate.email", "Email đã được sử dụng!");
        }

        // 3. Kiểm tra Mật khẩu và Xác nhận mật khẩu
        if (dto.getPassword() != null && dto.getConfirmPassword() != null
                && !dto.getPassword().equals(dto.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "mismatch.password", "Mật khẩu xác nhận không khớp!");
        }
    }
}
