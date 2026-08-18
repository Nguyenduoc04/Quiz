package com.codegym.Quiz.controller;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.service.ExamService;
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

    // Constructor Injection thủ công (Không dùng Lombok)
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    // 1. Danh sách bài thi & Lọc theo người tạo (Bước 3)
    @GetMapping
    public String listExams(
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) Boolean myExamsOnly,
            Authentication authentication,
            Model model
    ) {
        List<Exam> exams;

        if (Boolean.TRUE.equals(myExamsOnly) && authentication != null) {
            String currentUsername = authentication.getName();
            exams = examService.getExamsByCreatedBy(currentUsername);
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

    // 2. Form tạo bài thi mới (Bước 5 & 6)
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

    // 3. Xử lý lưu bài thi mới (Bước 5)
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

        if (authentication != null) {
            exam.setCreatedBy(authentication.getName());
        } else {
            exam.setCreatedBy("Anonymous");
        }

        examService.createExam(exam);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo đề thi mới thành công!");
        return "redirect:/exams";
    }

    // 4. Form chỉnh sửa thông tin bài thi (Bước 8 & 9)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Exam exam = examService.getExamById(id);

        model.addAttribute("exam", exam);
        model.addAttribute("statuses", ExamStatus.values());
        model.addAttribute("pageTitle", "Chỉnh sửa Đề thi");
        addAuthAttributes(model, authentication);
        return "exam/form";
    }

    // 5. Xử lý cập nhật bài thi (Bước 8)
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

    // 6. Xóa bài thi
    @GetMapping("/delete/{id}")
    public String deleteExam(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        examService.deleteExam(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa đề thi thành công!");
        return "redirect:/exams";
    }

    // Helper method đồng bộ dữ liệu User sang Navbar/Layout
    private void addAuthAttributes(Model model, Authentication authentication) {
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isLoggedIn", isLoggedIn);
        if (isLoggedIn) {
            model.addAttribute("username", authentication.getName());
        }
    }
}