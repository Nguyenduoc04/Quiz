package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.dto.QuestionDTO;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.service.CategoryService;
import com.codegym.Quiz.service.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/admin/questions", "/teacher/questions"})
public class QuestionController {

    private final QuestionService questionService;
    private final CategoryService categoryService;
    private final AuthenticationHelper authHelper;

    public QuestionController(QuestionService questionService,
            CategoryService categoryService,
            AuthenticationHelper authHelper) {
        this.questionService = questionService;
        this.categoryService = categoryService;
        this.authHelper = authHelper;
    }

    private String getBaseUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin")) {
            return "/admin/questions";
        }
        return "/teacher/questions";
    }

    private boolean isAdminView(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/admin");
    }

    /** Inject fullName & view metadata vào Model */
    private void injectViewMetadata(Model model, HttpServletRequest request) {
        authHelper.getCurrentUser().ifPresent(user -> {
            String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName()
                    : user.getUsername();
            model.addAttribute("fullName", displayName);
        });
        model.addAttribute("baseUrl", getBaseUrl(request));
        model.addAttribute("isAdminView", isAdminView(request));
    }

    private String getViewPath(HttpServletRequest request, String pageName) {
        if (isAdminView(request)) {
            return "admin/question/" + pageName;
        }
        return "teacher/question/" + pageName;
    }

    @GetMapping
    public String listQuestions(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "difficulty", required = false) String difficultyStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpServletRequest request) {

        injectViewMetadata(model, request);

        com.codegym.Quiz.entity.Question.DifficultyLevel difficulty = null;
        if (difficultyStr != null && !difficultyStr.isBlank()) {
            try {
                difficulty = com.codegym.Quiz.entity.Question.DifficultyLevel.valueOf(difficultyStr);
            } catch (IllegalArgumentException ignored) {}
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionDTO> questionPage = questionService.searchQuestions(keyword, categoryId, difficulty, pageable);

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("questionPage", questionPage);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedDifficulty", difficultyStr != null ? difficultyStr : "");
        model.addAttribute("categories", categoryService.getAllCategoryDTOs());
        model.addAttribute("difficultyLevels", com.codegym.Quiz.entity.Question.DifficultyLevel.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionPage.getTotalPages());
        model.addAttribute("totalItems", questionPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return getViewPath(request, "list");
    }

    @GetMapping("/{id}")
    public String viewQuestionDetail(@PathVariable("id") Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            injectViewMetadata(model, request);
            QuestionDTO questionDTO = questionService.getQuestionDTOById(id);
            model.addAttribute("question", questionDTO);
            return getViewPath(request, "detail");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:" + getBaseUrl(request);
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpServletRequest request) {
        injectViewMetadata(model, request);
        model.addAttribute("questionDTO", new QuestionDTO());
        model.addAttribute("categories", categoryService.getAllCategoryDTOs());
        model.addAttribute("isEdit", false);
        return getViewPath(request, "form");
    }

    @PostMapping("/create")
    public String createQuestion(
            @ModelAttribute("questionDTO") QuestionDTO questionDTO,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

            questionService.createQuestion(questionDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo câu hỏi thành công!");
            return "redirect:" + getBaseUrl(request);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:" + getBaseUrl(request) + "/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            injectViewMetadata(model, request);
            QuestionDTO questionDTO = questionService.getQuestionDTOById(id);
            model.addAttribute("questionDTO", questionDTO);
            model.addAttribute("categories", categoryService.getAllCategoryDTOs());
            model.addAttribute("isEdit", true);
            return getViewPath(request, "form");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:" + getBaseUrl(request);
        }
    }

    @PostMapping("/{id}/edit")
    public String updateQuestion(
            @PathVariable("id") Long id,
            @ModelAttribute("questionDTO") QuestionDTO questionDTO,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

            questionService.updateQuestion(id, questionDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật câu hỏi thành công!");
            return "redirect:" + getBaseUrl(request);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:" + getBaseUrl(request) + "/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable("id") Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            questionService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa câu hỏi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + getBaseUrl(request);
    }
}
