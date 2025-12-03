package io.github.intellinside.cache.tags;

import io.github.intellinside.cache.tags.context.TagContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replaces Spring `CacheManager` beans with proxies that provide tag-aware
 * caching.
 *
 * <p>
 * When a `CacheManager` is found, it is proxied so that calls to
 * `getCache(...)` return wrapped `Cache` instances. Wrapped caches capture
 * the cache name and keys on `put` / `putIfAbsent` calls to support
 * tag-based invalidation. Non-`CacheManager` beans are left unchanged.
 *
 */
public class TaggingCacheManagerBeanPostProcessor implements BeanPostProcessor {
    private final Map<String, Cache> caches = new ConcurrentHashMap<>();

    @Override
    public @Nullable Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName)
            throws BeansException {
        if (!(bean instanceof CacheManager cacheManager)) {
            return bean;
        }

        ProxyFactory factory = new ProxyFactory(cacheManager);
        factory.setProxyTargetClass(true);

        factory.addAdvice((MethodInterceptor) invocation -> {
            Method method = invocation.getMethod();

            if (!method.getName().equals("getCache")) {
                return invocation.proceed();
            }

            Object result = invocation.proceed();
            if (result instanceof Cache cache) {
                return caches.computeIfAbsent(cache.getName(), key -> wrapCache(cache));
            }

            return result;
        });

        return factory.getProxy();
    }

    private Cache wrapCache(Cache original) {
        ProxyFactory factory = new ProxyFactory(original);
        factory.setProxyTargetClass(true);

        factory.addAdvice((MethodInterceptor) invocation -> {
            String name = invocation.getMethod().getName();

            if (name.equals("put") || name.equals("putIfAbsent")) {
                TagContextHolder.set(original.getName(), invocation.getArguments()[0]);
            }

            return invocation.proceed();
        });

        return (Cache) factory.getProxy();
    }
}
