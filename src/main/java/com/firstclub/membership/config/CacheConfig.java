package com.firstclub.membership.config;

import com.firstclub.membership.common.cache.CacheNames;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Caffeine as the local cache provider. The cache spec is externalized so TTL
 * and size can be tuned per environment. {@code recordStats} feeds Actuator cache metrics.
 *
 * <p>This is the swap point for a distributed cache: replacing this bean with a Redis-backed
 * {@code CacheManager} requires no change to the cached services.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(@Value("${membership.cache.spec}") String spec) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.from(spec));
        manager.setCacheNames(CacheNames.ALL);
        return manager;
    }
}
