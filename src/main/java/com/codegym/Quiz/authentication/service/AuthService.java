package com.codegym.Quiz.authentication.service;

import com.codegym.Quiz.authentication.util.JwtUtil;
import com.codegym.Quiz.dto.UserRegisterDTO;
import com.codegym.Quiz.entity.Role;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.RoleRepository;
import com.codegym.Quiz.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    // ===================== ĐĂNG KÝ =====================

    /**
     * Đăng ký tài khoản mới với ROLE_USER (mặc định).
     * <p>
     * Người dùng mới sẽ có ROLE_USER:
     * - Có thể tham gia quiz công khai
     * - Kết quả KHÔNG được lưu lại
     * - Không thể vào lớp học riêng
     * <p>
     * Để nâng lên ROLE_STUDENT: người dùng cần nhập mã lớp học (xử lý bởi Class
     * Module).
     *
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public void registerUser(UserRegisterDTO dto) {
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

        // 4. Lấy hoặc tạo mới role ROLE_USER (mặc định khi đăng ký)
        Role userRole = roleRepository.findByName(com.codegym.Quiz.constant.RoleConstants.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(com.codegym.Quiz.constant.RoleConstants.ROLE_USER)));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // 5. Tạo User, mã hoá mật khẩu bằng BCrypt
        User user = new User(
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getEmail(),
                dto.getFullName());
        user.setRoles(roles);

        // 6. Lưu vào Database
        userRepository.save(user);
    }

    /**
     * @deprecated Dùng {@link #registerUser(UserRegisterDTO)} thay thế.
     *             Giữ lại để tránh breaking change với code cũ.
     */
    @Deprecated(since = "2026-08-02", forRemoval = true)
    @Transactional
    public void registerStudent(UserRegisterDTO dto) {
        registerUser(dto);
    }

    // ===================== ĐĂNG NHẬP (JWT) =====================

    /**
     * Xác thực thông tin đăng nhập và trả về JWT token.
     * Dùng cho REST API (/api/auth/login).
     *
     * @param username tên đăng nhập
     * @param password mật khẩu gốc (chưa mã hoá)
     * @return JWT token hợp lệ
     * @throws org.springframework.security.core.AuthenticationException nếu sai
     *                                                                   thông tin
     */
    public String loginAndGetToken(String username, String password) {
        // 1. Xác thực username/password qua AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        // 2. Load UserDetails đầy đủ (bao gồm roles) để tạo token
        UserDetails userDetails = userDetailsService.loadUserByUsername(
                authentication.getName());

        // 3. Tạo và trả về JWT token
        return jwtUtil.generateToken(userDetails);
    }
}
