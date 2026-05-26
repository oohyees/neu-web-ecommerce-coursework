package com.example.ecommerce.mapper;

import com.example.ecommerce.model.UserAddress;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AddressMapper {
    List<UserAddress> findByUserId(Long userId);
    UserAddress findById(Long id);
    int clearDefault(Long userId);
    int insert(UserAddress address);
    int update(UserAddress address);
    int delete(@Param("id") Long id, @Param("userId") Long userId);
    int setDefault(@Param("id") Long id, @Param("userId") Long userId);
}
