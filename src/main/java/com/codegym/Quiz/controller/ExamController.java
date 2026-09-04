package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.CategoryRepository;
import com.codegym.Quiz.repository.ExamQuestionRepository;
import com.codegym.Quiz.repository.ExamRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import com.codegym.Quiz.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Controller
@RequestMapping({"/admin/exams", "/teacher/exams"})
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private AuthenticationHelper authHelper;

    @Autowired
    private com.codegym.Quiz.service.ExamResultService examResultService;

    private String getBaseUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin")) {
            return "/admin/exams";
        }
        return "/teacher/exams";
    }

    private boolean isAdminView(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/admin");
    }

    private void injectViewMetadata(Model model, HttpServletRequest request) {
        model.addAttribute("baseUrl", getBaseUrl(request));
        model.addAttribute("isAdminView", isAdminView(request));
    }

    private String getViewPath(HttpServletRequest request, String pageName) {
        if (isAdminView(request)) {
            return "admin/exam/" + pageName;
        }
        return "teacher/exam/" + pageName;
    }

    // 1. Hiển thị danh sách đề thi
    @GetMapping
    public String showExamList(Model model, HttpServletRequest request) {
        injectViewMetadata(model, request);
        model.addAttribute("exams", examService.getAllExams());
        return getViewPath(request, "list");
    }

    // 2. Trang tạo mới đề thi (GET)
    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model,
            HttpServletRequest request) {

        injectViewMetadata(model, request);
        Exam exam = new Exam();
        List<Question> questions = (categoryId != null) ?
                questionRepository.findByCategoryId(categoryId) : questionRepository.findAll();

        model.addAttribute("exam", exam);
        model.addAttribute("statuses", ExamStatus.values());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("questions", questions);
        model.addAttribute("selectedQuestionIds", List.of());

        return getViewPath(request, "form");
    }

    // Xử lý submit form tạo mới đề thi (POST)
    @PostMapping("/create")
    public String createExam(
            @ModelAttribute("exam") Exam exam,
            @RequestParam(value = "questionIds", required = false) List<Long> questionIds,
            HttpServletRequest request) {

        authHelper.getCurrentUser().ifPresent(exam::setCreatedBy);
        Exam createdExam = examService.createExam(exam);

        if (questionIds != null && !questionIds.isEmpty()) {
            examService.updateExamQuestions(createdExam.getId(), questionIds);
        }
        return "redirect:" + getBaseUrl(request);
    }

    // 3. Trang chỉnh sửa thông tin & chọn câu hỏi cho đề thi (GET)
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model,
            HttpServletRequest request) {

        injectViewMetadata(model, request);
        Exam exam = examService.findById(id);
        List<Question> questions = (categoryId != null) ?
                questionRepository.findByCategoryId(categoryId) : questionRepository.findAll();
        List<Long> selectedQuestionIds = examService.getQuestionIdsByExamId(id);

        model.addAttribute("exam", exam);
        model.addAttribute("statuses", ExamStatus.values());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("questions", questions);
        model.addAttribute("selectedQuestionIds", selectedQuestionIds);

        return getViewPath(request, "form");
    }

    // Xử lý submit form cập nhật đề thi & danh sách câu hỏi (POST)
    @PostMapping("/edit/{id}")
    public String updateExam(
            @PathVariable Long id,
            @ModelAttribute("exam") Exam examDetails,
            @RequestParam(value = "questionIds", required = false) List<Long> questionIds,
            HttpServletRequest request) {

        examService.updateExam(id, examDetails);
        if (questionIds == null) {
            questionIds = List.of();
        }
        examService.updateExamQuestions(id, questionIds);

        return "redirect:" + getBaseUrl(request);
    }

    // 4. Xóa đề thi
    @RequestMapping(value = "/delete/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String deleteExam(@PathVariable Long id, HttpServletRequest request) {
        examService.deleteExam(id);
        return "redirect:" + getBaseUrl(request);
    }

    // 5. Trang chọn câu hỏi cho đề thi (GET: Lọc & Hiển thị)
    @GetMapping("/{id}/questions")
    public String showSelectQuestions(
            @PathVariable Long id,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model,
            HttpServletRequest request) {

        injectViewMetadata(model, request);
        Exam exam = examService.findById(id);
        List<Question> questions = (categoryId != null) ?
                questionRepository.findByCategoryId(categoryId) : questionRepository.findAll();

        model.addAttribute("exam", exam);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("questions", questions);
        model.addAttribute("selectedQuestionIds", examService.getQuestionIdsByExamId(id));

        return getViewPath(request, "select-questions");
    }

    // Xử lý lưu danh sách câu hỏi đã chọn vào đề thi (POST)
    @PostMapping("/{id}/questions")
    public String saveExamQuestions(
            @PathVariable Long id,
            @RequestParam(value = "questionIds", required = false) List<Long> questionIds,
            HttpServletRequest request) {

        if (questionIds == null) {
            questionIds = List.of();
        }
        examService.updateExamQuestions(id, questionIds);
        return "redirect:" + getBaseUrl(request);
    }

    @PostMapping("/{examId}/add-question")
    public String addQuestion(@PathVariable Long examId, @RequestParam Long questionId, HttpServletRequest request) {
        examService.addQuestionToExam(examId, questionId);
        return "redirect:" + getBaseUrl(request) + "/edit/" + examId;
    }

    @PostMapping("/{examId}/remove-question/{questionId}")
    public String removeQuestion(@PathVariable Long examId, @PathVariable Long questionId, HttpServletRequest request) {
        examService.removeQuestionFromExam(examId, questionId);
        return "redirect:" + getBaseUrl(request) + "/edit/" + examId;
    }

    // 6. Hiển thị Lịch sử nộp bài & Điểm số của Thí sinh (Dành cho Giáo viên & Admin)
    @GetMapping({"/results", "/exam-results"})
    public String showExamResultsList(
            @RequestParam(value = "examId", required = false) Long examId,
            Model model,
            HttpServletRequest request) {

        injectViewMetadata(model, request);

        User currentUser = authHelper.getCurrentUser().orElse(null);
        List<com.codegym.Quiz.dto.ExamResultDTO> results;

        if (examId != null && examId > 0) {
            results = examResultService.getResultsForExam(examId);
            model.addAttribute("selectedExamId", examId);
        } else if (isAdminView(request)) {
            results = examResultService.getResultsForExam(null);
        } else {
            results = examResultService.getResultsForTeacher(currentUser);
        }

        List<Exam> teacherExams = (currentUser != null) ? examService.getExamsByCreatedBy(currentUser.getUsername()) : examService.getAllExams();

        model.addAttribute("results", results);
        model.addAttribute("exams", teacherExams);

        return getViewPath(request, "results-list");
    }

    // 7. Xem chi tiết bài làm của Thí sinh (Dành cho Giáo viên & Admin)
    @GetMapping({"/results/{resultId}", "/exam-results/{resultId}"})
    public String viewStudentResultDetail(
            @PathVariable Long resultId,
            Model model,
            HttpServletRequest request,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        try {
            injectViewMetadata(model, request);
            com.codegym.Quiz.dto.ExamResultDTO result = examResultService.getResultById(resultId);
            if (result == null) {
                throw new IllegalArgumentException("Không tìm thấy bài làm ID: " + resultId);
            }
            model.addAttribute("result", result);
            model.addAttribute("isTeacherView", true);
            return "exams/result";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:" + getBaseUrl(request) + "/results";
        }
    }
}