package com.codegym.Quiz.authentication.controller;

import com.codegym.Quiz.authentication.service.AuthService;
import com.codegym.Quiz.authentication.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Controller cho Authentication (JWT).
 * <p>
 * Endpoints:
 * POST /api/auth/login  – đăng nhập, trả về JWT token
 * POST /api/auth/logout – đăng xuất (client xóa token)
 * GET  /api/auth/me     – thông tin user hiện tại từ token
 * <p>
 * Tất cả responses đều dạng JSON.
 * Không cần CSRF token (đã cấu hình bỏ qua /api/** trong SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthApiController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // ===================== ĐĂNG NHẬP =====================

    /**
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     * Response: { "token": "...", "username": "...", "roles": [...], "expiresAt": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(errorResponse("Tên đăng nhập và mật khẩu không được để trống."));
        }

        try {
            String token = authService.loginAndGetToken(username, password);

            List<String> roles = jwtUtil.extractRoles(token);
            String expiresAt = jwtUtil.extractExpiration(token).toInstant().toString();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("token", token);
            body.put("type", "Bearer");
            body.put("username", username);
            body.put("roles", roles);
            body.put("expiresAt", expiresAt);

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("Tên đăng nhập hoặc mật khẩu không đúng."));
        }
    }

    // ===================== ĐĂNG XUẤT =====================

    /**
     * POST /api/auth/logout
     * JWT là stateless – server không lưu token, nên đăng xuất phía client xóa token.
     * Response: thông báo thành công.
     * <p>
     * Lưu ý: Nếu cần blacklist token (revocation), implement thêm Redis cache sau.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        SecurityContextHolder.clearContext();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Đăng xuất thành công. Vui lòng xóa token phía client.");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    // ===================== THÔNG TIN USER HIỆN TẠI =====================

    /**
     * GET /api/auth/me
     * Header: Authorization: Bearer <token>
     * Response: thông tin user và roles từ SecurityContext.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("Chưa xác thực. Vui lòng đăng nhập."));
        }

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", authentication.getName());
        body.put("roles", roles);
        body.put("authenticated", true);

        return ResponseEntity.ok(body);
    }

    // ===================== HELPER =====================

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        error.put("timestamp", Instant.now().toString());
        return error;
    }
}
