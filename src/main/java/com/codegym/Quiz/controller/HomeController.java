package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.service.CategoryService;
import com.codegym.Quiz.service.ExamService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    private final AuthenticationHelper authHelper;
    private final CategoryService categoryService;
    private final ExamService examService;

    public HomeController(AuthenticationHelper authHelper,
                          CategoryService categoryService,
                          ExamService examService) {
        this.authHelper = authHelper;
        this.categoryService = categoryService;
        this.examService = examService;
    }

    /**
     * Trang gốc – render Landing page index.html nếu chưa đăng nhập hoặc là học sinh/user thường.
     * Admin sẽ redirect tới /admin/dashboard.
     */
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                     && authentication.getPrincipal().equals("anonymousUser"))) {
            if (authHelper.isAdmin()) {
                return "redirect:/admin/dashboard";
            }
        }
        return dashboard(model, authentication);
    }

    /**
     * Trang chủ / Dashboard dành cho học viên và khách công khai.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                     && authentication.getPrincipal().equals("anonymousUser"));
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("roles", authentication.getAuthorities());

            Optional<User> userOpt = authHelper.getCurrentUser();
            userOpt.ifPresent(user -> {
                model.addAttribute("fullName",
                        user.getFullName() != null ? user.getFullName() : user.getUsername());
            });

            model.addAttribute("isStudent", authHelper.isStudent());
            model.addAttribute("isAdmin", authHelper.isAdmin());
            model.addAttribute("canSaveResult", authHelper.canSaveQuizResult());
        } else {
            model.addAttribute("isStudent", false);
            model.addAttribute("isAdmin", false);
            model.addAttribute("canSaveResult", false);
        }

        model.addAttribute("categories", categoryService.getAllCategoryDTOs());
        model.addAttribute("exams", examService.getActiveExams());

        return "index";
    }
}
