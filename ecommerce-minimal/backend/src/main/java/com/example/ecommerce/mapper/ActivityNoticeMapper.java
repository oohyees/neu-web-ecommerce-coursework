package com.example.ecommerce.mapper;
import com.example.ecommerce.model.ActivityNotice;
import java.util.List;
public interface ActivityNoticeMapper {
    List<ActivityNotice> findEnabled();
    List<ActivityNotice> findAll(String keyword);
    int insert(ActivityNotice notice);
    int update(ActivityNotice notice);
    int delete(Long id);
}
