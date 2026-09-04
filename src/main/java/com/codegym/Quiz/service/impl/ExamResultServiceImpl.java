package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.dto.ExamResultDTO;
import com.codegym.Quiz.dto.QuestionReviewDTO;
import com.codegym.Quiz.dto.SubmitAnswerDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;
import com.codegym.Quiz.entity.*;
import com.codegym.Quiz.repository.*;
import com.codegym.Quiz.service.ExamResultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ExamResultServiceImpl implements ExamResultService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamResultDetailRepository examResultDetailRepository;

    public ExamResultServiceImpl(ExamRepository examRepository,
                                  QuestionRepository questionRepository,
                                  AnswerRepository answerRepository,
                                  ExamResultRepository examResultRepository,
                                  ExamResultDetailRepository examResultDetailRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.examResultRepository = examResultRepository;
        this.examResultDetailRepository = examResultDetailRepository;
    }

    @Override
    public ExamResultDTO submitExam(SubmitExamDTO submitDTO, User user) {
        String displayName = (user != null)
                ? (user.getFullName() != null ? user.getFullName() : user.getUsername())
                : "Thí sinh vãng lai";
        return submitExamWithStudentName(submitDTO, user, displayName);
    }

    @Override
    public ExamResultDTO submitExamWithStudentName(SubmitExamDTO submitDTO, User user, String studentName) {
        Exam exam = examRepository.findById(submitDTO.getExamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi ID: " + submitDTO.getExamId()));

        Map<Long, Long> userAnswersMap = new HashMap<>();
        if (submitDTO.getAnswers() != null) {
            for (SubmitAnswerDTO sa : submitDTO.getAnswers()) {
                userAnswersMap.put(sa.getQuestionId(), sa.getSelectedAnswerId());
            }
        }

        List<ExamQuestion> examQuestions = exam.getExamQuestions();
        int totalQuestions = examQuestions.size();
        int correctCount = 0;
        double totalEarnedScore = 0.0;
        double maxPossibleScore = 0.0;

        List<ExamResultDetail> details = new ArrayList<>();
        List<QuestionReviewDTO> reviewList = new ArrayList<>();

        for (ExamQuestion eq : examQuestions) {
            Question q = eq.getQuestion();
            if (q == null) continue;

            double questionScore = (eq.getCustomScore() != null) ? eq.getCustomScore() : (q.getScore() != null ? q.getScore() : 1.0);
            maxPossibleScore += questionScore;

            Long selectedAnsId = userAnswersMap.get(q.getId());
            boolean isCorrect = false;

            if (selectedAnsId != null) {
                Optional<Answer> ansOpt = answerRepository.findById(selectedAnsId);
                if (ansOpt.isPresent() && Boolean.TRUE.equals(ansOpt.get().isCorrect())) {
                    isCorrect = true;
                }
            }

            if (isCorrect) {
                correctCount++;
                totalEarnedScore += questionScore;
            }

            ExamResultDetail detail = new ExamResultDetail();
            detail.setQuestion(q);
            detail.setSelectedAnswerId(selectedAnsId);
            detail.setIsCorrect(isCorrect);
            detail.setScoreObtained(isCorrect ? questionScore : 0.0);
            details.add(detail);

            reviewList.add(buildQuestionReviewDTO(q, selectedAnsId, isCorrect));
        }

        boolean isPassed = maxPossibleScore > 0 ? (totalEarnedScore / maxPossibleScore >= 0.5) : false;

        ExamResult result = new ExamResult();
        result.setUser(user);
        result.setStudentName(studentName != null && !studentName.isBlank() ? studentName : "Thí sinh");
        result.setExam(exam);
        result.setSubmittedAt(LocalDateTime.now());
        result.setCorrectCount(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setScore(totalEarnedScore);
        result.setTotalScore(maxPossibleScore > 0 ? maxPossibleScore : 10.0);
        result.setIsPassed(isPassed);

        for (ExamResultDetail d : details) {
            result.addDetail(d);
        }

        try {
            ExamResult savedResult = examResultRepository.save(result);
            ExamResultDTO dto = mapToDTO(savedResult);
            dto.setDetails(reviewList);
            return dto;
        } catch (Exception e) {
            ExamResultDTO dto = new ExamResultDTO();
            dto.setId(0L);
            dto.setExamId(exam.getId());
            dto.setExamTitle(exam.getTitle());
            dto.setStudentName(studentName != null && !studentName.isBlank() ? studentName : "Thí sinh vãng lai");
            dto.setScore(totalEarnedScore);
            dto.setTotalScore(maxPossibleScore > 0 ? maxPossibleScore : 10.0);
            dto.setCorrectCount(correctCount);
            dto.setTotalQuestions(totalQuestions);
            dto.setIsPassed(isPassed);
            dto.setSubmittedAt(LocalDateTime.now());
            dto.setDetails(reviewList);
            return dto;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResultDTO getResultById(Long resultId) {
        if (resultId == null || resultId <= 0) {
            return null;
        }
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả ID: " + resultId));

        return mapToDTO(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultDTO> getStudentHistory(User user) {
        if (user == null) return List.of();
        List<ExamResult> results = examResultRepository.findByUserIdOrderBySubmittedAtDesc(user.getId());
        List<ExamResultDTO> dtos = new ArrayList<>();
        for (ExamResult r : results) {
            dtos.add(mapToDTO(r));
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultDTO> getResultsForExam(Long examId) {
        if (examId == null) return List.of();
        List<ExamResult> results = examResultRepository.findByExamIdOrderBySubmittedAtDesc(examId);
        List<ExamResultDTO> dtos = new ArrayList<>();
        for (ExamResult r : results) {
            dtos.add(mapToDTO(r));
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultDTO> getResultsForTeacher(User teacher) {
        if (teacher == null) return List.of();
        List<Exam> teacherExams = examRepository.findByCreatedBy(teacher);
        if (teacherExams.isEmpty()) return List.of();

        List<ExamResultDTO> allDtos = new ArrayList<>();
        for (Exam e : teacherExams) {
            List<ExamResult> results = examResultRepository.findByExamIdOrderBySubmittedAtDesc(e.getId());
            for (ExamResult r : results) {
                allDtos.add(mapToDTO(r));
            }
        }
        allDtos.sort((a, b) -> {
            if (a.getSubmittedAt() != null && b.getSubmittedAt() != null) {
                return b.getSubmittedAt().compareTo(a.getSubmittedAt());
            }
            return 0;
        });
        return allDtos;
    }

    private ExamResultDTO mapToDTO(ExamResult r) {
        ExamResultDTO dto = new ExamResultDTO();
        dto.setId(r.getId());
        if (r.getExam() != null) {
            dto.setExamId(r.getExam().getId());
            dto.setExamTitle(r.getExam().getTitle());
        }
        String name = r.getStudentName();
        if (name == null || name.isBlank()) {
            name = (r.getUser() != null)
                    ? (r.getUser().getFullName() != null ? r.getUser().getFullName() : r.getUser().getUsername())
                    : "Thí sinh";
        }
        dto.setStudentName(name);
        dto.setScore(r.getScore());
        dto.setTotalScore(r.getTotalScore());
        dto.setCorrectCount(r.getCorrectCount());
        dto.setTotalQuestions(r.getTotalQuestions());
        dto.setIsPassed(r.getIsPassed());
        dto.setSubmittedAt(r.getSubmittedAt());

        List<QuestionReviewDTO> reviewList = new ArrayList<>();
        if (r.getDetails() != null) {
            for (ExamResultDetail d : r.getDetails()) {
                if (d.getQuestion() != null) {
                    reviewList.add(buildQuestionReviewDTO(d.getQuestion(), d.getSelectedAnswerId(), Boolean.TRUE.equals(d.getIsCorrect())));
                }
            }
        }
        dto.setDetails(reviewList);

        return dto;
    }

    private QuestionReviewDTO buildQuestionReviewDTO(Question q, Long selectedAnsId, boolean isCorrect) {
        String selectedAnsContent = null;
        String correctAnsContent = null;

        List<Answer> answers = answerRepository.findByQuestionIdOrderByDisplayOrderAsc(q.getId());
        if (answers != null) {
            for (Answer ans : answers) {
                if (selectedAnsId != null && selectedAnsId.equals(ans.getId())) {
                    selectedAnsContent = ans.getContent();
                }
                if (Boolean.TRUE.equals(ans.isCorrect())) {
                    correctAnsContent = ans.getContent();
                }
            }
        }

        return new QuestionReviewDTO(
                q.getId(),
                q.getContent(),
                selectedAnsId,
                selectedAnsContent,
                correctAnsContent,
                isCorrect
        );
    }
}
