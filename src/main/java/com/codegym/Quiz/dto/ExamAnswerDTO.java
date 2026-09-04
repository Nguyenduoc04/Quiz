package com.codegym.Quiz.dto;

public class ExamAnswerDTO {

    private Long id;
    private String content;
    private Integer displayOrder;

    public ExamAnswerDTO() {
    }

    public ExamAnswerDTO(Long id, String content, Integer displayOrder) {
        this.id = id;
        this.content = content;
        this.displayOrder = displayOrder;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}