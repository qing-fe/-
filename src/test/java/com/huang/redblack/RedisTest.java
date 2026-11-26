package com.huang.redblack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@SpringBootTest
class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void testRedis() {
        redisTemplate.opsForValue().set("test:key", "hello", Duration.ofMinutes(1));
        System.out.println(redisTemplate.opsForValue().get("test:key"));
    }
}
