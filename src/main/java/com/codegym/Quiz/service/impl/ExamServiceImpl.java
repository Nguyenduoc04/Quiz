package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.repository.ExamRepository;
import com.codegym.Quiz.service.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;

    // Constructor Injection thủ công thay cho Lombok
    public ExamServiceImpl(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> getActiveExams() {
        return examRepository.findByStatus(ExamStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi có ID: " + id));
    }

    @Override
    public Exam createExam(Exam exam) {
        if (exam.getStatus() == null) {
            exam.setStatus(ExamStatus.DRAFT);
        }
        return examRepository.save(exam);
    }

    @Override
    public Exam updateExam(Long id, Exam examDetails) {
        Exam existingExam = getExamById(id);
        existingExam.setTitle(examDetails.getTitle());
        existingExam.setDescription(examDetails.getDescription());
        existingExam.setDurationMinutes(examDetails.getDurationMinutes());
        existingExam.setStatus(examDetails.getStatus());
        return examRepository.save(existingExam);
    }

    @Override
    public void deleteExam(Long id) {
        Exam exam = getExamById(id);
        examRepository.delete(exam);
    }

    @Override
    public void updateStatus(Long id, ExamStatus status) {
        Exam exam = getExamById(id);
        exam.setStatus(status);
        examRepository.save(exam);
    }
}