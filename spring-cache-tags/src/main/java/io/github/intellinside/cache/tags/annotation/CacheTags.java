package io.github.intellinside.cache.tags.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to tag cached methods with one or more string tags.
 *
 * <p>
 * This annotation must be used in conjunction with Spring Cache annotations
 * like {@code @Cacheable}.
 * The tags are processed by the {@link CacheTagsAspect} and stored in the
 * configured {@link io.github.intellinside.cache.tags.store.CacheTagsStore}.
 * Tags can later be used with {@link EvictTags} to evict multiple cache entries
 * at once.
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
 * &#64;Cacheable("users")
 * &#64;CacheTags("'user:' + #id")
 * public User getUserById(Long id) {
 *     return userRepository.findById(id);
 * }
 *
 * &#64;Cacheable("products")
 * &#64;CacheTags({ "'product:' + #productId", "'category:' + #result.category" })
 * public Product getProduct(Long productId) {
 *     return productRepository.findById(productId);
 * }
 * </pre>
 *
 * @author intellinside
 * @see EvictTags
 * @see CacheTagsAspect
 * @see io.github.intellinside.cache.tags.store.CacheTagsStore
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheTags {
    /**
     * Tag values to associate with the cached method result.
     * Multiple tags can be specified to classify a single cache entry.
     * Tags support SpEL expressions.
     *
     * @return array of tag expressions
     */
    String[] value();
}
