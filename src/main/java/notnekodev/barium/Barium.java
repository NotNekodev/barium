package notnekodev.barium;

import notnekodev.barium.database.Database;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public final class Barium extends JavaPlugin {

    private static Barium instance;
    public Logger logger;
    private Database db;

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
