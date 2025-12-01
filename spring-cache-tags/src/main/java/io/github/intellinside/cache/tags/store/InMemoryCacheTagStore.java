package io.github.intellinside.cache.tags.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link CacheTagsStore} using ConcurrentHashMap.
 *
 * <p>
 * This implementation stores tag-to-key mappings entirely in application
 * memory.
 * It is suitable for single-instance applications or scenarios where tag
 * associations
 * do not need to be shared across multiple application instances.
 *
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Thread-safe using {@link ConcurrentHashMap}</li>
 * <li>No external dependencies (e.g., Redis)</li>
 * <li>Fast in-memory lookups</li>
 * <li>Mappings are lost when the application restarts</li>
 * </ul>
 *
 * <p>
 * <b>Memory Considerations:</b>
 * This implementation keeps all tag mappings in memory. For applications with
 * large numbers of cache entries, consider using RedisCacheTagStore
 * as an alternative.
 *
 * @author intellinside
 * @see CacheTagsStore
 */
public class InMemoryCacheTagStore implements CacheTagsStore {
    private final Map<String, Set<KeyRef>> tagMap = new ConcurrentHashMap<>();

    /**
     * Internal record representing a cache key reference.
     */
    private record KeyRef(String cacheName, Object key) {
    }

    /**
     * Associates multiple tags with a cache key.
     *
     * @param tags      the set of tags to associate
     * @param cacheName the name of the cache
     * @param key       the cache key
     */
    @Override
    public void addMappings(Set<String> tags, String cacheName, Object key) {
        KeyRef ref = new KeyRef(cacheName, key);
        tags.forEach(tag -> tagMap.computeIfAbsent(tag, t -> ConcurrentHashMap.newKeySet()).add(ref));
    }

    /**
     * Retrieves all cache keys associated with the specified tags.
     *
     * @param tags the tags to search for
     * @return a map of cache names to sets of keys
     */
    @Override
    public Map<String, Set<Object>> getKeysForTags(Set<String> tags) {
        Map<String, Set<Object>> result = new HashMap<>();
        for (String tag : tags) {
            Set<KeyRef> refs = tagMap.get(tag);
            if (refs != null) {
                for (KeyRef ref : refs) {
                    result.computeIfAbsent(ref.cacheName, k -> new HashSet<>()).add(ref.key);
                }
            }
        }
        return result;
    }

    /**
     * Removes all associations for a tag.
     *
     * @param tag the tag to remove
     */
    @Override
    public void removeTag(String tag) {
        tagMap.remove(tag);
    }
}
