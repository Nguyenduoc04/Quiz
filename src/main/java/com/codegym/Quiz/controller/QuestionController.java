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

@Controller
@RequestMapping("/teacher/questions")
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

    /** Inject fullName vào Model để Navbar hiển thị đúng họ tên đầy đủ */
    private void injectFullName(Model model) {
        authHelper.getCurrentUser().ifPresent(user -> {
            String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName()
                    : user.getUsername();
            model.addAttribute("fullName", displayName);
        });
    }

    /** US 26: Giáo viên xem danh sách câu hỏi trong hệ thống (Phân trang) */
    @GetMapping
    public String listQuestions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        injectFullName(model);

        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionDTO> questionPage = questionService.getAllQuestionsPaged(keyword, pageable);

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("questionPage", questionPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionPage.getTotalPages());
        model.addAttribute("totalItems", questionPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "question/list";
    }

    /** US 25: Giáo viên xem chi tiết 1 câu hỏi */
    @GetMapping("/{id}")
    public String viewQuestionDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            injectFullName(model);
            QuestionDTO questionDTO = questionService.getQuestionDTOById(id);
            model.addAttribute("question", questionDTO);
            return "question/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/questions";
        }
    }

    /** Form tạo mới câu hỏi */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        injectFullName(model);
        model.addAttribute("questionDTO", new QuestionDTO());
        model.addAttribute("categories", categoryService.getAllCategoryDTOs());
        model.addAttribute("isEdit", false);
        return "question/form";
    }

    /** US 23: Giáo viên tạo mới 1 câu hỏi */
    @PostMapping("/create")
    public String createQuestion(
            @ModelAttribute("questionDTO") QuestionDTO questionDTO,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

            questionService.createQuestion(questionDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo câu hỏi thành công!");
            return "redirect:/teacher/questions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/questions/create";
        }
    }

    /** Form cập nhật câu hỏi */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            injectFullName(model);
            QuestionDTO questionDTO = questionService.getQuestionDTOById(id);
            model.addAttribute("questionDTO", questionDTO);
            model.addAttribute("categories", categoryService.getAllCategoryDTOs());
            model.addAttribute("isEdit", true);
            return "question/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/questions";
        }
    }

    /** US 24: Giáo viên cập nhật thông tin 1 câu hỏi */
    @PostMapping("/{id}/edit")
    public String updateQuestion(
            @PathVariable("id") Long id,
            @ModelAttribute("questionDTO") QuestionDTO questionDTO,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

            questionService.updateQuestion(id, questionDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật câu hỏi thành công!");
            return "redirect:/teacher/questions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/questions/" + id + "/edit";
        }
    }

    /** US 27: Giáo viên xóa 1 câu hỏi */
    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            questionService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa câu hỏi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/questions";
    }
}
