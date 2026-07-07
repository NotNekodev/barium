package notnekodev.barium.cache;

import notnekodev.barium.Barium;
import notnekodev.barium.model.Account;
import notnekodev.barium.repository.AccountRepository;

public class CacheSyncTask implements Runnable {
    private final PlayerCache playerCache;
    private final AccountRepository accountRepository;

    public CacheSyncTask(PlayerCache playerCache, AccountRepository accountRepository) {
        this.playerCache = playerCache;
        this.accountRepository = accountRepository;
    }

    @Override
    public void run() {
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
    }
}
