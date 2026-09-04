package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.ExamResultDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;

public interface ExamSubmissionService {

    /**
     * Nộp bài thi Offline:
     * - Chấm điểm
     * - Lưu kết quả
     * - Trả kết quả cho học viên
     */
    ExamResultDTO submitExam(
            SubmitExamDTO submitExamDTO,
            Long studentId
    );
}