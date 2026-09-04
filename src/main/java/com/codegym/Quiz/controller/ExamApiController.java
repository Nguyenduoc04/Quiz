package com.codegym.Quiz.controller;

import com.codegym.Quiz.dto.ExamDetailDTO;
import com.codegym.Quiz.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
public class ExamApiController {

    private final ExamService examService;

    public ExamApiController(ExamService examService) {
        this.examService = examService;
    }

    /**
     * API lấy chi tiết đề thi dành cho học viên.
     */
    @GetMapping("/{examId}")
    public ResponseEntity<ExamDetailDTO> getExamDetail(
            @PathVariable Long examId) {

        ExamDetailDTO exam =
                examService.getExamDetailForStudent(examId);

        return ResponseEntity.ok(exam);
    }
}