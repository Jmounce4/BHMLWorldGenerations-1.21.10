package me.bhml.bhmlworldgenerations;

import me.bhml.bhmlworldgenerations.Listeners.WorldGenerations;
import me.bhml.bhmlworldgenerations.Listeners.WorldLoot;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getServer;

public final class BHMLWorldGenerations extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new WorldGenerations(), this);
        this.getServer().getPluginManager().registerEvents(new WorldLoot(), this);
        this.getServer().getLogger().info("BHML's World Generations enabled successfully!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
