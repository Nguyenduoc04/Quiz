package com.codegym.Quiz.controller;

import com.codegym.Quiz.dto.ChangePasswordDTO;
import com.codegym.Quiz.dto.ForgotPasswordDTO;
import com.codegym.Quiz.dto.ResetPasswordDTO;
import com.codegym.Quiz.dto.UserRegisterDTO;
import com.codegym.Quiz.dto.UserUpdateDTO;
import com.codegym.Quiz.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==========================================
    // 1. CẬP NHẬT THÔNG TIN TÀI KHOẢN
    // ==========================================
    @GetMapping("/profile")
    public String showProfileForm(Model model, Principal principal) {
        // Lấy ID/Username thực tế từ Spring Security (fallback 1L nếu chưa đăng nhập)
        Long currentUserId = getCurrentUserId(principal);

        UserUpdateDTO userDTO = userService.getUserProfileForEdit(currentUserId);
        model.addAttribute("userDTO", userDTO);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("userDTO") UserUpdateDTO userDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (userService.isEmailTakenByAnotherUser(userDTO.getEmail(), userDTO.getId())) {
            bindingResult.rejectValue("email", "error.userDTO", "Email này đã được sử dụng bởi một tài khoản khác!");
        }

        if (bindingResult.hasErrors()) {
            return "user/profile";
        }

        userService.updateUserProfile(userDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/user/profile";
    }

    // ==========================================
    // 2. ĐĂNG KÝ TÀI KHOẢN
    // ==========================================
    @GetMapping("/register")

    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new UserRegisterDTO());
        return "user/register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @Valid @ModelAttribute("registerDTO") UserRegisterDTO registerDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (userService.existsByUsername(registerDTO.getUsername())) {
            bindingResult.rejectValue("username", "error.registerDTO", "Tên đăng nhập này đã được sử dụng!");
        }

        if (userService.existsByEmail(registerDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.registerDTO", "Email này đã được đăng ký!");
        }

        if (registerDTO.getPassword() != null && !registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerDTO", "Mật khẩu xác nhận không trùng khớp!");
        }

        if (bindingResult.hasErrors()) {
            return "user/register";
        }

        userService.registerNewUser(registerDTO);

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }

    // ==========================================
    // 3. ĐỔI MẬT KHẨU
    // ==========================================
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordDTO", new ChangePasswordDTO());
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String handleChangePassword(
            @Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            Principal principal) {

        if (bindingResult.hasErrors()) {
            return "user/change-password";
        }

        Long currentUserId = getCurrentUserId(principal);

        try {
            userService.changePassword(currentUserId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/user/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/change-password";
        }
    }

    // ==========================================
    // 4. QUÊN MẬT KHẨU & GỬI EMAIL OTP
    // ==========================================

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "user/forgot-password";
        }

        try {
            userService.generateForgotPasswordOtp(dto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP đã được gửi thành công! Vui lòng kiểm tra hòm thư Email của bạn.");
            redirectAttributes.addAttribute("email", dto.getEmail());
            return "redirect:/user/reset-password";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/forgot-password";
        } catch (Exception e) {
            // Bắt lỗi hệ thống / Lỗi SMTP gửi mail không thành công
            model.addAttribute("errorMessage", "Không thể gửi email OTP lúc này. Vui lòng thử lại sau!");
            return "user/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "email", required = false) String email, Model model) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setEmail(email);
        model.addAttribute("resetPasswordDTO", dto);
        return "user/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "user/reset-password";
        }

        try {
            userService.resetPasswordWithOtp(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/reset-password";
        }
    }

    // Helper method lấy ID người dùng hiện tại (Tối ưu chống crash)
    private Long getCurrentUserId(Principal principal) {
        if (principal != null) {
            try {
                return userService.findByUsername(principal.getName()).getId();
            } catch (Exception e) {
                // Trường hợp tài khoản trong Session không còn trong DB
                return 1L;
            }
        }
        return 1L; // Mặc định cho môi trường test khi chưa bật Spring Security Login
    }
}