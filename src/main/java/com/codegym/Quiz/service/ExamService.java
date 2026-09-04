package com.codegym.Quiz.service;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.dto.ExamDetailDTO;

import java.util.List;

public interface ExamService {

    List<Exam> getAllExams();

    List<Exam> getActiveExams();

    List<Exam> getExamsByCreatedBy(String createdBy);

    Exam getExamById(Long id);

    Exam findById(Long id);

    Exam createExam(Exam exam);

    Exam updateExam(Long id, Exam examDetails);

    void deleteExam(Long id);

    void updateStatus(Long id, ExamStatus status);

    List<Long> getQuestionIdsByExamId(Long examId);

    void updateExamQuestions(Long examId, List<Long> questionIds);

    void addQuestionToExam(Long examId, Long questionId);

    void removeQuestionFromExam(Long examId, Long questionId);

    ExamDetailDTO getExamDetailForStudent(Long examId);
}