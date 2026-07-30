package com.codegym.Quiz.controller;

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

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model) {
        // GIẢ LẬP: Lấy User ID = 1L (Sau này dùng Spring Security sẽ lấy ID từ session đăng nhập)
        Long currentUserId = 1L;

        UserUpdateDTO userDTO = userService.getUserProfileForEdit(currentUserId);
        model.addAttribute("userDTO", userDTO);
        return "user/profile";
    }

    //Xử lý cập nhật thông tin
    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("userDTO") UserUpdateDTO userDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        //Kiểm tra Email trùng với người khác
        if (userService.isEmailTakenByAnotherUser(userDTO.getEmail(), userDTO.getId())) {
            bindingResult.rejectValue("email", "error.userDTO", "Email này đã được sử dụng bởi một tài khoản khác!");
        }

        if (bindingResult.hasErrors()) {
            return "user/profile"; // Quay lại trang form và hiển thị thông báo lỗi
        }

        //Cập nhật vào DB nếu dữ liệu hợp lệ
        userService.updateUserProfile(userDTO);

        //Gửi thông báo thành công khi chuyển trang
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/user/profile";
    }


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

        //Kiểm tra Username đã tồn tại chưa
        if (userService.existsByUsername(registerDTO.getUsername())) {
            bindingResult.rejectValue("username", "error.registerDTO", "Tên đăng nhập này đã được sử dụng!");
        }

        //Kiểm tra Email đã tồn tại chưa
        if (userService.existsByEmail(registerDTO.getEmail())) {
            bindingResult.rejectValue("email", "error.registerDTO", "Email này đã được đăng ký!");
        }

        //Kiểm tra mật khẩu xác nhận
        if (registerDTO.getPassword() != null && !registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerDTO", "Mật khẩu xác nhận không trùng khớp!");
        }

        //Nếu phát hiện có lỗi
        if (bindingResult.hasErrors()) {
            return "user/register";
        }

        //Gọi Service lưu User mới vào Database
        userService.registerNewUser(registerDTO);

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}