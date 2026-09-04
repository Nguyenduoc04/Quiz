package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.dto.QuestionDTO;
import com.codegym.Quiz.entity.Category;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.CategoryRepository;
import com.codegym.Quiz.service.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/teacher/questions")
public class TeacherQuestionController {

    private final QuestionService questionService;
    private final CategoryRepository categoryRepository;
    private final AuthenticationHelper authHelper;

    public TeacherQuestionController(
            QuestionService questionService,
            CategoryRepository categoryRepository,
            AuthenticationHelper authHelper) {

        this.questionService = questionService;
        this.categoryRepository = categoryRepository;
        this.authHelper = authHelper;
    }

    /**
     * Teacher xem và tìm kiếm câu hỏi của chính mình.
     * Hỗ trợ:
     * - keyword
     * - category
     * - difficulty
     * - pagination
     */
    @GetMapping
    public String listQuestions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false)
            Question.DifficultyLevel difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Lấy Teacher đang đăng nhập
        User currentUser = authHelper.getCurrentUser()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Người dùng chưa đăng nhập"
                        ));

        // Pagination
        Pageable pageable = PageRequest.of(page, size);

        // Search Question của chính Teacher
        Page<QuestionDTO> questionPage =
                questionService.searchQuestionsByUser(
                        currentUser,
                        keyword,
                        categoryId,
                        difficulty,
                        pageable
                );

        // Chỉ lấy Category của Teacher hiện tại
        List<Category> categories =
                categoryRepository.findByCreatedBy(currentUser);

        // Danh sách Question
        model.addAttribute(
                "questions",
                questionPage.getContent()
        );

        model.addAttribute(
                "questionPage",
                questionPage
        );

        // Category filter
        model.addAttribute(
                "categories",
                categories
        );

        // Difficulty filter
        model.addAttribute(
                "difficulties",
                Question.DifficultyLevel.values()
        );

        // Giữ điều kiện search
        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "categoryId",
                categoryId
        );

        model.addAttribute(
                "difficulty",
                difficulty
        );

        // Pagination
        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "totalPages",
                questionPage.getTotalPages()
        );

        model.addAttribute(
                "totalItems",
                questionPage.getTotalElements()
        );

        model.addAttribute(
                "pageSize",
                size
        );

        return "teacher/question/list";
    }
}