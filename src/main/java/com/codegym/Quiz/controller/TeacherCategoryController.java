package com.codegym.Quiz.controller;

import com.codegym.Quiz.authentication.util.AuthenticationHelper;
import com.codegym.Quiz.dto.CategoryDTO;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/teacher/categories")
public class TeacherCategoryController {

    private final CategoryService categoryService;
    private final AuthenticationHelper authHelper;

    public TeacherCategoryController(CategoryService categoryService,
                                     AuthenticationHelper authHelper) {
        this.categoryService = categoryService;
        this.authHelper = authHelper;
    }

    /**
     * Teacher xem danh sách category của chính mình.
     * Có hỗ trợ tìm kiếm theo keyword và phân trang.
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        User currentUser = authHelper.getCurrentUser()
                .orElseThrow(() ->
                        new IllegalStateException("Người dùng chưa đăng nhập"));

        Pageable pageable = PageRequest.of(page, size);

        Page<CategoryDTO> categoryPage =
                categoryService.getCategoriesByUserPaged(
                        currentUser,
                        keyword,
                        pageable
                );
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);

        model.addAttribute("keyword", keyword);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "teacher/category/list";
    }
    /**
     * Hiển thị form tạo category mới cho Teacher.
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categoryDTO", new CategoryDTO());
        model.addAttribute("isEdit", false);

        return "teacher/category/form";
    }

    /**
     * Teacher tạo category mới.
     */
    @PostMapping("/create")
    public String createCategory(
            @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() ->
                            new IllegalStateException("Người dùng chưa đăng nhập"));

            categoryService.createCategory(categoryDTO, currentUser);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Tạo danh mục thành công!"
            );

            return "redirect:/teacher/categories";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "categoryDTO",
                    categoryDTO
            );

            return "redirect:/teacher/categories/create";
        }
    }
    /**
     * Hiển thị form chỉnh sửa category.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            CategoryDTO categoryDTO = categoryService.getCategoryDTOById(id);

            model.addAttribute("categoryDTO", categoryDTO);
            model.addAttribute("isEdit", true);

            return "teacher/category/form";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/teacher/categories";
        }
    }


    /**
     * Teacher cập nhật category.
     */
    @PostMapping("/{id}/edit")
    public String updateCategory(
            @PathVariable("id") Long id,
            @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() ->
                            new IllegalStateException("Người dùng chưa đăng nhập"));

            categoryService.updateCategory(
                    id,
                    categoryDTO,
                    currentUser
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật danh mục thành công!"
            );

            return "redirect:/teacher/categories";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/teacher/categories/" + id + "/edit";
        }
    }
    /**
     * Teacher xóa category.
     * Category đang chứa câu hỏi sẽ không được phép xóa.
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {

            categoryService.deleteCategory(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa danh mục thành công!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/teacher/categories";
    }
}