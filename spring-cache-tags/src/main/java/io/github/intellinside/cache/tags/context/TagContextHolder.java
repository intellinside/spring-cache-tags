package io.github.intellinside.cache.tags.context;

/**
 * Thread-local holder for managing the current {@link TagContext}.
 *
 * <p>
 * This class provides static methods to store, retrieve, and clear the cache
 * context
 * for the current thread. It is used internally by the caching framework to
 * maintain
 * the cache name and key during method execution.
 *
 * <p>
 * <b>Thread Safety:</b>
 * Uses {@link ThreadLocal} to ensure each thread has its own isolated context,
 * making this class thread-safe for concurrent operations.
 *
 * <p>
 * <b>Usage:</b>
 * 
 * <pre>
 * // Set context
 * TagContextHolder.set("cacheName", cacheKey);
 *
 * // Get context
 * TagContext context = TagContextHolder.get();
 *
 * // Clear context (important for cleanup)
 * TagContextHolder.clear();
 * </pre>
 *
 * @author intellinside
 * @see TagContext
 */
public final class TagContextHolder {
    private static final ThreadLocal<TagContext> INSTANCE = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private TagContextHolder() {
    }

    /**
     * Sets the cache context for the current thread.
     *
     * @param cacheName the name of the cache
     * @param key       the cache key
     */
    public static void set(String cacheName, Object key) {
        INSTANCE.set(new TagContext(cacheName, key));
    }

    /**
     * Gets the cache context for the current thread.
     *
     * @return the {@link TagContext} for this thread, or null if not set
     */
    public static TagContext get() {
        return INSTANCE.get();
    }

    /**
     * Clears the cache context for the current thread.
     * Should be called in a finally block to ensure cleanup.
     */
    public static void clear() {
        INSTANCE.remove();
    }
}
