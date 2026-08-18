package com.codegym.Quiz.repository;

import com.codegym.Quiz.entity.Answer;
import com.codegym.Quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /** Lấy danh sách đáp án của một câu hỏi xếp theo thứ tự displayOrder */
    List<Answer> findByQuestionOrderByDisplayOrderAsc(Question question);

    /** Lấy danh sách đáp án theo Question ID */
    List<Answer> findByQuestionIdOrderByDisplayOrderAsc(Long questionId);

    /** Xóa tất cả đáp án của 1 câu hỏi */
    void deleteByQuestionId(Long questionId);
}
