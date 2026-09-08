package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.dto.ExamDetailDTO;
import com.codegym.Quiz.dto.ExamResultDTO;
import com.codegym.Quiz.dto.SubmitAnswerDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;
import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.service.CategoryService;
import com.codegym.Quiz.service.ExamResultService;
import com.codegym.Quiz.service.ExamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/exams")
public class PublicExamController {

    private final ExamService examService;
    private final CategoryService categoryService;
    private final ExamResultService examResultService;
    private final AuthenticationHelper authHelper;

    public PublicExamController(ExamService examService,
            CategoryService categoryService,
            ExamResultService examResultService,
            AuthenticationHelper authHelper) {
        this.examService = examService;
        this.categoryService = categoryService;
        this.examResultService = examResultService;
        this.authHelper = authHelper;
    }

    /**
     * Tìm kiếm và lọc bài thi công khai cho Học sinh và Khách vãng lai
     */
    @GetMapping({ "", "/search" })
    public String searchExams(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            Model model) {

        List<Exam> activeExams = examService.getActiveExams();

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            activeExams = activeExams.stream()
                    .filter(e -> (e.getTitle() != null && e.getTitle().toLowerCase().contains(kw))
                            || (e.getDescription() != null && e.getDescription().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        model.addAttribute("exams", activeExams);
        model.addAttribute("totalElements", activeExams.size());
        model.addAttribute("categories", categoryService.getAllCategoryDTOs());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedCategoryId", categoryId);

        return "exams/search";
    }

    /**
     * Chi tiết thông tin bài thi trước khi vào thi
     */
    @GetMapping("/{id}/detail")
    public String viewDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Exam exam = examService.getExamById(id);
            model.addAttribute("exam", exam);
            return "redirect:/exams/" + id + "/take";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/exams/search";
        }
    }

    /**
     * Màn hình làm bài thi (Cho phép cả Khách vãng lai và User đã đăng nhập)
     */
    @GetMapping("/{id}/take")
    public String takeExam(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ExamDetailDTO examDetail = examService.getExamDetailForStudent(id);

            Optional<User> currentUserOpt = authHelper.getCurrentUser();
            boolean isLoggedIn = currentUserOpt.isPresent();
            String studentName = currentUserOpt.map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                    .orElse("Khách vãng lai");

            model.addAttribute("examId", examDetail.getId());
            model.addAttribute("examTitle", examDetail.getTitle());
            model.addAttribute("durationMinutes", examDetail.getDurationMinutes());
            model.addAttribute("questions", examDetail.getQuestions());
            model.addAttribute("studentName", studentName);
            model.addAttribute("isLoggedIn", isLoggedIn);

            return "exams/take";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/exams/search";
        }
    }

    /**
     * Nộp bài thi
     * - Khách vãng lai (Chưa đăng nhập): Tính điểm không lưu CSDL
     * - User đã đăng nhập (STUDENT, TEACHER, ADMIN): Tính điểm và lưu CSDL
     */
    @PostMapping("/submit")
    public String submitExam(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        try {
            String examIdStr = request.getParameter("examId");
            if (examIdStr == null) {
                throw new IllegalArgumentException("Không tìm thấy mã bài thi");
            }
            Long examId = Long.parseLong(examIdStr);

            SubmitExamDTO submitDTO = new SubmitExamDTO();
            submitDTO.setExamId(examId);

            List<SubmitAnswerDTO> answers = new ArrayList<>();
            Map<String, String[]> paramMap = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("q_") || key.startsWith("question_")) {
                    try {
                        String qIdStr = key.replace("q_", "").replace("question_", "");
                        Long questionId = Long.parseLong(qIdStr);
                        Long answerId = Long.parseLong(entry.getValue()[0]);
                        answers.add(new SubmitAnswerDTO(questionId, answerId));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            submitDTO.setAnswers(answers);

            User currentUser = authHelper.getCurrentUser().orElse(null);
            String studentName = request.getParameter("studentName");
            if (studentName == null || studentName.isBlank()) {
                studentName = (currentUser != null)
                        ? (currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername())
                        : "Thí sinh";
            }

            ExamResultDTO resultDTO = examResultService.submitExamWithStudentName(submitDTO, currentUser, studentName);

            String pin = request.getParameter("pin");
            if (pin != null && !pin.isBlank()) {
                String cleanPin = pin.replace("-", "").trim();
                OnlineExamController.recordParticipantResult(cleanPin, studentName, resultDTO.getScore(),
                        resultDTO.getCorrectCount(), resultDTO.getTotalQuestions());
                Long resId = resultDTO.getId();
                return "redirect:/user/online-result?pin=" + cleanPin + "&examId=" + examId + "&studentName="
                        + java.net.URLEncoder.encode(studentName, java.nio.charset.StandardCharsets.UTF_8)
                        + (resId != null && resId > 0 ? "&resultId=" + resId : "");
            }

            if (currentUser == null || resultDTO.getId() == null || resultDTO.getId() == 0L) {
                model.addAttribute("result", resultDTO);
                model.addAttribute("isGuest", true);
                return "exams/result";
            }

            redirectAttributes.addFlashAttribute("result", resultDTO);
            redirectAttributes.addFlashAttribute("isGuest", false);
            return "redirect:/exams/result?resultId=" + resultDTO.getId();
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nộp bài thi: " + e.getMessage());
            return "redirect:/exams/search";
        }
    }

    /**
     * Xem kết quả bài thi sau khi nộp
     */
    @GetMapping("/result")
    public String showResult(
            @RequestParam(value = "resultId", required = false) Long resultId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Nếu đã có result từ FlashAttributes (nộp từ Form)
            if (!model.containsAttribute("result") && resultId != null && resultId > 0) {
                ExamResultDTO result = examResultService.getResultById(resultId);
                model.addAttribute("result", result);
            }
            return "exams/result";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Không thể hiển thị kết quả: " + e.getMessage());
            return "exams/result";
        }
    }

    /**
     * Lịch sử thi cá nhân (Chỉ dành cho User đã đăng nhập)
     */
    @GetMapping("/history")
    public String showHistory(Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Vui lòng đăng nhập để xem lịch sử làm bài."));

            List<ExamResultDTO> historyList = examResultService.getStudentHistory(currentUser);
            model.addAttribute("historyList", historyList);
            return "exams/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/login";
        }
    }
}
