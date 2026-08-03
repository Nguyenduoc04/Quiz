package com.codegym.Quiz.authentication.controller;

import com.codegym.Quiz.authentication.service.AuthService;
import com.codegym.Quiz.dto.UserRegisterDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
    public String showLoginPage(Authentication authentication) {
        // Nếu đã đăng nhập, redirect về dashboard
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    // ========== ĐĂNG XUẤT ==========

    /**
     * GET /auth/logout – hỗ trợ đăng xuất qua link thông thường.
     * Thực hiện logout programmatically rồi redirect về trang login.
     */
    @GetMapping("/logout")
    public String logoutGet(HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        redirectAttributes.addFlashAttribute("logoutMessage", "Bạn đã đăng xuất thành công.");
        return "redirect:/auth/login?logout=true";
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

        // Xử lý logic đăng ký, bắt lỗi business (username/email trùng, password không
        // khớp)
        try {
            authService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}
