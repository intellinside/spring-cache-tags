package io.github.intellinside.cache.tags.annotation;

import io.github.intellinside.cache.tags.store.CacheTagsStore;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect that handles the {@link EvictTags} annotation processing.
 *
 * <p>
 * This aspect intercepts method calls annotated with {@code @EvictTags} and
 * performs the following:
 * <ol>
 * <li>Evaluates all tag expressions using SpEL with the method result</li>
 * <li>Retrieves cache keys associated with the evaluated tags from the
 * {@link CacheTagsStore}</li>
 * <li>Evicts all matching entries from their respective caches</li>
 * <li>Removes the tag entries from the store</li>
 * </ol>
 *
 * <p>
 * The eviction is performed using the AfterReturning advice, which means
 * eviction happens only if
 * the method completes successfully (no exception thrown).
 *
 * <p>
 * The aspect is ordered at {@code LOWEST_PRECEDENCE - 100} to ensure proper
 * coordination
 * with Spring Cache and other interceptors.
 *
 * @author intellinside
 * @see EvictTags
 * @see SpelExpressionEvaluator
 * @see CacheTagsStore
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RequiredArgsConstructor
public class EvictTagsAspect {
    private final CacheTagsStore store;
    private final CacheManager cacheManager;
    private final SpelExpressionEvaluator evaluator = new SpelExpressionEvaluator();

    /**
     * Intercepts method calls annotated with {@code @EvictTags} after successful
     * execution.
     *
     * <p>
     * This method:
     * <ol>
     * <li>Evaluates tag expressions with SpEL using the method result</li>
     * <li>Retrieves all cache keys associated with the tags</li>
     * <li>Evicts the cache entries from their respective caches</li>
     * <li>Removes the tag records from the store</li>
     * </ol>
     *
     * @param jp        the join point representing the intercepted method call
     * @param evictTags the {@link EvictTags} annotation with tag expressions
     * @param result    the return value of the intercepted method
     */
    @AfterReturning(value = "@annotation(evictTags)", returning = "result")
    public void handle(JoinPoint jp, EvictTags evictTags, Object result) {
        Set<String> tags = Arrays.stream(evictTags.value())
                .map(tag -> evaluator.evaluate(tag, jp, result))
                .collect(Collectors.toSet());

        Map<String, Set<Object>> keysByCache = store.getKeysForTags(tags);
        for (Map.Entry<String, Set<Object>> entry : keysByCache.entrySet()) {
            Cache cache = cacheManager.getCache(entry.getKey());
            if (cache == null)
                continue;
            entry.getValue().forEach(cache::evict);
        }

        tags.forEach(store::removeTag);
    }
}
