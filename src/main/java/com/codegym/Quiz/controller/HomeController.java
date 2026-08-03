package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    private final AuthenticationHelper authHelper;

    public HomeController(AuthenticationHelper authHelper) {
        this.authHelper = authHelper;
    }

    /**
     * Trang gốc – redirect thông minh theo trạng thái đăng nhập và role.
     */
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Đã đăng nhập → redirect theo role
            if (authHelper.isAdmin()) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/dashboard";
        }
        return "redirect:/auth/login";
    }

    /**
     * Dashboard dành cho ROLE_USER và ROLE_STUDENT.
     * ROLE_ADMIN được redirect từ / về /admin/dashboard.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        // Lấy thông tin đầy đủ từ DB để hiển thị fullName
        Optional<User> userOpt = authHelper.getCurrentUser();
        userOpt.ifPresent(user -> {
            model.addAttribute("fullName",
                    user.getFullName() != null ? user.getFullName() : user.getUsername());
        });

        // Thông tin quyền để dashboard biết hiển thị gì
        model.addAttribute("isStudent", authHelper.isStudent());
        model.addAttribute("isAdmin", authHelper.isAdmin());
        model.addAttribute("canSaveResult", authHelper.canSaveQuizResult());

        return "dashboard";
    }
}
