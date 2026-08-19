package com.codegym.Quiz.authentication.util;

import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tiện ích xác thực – dùng trong các module khác để lấy thông tin người dùng hiện tại.
 * <p>
 * Cách dùng: inject @Autowired AuthenticationHelper vào Service/Controller cần dùng.
 * <p>
 * Ví dụ kiểm tra quyền trước khi lưu kết quả quiz:
 * <pre>
 *   if (authHelper.isStudent()) {
 *       resultService.save(result);
 *   } else {
 *       // ROLE_USER: không lưu kết quả
 *   }
 * </pre>
 */
@Component
public class AuthenticationHelper {

    private final UserRepository userRepository;

    public AuthenticationHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ===================== LẤY THÔNG TIN USER HIỆN TẠI =====================

    /**
     * Lấy Authentication hiện tại từ SecurityContext.
     * @return Authentication hoặc null nếu chưa đăng nhập
     */
    public Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth;
    }

    /**
     * Lấy username của người dùng hiện tại.
     * @return username hoặc null nếu chưa đăng nhập
     */
    public String getCurrentUsername() {
        Authentication auth = getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /**
     * Lấy User entity của người dùng hiện tại từ database.
     * @return Optional<User>
     */
    public Optional<User> getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) return Optional.empty();
        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(user -> {
            if (user.getLastLogin() == null || user.getLastLogin().isBefore(java.time.LocalDateTime.now().minusMinutes(1))) {
                user.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(user);
            }
        });
        return userOpt;
    }

    /**
     * Lấy danh sách tên roles của người dùng hiện tại.
     * @return Set các role name (VD: {"ROLE_USER", "ROLE_STUDENT"}) hoặc empty set
     */
    public Set<String> getCurrentUserRoles() {
        Authentication auth = getAuthentication();
        if (auth == null) return Collections.emptySet();

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    // ===================== KIỂM TRA TRẠNG THÁI =====================

    /**
     * Kiểm tra người dùng đã đăng nhập chưa.
     */
    public boolean isAuthenticated() {
        return getAuthentication() != null;
    }

    /**
     * Kiểm tra người dùng có role cụ thể không.
     * @param roleName tên role VD: "ROLE_ADMIN", "ROLE_STUDENT", "ROLE_USER"
     */
    public boolean hasRole(String roleName) {
        return getCurrentUserRoles().contains(roleName);
    }

    /**
     * Kiểm tra người dùng có quyền ADMIN không.
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * Kiểm tra người dùng có quyền STUDENT không.
     * STUDENT: đã nhập mã lớp, kết quả được lưu, có thể vào lớp riêng.
     */
    public boolean isStudent() {
        return hasRole("ROLE_STUDENT");
    }


    /**
     * Kiểm tra người dùng có thể lưu kết quả quiz không.
     * Chỉ STUDENT và ADMIN mới được lưu kết quả.
     */
    public boolean canSaveQuizResult() {
        return isStudent() || isAdmin();
    }

    /**
     * Kiểm tra người dùng có thể tham gia lớp học riêng không.
     * Yêu cầu: phải là STUDENT (đã nhập mã lớp và được duyệt).
     *
     * @param classId ID lớp học cần kiểm tra
     * @return true nếu là STUDENT (kiểm tra membership trong Class Module)
     */
    public boolean canJoinPrivateClass(Long classId) {
        // Kiểm tra cơ bản: phải có role STUDENT
        // Class Module sẽ kiểm tra thêm xem user có trong danh sách thành viên lớp không
        return isStudent() || isAdmin();
    }
}
