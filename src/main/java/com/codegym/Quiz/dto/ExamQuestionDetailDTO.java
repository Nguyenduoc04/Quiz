package com.codegym.Quiz.dto;

import com.codegym.Quiz.entity.Question;

import java.util.ArrayList;
import java.util.List;

public class ExamQuestionDetailDTO {

    private Long id;
    private String content;

    private Question.QuestionType questionType;

    private Question.DifficultyLevel difficultyLevel;

    private Double score;

    private Integer questionOrder;

    private List<ExamAnswerDTO> answers = new ArrayList<>();

    public ExamQuestionDetailDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Question.QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Question.QuestionType questionType) {
        this.questionType = questionType;
    }

    public Question.DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Question.DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public List<ExamAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ExamAnswerDTO> answers) {
        this.answers = answers;
    }
}