package notnekodev.barium.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Cache<K, V> {
    CacheItem<V> get(K key);
    CacheItem<V> getOrCreate(K key, Supplier<V> supplier);

    void put(K key, V value);
    void remove(K key);

    boolean contains(K key);
    int size();
    void clear();

    Collection<CacheItem<V>> values();
    Map<K, CacheItem<V>> asMap();

    CompletableFuture<Integer> sync();
}
