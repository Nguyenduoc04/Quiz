package com.codegym.Quiz.controller;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.repository.CategoryRepository;
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

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. Hiển thị danh sách đề thi
    @GetMapping
    public String showExamList(Model model) {
        model.addAttribute("exams", examService.getAllExams());
        return "exams/list";
    }

    // 2. Trang tạo mới đề thi (GET)
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("exam", new Exam());
        model.addAttribute("statuses", ExamStatus.values());
        return "exams/form";
    }

    // Xử lý submit form tạo mới đề thi (POST)
    @PostMapping("/create")
    public String createExam(@ModelAttribute("exam") Exam exam) {
        examService.createExam(exam);
        return "redirect:/admin/exams";
    }

    // 3. Trang chỉnh sửa thông tin đề thi (GET)
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
        model.addAttribute("statuses", ExamStatus.values());
        model.addAttribute("currentQuestions", currentQuestions);
        model.addAttribute("availableQuestions", availableQuestions);

        return "exams/form";
    }

    // Xử lý submit form cập nhật đề thi (POST)
    @PostMapping("/edit/{id}")
    public String updateExam(@PathVariable Long id, @ModelAttribute("exam") Exam examDetails) {
        examService.updateExam(id, examDetails);
        return "redirect:/admin/exams";
    }

    // 4. Xóa đề thi (GET)
    @GetMapping("/delete/{id}")
    public String deleteExam(@PathVariable Long id) {
        examService.deleteExam(id); // Nếu trong ExamService bạn đặt tên hàm là deleteById(id) thì sửa lại ở đây nhé
        return "redirect:/admin/exams";
    }

    // 5. Trang chọn câu hỏi cho đề thi (GET: Lọc & Hiển thị)
    @GetMapping("/{id}/questions")
    public String showSelectQuestions(
            @PathVariable Long id,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model) {

        Exam exam = examService.findById(id);
        List<Question> questions = (categoryId != null) ?
                questionRepository.findByCategoryId(categoryId) : questionRepository.findAll();

        model.addAttribute("exam", exam);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("questions", questions);
        model.addAttribute("selectedQuestionIds", examService.getQuestionIdsByExamId(id));

        return "exams/select-questions";
    }

    // Xử lý lưu danh sách câu hỏi đã chọn vào đề thi (POST)
    @PostMapping("/{id}/questions")
    public String saveExamQuestions(
            @PathVariable Long id,
            @RequestParam(value = "questionIds", required = false) List<Long> questionIds) {

        if (questionIds == null) {
            questionIds = List.of();
        }
        examService.updateExamQuestions(id, questionIds);
        return "redirect:/admin/exams";
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