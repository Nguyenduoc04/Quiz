package com.codegym.Quiz.service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.codegym.Quiz.dto.ChangePasswordDTO;
import com.codegym.Quiz.dto.ResetPasswordDTO;
import com.codegym.Quiz.dto.UserRegisterDTO;
import com.codegym.Quiz.dto.UserUpdateDTO;
import com.codegym.Quiz.entity.Role;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.RoleRepository;
import com.codegym.Quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.codegym.Quiz.constant.RoleConstants;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Bổ sung RoleRepository
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));
    }

    // 1. Đăng ký người dùng mới (Đã bổ sung gán ROLE_USER mặc định)
    public void registerNewUser(UserRegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setFullName(registerDTO.getFullName());
        user.setEnabled(true);

        // Gán Role mặc định ROLE_USER
        Role userRole = roleRepository.findByName(com.codegym.Quiz.constant.RoleConstants.ROLE_USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(com.codegym.Quiz.constant.RoleConstants.ROLE_USER);
                    return roleRepository.save(newRole);
                });
        user.getRoles().add(userRole);

        userRepository.save(user);
    }

    // Lấy thông tin user hiện tại đổi sang DTO để điền sẵn vào Form
    public UserUpdateDTO getUserProfileForEdit(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        return new UserUpdateDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail());
    }

    // 2. Tối ưu kiểm tra Email bị trùng với người khác bằng Query chuẩn JPA
    public boolean isEmailTakenByAnotherUser(String email, Long currentUserId) {
        return userRepository.existsByEmailAndIdNot(email, currentUserId);
    }

    // Cập nhật thông tin User vào Database
    public void updateUserProfile(UserUpdateDTO updateDTO) {
        User user = userRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setFullName(updateDTO.getFullName());
        user.setEmail(updateDTO.getEmail());

        userRepository.save(user);
    }

    // Chức năng Đổi mật khẩu
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    // ==========================================
    // CHỨC NĂNG QUÊN MẬT KHẨU & GỬI EMAIL OTP
    // ==========================================

    // 3. Tối ưu hàm sinh OTP 6 chữ số ngẫu nhiên chính xác
    public void generateForgotPasswordOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống"));

        // Sinh số ngẫu nhiên từ 100000 đến 999999
        int randomOtp = 100000 + new Random().nextInt(900000);
        String otp = String.valueOf(randomOtp);

        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public void resetPasswordWithOtp(ResetPasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không hợp lệ"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(dto.getOtp())) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng gửi lại yêu cầu!");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setResetOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    // ==========================================
    // QUẢN LÝ NGƯỜI DÙNG DÀNH CHO ADMIN
    // ==========================================

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Lấy danh sách người dùng đang chờ duyệt Giáo viên
    public java.util.List<User> getPendingTeachers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName().equals(com.codegym.Quiz.constant.RoleConstants.ROLE_PENDING_TEACHER)))
                .collect(java.util.stream.Collectors.toList());
    }

    public void approveTeacher(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // Lấy hoặc tạo ROLE_TEACHER
        Role teacherRole = roleRepository.findByName(com.codegym.Quiz.constant.RoleConstants.ROLE_TEACHER)
                .orElseGet(() -> roleRepository.save(new Role(com.codegym.Quiz.constant.RoleConstants.ROLE_TEACHER)));

        // Xóa ROLE_PENDING_TEACHER và thêm ROLE_TEACHER
        user.getRoles().removeIf(role -> role.getName().equals(com.codegym.Quiz.constant.RoleConstants.ROLE_PENDING_TEACHER));
        user.getRoles().add(teacherRole);

        userRepository.save(user);
    }

    // Từ chối đăng ký làm Giáo viên (Xóa ROLE_PENDING_TEACHER, đưa về người dùng bình thường)
    public void rejectTeacher(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        user.getRoles().removeIf(role -> role.getName().equals(com.codegym.Quiz.constant.RoleConstants.ROLE_PENDING_TEACHER));
        userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    // Đăng ký làm Giáo viên đối với người dùng đang đăng nhập
    public void requestTeacherRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // Nếu đã là Giáo viên hoặc đang chờ duyệt thì báo lỗi
        boolean isTeacher = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(com.codegym.Quiz.constant.RoleConstants.ROLE_TEACHER));
        if (isTeacher) {
            throw new IllegalArgumentException("Tài khoản của bạn đã là Giáo viên!");
        }

        boolean isPending = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(com.codegym.Quiz.constant.RoleConstants.ROLE_PENDING_TEACHER));
        if (isPending) {
            throw new IllegalArgumentException("Yêu cầu của bạn đang chờ Admin duyệt!");
        }

        // Lấy hoặc tạo ROLE_PENDING_TEACHER
        Role pendingRole = roleRepository.findByName(com.codegym.Quiz.constant.RoleConstants.ROLE_PENDING_TEACHER)
                .orElseGet(() -> roleRepository.save(new Role(com.codegym.Quiz.constant.RoleConstants.ROLE_PENDING_TEACHER)));

        user.getRoles().add(pendingRole);
        userRepository.save(user);
    }
    public Page<User> getStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByRoles_Name(
                com.codegym.Quiz.constant.RoleConstants.ROLE_STUDENT,
                pageable
        );
    }
    public long countStudents() {
        return userRepository.countByRoles_Name(
                com.codegym.Quiz.constant.RoleConstants.ROLE_USER
        );
    }
}