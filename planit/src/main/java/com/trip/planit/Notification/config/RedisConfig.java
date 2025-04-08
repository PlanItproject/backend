package com.trip.planit.Notification.config;

import com.trip.planit.Notification.model.Notification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    // Notification 저장용 RedisTemplate
    @Bean
    public RedisTemplate<String, com.trip.planit.Notification.model.Notification> redisNotificationTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, com.trip.planit.Notification.model.Notification> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    // 중복 체크용 RedisTemplate (String)
    @Bean
    public RedisTemplate<String, String> redisStringTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
