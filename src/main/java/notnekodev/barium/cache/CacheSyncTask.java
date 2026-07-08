package notnekodev.barium.cache;

import notnekodev.barium.Barium;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CacheSyncTask implements Runnable {
    private final List<Cache<?, ?>> caches = new CopyOnWriteArrayList<>();
    private volatile boolean running;

    public void addSyncableCache(Cache<?, ?> cache) {
        caches.add(cache);
    }

    public void removeSyncableCache(Cache<?, ?> cache) {
        caches.remove(cache);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            for (Cache<?, ?> cache : caches) {
                cache.sync().thenAccept(count -> {
                    if (count > 0) {
                        Barium.LOGGER.info("{} synchronized {} entries", cache.getClass().getSimpleName(), count);
                    }
                });
            }

            try {
                Thread.sleep(Duration.ofMinutes(Barium.INSTANCE.getConfig().getInt("ddatabase.cache_sync", 1)));
            } catch (InterruptedException e) {
                Barium.LOGGER.warn("Cache sync thread interrupt: {}", e.toString());
            }
        }
    }

    public void stop() {
        running = false;
    }
}
