package com.example.ecommerce.service;

import com.example.ecommerce.model.SessionInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.Duration;

@Service
public class SessionService {
    private final RedisTemplate<String,Object> redisTemplate;
    public SessionService(RedisTemplate<String,Object> redisTemplate){this.redisTemplate=redisTemplate;}
    public String create(Long id, String role) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("session:"+token, new SessionInfo(id, role), Duration.ofHours(12));
        return token;
    }
    public SessionInfo get(String token) { return (SessionInfo) redisTemplate.opsForValue().get("session:"+token); }
    public void delete(String token) { redisTemplate.delete("session:"+token); }
}
