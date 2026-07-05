package notnekodev.barium.cache;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache {
    private final Map<UUID, CachedAccount> cache = new ConcurrentHashMap<>();

    public void put(UUID uuid, CachedAccount account) {
        cache.put(uuid, account);
    }

    public CachedAccount get(UUID uuid) {
        return cache.get(uuid);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public Map<UUID, CachedAccount> all() {
        return cache;
    }

    public boolean contains(UUID uuid) {
        return cache.containsKey(uuid);
    }
}