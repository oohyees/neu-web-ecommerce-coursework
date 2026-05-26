package com.example.ecommerce.mapper;
import com.example.ecommerce.model.CustomerConsultation;
import java.util.List;
public interface ConsultationMapper {
    List<CustomerConsultation> findByUserId(Long userId);
    List<CustomerConsultation> findAll();
    int insert(CustomerConsultation consultation);
    int reply(CustomerConsultation consultation);
    int markProcessed(Long id);
}
