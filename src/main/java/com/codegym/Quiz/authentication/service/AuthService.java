package com.codegym.Quiz.authentication.service;

import com.codegym.Quiz.dto.UserRegisterDTO;
import com.codegym.Quiz.entity.Role;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.RoleRepository;
import com.codegym.Quiz.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Đăng ký tài khoản Học viên.
     * Validate dữ liệu + mã hoá mật khẩu + gán ROLE_STUDENT + lưu DB.
     *
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public void registerStudent(UserRegisterDTO dto) {
        // 1. Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }

        // 2. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        // 3. Kiểm tra mật khẩu và xác nhận khớp nhau
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }

        // 4. Lấy hoặc tạo mới role ROLE_STUDENT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT")));

        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);

        // 5. Tạo User, mã hoá mật khẩu bằng BCrypt
        User user = new User(
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getEmail(),
                dto.getFullName()
        );
        user.setRoles(roles);

        // 6. Lưu vào Database
        userRepository.save(user);
    }
}
