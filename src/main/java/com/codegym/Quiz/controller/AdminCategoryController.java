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
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final AuthenticationHelper authHelper;

    public AdminCategoryController(CategoryService categoryService, AuthenticationHelper authHelper) {
        this.categoryService = categoryService;
        this.authHelper = authHelper;
    }

    /** US 17: Admin xem danh sách danh mục câu hỏi */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDTO> categoryPage = categoryService.getAllCategoriesPaged(keyword, pageable);

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "admin/category/list";
    }

    /** Form tạo mới danh mục */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categoryDTO", new CategoryDTO());
        model.addAttribute("isEdit", false);
        return "admin/category/form";
    }

    /** US 15: Admin tạo mới 1 danh mục câu hỏi */
    @PostMapping("/create")
    public String createCategory(
            @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = authHelper.getCurrentUser()
                    .orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập"));

            categoryService.createCategory(categoryDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo danh mục thành công!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("categoryDTO", categoryDTO);
            return "redirect:/admin/categories/create";
        }
    }

    /** Form cập nhật danh mục */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CategoryDTO categoryDTO = categoryService.getCategoryDTOById(id);
            model.addAttribute("categoryDTO", categoryDTO);
            model.addAttribute("isEdit", true);
            return "admin/category/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    /** US 16: Admin cập nhật thông tin danh mục câu hỏi */
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
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    /** US 18: Admin xóa 1 danh mục câu hỏi */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
