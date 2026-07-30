package com.codegym.Quiz.authentication.controller;

import com.codegym.Quiz.authentication.service.AuthService;
import com.codegym.Quiz.dto.UserRegisterDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ========== ĐĂNG NHẬP ==========

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    // ========== ĐĂNG KÝ ==========

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("userRegisterDTO") UserRegisterDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Nếu có lỗi validation từ @Valid/@NotBlank/@Email...
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Xử lý logic đăng ký, bắt lỗi business (username/email trùng, password không khớp)
        try {
            authService.registerStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}
