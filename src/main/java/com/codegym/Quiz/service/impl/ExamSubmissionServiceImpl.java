package com.codegym.Quiz.service.impl;

import com.codegym.Quiz.dto.ExamGradingDetailDTO;
import com.codegym.Quiz.dto.ExamGradingResultDTO;
import com.codegym.Quiz.dto.ExamResultDTO;
import com.codegym.Quiz.dto.SubmitAnswerDTO;
import com.codegym.Quiz.dto.SubmitExamDTO;
import com.codegym.Quiz.entity.Exam;
import com.codegym.Quiz.entity.ExamResult;
import com.codegym.Quiz.entity.ExamResultDetail;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.ExamRepository;
import com.codegym.Quiz.repository.ExamResultRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import com.codegym.Quiz.repository.UserRepository;
import com.codegym.Quiz.service.ExamGradingService;
import com.codegym.Quiz.service.ExamSubmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamSubmissionServiceImpl
        implements ExamSubmissionService {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamGradingService examGradingService;

    public ExamSubmissionServiceImpl(
            ExamRepository examRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository,
            ExamResultRepository examResultRepository,
            ExamGradingService examGradingService) {

        this.examRepository = examRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.examResultRepository = examResultRepository;
        this.examGradingService = examGradingService;
    }

    @Override
    public ExamResultDTO submitExam(
            SubmitExamDTO submitExamDTO,
            Long studentId) {

        // 1. Kiểm tra dữ liệu đầu vào
        if (submitExamDTO == null
                || submitExamDTO.getExamId() == null) {

            throw new IllegalArgumentException(
                    "Dữ liệu nộp bài không hợp lệ"
            );
        }

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "Không xác định được học viên"
            );
        }

        // 2. Lấy Exam
        Exam exam = examRepository
                .findById(submitExamDTO.getExamId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy đề thi ID: "
                                        + submitExamDTO.getExamId()
                        )
                );

        // 3. Lấy học viên
        User student = userRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy học viên ID: "
                                        + studentId
                        )
                );

        // 4. Gọi engine chấm điểm Ngày 3
        ExamGradingResultDTO gradingResult =
                examGradingService.gradeExam(
                        submitExamDTO
                );

        LocalDateTime submittedAt =
                LocalDateTime.now();

        LocalDateTime startedAt =
                submitExamDTO.getStartedAt();

        // 5. Tạo ExamResult
        ExamResult examResult =
                new ExamResult();

        examResult.setExam(exam);
        examResult.setStudent(student);

        examResult.setScore(
                gradingResult.getScore()
        );

        examResult.setTotalScore(
                gradingResult.getTotalScore()
        );

        examResult.setCorrectAnswers(
                gradingResult.getCorrectAnswers()
        );

        examResult.setTotalQuestions(
                gradingResult.getTotalQuestions()
        );

        examResult.setPassed(
                gradingResult.isPassed()
        );

        examResult.setStartedAt(startedAt);
        examResult.setSubmittedAt(submittedAt);

        // 6. Tính thời gian làm bài
        if (startedAt != null) {

            long durationSeconds =
                    Duration.between(
                            startedAt,
                            submittedAt
                    ).getSeconds();

            examResult.setDurationSeconds(
                    Math.max(durationSeconds, 0)
            );
        }

        // 7. Lưu chi tiết từng câu
        for (ExamGradingDetailDTO gradingDetail
                : gradingResult.getDetails()) {

            Question question =
                    questionRepository
                            .findById(
                                    gradingDetail.getQuestionId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy câu hỏi ID: "
                                                    + gradingDetail.getQuestionId()
                                    )
                            );

            SubmitAnswerDTO submittedAnswer =
                    findSubmittedAnswer(
                            submitExamDTO.getAnswers(),
                            gradingDetail.getQuestionId()
                    );

            ExamResultDetail resultDetail =
                    new ExamResultDetail();

            resultDetail.setQuestion(question);

            resultDetail.setSelectedAnswerIds(
                    convertAnswerIdsToString(
                            submittedAnswer
                    )
            );

            resultDetail.setCorrect(
                    gradingDetail.isCorrect()
            );

            resultDetail.setScoreAwarded(
                    gradingDetail.getScoreAwarded()
            );

            resultDetail.setMaxScore(
                    gradingDetail.getMaxScore()
            );

            /*
             * addDetail() đồng thời:
             * - thêm detail vào ExamResult
             * - set examResult cho detail
             */
            examResult.addDetail(resultDetail);
        }

        // 8. Save ExamResult
        // ExamResultDetail được save cùng nhờ CascadeType.ALL
        ExamResult savedResult =
                examResultRepository.save(examResult);

        // 9. Convert Entity -> DTO
        return convertToDTO(savedResult);
    }

    /**
     * Tìm câu trả lời học viên đã gửi
     * theo questionId.
     */
    private SubmitAnswerDTO findSubmittedAnswer(
            List<SubmitAnswerDTO> answers,
            Long questionId) {

        if (answers == null) {
            return null;
        }

        for (SubmitAnswerDTO answer : answers) {

            if (answer != null
                    && answer.getQuestionId() != null
                    && answer.getQuestionId()
                    .equals(questionId)) {

                return answer;
            }
        }

        return null;
    }

    /**
     * Convert:
     *
     * [1, 3, 5]
     *
     * thành:
     *
     * "1,3,5"
     */
    private String convertAnswerIdsToString(
            SubmitAnswerDTO submittedAnswer) {

        if (submittedAnswer == null
                || submittedAnswer
                .getSelectedAnswerIds() == null
                || submittedAnswer
                .getSelectedAnswerIds()
                .isEmpty()) {

            return "";
        }

        List<Long> ids =
                submittedAnswer
                        .getSelectedAnswerIds()
                        .stream()
                        .sorted()
                        .collect(Collectors.toList());

        return ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * Convert ExamResult -> ExamResultDTO
     */
    private ExamResultDTO convertToDTO(
            ExamResult result) {

        ExamResultDTO dto =
                new ExamResultDTO();

        dto.setResultId(result.getId());

        dto.setExamId(
                result.getExam().getId()
        );

        dto.setExamTitle(
                result.getExam().getTitle()
        );

        dto.setStudentId(
                result.getStudent().getId()
        );

        String studentName =
                result.getStudent().getFullName();

        if (studentName == null
                || studentName.isBlank()) {

            studentName =
                    result.getStudent().getUsername();
        }

        dto.setStudentName(studentName);

        dto.setScore(result.getScore());
        dto.setTotalScore(result.getTotalScore());

        dto.setCorrectAnswers(
                result.getCorrectAnswers()
        );

        dto.setTotalQuestions(
                result.getTotalQuestions()
        );

        dto.setPassed(
                result.isPassed()
        );

        dto.setStartedAt(
                result.getStartedAt()
        );

        dto.setSubmittedAt(
                result.getSubmittedAt()
        );

        dto.setDurationSeconds(
                result.getDurationSeconds()
        );

        return dto;
    }
}