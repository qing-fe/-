package com.huang.redblack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Redis 配置。
 *
 * <p>验证码、Token、计数器等字符串场景优先使用 Spring Boot 自动配置的
 * {@code StringRedisTemplate}；这里只额外配置一个用于存储 Java 对象的
 * {@code RedisTemplate}。</p>
 *
 * <p>Spring Boot 4.2 / Spring Data Redis 4.2 使用 Jackson 3，
 * 对应序列化器是 {@link GenericJacksonJsonRedisSerializer}，
 * 不是旧版本里的 GenericJackson2JsonRedisSerializer。</p>
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // 开启默认类型信息（@class），并只允许本项目、JDK 集合和时间类型，
        // 避免反序列化任意类带来的安全风险。
        GenericJacksonJsonRedisSerializer valueSerializer =
                GenericJacksonJsonRedisSerializer.builder()
                        .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("com.huang.redblack.")
                                .allowIfSubType("java.util.")
                                .allowIfSubType("java.time.")
                                .build())
                        .build();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        // RedisTemplate 实现了 InitializingBean，Spring 会自动调用 afterPropertiesSet()
        return template;
    }
}
