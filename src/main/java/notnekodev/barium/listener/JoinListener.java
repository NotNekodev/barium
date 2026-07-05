package notnekodev.barium.listener;

import notnekodev.barium.Barium;
import notnekodev.barium.model.Account;
import notnekodev.barium.repository.AccountRepository;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {
    private final AccountRepository repo;

    public JoinListener(AccountRepository repo) {
        this.repo = repo;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        repo.create(uuid).thenCompose(v -> repo.find(uuid)).thenAccept(account -> {
            if (account == null) {
                // in theory we shouldn't get here
                Barium.getInstance().logger.warn("Account still null, even after creating (and ignoration)!");
                account = new Account(uuid, Barium.getInstance().getConfig().getInt("currency.starting_balance", 500));
                repo.save(account);
            }
        });

        repo.find(uuid).thenAccept(account -> {
            Bukkit.getScheduler().runTask(Barium.getInstance(), () -> {
                event.getPlayer().sendMessage("Balance: " + account.getBalance()
                        + Barium.getInstance().getConfig().getString("currency.symbol"));
            });
        });
    }
}
