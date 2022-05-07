package net.cibmc.spigot.bbt;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Boat;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {
    ConcurrentHashMap<Boat, Boolean> isHighSpeedMode = new ConcurrentHashMap<Boat, Boolean>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        this.getLogger().info("Plugin initialize finished.");
    }

    @Override
    public void onDisable() {
        this.isHighSpeedMode.forEach((boat, bool) -> {
            boat.eject();
            boat.remove();
        });
        this.getLogger().info("Plugin has been disabled.");
    }
}
