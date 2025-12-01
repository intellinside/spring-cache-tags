package io.github.intellinside.cache.tags.store;

import java.util.Map;
import java.util.Set;

/**
 * Interface for storing and retrieving the association between cache tags and
 * cache keys.
 *
 * <p>
 * This interface defines the contract for managing tag-to-key mappings that are
 * used to
 * implement tag-based cache invalidation. Implementations can use different
 * storage backends
 * (in-memory, Redis, etc.) to maintain these mappings.
 *
 * <p>
 * <b>Thread Safety:</b>
 * Implementations should be thread-safe to support concurrent cache operations.
 *
 * @author intellinside
 * @see InMemoryCacheTagStore
 */
public interface CacheTagsStore {
    /**
     * Associates one or more tags with a specific cache key.
     *
     * <p>
     * This method is called after a cacheable method execution to record which tags
     * are associated with the cached result.
     *
     * @param tags      the set of tags to associate with the key
     * @param cacheName the name of the cache
     * @param key       the cache key to tag
     */
    void addMappings(Set<String> tags, String cacheName, Object key);

    /**
     * Retrieves all cache keys associated with the specified tags.
     *
     * <p>
     * This method returns a map where keys are cache names and values are sets of
     * cache keys
     * that should be evicted from those caches.
     *
     * @param tags the set of tags to search for
     * @return a map of cache names to sets of keys associated with the tags
     */
    Map<String, Set<Object>> getKeysForTags(Set<String> tags);

    /**
     * Removes all associations for a specific tag.
     *
     * <p>
     * This method is called after cache entries associated with a tag have been
     * evicted
     * to clean up the tag entry and all its associated mappings.
     *
     * @param tag the tag to remove
     */
    void removeTag(String tag);
}
