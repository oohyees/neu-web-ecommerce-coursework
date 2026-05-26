package com.example.ecommerce.mapper;
import com.example.ecommerce.model.Feedback;
import java.util.List;
public interface FeedbackMapper {
    List<Feedback> findAll();
    List<Feedback> findByUserId(Long userId);
    int insert(Feedback feedback);
    int reply(Feedback feedback);
    int markProcessed(Long id);
}
