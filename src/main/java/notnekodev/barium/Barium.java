package notnekodev.barium;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Barium extends JavaPlugin {

    @Override
    public void onEnable() {
        Logger logger = LoggerFactory.getLogger("barium");
        logger.info("Barium enabled");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
