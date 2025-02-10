package de.moritzhuber.hubercraft

import de.moritzhuber.hubercraft.beaconFly.BeaconFlyListener
import de.moritzhuber.hubercraft.spawnIsland.SavedChestplates
import de.moritzhuber.hubercraft.spawnIsland.SpawnIslandListener
import org.bukkit.plugin.java.JavaPlugin

class Hubercraft : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        restoreSavedData()

        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BeaconFlyListener(), this)
        pluginManager.registerEvents(SpawnIslandListener(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun restoreSavedData() {
        logger.info("Restoring saved data...")
        SavedChestplates.loadFromDisk()
    }
}
