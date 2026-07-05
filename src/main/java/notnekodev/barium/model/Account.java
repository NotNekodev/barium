package notnekodev.barium.model;

import java.util.UUID;

public class Account {
    private final UUID uuid;
    private long balance;

    public Account(UUID uuid, long balance) {
        this.uuid = uuid;
        this.balance = balance;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void add(long amount) {
        this.balance += amount;
    }

    public void subtract(long amount) {
        this.balance -= amount;
    }
}
