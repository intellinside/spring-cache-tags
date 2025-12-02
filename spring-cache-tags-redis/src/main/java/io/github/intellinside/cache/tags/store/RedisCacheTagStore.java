package io.github.intellinside.cache.tags.store;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Redis-based implementation of {@link CacheTagsStore}.
 *
 * <p>
 * This implementation stores tag-to-key mappings in Redis using Redis Sets
 * (one Redis Set per tag). It is intended for distributed or clustered
 * deployments where tag associations must be shared between application
 * instances.
 * </p>
 *
 * <p>
 * <b>Behavior and guarantees</b>:
 * <ul>
 * <li>Associations are stored in Redis Sets (atomic, concurrent-safe on the
 * Redis side).</li>
 * <li>Entries are stored as strings in the format {@code {cacheName}:{key}},
 * where {@code key} is obtained via {@code toString()} on the provided key
 * object.</li>
 * <li>TTL (expiration) is applied per-tag when the configured
 * {@code RedisCacheConfiguration#getTtlFunction()} returns a positive
 * {@code Duration} for the tag. TTL is set using {@code RedisTemplate.expire}.
 * </li>
 * <li>Methods are null-tolerant: missing Redis Set members produce no entries
 * and the lookup returns an empty map when no associations are found.</li>
 * </ul>
 *
 * @author intellinside
 * @see CacheTagsStore
 * @see InMemoryCacheTagStore
 */
@RequiredArgsConstructor
public class RedisCacheTagStore implements CacheTagsStore {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCacheConfiguration redisCacheConfiguration;

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
        tags.stream()
                .map(tag -> "tag:" + tag)
                .forEach(tag -> {
                    redisTemplate.opsForSet().add(tag, serializedKey);
                    Duration timeToLive = redisCacheConfiguration.getTtlFunction().getTimeToLive(tag, null);
                    if (timeToLive.isPositive()) {
                        redisTemplate.expire(tag, timeToLive);
                    }
                });
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
