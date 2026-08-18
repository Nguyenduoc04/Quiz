package com.codegym.Quiz.controller;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.service.CategoryService;
import com.codegym.Quiz.service.ExamService;
import com.codegym.Quiz.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/exams")
public class ExamController {

    private final ExamService examService;
    private final QuestionService questionService;
    private final CategoryService categoryService;

    public ExamController(ExamService examService, QuestionService questionService, CategoryService categoryService) {
        this.examService = examService;
        this.questionService = questionService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listExams(
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) Boolean myExamsOnly,
            Authentication authentication,
            Model model
    ) {
        List<Exam> exams;
        if (Boolean.TRUE.equals(myExamsOnly) && authentication != null) {
            exams = examService.getExamsByCreatedBy(authentication.getName());
            model.addAttribute("myExamsOnly", true);
        } else if (createdBy != null && !createdBy.isBlank()) {
            exams = examService.getExamsByCreatedBy(createdBy);
            model.addAttribute("createdBy", createdBy);
        } else {
            exams = examService.getAllExams();
        }

        model.addAttribute("exams", exams);
        addAuthAttributes(model, authentication);
        return "exam/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication) {
        Exam exam = new Exam();
        exam.setDurationMinutes(45);
        exam.setStatus(ExamStatus.DRAFT);

        model.addAttribute("exam", exam);
        model.addAttribute("statuses", ExamStatus.values());
        model.addAttribute("pageTitle", "Tạo Đề thi Mới");
        addAuthAttributes(model, authentication);
        return "exam/form";
    }

    @PostMapping("/create")
    public String createExam(
            @Valid @ModelAttribute("exam") Exam exam,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", ExamStatus.values());
            model.addAttribute("pageTitle", "Tạo Đề thi Mới");
            addAuthAttributes(model, authentication);
            return "exam/form";
        }

        exam.setCreatedBy((authentication != null) ? authentication.getName() : "Anonymous");
        examService.createExam(exam);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo đề thi mới thành công!");
        return "redirect:/exams";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, Authentication authentication) {
        model.addAttribute("exam", examService.getExamById(id));
        model.addAttribute("statuses", ExamStatus.values());
        model.addAttribute("pageTitle", "Chỉnh sửa Đề thi");
        addAuthAttributes(model, authentication);
        return "exam/form";
    }

    @PostMapping("/edit/{id}")
    public String updateExam(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("exam") Exam exam,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", ExamStatus.values());
            model.addAttribute("pageTitle", "Chỉnh sửa Đề thi");
            addAuthAttributes(model, authentication);
            return "exam/form";
        }

        examService.updateExam(id, exam);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đề thi thành công!");
        return "redirect:/exams";
    }

    @GetMapping("/delete/{id}")
    public String deleteExam(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        examService.deleteExam(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa đề thi thành công!");
        return "redirect:/exams";
    }

    @GetMapping("/{id}/questions")
    public String showSelectQuestionsForm(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Long categoryId,
            Model model,
            Authentication authentication
    ) {
        Exam exam = examService.getExamById(id);
        List<Question> questions = (categoryId != null)
                ? questionService.getQuestionsByCategoryId(categoryId)
                : questionService.getAllQuestions();

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedQuestionIds", examService.getQuestionIdsByExamId(id));
        addAuthAttributes(model, authentication);

        return "exam/select-questions";
    }

    @PostMapping("/{id}/questions")
    public String saveSelectedQuestions(
            @PathVariable("id") Long id,
            @RequestParam(name = "questionIds", required = false) List<Long> questionIds,
            RedirectAttributes redirectAttributes
    ) {
        examService.updateExamQuestions(id, questionIds);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh sách câu hỏi thành công!");
        return "redirect:/exams";
    }

    private void addAuthAttributes(Model model, Authentication authentication) {
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isLoggedIn", isLoggedIn);
        if (isLoggedIn) {
            model.addAttribute("username", authentication.getName());
        }
    }
}