package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.ExamResultDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;
import com.codegym.Quiz.entity.User;

import java.util.List;

public interface ExamResultService {
    ExamResultDTO submitExam(SubmitExamDTO submitDTO, User user);
    ExamResultDTO submitExamWithStudentName(SubmitExamDTO submitDTO, User user, String studentName);
    ExamResultDTO getResultById(Long resultId);
    List<ExamResultDTO> getStudentHistory(User user);
    List<ExamResultDTO> getResultsForExam(Long examId);
    List<ExamResultDTO> getResultsForTeacher(User teacher);
}
