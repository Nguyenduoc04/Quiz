package com.codegym.Quiz.service;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.repository.ExamQuestionRepository;
import com.codegym.Quiz.repository.ExamRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));
    }

    public void addQuestionToExam(Long examId, Long questionId) {
        if (examQuestionRepository.existsByExamIdAndQuestionId(examId, questionId)) {
            return;
        }

        Exam exam = findById(examId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        Integer maxOrder = examQuestionRepository.findMaxOrderNumByExamId(examId).orElse(0);

        ExamQuestion eq = new ExamQuestion();
        eq.setExam(exam);
        eq.setQuestion(question);
        eq.setQuestionOrder(maxOrder + 1);

        examQuestionRepository.save(eq);
    }

    public void removeQuestionFromExam(Long examId, Long questionId) {
        examQuestionRepository.deleteByExamIdAndQuestionId(examId, questionId);
    }
}