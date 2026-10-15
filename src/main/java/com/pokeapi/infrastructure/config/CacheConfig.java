package com.pokeapi.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig {

    public static final String POKEMON_PAGES = "pokemonPages";

    @Bean
    public CacheManager cacheManager(@Value("${pokeapi.cache.ttl-minutes}") long ttlMinutes,
                                     @Value("${pokeapi.cache.max-size}") long maxSize) {
        CaffeineCacheManager manager = new CaffeineCacheManager(POKEMON_PAGES);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                .maximumSize(maxSize));
        return manager;
    }
}