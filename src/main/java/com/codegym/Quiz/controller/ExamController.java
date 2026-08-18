package com.codegym.Quiz.controller;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.service.ExamService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public String listExams(
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) Boolean myExamsOnly,
            Authentication authentication,
            Model model
    ) {
        List<Exam> exams;

        // Nếu tích chọn "Chỉ đề thi của tôi"
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
        return "exam/list";
    }
}