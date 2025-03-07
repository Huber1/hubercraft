package de.moritzhuber.hubercraft

import de.moritzhuber.hubercraft.beaconFly.BeaconFlyListener
import de.moritzhuber.hubercraft.commands.devmodeCommand
import de.moritzhuber.hubercraft.commands.mapCommand
import de.moritzhuber.hubercraft.devmode.DevmodeListener
import de.moritzhuber.hubercraft.spawnIsland.SpawnIslandListener
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

class Hubercraft : JavaPlugin() {

    companion object {
        lateinit var instance: Hubercraft
            private set
    }

    private lateinit var spawnIslandListener: SpawnIslandListener

    override fun onEnable() {
        logger.info("Starting up")
        instance = this
        spawnIslandListener = SpawnIslandListener(this)

        registerEvents()
        registerCommands()
    }

    private fun registerEvents() {
        val pluginManager = server.pluginManager
        pluginManager.registerEvents(BeaconFlyListener(), this)
        pluginManager.registerEvents(spawnIslandListener, this)
        pluginManager.registerEvents(DevmodeListener(), this)
    }

    @Suppress("UnstableApiUsage")
    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(mapCommand())
            commands.registrar().register(devmodeCommand())
        }
    }

    override fun onDisable() = runBlocking {
        logger.info("Shutting down")
        spawnIslandListener.savedInventorySlots.persistToDisk()
    }
}
