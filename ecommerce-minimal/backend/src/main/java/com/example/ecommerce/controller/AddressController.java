package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.common.CurrentSession;
import com.example.ecommerce.mapper.AddressMapper;
import com.example.ecommerce.model.UserAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin
public class AddressController {
    private final AddressMapper addressMapper;
    public AddressController(AddressMapper addressMapper) { this.addressMapper = addressMapper; }
    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) Long userId, HttpServletRequest request) {
        return ApiResponse.ok(addressMapper.findByUserId(CurrentSession.userId(request)));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody UserAddress address, HttpServletRequest request) {
        address.setUserId(CurrentSession.userId(request));
        if (Boolean.TRUE.equals(address.getIsDefault())) addressMapper.clearDefault(address.getUserId());
        addressMapper.insert(address);
        return ApiResponse.ok(address);
    }

    @PutMapping
    public ApiResponse<?> update(@RequestBody UserAddress address, HttpServletRequest request) {
        address.setUserId(CurrentSession.userId(request));
        if (Boolean.TRUE.equals(address.getIsDefault())) addressMapper.clearDefault(address.getUserId());
        addressMapper.update(address);
        return ApiResponse.ok(address);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest request) {
        addressMapper.delete(id, CurrentSession.userId(request));
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<?> setDefault(@PathVariable Long id, @RequestParam(required = false) Long userId, HttpServletRequest request) {
        userId = CurrentSession.userId(request);
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(id, userId);
        return ApiResponse.ok(null);
    }
}
