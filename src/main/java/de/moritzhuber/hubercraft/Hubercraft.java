package de.moritzhuber.hubercraft;

import de.moritzhuber.hubercraft.beaconFly.BeaconFlyListener;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Hubercraft extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new BeaconFlyListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
