package io.github.intellinside.cache.tags;

import io.github.intellinside.cache.tags.context.TagContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A decorator implementation of {@link Cache} that integrates tag-based caching
 * functionality.
 *
 * <p>
 * This class wraps a standard Spring {@link Cache} and intercepts put
 * operations to
 * establish the cache context (cache name and key) that is used by the tagging
 * framework.
 * When a cache entry is stored, this wrapper ensures that the
 * {@link TagContextHolder}
 * is populated with the relevant cache metadata.
 *
 * <p>
 * <b>Integration with Spring Cache:</b>
 * This decorator is automatically applied to all caches through the
 * {@link TaggingCacheManagerBeanPostProcessor}. It is transparent to the
 * application code.
 *
 * <p>
 * <b>How it works:</b>
 * <ol>
 * <li>When put() is called, context is established via
 * {@link TagContextHolder}</li>
 * <li>The cache operation proceeds normally</li>
 * <li>Any {@code @CacheTags} aspect intercepts the operation and records
 * tags</li>
 * </ol>
 *
 * <p>
 * <b>All other operations:</b>
 * Get, retrieve, and evict operations are delegated directly to the wrapped
 * cache.
 *
 * @author intellinside
 * @see TagContextHolder
 * @see TaggingCacheManager
 * @see io.github.intellinside.cache.tags.annotation.CacheTags
 */
@RequiredArgsConstructor
public class TaggingCache implements Cache {
    private final Cache cache;

    @Override
    @Nonnull
    public String getName() {
        return cache.getName();
    }

    @Override
    @Nonnull
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Nullable
    @Override
    public ValueWrapper get(@Nonnull Object key) {
        return cache.get(key);
    }

    @Nullable
    @Override
    public <T> T get(@Nonnull Object key, @Nullable Class<T> type) {
        return cache.get(key, type);
    }

    @Nullable
    @Override
    public <T> T get(@Nonnull Object key, @Nonnull Callable<T> valueLoader) {
        return cache.get(key, valueLoader);
    }

    @Nullable
    @Override
    public CompletableFuture<?> retrieve(@Nonnull Object key) {
        return cache.retrieve(key);
    }

    @Override
    @Nonnull
    public <T> CompletableFuture<T> retrieve(@Nonnull Object key, @Nonnull Supplier<CompletableFuture<T>> valueLoader) {
        return cache.retrieve(key, valueLoader);
    }

    /**
     * Stores a value in the cache and establishes the tag context.
     *
     * @param key   the cache key
     * @param value the value to cache
     */
    @Override
    public void put(@Nonnull Object key, @Nullable Object value) {
        createContext(key);
        cache.put(key, value);
    }

    /**
     * Stores a value in the cache if absent and establishes the tag context.
     *
     * @param key   the cache key
     * @param value the value to cache
     * @return the previous value if present, otherwise null
     */
    @Nullable
    @Override
    public ValueWrapper putIfAbsent(@Nonnull Object key, @Nullable Object value) {
        createContext(key);
        return cache.putIfAbsent(key, value);
    }

    @Override
    public void evict(@Nonnull Object key) {
        cache.evict(key);
    }

    @Override
    public boolean evictIfPresent(@Nonnull Object key) {
        return cache.evictIfPresent(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public boolean invalidate() {
        return cache.invalidate();
    }

    /**
     * Establishes the cache context for the current thread.
     *
     * @param key the cache key being operated on
     */
    private void createContext(@Nonnull Object key) {
        TagContextHolder.set(cache.getName(), key);
    }
}
