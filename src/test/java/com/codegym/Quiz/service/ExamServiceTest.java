package com.codegym.Quiz.service;

import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamQuestion;
import com.codegym.Quiz.entity.ExamStatus;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.ExamQuestionRepository;
import com.codegym.Quiz.repository.ExamRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import com.codegym.Quiz.service.impl.ExamServiceImpl;
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
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ExamQuestionRepository examQuestionRepository;

    @InjectMocks
    private ExamServiceImpl examService;

    private User testUser;
    private Exam testExam;
    private Question testQuestion;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("teacher1");

        testExam = new Exam();
        testExam.setId(50L);
        testExam.setTitle("Đề thi Java Cuối Kỳ");
        testExam.setDurationMinutes(45);
        testExam.setStatus(ExamStatus.DRAFT);
        testExam.setCreatedBy(testUser);

        testQuestion = new Question();
        testQuestion.setId(200L);
        testQuestion.setContent("Lớp Abstract là gì?");
    }

    @Test
    void testGetAllExams() {
        when(examRepository.findAll()).thenReturn(List.of(testExam));

        List<Exam> result = examService.getAllExams();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Đề thi Java Cuối Kỳ", result.get(0).getTitle());
    }

    @Test
    void testCreateExam_DefaultStatus() {
        Exam exam = new Exam();
        exam.setTitle("Đề thi mới");

        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Exam created = examService.createExam(exam);

        assertNotNull(created);
        assertEquals(ExamStatus.DRAFT, created.getStatus());
        verify(examRepository, times(1)).save(exam);
    }

    @Test
    void testAddQuestionToExam_Success() {
        when(examQuestionRepository.existsByExamIdAndQuestionId(50L, 200L)).thenReturn(false);
        when(examRepository.findById(50L)).thenReturn(Optional.of(testExam));
        when(questionRepository.findById(200L)).thenReturn(Optional.of(testQuestion));
        when(examQuestionRepository.findMaxOrderNumByExamId(50L)).thenReturn(Optional.of(2));

        examService.addQuestionToExam(50L, 200L);

        verify(examQuestionRepository, times(1)).save(any(ExamQuestion.class));
    }

    @Test
    void testAddQuestionToExam_AlreadyExists() {
        when(examQuestionRepository.existsByExamIdAndQuestionId(50L, 200L)).thenReturn(true);

        examService.addQuestionToExam(50L, 200L);

        verify(examQuestionRepository, never()).save(any());
    }

    @Test
    void testRemoveQuestionFromExam() {
        examService.removeQuestionFromExam(50L, 200L);

        verify(examQuestionRepository, times(1)).deleteByExamIdAndQuestionId(50L, 200L);
    }
}
