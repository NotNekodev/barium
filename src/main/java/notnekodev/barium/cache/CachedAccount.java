package notnekodev.barium.cache;

import notnekodev.barium.model.Account;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an account that is cached inside of {@link PlayerCache}
 */
public class CachedAccount {
    private final Account account;
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    /**
     * CachedAccount constructor
     * @param account The {@link Account} to cache
     */
    public CachedAccount(Account account) {
        this.account = account;
    }

    /**
     * Get the actual {@link Account} object
     * @return The {@link Account} object cached in this entry
     */
    public Account get() {
        return account;
    }

    /**
     * Get the current dirtiness of the cache entry
     * @return The current dirtiness of the cache as a {@link Boolean}
     */
    public boolean isDirty() {
        return dirty.get();
    }

    /**
     * Mark the current cache entry as dirty
     */
    public void markDirty() {
        dirty.set(true);
    }

    /**
     * Clear the dirty flag on the current cache entry
     */
    public void clearDirty() {
        dirty.set(false);
    }

    /**
     * Deposit {@code amount} of currency into the backed {@link Account}
     * @param amount The amount to deposit
     */
    public void deposit(long amount) {
        account.add(amount);
        markDirty();
    }

    /**
     * Withdraw {@code amount} of currency from the backed {@link Account}
     * @param amount The amount to withdraw
     */
    public void withdraw(long amount) {
        account.subtract(amount);
        markDirty();
    }
}
