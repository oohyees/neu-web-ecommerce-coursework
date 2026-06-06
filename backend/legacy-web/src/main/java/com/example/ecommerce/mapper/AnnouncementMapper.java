package com.example.ecommerce.mapper;
import com.example.ecommerce.model.Announcement;
import java.util.List;
public interface AnnouncementMapper {
    List<Announcement> findAll();
    int insert(Announcement announcement);
    int update(Announcement announcement);
    int delete(Long id);
}
