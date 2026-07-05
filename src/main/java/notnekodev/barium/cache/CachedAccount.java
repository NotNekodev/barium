package notnekodev.barium.cache;

import notnekodev.barium.model.Account;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedAccount {
    private final Account account;
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public CachedAccount(Account account) {
        this.account = account;
    }

    public Account get() {
        return account;
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void markDirty() {
        dirty.set(true);
    }

    public void clearDirty() {
        dirty.set(false);
    }

    public void deposit(long amount) {
        account.add(amount);
        markDirty();
    }

    public void withdraw(long amount) {
        account.subtract(amount);
        markDirty();
    }
}
