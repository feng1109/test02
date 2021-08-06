package com.eseasky.book.redis.starter;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@EnableConfigurationProperties({RedisProperties.class})
@EnableCaching
public class RedisAutoConfigure {

    @Bean
    public RedisSerializer<String> redisKeySerializer() {
        return RedisSerializer.string();
    }

    @Bean
    public RedisSerializer<Object> redisValueSerializer() {
        return RedisSerializer.json();
    }

    /**
     * RedisTemplate配置
     * @param factory
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory
            , RedisSerializer<String> redisKeySerializer, RedisSerializer<Object> redisValueSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        redisTemplate.setDefaultSerializer(redisValueSerializer);
        redisTemplate.setKeySerializer(redisKeySerializer);
        redisTemplate.setHashKeySerializer(redisKeySerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    /**
     * 配置缓存管理器为RedisCacheManager.
     * @param redisTemplate
     * @return
     */
    @Bean
    public CacheManager cacheManager(RedisTemplate<String, Object> redisTemplate) {
        RedisCacheManager redisCacheManager = RedisCacheManager.create(redisTemplate.getConnectionFactory());
        return redisCacheManager;
    }


}
