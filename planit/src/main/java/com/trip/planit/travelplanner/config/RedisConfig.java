package com.trip.planit.travelplanner.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching  // 스프링 캐시 기능 활성화
public class RedisConfig {

    // -- (1) RedisConnectionFactory 설정
    //     실제 Redis 서버 주소/포트 설정
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 예: 기본 포트 6379, localhost에 Redis가 띄워져 있는 경우
        // 실제 운영환경에서는 호스트/비번 등 추가 설정 필요
        return new LettuceConnectionFactory("localhost", 6379);
    }

    // -- (2) RedisTemplate 설정
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 직렬화, 역직렬화 설정 등 추가 가능
        return redisTemplate;
    }
}
