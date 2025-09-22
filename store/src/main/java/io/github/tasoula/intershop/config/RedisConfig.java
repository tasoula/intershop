package io.github.tasoula.intershop.config;

import io.github.tasoula.intershop.dto.ProductDto;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class RedisConfig {
    @Bean
    public RedisCacheManagerBuilderCustomizer productsCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "products",                                         // Имя кеша
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.of(3, ChronoUnit.MINUTES))  // TTL
                        .serializeValuesWith(                          // Сериализация JSON
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new Jackson2JsonRedisSerializer<>(ProductDto.class)
                                )
                        )
        );
    }
}
