package com.codegym.Quiz.service;

import com.codegym.Quiz.dto.AnswerDTO;
import com.codegym.Quiz.dto.QuestionDTO;
import com.codegym.Quiz.entity.Answer;
import com.codegym.Quiz.entity.Category;
import com.codegym.Quiz.entity.Question;
import com.codegym.Quiz.entity.User;
import com.codegym.Quiz.repository.AnswerRepository;
import com.codegym.Quiz.repository.CategoryRepository;
import com.codegym.Quiz.repository.ExamQuestionRepository;
import com.codegym.Quiz.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CategoryRepository categoryRepository;
    private final ExamQuestionRepository examQuestionRepository;

    public QuestionService(
            QuestionRepository questionRepository,
            AnswerRepository answerRepository,
            CategoryRepository categoryRepository,
            ExamQuestionRepository examQuestionRepository) {

        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.categoryRepository = categoryRepository;
        this.examQuestionRepository = examQuestionRepository;
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getQuestionsByCategoryId(Long categoryId) {
        return questionRepository.findByCategoryId(categoryId);
    }

    public Page<QuestionDTO> getAllQuestionsPaged(
            String keyword,
            Pageable pageable) {

        Page<Question> questionPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            questionPage =
                    questionRepository.searchAllByKeyword(
                            keyword.trim(),
                            pageable
                    );
        } else {
            questionPage =
                    questionRepository.findAllByOrderByCreatedAtDesc(
                            pageable
                    );
        }

        return questionPage.map(this::convertToDTO);
    }

    /**
     * Lấy câu hỏi của chính Teacher đang đăng nhập.
     */
    public Page<QuestionDTO> getQuestionsByUserPaged(
            User user,
            String keyword,
            Pageable pageable) {

        Page<Question> questionPage;

        if (keyword != null && !keyword.trim().isEmpty()) {

            questionPage =
                    questionRepository.searchAdvancedByUser(
                            user,
                            keyword.trim(),
                            null,
                            null,
                            pageable
                    );

        } else {

            questionPage =
                    questionRepository
                            .findByCreatedByOrderByCreatedAtDesc(
                                    user,
                                    pageable
                            );
        }

        return questionPage.map(this::convertToDTO);
    }

    /**
     * Search toàn hệ thống.
     */
    public Page<QuestionDTO> searchQuestions(
            String keyword,
            Long categoryId,
            Question.DifficultyLevel difficulty,
            Pageable pageable) {

        String cleanKeyword =
                (keyword != null && !keyword.trim().isEmpty())
                        ? keyword.trim()
                        : null;

        Page<Question> questionPage =
                questionRepository.searchAdvanced(
                        cleanKeyword,
                        categoryId,
                        difficulty,
                        pageable
                );

        return questionPage.map(this::convertToDTO);
    }

    /**
     * Teacher search câu hỏi của chính mình.
     */
    public Page<QuestionDTO> searchQuestionsByUser(
            User user,
            String keyword,
            Long categoryId,
            Question.DifficultyLevel difficulty,
            Pageable pageable) {

        String cleanKeyword =
                (keyword != null && !keyword.trim().isEmpty())
                        ? keyword.trim()
                        : null;

        Page<Question> questionPage =
                questionRepository.searchAdvancedByUser(
                        user,
                        cleanKeyword,
                        categoryId,
                        difficulty,
                        pageable
                );

        return questionPage.map(this::convertToDTO);
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy câu hỏi với ID: " + id
                        ));
    }

    public QuestionDTO getQuestionDTOById(Long id) {
        return convertToDTO(getQuestionById(id));
    }

    @Transactional
    public Question createQuestion(
            QuestionDTO dto,
            User user) {

        Category category =
                categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Danh mục chọn không tồn tại"
                                ));

        Question question = new Question();

        question.setContent(dto.getContent().trim());
        question.setQuestionType(dto.getQuestionType());
        question.setDifficultyLevel(dto.getDifficultyLevel());
        question.setScore(
                dto.getScore() != null
                        ? dto.getScore()
                        : 1.0
        );
        question.setExplanation(dto.getExplanation());
        question.setCategory(category);
        question.setCreatedBy(user);

        Question savedQuestion =
                questionRepository.save(question);

        // Tạo danh sách đáp án
        if (dto.getAnswers() != null
                && !dto.getAnswers().isEmpty()) {

            List<Answer> answers = new ArrayList<>();
            int order = 1;

            for (AnswerDTO aDto : dto.getAnswers()) {

                if (aDto.getContent() != null
                        && !aDto.getContent().trim().isEmpty()) {

                    Answer answer = new Answer();

                    answer.setContent(
                            aDto.getContent().trim()
                    );

                    answer.setCorrect(
                            aDto.isCorrect()
                    );

                    answer.setDisplayOrder(order++);
                    answer.setQuestion(savedQuestion);

                    answers.add(answer);
                }
            }

            answerRepository.saveAll(answers);
            savedQuestion.setAnswers(answers);
        }

        return savedQuestion;
    }

    @Transactional
    public Question updateQuestion(
            Long id,
            QuestionDTO dto,
            User currentUser) {

        Question question =
                getQuestionById(id);

        Category category =
                categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Danh mục chọn không tồn tại"
                                ));

        question.setContent(dto.getContent().trim());
        question.setQuestionType(dto.getQuestionType());
        question.setDifficultyLevel(dto.getDifficultyLevel());

        question.setScore(
                dto.getScore() != null
                        ? dto.getScore()
                        : 1.0
        );

        question.setExplanation(dto.getExplanation());
        question.setCategory(category);

        /*
         * Question.answers có cascade ALL + orphanRemoval=true.
         * Clear collection để Hibernate xóa Answer cũ,
         * sau đó thêm Answer mới.
         */
        question.getAnswers().clear();

        if (dto.getAnswers() != null
                && !dto.getAnswers().isEmpty()) {

            int order = 1;

            for (AnswerDTO aDto : dto.getAnswers()) {

                if (aDto.getContent() != null
                        && !aDto.getContent().trim().isEmpty()) {

                    Answer answer = new Answer();

                    answer.setContent(
                            aDto.getContent().trim()
                    );

                    answer.setCorrect(
                            aDto.isCorrect()
                    );

                    answer.setDisplayOrder(order++);
                    answer.setQuestion(question);

                    question.getAnswers().add(answer);
                }
            }
        }

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {

        Question question =
                getQuestionById(id);

        /*
         * Không cho xóa Question nếu đang được sử dụng
         * trong một Exam.
         */
        if (examQuestionRepository.existsByQuestionId(id)) {

            throw new IllegalStateException(
                    "Không thể xóa câu hỏi đang thuộc bài thi! "
                            + "Vui lòng gỡ câu hỏi khỏi bài thi trước."
            );
        }

        questionRepository.delete(question);
    }

    public QuestionDTO convertToDTO(
            Question question) {

        QuestionDTO dto = new QuestionDTO();

        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setQuestionType(question.getQuestionType());
        dto.setDifficultyLevel(question.getDifficultyLevel());
        dto.setScore(question.getScore());
        dto.setExplanation(question.getExplanation());

        // Giữ createdAt từ branch hang
        dto.setCreatedAt(question.getCreatedAt());

        if (question.getCategory() != null) {

            dto.setCategoryId(
                    question.getCategory().getId()
            );

            dto.setCategoryName(
                    question.getCategory().getName()
            );
        }

        if (question.getCreatedBy() != null) {

            dto.setCreatedById(
                    question.getCreatedBy().getId()
            );

            String name =
                    (question.getCreatedBy().getFullName() != null
                            && !question.getCreatedBy()
                            .getFullName()
                            .isBlank())
                            ? question.getCreatedBy().getFullName()
                            : question.getCreatedBy().getUsername();

            dto.setCreatedByName(name);
        }

        if (question.getAnswers() != null) {

            List<AnswerDTO> answerDTOs =
                    question.getAnswers()
                            .stream()
                            .map(a ->
                                    new AnswerDTO(
                                            a.getId(),
                                            a.getContent(),
                                            a.isCorrect(),
                                            a.getDisplayOrder()
                                    ))
                            .collect(Collectors.toList());

            dto.setAnswers(answerDTOs);
        }

        return dto;
    }
}