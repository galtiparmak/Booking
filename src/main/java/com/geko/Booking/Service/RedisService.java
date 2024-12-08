package com.geko.Booking.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveValue(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void saveList(String key, List<Object> list, Duration ttl) {
        redisTemplate.opsForValue().set(key, list, ttl);
    }

    public <T> List<T> getList(String key, Class<T> type) {
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null) {
            return null;
        }
        return objectMapper.convertValue(data, new TypeReference<List<T>>() {});
    }
}
