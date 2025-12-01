package io.github.intellinside.cache.tags.annotation;

import io.github.intellinside.cache.tags.context.TagContext;
import io.github.intellinside.cache.tags.store.CacheTagsStore;
import io.github.intellinside.cache.tags.context.TagContextHolder;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aspect that handles the {@link CacheTags} annotation processing.
 *
 * <p>
 * This aspect intercepts method calls annotated with {@code @CacheTags} and
 * performs the following:
 * <ol>
 * <li>Proceeds with the original method execution</li>
 * <li>Evaluates all tag expressions using SpEL</li>
 * <li>Stores the tag-to-key mappings in the configured
 * {@link CacheTagsStore}</li>
 * <li>Clears the thread-local context</li>
 * </ol>
 *
 * <p>
 * The aspect is ordered at {@code LOWEST_PRECEDENCE - 100} to ensure it
 * executes after Spring Cache interceptors.
 * This allows the aspect to access the cache name and key that were set by
 * Spring Cache components.
 *
 * <p>
 * <b>Thread Safety:</b>
 * Uses {@link TagContextHolder} to maintain the cache context for the current
 * thread,
 * which is automatically cleared in the finally block.
 *
 * @author intellinside
 * @see CacheTags
 * @see SpelExpressionEvaluator
 * @see CacheTagsStore
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RequiredArgsConstructor
public class CacheTagsAspect {
    private final SpelExpressionEvaluator evaluator = new SpelExpressionEvaluator();
    private final CacheTagsStore store;

    /**
     * Intercepts method calls annotated with {@code @CacheTags}.
     *
     * <p>
     * This method:
     * <ol>
     * <li>Executes the original method</li>
     * <li>Evaluates tag expressions with SpEL</li>
     * <li>Stores tag-to-key mappings in the cache store</li>
     * </ol>
     *
     * @param pjp       the join point representing the intercepted method call
     * @param cacheTags the {@link CacheTags} annotation with tag expressions
     * @return the result of the method execution
     * @throws Throwable if the original method throws an exception
     */
    @Around("@annotation(cacheTags)")
    public Object handle(ProceedingJoinPoint pjp, CacheTags cacheTags) throws Throwable {
        try {
            Object result = pjp.proceed();

            Set<String> tags = Arrays.stream(cacheTags.value())
                    .map(tag -> evaluator.evaluate(tag, pjp, result))
                    .collect(Collectors.toSet());

            if (!CollectionUtils.isEmpty(tags)) {
                TagContext context = TagContextHolder.get();
                if (context != null) {
                    store.addMappings(tags, context.cacheName(), context.key());
                }
            }

            return result;
        } finally {
            TagContextHolder.clear();
        }
    }
}
