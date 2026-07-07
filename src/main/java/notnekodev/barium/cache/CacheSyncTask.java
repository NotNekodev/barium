package notnekodev.barium.cache;

import notnekodev.barium.Barium;
import notnekodev.barium.model.Account;
import notnekodev.barium.repository.AccountRepository;

/**
 * Represents the task that runs in a different thread to sync the cache to the on-disk SQL database regularly
 */
public class CacheSyncTask implements Runnable {
    private final PlayerCache playerCache;
    private final AccountRepository accountRepository;
    private volatile boolean running;

    /**
     * Constructor of the synchronization task
     * @param playerCache The backing {@link PlayerCache} to sync from
     * @param accountRepository The backing {@link AccountRepository} to sync to
     */
    public CacheSyncTask(PlayerCache playerCache, AccountRepository accountRepository) {
        this.playerCache = playerCache;
        this.accountRepository = accountRepository;
    }

    /**
     * Actual run function that the thread runs. Loops until {@link #stop()} is called, sleeps per config specified amount of seconds
     * after each iteration
     */
    @Override
    public void run() {
        running = true;
        int minutes = Barium.INSTANCE.getConfig().getInt("database.cache_sync", 1);

        Barium.LOGGER.info("Cache will sync to on-disk SQL database every {} minutes", minutes);

        while (running) {
            int syncedAccs = 0;
            for (var entry : playerCache.all().entrySet()) {
                CachedAccount cached = entry.getValue();
                if (!cached.isDirty()) continue;
                Account acc = cached.get();
                syncedAccs++;

                accountRepository.save(acc).thenRun(cached::clearDirty);
            }

            if (syncedAccs > 0) {
                Barium.LOGGER.info("Synced cache with on-disk SQL database ({} accounts)", syncedAccs);
            }
            try {
                Thread.sleep(1000L * 60L * minutes);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Barium.LOGGER.error("Failed to sleep on cache sync thread: {}", e.toString());
            }
        }
        Barium.LOGGER.info("Cache synchronization task stopped!");
    }

    /**
     * Stop the synchronization task
     */
    public void stop() {
        running = false;
    }
}
