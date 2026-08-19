package com.codegym.Quiz.dto;

public class AnswerDTO {

    private Long id;
    private String content;
    private boolean correct;
    private Integer displayOrder;

    public AnswerDTO() {}

    public AnswerDTO(Long id, String content, boolean correct, Integer displayOrder) {
        this.id = id;
        this.content = content;
        this.correct = correct;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
