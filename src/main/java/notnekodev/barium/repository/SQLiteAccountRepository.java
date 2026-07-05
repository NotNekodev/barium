package notnekodev.barium.repository;

import notnekodev.barium.Barium;
import notnekodev.barium.database.Database;
import notnekodev.barium.model.Account;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteAccountRepository implements AccountRepository {
    private final Database db;

    public SQLiteAccountRepository(Database db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Account> find(UUID uuid) {
        return db.query(
                "SELECT uuid, balance FROM accounts WHERE uuid = ?",
                rs -> {
                    if (!rs.next()) return null;

                    return new Account(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getLong("balance")
                    );
                },
                uuid.toString()
        );
    }

    @Override
    public CompletableFuture<Void> create(UUID uuid) {
        return db.execute(
                "INSERT OR IGNORE INTO accounts(uuid, balance) VALUES(?, ?)",
                uuid.toString(),
                Barium.getInstance().getConfig().getInt("currency.starting_balance", 500)
        );
    }

    @Override
    public CompletableFuture<Void> save(Account account) {
        return db.execute(
                "UPDATE accounts SET balance = ? WHERE uuid = ?",
                account.getBalance(),
                account.getUuid().toString()
        );
    }
}