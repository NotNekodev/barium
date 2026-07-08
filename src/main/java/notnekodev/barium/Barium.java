package notnekodev.barium;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import notnekodev.barium.cache.CacheSyncTask;
import notnekodev.barium.cache.PlayerCache;
import notnekodev.barium.command.AdminBalanceCommand;
import notnekodev.barium.command.BalanceCommand;
import notnekodev.barium.command.TransferCommand;
import notnekodev.barium.database.Database;
import notnekodev.barium.listener.JoinListener;
import notnekodev.barium.listener.QuitListener;
import notnekodev.barium.repository.AccountRepository;
import notnekodev.barium.repository.SQLiteAccountRepository;
import notnekodev.barium.service.EconomyService;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public final class Barium extends JavaPlugin {

    public static Barium INSTANCE = null;

    private Database db;
    private CacheSyncTask cacheSyncTask;

    public static Logger LOGGER = LoggerFactory.getLogger("barium");

    public EconomyService economyService;
    public AccountRepository accountRepository;
    public PlayerCache playerCache;

    @Override
    public void onEnable() {
        if (INSTANCE != null) {
            throw new RuntimeException("Another instance of barium is already running!");
        }

        INSTANCE = this;

        LOGGER.info("Barium enabled");

        saveDefaultConfig(); // copies the config.yml from the JAR into the server directory on first run

        String db_path = getConfig().getString("database.db_path");
        if (db_path == null) {
            LOGGER.warn("Database path is null, using default data/database.db");
            db_path = "data/database.db";
        }

        db = new Database(db_path);
        try {
            db.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        db.execute("PRAGMA journal_mode=WAL;");
        db.execute("PRAGMA synchronous=NORMAL;");
        db.execute("PRAGMA busy_timeout=5000;");
        db.execute("CREATE TABLE IF NOT EXISTS accounts (uuid TEXT PRIMARY KEY, balance INTEGER NOT NULL);");

        LOGGER.info("Set up SQLite database at {}", db_path);

        accountRepository = new SQLiteAccountRepository(db);
        playerCache = new PlayerCache(accountRepository);
        economyService = new EconomyService(playerCache);

        cacheSyncTask = new CacheSyncTask();
        cacheSyncTask.addSyncableCache(playerCache);
        Thread syncTaskThread = new Thread(cacheSyncTask, "barium-cache-sync");

        getServer().getPluginManager().registerEvents(
                new JoinListener(accountRepository, playerCache),
                this
        );

        getServer().getPluginManager().registerEvents(
                new QuitListener(accountRepository, playerCache),
                this
        );

        LOGGER.info("Registered events");

        syncTaskThread.start();

        this.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                commands -> {
                    commands.registrar().register(
                            AdminBalanceCommand.createCommand().build()
                    );
                    commands.registrar().register(
                            BalanceCommand.createCommand().build()
                    );
                    commands.registrar().register(
                            TransferCommand.createCommand().build()
                    );
                }
        );

        LOGGER.info("Barium init done");
    }

    @Override
    public void onDisable() {
        db.close();
        cacheSyncTask.stop();
        INSTANCE = null;
    }
}
