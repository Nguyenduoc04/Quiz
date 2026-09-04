package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.CategoryDTO;
import com.codegym.Quiz.entity.Category;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.CategoryRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    public CategoryService(CategoryRepository categoryRepository, QuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<CategoryDTO> getAllCategoryDTOs() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<CategoryDTO> getAllCategoriesPaged(String keyword, Pageable pageable) {
        Page<Category> categoryPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            categoryPage = categoryRepository.searchAll(keyword.trim(), pageable);
        } else {
            categoryPage = categoryRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return categoryPage.map(this::convertToDTO);
    }

    public Page<CategoryDTO> getCategoriesByUserPaged(User user, String keyword, Pageable pageable) {
        Page<Category> categoryPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            categoryPage = categoryRepository.searchByUser(user, keyword.trim(), pageable);
        } else {
            categoryPage = categoryRepository.findByCreatedBy(user, pageable);
        }
        return categoryPage.map(this::convertToDTO);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));
    }

    public CategoryDTO getCategoryDTOById(Long id) {
        Category category = getCategoryById(id);
        return convertToDTO(category);
    }

    @Transactional
    public Category createCategory(CategoryDTO dto, User user) {
        if (categoryRepository.existsByName(dto.getName().trim())) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại trong hệ thống!");
        }

        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        category.setCreatedBy(user);

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryDTO dto, User currentUser) {
        Category category = getCategoryById(id);

        if (categoryRepository.existsByNameAndIdNot(dto.getName().trim(), id)) {
            throw new IllegalArgumentException("Tên danh mục đã trùng với một danh mục khác!");
        }

        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // Kiểm tra xem danh mục có đang chứa câu hỏi nào không
        if (questionRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Không thể xóa danh mục đang chứa câu hỏi! Vui lòng xóa hoặc di chuyển các câu hỏi trước.");
        }

        categoryRepository.delete(category);
    }
    public Category getCategoryByIdAndUser(Long id, User user) {
        Category category = getCategoryById(id);

        if (category.getCreatedBy() == null
                || !category.getCreatedBy().getId().equals(user.getId())) {

            throw new IllegalStateException(
                    "Bạn không có quyền truy cập danh mục này!"
            );
        }

        return category;
    }
    public CategoryDTO getCategoryDTOByIdAndUser(Long id, User user) {
        Category category = getCategoryByIdAndUser(id, user);
        return convertToDTO(category);
    }
    public CategoryDTO convertToDTO(Category category) {
        long count = categoryRepository.countQuestionsByCategoryId(category.getId());
        String createdByName = (category.getCreatedBy() != null && category.getCreatedBy().getFullName() != null && !category.getCreatedBy().getFullName().isBlank())
                ? category.getCreatedBy().getFullName()
                : (category.getCreatedBy() != null ? category.getCreatedBy().getUsername() : "N/A");

        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                createdByName,
                category.getCreatedBy() != null ? category.getCreatedBy().getId() : null,
                category.getCreatedAt(),
                count
        );
    }
}