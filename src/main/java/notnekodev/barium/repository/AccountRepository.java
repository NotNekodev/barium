package notnekodev.barium.repository;

import notnekodev.barium.model.Account;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AccountRepository {
    CompletableFuture<Account> find(UUID uuid);
    CompletableFuture<Void> create(UUID uuid);
    CompletableFuture<Void> save(Account account);
}