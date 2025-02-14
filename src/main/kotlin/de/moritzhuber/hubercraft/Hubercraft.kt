package de.moritzhuber.hubercraft

import de.moritzhuber.hubercraft.beaconFly.BeaconFlyListener
import de.moritzhuber.hubercraft.commands.mapCommand
import de.moritzhuber.hubercraft.spawnIsland.SavedChestplates
import de.moritzhuber.hubercraft.spawnIsland.SpawnIslandListener
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Hubercraft : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        restoreSavedData()

        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BeaconFlyListener(), this)
        pluginManager.registerEvents(SpawnIslandListener(this), this)

        @Suppress("UnstableApiUsage")
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(mapCommand())
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun restoreSavedData() {
        logger.info("Restoring saved data...")
        SavedChestplates.loadFromDisk()
    }
}
