package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AuthenticationHelper authHelper;
    private final UserService userService;

    public AdminController(AuthenticationHelper authHelper, UserService userService) {
        this.authHelper = authHelper;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        Optional<User> adminUser = authHelper.getCurrentUser();
        adminUser.ifPresent(user -> {
            model.addAttribute("fullName",
                    user.getFullName() != null ? user.getFullName() : user.getUsername());
        });

        return "admin/dashboard";
    }

    // ==========================================
    // 1. DANG SACH NGUOI DUNG & DUYET GIAO VIEN
    // ==========================================
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // 2. Trang danh sách giáo viên chờ duyệt (/admin/teachers/pending)
    @GetMapping("/teachers/pending")
    public String listPendingTeachers(Model model) {
        List<User> pendingTeachers = userService.getPendingTeachers();
        model.addAttribute("pendingTeachers", pendingTeachers);
        return "admin/pending-teachers";
    }

    // 3. Duyệt tài khoản đăng ký làm Giáo viên
    @PostMapping("/users/{id}/approve-teacher")
    public String approveTeacher(@PathVariable("id") Long id,
                                 @RequestParam(value = "redirectUrl", defaultValue = "/admin/users") String redirectUrl,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.approveTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt tài khoản thành Giáo viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể duyệt tài khoản: " + e.getMessage());
        }
        return "redirect:" + redirectUrl;
    }

    // 4. Từ chối yêu cầu đăng ký làm Giáo viên
    @PostMapping("/users/{id}/reject-teacher")
    public String rejectTeacher(@PathVariable("id") Long id,
                                @RequestParam(value = "redirectUrl", defaultValue = "/admin/users") String redirectUrl,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.rejectTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối yêu cầu Đăng ký Giáo viên!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể từ chối: " + e.getMessage());
        }
        return "redirect:" + redirectUrl;
    }

    // 3. Xoa tai khoan nguoi dung
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @GetMapping("/students")
    public String listStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<User> studentPage;

        if (keyword.isBlank()) {
            studentPage = userService.getStudents(pageable);
        } else {
            studentPage = userService.searchStudents(keyword, pageable);
        }

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("studentPage", studentPage);

        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalItems", studentPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "admin/students";
    }
    @GetMapping("/teachers")
    public String listTeachers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<User> teacherPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            teacherPage = userService.getTeachers(pageable);
        } else {
            teacherPage = userService.searchTeachers(keyword.trim(), pageable);
        }

        model.addAttribute("teachers", teacherPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", teacherPage.getNumber());
        model.addAttribute("totalPages", teacherPage.getTotalPages());
        model.addAttribute("totalItems", teacherPage.getTotalElements());
        model.addAttribute("pageSize", teacherPage.getSize());

        return "admin/teachers";
    }
}