package notnekodev.barium.cache;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Generic cache item class
 * @param <T> The backing type of the CacheItem
 */
public final class CacheItem<T> {
    private T value;
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public CacheItem(@NotNull T value) {
        this.value = value;
    }

    /**
     * Get the backing items class
     * @return The backing items class
     */
    public T get() {
        return value;
    }

    /**
     * Check if the current CacheEntry is dirty
     * @return The dirtiness of the item
     */
    public boolean isDirty() {
        return dirty.get();
    }

    /**
     * Mark the current CacheItem as dirty
     */
    public void markDirty() {
        dirty.set(true);
    }

    /**
     * Mark the current CacheItem as clean
     */
    public void clearDirty() {
        dirty.set(false);
    }

    /**
     * Run an updater on the entry that can update the backing class
     * @param updater The updater runnable Consumer to run
     */
    public void update(Consumer<? super T> updater) {
        updater.accept(value);
        markDirty();
    }
}
