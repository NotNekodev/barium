package notnekodev.barium.service;

import notnekodev.barium.Barium;
import notnekodev.barium.cache.CachedAccount;
import notnekodev.barium.cache.PlayerCache;

import java.util.UUID;

/**
 * Service to interact with the economy and accounts of players.
 * @apiNote Thread safe
 */
public class EconomyService {
    private final PlayerCache playerCache;

    /**
     * Constructor of the EconomyService, should be called from {@link Barium#onEnable()}
     * @param playerCache The global {@link PlayerCache} instance
     */
    public EconomyService(PlayerCache playerCache) {
        this.playerCache = playerCache;
    }

    /**
     * Get the current account balance of a player
     * @param uuid UUID of the player
     * @return The balance of the player or 0
     */
    public long getBalance(UUID uuid) {
        CachedAccount acc = playerCache.get(uuid);
        return acc != null ? acc.get().getBalance() : 0;
    }

    /**
     * Sets the balance of a specific player
     * @param uuid UUID of the player
     * @param amount The new balance of the players account
     */
    public void setBalance(UUID uuid, long amount) {
        CachedAccount acc = playerCache.get(uuid);
        if (acc != null) {
            acc.get().setBalance(amount);
            acc.markDirty();
        }
    }

    /**
     * Adds a certain amount to a players account
     * @param uuid UUID of a player
     * @param amount The amount to be added
     */
    public void addBalance(UUID uuid, long amount) {
        CachedAccount acc = playerCache.get(uuid);
        if (acc != null) {
            acc.deposit(amount);
        }
    }

    /**
     * Transfer money from one player to another
     * @param from UUID of the player to transfer the money from
     * @param to UUID of the player to transfer the money to
     * @param amount The amount of money that should be transferred
     * @return 0 on success, 1 on invalid UUIDs, 2 if the players balance isn't enough and 3 if the amount is invalid
     */
    public int transfer(UUID from, UUID to, long amount) {
        CachedAccount sender = playerCache.get(from);
        CachedAccount receiver = playerCache.get(to);

        if (sender == null || receiver == null) return 1;
        if (sender.get().getBalance() < amount) return 2;

        if (amount <= 0) return 3;

        sender.withdraw(amount);
        receiver.deposit(amount);

        return 0;
    }
}
