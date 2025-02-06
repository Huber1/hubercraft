package de.moritzhuber.hubercraft

import de.moritzhuber.hubercraft.beaconFly.BeaconFlyListener
import org.bukkit.plugin.java.JavaPlugin

class Hubercraft : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        server.pluginManager.registerEvents(BeaconFlyListener(), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
