package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.ExamGradingResultDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;

public interface ExamGradingService {

    ExamGradingResultDTO gradeExam(SubmitExamDTO submitExamDTO);
}