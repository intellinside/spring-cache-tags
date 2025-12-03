package io.github.intellinside.cache.tags;

import io.github.intellinside.cache.tags.annotation.CacheTagsAspect;
import io.github.intellinside.cache.tags.annotation.EvictTagsAspect;
import io.github.intellinside.cache.tags.store.CacheTagsStore;
import io.github.intellinside.cache.tags.store.InMemoryCacheTagStore;
import io.github.intellinside.cache.tags.store.RedisCacheTagStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Spring Boot AutoConfiguration for enabling cache tag-based operations.
 *
 * <p>
 * This configuration automatically registers all necessary components for
 * tag-based cache management:
 * <ul>
 * <li>{@link TaggingCacheManagerBeanPostProcessor} - wraps CacheManager
 * beans</li>
 * <li>{@link CacheTagsStore} - stores tag-to-key mappings (in-memory or
 * Redis)</li>
 * <li>{@link CacheTagsAspect} - handles {@code @CacheTags} annotations</li>
 * <li>{@link EvictTagsAspect} - handles {@code @EvictTags} annotations</li>
 * </ul>
 *
 * <p>
 * <b>Storage Backend Selection:</b>
 * <ul>
 * <li>If {@code spring.cache.type=redis} and StringRedisTemplate is available:
 * Uses {@link RedisCacheTagStore}</li>
 * <li>Otherwise: Uses {@link InMemoryCacheTagStore} (default)</li>
 * </ul>
 *
 * <p>
 * <b>Auto-configuration ordering:</b>
 * This configuration is applied after {@link CacheAutoConfiguration} to ensure
 * the underlying cache infrastructure is ready.
 *
 * @author intellinside
 * @see CacheTagsStore
 * @see CacheTagsAspect
 * @see EvictTagsAspect
 */
@AutoConfiguration
@AutoConfigureAfter(CacheAutoConfiguration.class)
public class CacheTagsAutoConfiguration {
    /**
     * Registers the cache manager bean post processor.
     *
     * @return a new {@link TaggingCacheManagerBeanPostProcessor}
     */
    @Bean
    public TaggingCacheManagerBeanPostProcessor taggingCacheManagerBeanPostProcessor() {
        return new TaggingCacheManagerBeanPostProcessor();
    }

    /**
     * Registers a Redis-based cache tags store if Redis caching is enabled.
     *
     * <p>
     * This bean is only created if:
     * <ul>
     * <li>{@code spring.cache.type} property equals "redis"</li>
     * <li>{@link StringRedisTemplate} bean is available</li>
     * </ul>
     *
     * @param redisTemplate the Redis template for operations
     * @return a {@link RedisCacheTagStore} instance
     */
    @Bean
    @ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
    @ConditionalOnBean(StringRedisTemplate.class)
    public CacheTagsStore redisCacheTagsStore(StringRedisTemplate redisTemplate,
                                              RedisCacheManager redisCacheManager) {
        return new RedisCacheTagStore(redisTemplate, redisCacheManager);
    }

    /**
     * Registers the default in-memory cache tags store.
     *
     * <p>
     * This bean is created if no other {@link CacheTagsStore} bean is present.
     * It's used for non-Redis or single-instance deployments.
     *
     * @return an {@link InMemoryCacheTagStore} instance
     */
    @Bean
    @ConditionalOnMissingBean(CacheTagsStore.class)
    public CacheTagsStore cacheTagsStore() {
        return new InMemoryCacheTagStore();
    }

    /**
     * Registers the aspect that handles {@code @CacheTags} annotations.
     *
     * @param store the cache tags store
     * @return a {@link CacheTagsAspect} instance
     */
    @Bean
    public CacheTagsAspect cacheTagsAspect(CacheTagsStore store) {
        return new CacheTagsAspect(store);
    }

    /**
     * Registers the aspect that handles {@code @EvictTags} annotations.
     *
     * @param store        the cache tags store
     * @param cacheManager the cache manager
     * @return an {@link EvictTagsAspect} instance
     */
    @Bean
    public EvictTagsAspect evictTagsAspect(CacheTagsStore store, CacheManager cacheManager) {
        return new EvictTagsAspect(store, cacheManager);
    }
}
