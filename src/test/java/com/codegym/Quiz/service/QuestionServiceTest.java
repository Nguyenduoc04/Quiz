package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.AnswerDTO;
import com.codegym.Quiz.dto.QuestionDTO;
import com.codegym.Quiz.entity.Category;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.AnswerRepository;
import com.codegym.Quiz.repository.CategoryRepository;
import com.codegym.Quiz.repository.ExamQuestionRepository;
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
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExamQuestionRepository examQuestionRepository;

    @InjectMocks
    private QuestionService questionService;

    private User testUser;
    private Category testCategory;
    private Question testQuestion;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("teacher1");

        testCategory = new Category();
        testCategory.setId(5L);
        testCategory.setName("Java Basic");

        testQuestion = new Question();
        testQuestion.setId(100L);
        testQuestion.setContent("JDK là gì?");
        testQuestion.setCategory(testCategory);
        testQuestion.setCreatedBy(testUser);
    }

    @Test
    void testGetQuestionById_Success() {
        when(questionRepository.findById(100L)).thenReturn(Optional.of(testQuestion));

        Question result = questionService.getQuestionById(100L);

        assertNotNull(result);
        assertEquals("JDK là gì?", result.getContent());
    }

    @Test
    void testCreateQuestion_Success() {
        QuestionDTO dto = new QuestionDTO();
        dto.setContent("JVM là gì?");
        dto.setCategoryId(5L);
        dto.setQuestionType(Question.QuestionType.SINGLE_CHOICE);
        dto.setDifficultyLevel(Question.DifficultyLevel.EASY);

        AnswerDTO a1 = new AnswerDTO(null, "Java Virtual Machine", true, 1);
        dto.setAnswers(List.of(a1));

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(testCategory));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question q = invocation.getArgument(0);
            q.setId(101L);
            return q;
        });

        Question created = questionService.createQuestion(dto, testUser);

        assertNotNull(created);
        assertEquals("JVM là gì?", created.getContent());
        verify(questionRepository, times(1)).save(any(Question.class));
        verify(answerRepository, times(1)).saveAll(any());
    }

    @Test
    void testDeleteQuestion_Success() {
        when(questionRepository.findById(100L)).thenReturn(Optional.of(testQuestion));
        when(examQuestionRepository.existsByQuestionId(100L)).thenReturn(false);

        questionService.deleteQuestion(100L);

        verify(questionRepository, times(1)).delete(testQuestion);
    }

    @Test
    void testDeleteQuestion_ConstraintWithExam() {
        when(questionRepository.findById(100L)).thenReturn(Optional.of(testQuestion));
        when(examQuestionRepository.existsByQuestionId(100L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> questionService.deleteQuestion(100L));
        verify(questionRepository, never()).delete(any());
    }

    @Test
    void testUpdateQuestion_Success() {
        QuestionDTO dto = new QuestionDTO();
        dto.setContent("Nội dung đã sửa");
        dto.setCategoryId(5L);
        dto.setQuestionType(Question.QuestionType.SINGLE_CHOICE);
        dto.setDifficultyLevel(Question.DifficultyLevel.MEDIUM);
        dto.setAnswers(List.of(
                new AnswerDTO(null, "Đáp án A", true, 1),
                new AnswerDTO(null, "Đáp án B", false, 2)
        ));

        when(questionRepository.findById(100L)).thenReturn(Optional.of(testQuestion));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(testCategory));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Question updated = questionService.updateQuestion(100L, dto, testUser);

        assertNotNull(updated);
        assertEquals("Nội dung đã sửa", updated.getContent());
        assertEquals(2, updated.getAnswers().size());
        verify(questionRepository, times(1)).save(testQuestion);
    }
}
