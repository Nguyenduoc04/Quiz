package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.repository.ExamQuestionRepository;
import com.codegym.Quiz.repository.ExamRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import com.codegym.Quiz.service.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;

    public ExamServiceImpl(ExamRepository examRepository,
                           QuestionRepository questionRepository,
                           ExamQuestionRepository examQuestionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examQuestionRepository = examQuestionRepository;
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
    public List<Exam> getExamsByCreatedBy(String createdBy) {
        return examRepository.findByCreatedByUsername(createdBy);
    }

    @Override
    @Transactional(readOnly = true)
    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Exam findById(Long id) {
        return getExamById(id);
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

    @Override
    @Transactional(readOnly = true)
    public List<Long> getQuestionIdsByExamId(Long examId) {
        Exam exam = getExamById(examId);
        List<Long> ids = new ArrayList<>();
        for (ExamQuestion eq : exam.getExamQuestions()) {
            if (eq.getQuestion() != null) {
                ids.add(eq.getQuestion().getId());
            }
        }
        return ids;
    }

    @Override
    public void updateExamQuestions(Long examId, List<Long> questionIds) {
        Exam exam = getExamById(examId);
        exam.getExamQuestions().clear();

        if (questionIds != null && !questionIds.isEmpty()) {
            int ordinal = 1;
            for (Long qId : questionIds) {
                Question question = questionRepository.findById(qId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi ID: " + qId));

                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setExam(exam);
                examQuestion.setQuestion(question);
                examQuestion.setQuestionOrder(ordinal++);

                exam.getExamQuestions().add(examQuestion);
            }
        }
        examRepository.save(exam);
    }

    @Override
    public void addQuestionToExam(Long examId, Long questionId) {
        if (examQuestionRepository.existsByExamIdAndQuestionId(examId, questionId)) {
            return;
        }

        Exam exam = getExamById(examId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi ID: " + questionId));

        Integer maxOrder = examQuestionRepository.findMaxOrderNumByExamId(examId).orElse(0);

        ExamQuestion eq = new ExamQuestion();
        eq.setExam(exam);
        eq.setQuestion(question);
        eq.setQuestionOrder(maxOrder + 1);

        examQuestionRepository.save(eq);
    }

    @Override
    public void removeQuestionFromExam(Long examId, Long questionId) {
        examQuestionRepository.deleteByExamIdAndQuestionId(examId, questionId);
    }
}