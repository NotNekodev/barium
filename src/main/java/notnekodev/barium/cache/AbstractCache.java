package notnekodev.barium.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class AbstractCache<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, CacheItem<V>> map = new ConcurrentHashMap<>();

    @Override
    public CacheItem<V> get(K key) {
        return map.get(key);
    }

    @Override
    public CacheItem<V> getOrCreate(K key, Supplier<V> supplier) {
        CacheItem<V> maybe = map.get(key);

        if (maybe != null) {
            return maybe;
        }

        map.put(key, new CacheItem<>(supplier.get()));

        return map.get(key);
    }

    @Override
    public void put(K key, V value) {
        map.put(key, new CacheItem<>(value));
    }

    @Override
    public void remove(K key) {
        map.remove(key);
    }

    @Override
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Collection<CacheItem<V>> values() {
        return map.values();
    }

    @Override
    public Map<K, CacheItem<V>> asMap() {
        return map;
    }

    @Override
    public CompletableFuture<Integer> sync() {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        int dirty = 0;

        for (CacheItem<V> item : map.values()) {
            if (!item.isDirty()) continue;
            dirty++;

            futures.add(save(item.get())
                    .thenRun(item::clearDirty)
            );
        }

        final int finalDirty = dirty; // fuck you java

        return CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> finalDirty);
    }

    protected abstract CompletableFuture<Void> save(V value);
}
