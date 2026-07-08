package notnekodev.barium.cache;

import notnekodev.barium.model.Account;
import notnekodev.barium.repository.AccountRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerCache extends AbstractCache<UUID, Account> {
    private final AccountRepository repository;

    public PlayerCache(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    protected CompletableFuture<Void> save(Account value) {
        return repository.save(value);
    }
}