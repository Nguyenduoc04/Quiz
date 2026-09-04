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
import com.codegym.Quiz.dto.ExamAnswerDTO;
import com.codegym.Quiz.dto.ExamDetailDTO;
import com.codegym.Quiz.dto.ExamQuestionDetailDTO;
import com.codegym.Quiz.entity.Answer;

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
        // Xóa tất cả câu hỏi cũ và flush ngay xuống DB
        // để tránh lỗi duplicate key khi INSERT mới trong cùng transaction
        examQuestionRepository.deleteByExamId(examId);
        examQuestionRepository.flush();

        if (questionIds != null && !questionIds.isEmpty()) {
            Exam exam = getExamById(examId);
            List<ExamQuestion> newQuestions = new ArrayList<>();
            int ordinal = 1;
            for (Long qId : questionIds) {
                Question question = questionRepository.findById(qId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi ID: " + qId));

                ExamQuestion eq = new ExamQuestion();
                eq.setExam(exam);
                eq.setQuestion(question);
                eq.setQuestionOrder(ordinal++);
                newQuestions.add(eq);
            }
            examQuestionRepository.saveAll(newQuestions);
        }
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
    @Override
    @Transactional(readOnly = true)
    public ExamDetailDTO getExamDetailForStudent(Long examId) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy đề thi ID: " + examId));

        // Chỉ cho học viên lấy đề đang ACTIVE
        if (exam.getStatus() != ExamStatus.ACTIVE) {
            throw new RuntimeException("Đề thi hiện không khả dụng");
        }

        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByQuestionOrderAsc(examId);

        ExamDetailDTO examDTO = new ExamDetailDTO();

        examDTO.setId(exam.getId());
        examDTO.setTitle(exam.getTitle());
        examDTO.setDescription(exam.getDescription());
        examDTO.setDurationMinutes(exam.getDurationMinutes());
        examDTO.setTotalScore(exam.getTotalScore());
        examDTO.setPassingScore(exam.getPassingScore());
        examDTO.setStatus(exam.getStatus());
        examDTO.setTotalQuestions(examQuestions.size());

        List<ExamQuestionDetailDTO> questionDTOs = new ArrayList<>();

        for (ExamQuestion examQuestion : examQuestions) {

            Question question = examQuestion.getQuestion();

            ExamQuestionDetailDTO questionDTO =
                    new ExamQuestionDetailDTO();

            questionDTO.setId(question.getId());
            questionDTO.setContent(question.getContent());
            questionDTO.setQuestionType(question.getQuestionType());
            questionDTO.setDifficultyLevel(question.getDifficultyLevel());
            questionDTO.setQuestionOrder(examQuestion.getQuestionOrder());

            Double score = examQuestion.getCustomScore() != null
                    ? examQuestion.getCustomScore()
                    : question.getScore();

            questionDTO.setScore(score);

            List<ExamAnswerDTO> answerDTOs = new ArrayList<>();

            for (Answer answer : question.getAnswers()) {

                ExamAnswerDTO answerDTO =
                        new ExamAnswerDTO(
                                answer.getId(),
                                answer.getContent(),
                                answer.getDisplayOrder()
                        );

                answerDTOs.add(answerDTO);
            }

            questionDTO.setAnswers(answerDTOs);

            questionDTOs.add(questionDTO);
        }

        examDTO.setQuestions(questionDTOs);

        return examDTO;
    }
}