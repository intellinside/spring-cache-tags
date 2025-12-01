package io.github.intellinside.cache.tags.context;

/**
 * Record that holds the current caching context for a thread.
 *
 * <p>
 * This record is used to store information about the cache operation being
 * performed,
 * including the cache name and the key being used. It is managed by
 * {@link TagContextHolder}
 * and is thread-local to support concurrent cache operations.
 *
 * @param cacheName the name of the cache where the entry is being stored
 * @param key       the cache key for the entry
 *
 * @author intellinside
 * @see TagContextHolder
 */
public record TagContext(String cacheName, Object key) {
}
