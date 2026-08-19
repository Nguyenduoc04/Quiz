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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/categories")
public class TeacherCategoryController {

    private final CategoryService categoryService;
    private final AuthenticationHelper authHelper;

    public TeacherCategoryController(CategoryService categoryService, AuthenticationHelper authHelper) {
        this.categoryService = categoryService;
        this.authHelper = authHelper;
    }

    private void injectFullName(Model model) {
        authHelper.getCurrentUser().ifPresent(user -> {
            String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName()
                    : user.getUsername();
            model.addAttribute("fullName", displayName);
        });
    }

    /** Danh sách danh mục của Giáo viên */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        injectFullName(model);
        User currentUser = authHelper.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDTO> categoryPage = categoryService.getCategoriesByUserPaged(currentUser, keyword, pageable);

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "teacher/category/list";
    }

    /** Form tạo mới danh mục cho Giáo viên */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        injectFullName(model);
        model.addAttribute("categoryDTO", new CategoryDTO());
        model.addAttribute("isEdit", false);
        return "teacher/category/form";
    }

    /** Xử lý tạo mới danh mục */
    @PostMapping("/create")
    public String createCategory(
            @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

            categoryService.createCategory(categoryDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo danh mục thành công!");
            return "redirect:/teacher/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("categoryDTO", categoryDTO);
            return "redirect:/teacher/categories/create";
        }
    }

    /** Form cập nhật danh mục của Giáo viên */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            injectFullName(model);
            CategoryDTO categoryDTO = categoryService.getCategoryDTOById(id);
            model.addAttribute("categoryDTO", categoryDTO);
            model.addAttribute("isEdit", true);
            return "teacher/category/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/categories";
        }
    }

    /** Xử lý cập nhật danh mục */
    @PostMapping("/{id}/edit")
    public String updateCategory(
            @PathVariable("id") Long id,
            @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

            categoryService.updateCategory(id, categoryDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
            return "redirect:/teacher/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/categories/" + id + "/edit";
        }
    }

    /** Xóa danh mục */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/categories";
    }
}
