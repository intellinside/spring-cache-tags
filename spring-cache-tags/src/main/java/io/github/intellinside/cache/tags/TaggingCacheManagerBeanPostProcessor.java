package io.github.intellinside.cache.tags;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.CacheManager;

/**
 * A Spring BeanPostProcessor that automatically wraps all {@link CacheManager}
 * beans
 * with {@link TaggingCacheManager} to enable tag-based cache operations.
 *
 * <p>
 * This processor is registered in the Spring context by CacheTagsAutoConfiguration}
 * and intercepts the initialization of CacheManager beans. Each CacheManager
 * encountered
 * is wrapped to provide tag-aware caching functionality transparently.
 *
 * <p>
 * <b>How it works:</b>
 * <ol>
 * <li>Spring instantiates and initializes the CacheManager bean</li>
 * <li>This processor's postProcessAfterInitialization method is called</li>
 * <li>If the bean is a CacheManager, it is wrapped with
 * {@link TaggingCacheManager}</li>
 * <li>The wrapped manager is used throughout the application</li>
 * </ol>
 *
 * <p>
 * <b>Note:</b>
 * Only CacheManager instances are wrapped. Other beans are passed through
 * unchanged.
 *
 * @author intellinside
 * @see TaggingCacheManager
 */
public class TaggingCacheManagerBeanPostProcessor implements BeanPostProcessor {
    /**
     * Wraps CacheManager beans with TaggingCacheManager after initialization.
     *
     * @param bean     the bean instance
     * @param beanName the bean name
     * @return a TaggingCacheManager if the bean is a CacheManager, otherwise the
     *         original bean
     * @throws BeansException if an error occurs during processing
     */
    @Override
    public @Nullable Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName)
            throws BeansException {
        if (bean instanceof CacheManager cacheManager) {
            return new TaggingCacheManager(cacheManager);
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
