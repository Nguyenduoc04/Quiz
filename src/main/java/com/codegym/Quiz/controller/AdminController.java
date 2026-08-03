package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AuthenticationHelper authHelper;

    public AdminController(AuthenticationHelper authHelper) {
        this.authHelper = authHelper;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        // Truyền thông tin admin vào model để hiển thị trong header
        Optional<User> adminUser = authHelper.getCurrentUser();
        adminUser.ifPresent(user -> {
            model.addAttribute("fullName",
                    user.getFullName() != null ? user.getFullName() : user.getUsername());
        });

        return "admin/dashboard";
    }
}