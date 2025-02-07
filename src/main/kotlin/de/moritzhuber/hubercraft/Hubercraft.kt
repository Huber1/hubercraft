package de.moritzhuber.hubercraft

import de.moritzhuber.hubercraft.beaconFly.BeaconFlyListener
import de.moritzhuber.hubercraft.spawnIsland.SpawnIslandListener
import org.bukkit.plugin.java.JavaPlugin

class Hubercraft : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BeaconFlyListener(), this)
        pluginManager.registerEvents(SpawnIslandListener(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
