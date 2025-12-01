package io.github.intellinside.cache.tags;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A decorator implementation of {@link CacheManager} that wraps all caches with
 * {@link TaggingCache}.
 *
 * <p>
 * This cache manager acts as a proxy to the underlying Spring CacheManager and
 * ensures
 * that every cache retrieved is wrapped with tag-aware functionality. The
 * wrapping is
 * transparent to the application code.
 *
 * <p>
 * <b>How it works:</b>
 * <ol>
 * <li>When getCache(name) is called, it retrieves the cache from the delegate
 * manager</li>
 * <li>If not already wrapped, it decorates it with {@link TaggingCache}</li>
 * <li>The wrapped cache is cached in a local map to avoid repeated
 * wrapping</li>
 * </ol>
 *
 * <p>
 * <b>Integration:</b>
 * This manager is typically applied automatically by
 * {@link TaggingCacheManagerBeanPostProcessor}
 * which wraps the primary CacheManager bean during Spring initialization.
 *
 * @author intellinside
 * @see TaggingCache
 * @see TaggingCacheManagerBeanPostProcessor
 */
@RequiredArgsConstructor
public class TaggingCacheManager implements CacheManager {
    private final CacheManager cacheManager;
    private final ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

    /**
     * Retrieves a cache by name, wrapping it with tag functionality if needed.
     *
     * <p>
     * The wrapped cache is cached internally to avoid repeated wrapping of the same
     * cache.
     *
     * @param name the name of the cache to retrieve
     * @return the wrapped {@link TaggingCache}, or null if no cache exists with the
     *         given name
     */
    @Override
    public @Nullable Cache getCache(@Nonnull String name) {
        Cache cache = cacheMap.get(name);
        if (cache == null) {
            cache = cacheManager.getCache(name);
            if (cache != null) {
                cache = new TaggingCache(cache);
                cacheMap.put(name, cache);
            }
        }

        return cache;
    }

    /**
     * Gets the collection of cache names from the underlying cache manager.
     *
     * @return collection of cache names
     */
    @Override
    @Nonnull
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}
