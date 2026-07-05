package notnekodev.barium;

import notnekodev.barium.database.Database;
import notnekodev.barium.listener.JoinListener;
import notnekodev.barium.repository.AccountRepository;
import notnekodev.barium.repository.SQLiteAccountRepository;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public final class Barium extends JavaPlugin {

    private static Barium instance;
    private Database db;

    public Logger logger;

    @Override
    public void onEnable() {
        instance = this; // singleton shenanigans

        logger = LoggerFactory.getLogger("barium");
        logger.info("Barium enabled");

        saveDefaultConfig(); // copies the config.yml from the JAR into the server directory on first run

        String db_path = getConfig().getString("database.db_path");
        if (db_path == null) {
            logger.warn("Database path is null, using default data/database.db");
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

        AccountRepository accountRepository = new SQLiteAccountRepository(db);

        getServer().getPluginManager().registerEvents(
                new JoinListener(accountRepository),
                this
        );
    }

    @Override
    public void onDisable() {
        db.close();
        instance = null;
    }

    public static Barium getInstance() {
        return instance;
    }
}
