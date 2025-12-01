package io.github.intellinside.cache.tags.store;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Redis-based implementation of {@link CacheTagsStore}.
 *
 * <p>
 * This implementation stores tag-to-key mappings in Redis using Redis Sets.
 * It is suitable for distributed systems, clustered deployments, or scenarios
 * where
 * tag associations need to be shared across multiple application instances.
 *
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Distributed tag mappings across multiple instances</li>
 * <li>Automatic data persistence</li>
 * <li>Thread-safe operations through Redis</li>
 * <li>Suitable for high-concurrency environments</li>
 * </ul>
 *
 * <p>
 * <b>Redis Data Structure:</b>
 * <ul>
 * <li>Keys: "tag:{tagName}" (Redis Set)</li>
 * <li>Values: "{cacheName}:{key}" (serialized cache references)</li>
 * </ul>
 *
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * SET tag:user:123 user-cache:123:details
 * SET tag:user:123 user-cache:123:permissions
 * SET tag:type:admin admin-cache:456:settings
 * </pre>
 *
 * @author intellinside
 * @see CacheTagsStore
 * @see InMemoryCacheTagStore
 */
@RequiredArgsConstructor
public class RedisCacheTagStore implements CacheTagsStore {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Associates multiple tags with a cache key by storing them in Redis Sets.
     *
     * <p>
     * For each tag, adds a reference to the cache entry (cacheName:key) to a Redis
     * Set.
     *
     * @param tags      the set of tags to associate
     * @param cacheName the name of the cache
     * @param key       the cache key
     */
    @Override
    public void addMappings(Set<String> tags, String cacheName, Object key) {
        String serializedKey = cacheName + ":" + key.toString();
        tags.forEach(tag -> redisTemplate.opsForSet().add("tag:" + tag, serializedKey));
    }

    /**
     * Retrieves all cache keys associated with the specified tags from Redis.
     *
     * <p>
     * For each tag, retrieves all members from the corresponding Redis Set,
     * parses the cache name from the stored references, and groups keys by cache
     * name.
     *
     * @param tags the tags to search for
     * @return a map of cache names to sets of keys
     */
    @Override
    public Map<String, Set<Object>> getKeysForTags(Set<String> tags) {
        Map<String, Set<Object>> result = new HashMap<>();
        for (String tag : tags) {
            Set<String> members = redisTemplate.opsForSet().members("tag:" + tag);
            if (members != null) {
                for (String s : members) {
                    String[] parts = s.split(":", 2);
                    result.computeIfAbsent(parts[0], k -> new HashSet<>()).add(parts[1]);
                }
            }
        }
        return result;
    }

    /**
     * Removes all associations for a tag by deleting the Redis Set.
     *
     * @param tag the tag to remove
     */
    @Override
    public void removeTag(String tag) {
        redisTemplate.delete("tag:" + tag);
    }
}
