package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.CategoryDTO;
import com.codegym.Quiz.entity.Category;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.CategoryRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("teacher1");
        testUser.setFullName("Giáo Viên A");

        testCategory = new Category();
        testCategory.setId(10L);
        testCategory.setName("Java Core");
        testCategory.setDescription("Lập trình Java");
        testCategory.setCreatedBy(testUser);
    }

    @Test
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory));

        List<Category> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Core", result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testGetCategoryById_Success() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));

        Category result = categoryService.getCategoryById(10L);

        assertNotNull(result);
        assertEquals("Java Core", result.getName());
    }

    @Test
    void testGetCategoryById_NotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void testCreateCategory_Success() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Spring Boot");
        dto.setDescription("Khóa học Spring");

        when(categoryRepository.existsByName("Spring Boot")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(20L);
            return c;
        });

        Category created = categoryService.createCategory(dto, testUser);

        assertNotNull(created);
        assertEquals("Spring Boot", created.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategory_DuplicateName() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Java Core");

        when(categoryRepository.existsByName("Java Core")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(dto, testUser));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));
        when(questionRepository.existsByCategoryId(10L)).thenReturn(false);

        categoryService.deleteCategory(10L);

        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void testDeleteCategory_HasQuestionsConstraint() {
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));
        when(questionRepository.existsByCategoryId(10L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(10L));
        verify(categoryRepository, never()).delete(any());
    }
}
