package notnekodev.barium.listener;

import notnekodev.barium.cache.CachedAccount;
import notnekodev.barium.cache.PlayerCache;
import notnekodev.barium.model.Account;
import notnekodev.barium.repository.AccountRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class QuitListener implements Listener {
    private final AccountRepository repo;
    private final PlayerCache playerCache;

    public QuitListener(AccountRepository repo, PlayerCache playerCache) {
        this.repo = repo;
        this.playerCache = playerCache;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        CachedAccount cached = playerCache.get(uuid);
        if (cached == null) return;

        Account acc = cached.get();

        repo.save(acc).thenRun(() -> playerCache.remove(uuid));
    }

}
