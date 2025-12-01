package io.github.intellinside.cache.tags.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to evict all cache entries associated with specified tags.
 *
 * <p>
 * This annotation is processed by {@link EvictTagsAspect} after the annotated
 * method completes successfully.
 * It finds all cache entries tagged with the specified tags and removes them
 * from the cache.
 * The eviction happens automatically after the method returns (AfterReturning
 * advice).
 *
 * <p>
 * Tags support Spring Expression Language (SpEL) expressions, which can
 * reference:
 * <ul>
 * <li>Method arguments by name (requires -parameters compiler flag)</li>
 * <li>Method return value via {@code #result}</li>
 * <li>All method arguments via {@code #args}</li>
 * <li>Method metadata via {@code #method}</li>
 * </ul>
 *
 * <p>
 * <b>Example usage:</b>
 * 
 * <pre>
 * &#64;EvictTags("'user:' + #id")
 * public void updateUser(Long id, UserDTO userDTO) {
 *     userRepository.save(userDTO);
 * }
 *
 * &#64;EvictTags({ "'product:' + #productId", "'category:all'" })
 * public void deleteProduct(Long productId) {
 *     productRepository.deleteById(productId);
 * }
 * </pre>
 *
 * @author intellinside
 * @see CacheTags
 * @see EvictTagsAspect
 * @see io.github.intellinside.cache.tags.store.CacheTagsStore
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EvictTags {
    /**
     * Tag values that identify cache entries to be evicted.
     * All cache entries associated with these tags will be removed.
     * Tags support SpEL expressions.
     *
     * @return array of tag expressions
     */
    String[] value();
}
