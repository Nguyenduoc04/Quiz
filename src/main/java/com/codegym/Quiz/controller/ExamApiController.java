package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.dto.ExamDetailDTO;
import com.codegym.Quiz.dto.ExamResultDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.service.ExamService;
import com.codegym.Quiz.service.ExamSubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
public class ExamApiController {

    private final ExamService examService;
    private final ExamSubmissionService examSubmissionService;
    private final AuthenticationHelper authHelper;

    public ExamApiController(
            ExamService examService,
            ExamSubmissionService examSubmissionService,
            AuthenticationHelper authHelper) {

        this.examService = examService;
        this.examSubmissionService = examSubmissionService;
        this.authHelper = authHelper;
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

    /**
     * API nộp bài thi Offline.
     *
     * POST /api/exams/{examId}/submit
     */
    @PostMapping("/{examId}/submit")
    public ResponseEntity<?> submitExam(
            @PathVariable Long examId,
            @RequestBody SubmitExamDTO submitExamDTO) {

        // 1. Kiểm tra người dùng đã đăng nhập chưa
        User currentUser = authHelper
                .getCurrentUser()
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Bạn chưa đăng nhập.");
        }

        // 2. Chỉ STUDENT hoặc ADMIN được lưu kết quả
        if (!authHelper.canSaveQuizResult()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Tài khoản không có quyền nộp bài thi.");
        }

        // 3. Lấy examId từ URL
        // Không tin examId do client gửi trong JSON
        submitExamDTO.setExamId(examId);

        // 4. Chấm bài + lưu kết quả
        ExamResultDTO result =
                examSubmissionService.submitExam(
                        submitExamDTO,
                        currentUser.getId()
                );

        // 5. Trả kết quả
        return ResponseEntity.ok(result);
    }
}