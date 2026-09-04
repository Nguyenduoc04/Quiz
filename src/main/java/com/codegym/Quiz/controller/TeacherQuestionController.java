package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.dto.QuestionDTO;
import com.codegym.Quiz.entity.Category;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.CategoryRepository;
import com.codegym.Quiz.service.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teacher/questions")
public class TeacherQuestionController {

    private final QuestionService questionService;
    private final CategoryRepository categoryRepository;
    private final AuthenticationHelper authHelper;

    public TeacherQuestionController(
            QuestionService questionService,
            CategoryRepository categoryRepository,
            AuthenticationHelper authHelper) {

        this.questionService = questionService;
        this.categoryRepository = categoryRepository;
        this.authHelper = authHelper;
    }

    private void injectFullName(Model model) {
        authHelper.getCurrentUser().ifPresent(user -> {
            String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName()
                    : user.getUsername();
            model.addAttribute("fullName", displayName);
        });
    }

    /**
     * Teacher xem và tìm kiếm câu hỏi của chính mình.
     */
    @GetMapping
    public String listQuestions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Question.DifficultyLevel difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        injectFullName(model);

        User currentUser = authHelper.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

        Pageable pageable = PageRequest.of(page, size);

        Page<QuestionDTO> questionPage = questionService.searchQuestionsByUser(
                currentUser, keyword, categoryId, difficulty, pageable);

        List<Category> categories = categoryRepository.findByCreatedBy(currentUser);

        model.addAttribute("questions", questionPage.getContent());
        model.addAttribute("questionPage", questionPage);
        model.addAttribute("categories", categories);
        model.addAttribute("difficulties", Question.DifficultyLevel.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", questionPage.getTotalPages());
        model.addAttribute("totalItems", questionPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "teacher/question/list";
    }

    @GetMapping("/{id}")
    public String viewQuestionDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            injectFullName(model);
            QuestionDTO questionDTO = questionService.getQuestionDTOById(id);
            model.addAttribute("question", questionDTO);
            return "teacher/question/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/questions";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        injectFullName(model);
        User currentUser = authHelper.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

        List<Category> categories = categoryRepository.findByCreatedBy(currentUser);
        model.addAttribute("questionDTO", new QuestionDTO());
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);
        return "teacher/question/form";
    }

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

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            injectFullName(model);
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));
            QuestionDTO questionDTO = questionService.getQuestionDTOById(id);
            List<Category> categories = categoryRepository.findByCreatedBy(currentUser);
            model.addAttribute("questionDTO", questionDTO);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", true);
            return "teacher/question/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/questions";
        }
    }

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