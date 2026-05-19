package com.example.ecommerce.controller;

import com.example.ecommerce.common.ApiResponse;
import com.example.ecommerce.mapper.AddressMapper;
import com.example.ecommerce.model.UserAddress;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin
public class AddressController {
    private final AddressMapper addressMapper;
    public AddressController(AddressMapper addressMapper) { this.addressMapper = addressMapper; }
    @GetMapping
    public ApiResponse<?> list(@RequestParam Long userId) {
        return ApiResponse.ok(addressMapper.findByUserId(userId));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody UserAddress address) {
        if (Boolean.TRUE.equals(address.getIsDefault())) addressMapper.clearDefault(address.getUserId());
        addressMapper.insert(address);
        return ApiResponse.ok(address);
    }

    @PutMapping
    public ApiResponse<?> update(@RequestBody UserAddress address) {
        if (Boolean.TRUE.equals(address.getIsDefault())) addressMapper.clearDefault(address.getUserId());
        addressMapper.update(address);
        return ApiResponse.ok(address);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        addressMapper.delete(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<?> setDefault(@PathVariable Long id, @RequestParam Long userId) {
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(id);
        return ApiResponse.ok(null);
    }
}
