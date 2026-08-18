package com.codegym.Quiz.controller;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.repository.ExamQuestionRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import com.codegym.Quiz.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Exam exam = examService.findById(id);
        List<ExamQuestion> currentQuestions = examQuestionRepository.findByExamIdOrderByQuestionOrderAsc(id);

        List<Long> addedQuestionIds = currentQuestions.stream()
                .map(eq -> eq.getQuestion().getId())
                .toList();

        List<Question> availableQuestions = addedQuestionIds.isEmpty() ?
                questionRepository.findAll() : questionRepository.findByIdNotIn(addedQuestionIds);

        model.addAttribute("exam", exam);
        model.addAttribute("currentQuestions", currentQuestions);
        model.addAttribute("availableQuestions", availableQuestions);
        return "admin/exam-edit";
    }

    @PostMapping("/{examId}/add-question")
    public String addQuestion(@PathVariable Long examId, @RequestParam Long questionId) {
        examService.addQuestionToExam(examId, questionId);
        return "redirect:/admin/exams/edit/" + examId;
    }

    @PostMapping("/{examId}/remove-question/{questionId}")
    public String removeQuestion(@PathVariable Long examId, @PathVariable Long questionId) {
        examService.removeQuestionFromExam(examId, questionId);
        return "redirect:/admin/exams/edit/" + examId;
    }
}