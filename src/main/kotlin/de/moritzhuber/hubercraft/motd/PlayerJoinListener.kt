package de.moritzhuber.hubercraft.motd

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PlayerJoinListener(private val plugin: JavaPlugin) : Listener {
    private val mm = MiniMessage.miniMessage()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        CoroutineScope(Dispatchers.Default).launch {
            if (event.player.hasPlayedBefore())
                sendJoinMessage(event.player)
            else
                sendFirstTimeJoinMessage(event.player)
        }
    }

    private suspend fun sendFirstTimeJoinMessage(player: Player) {
        val file = File(plugin.dataFolder, "firstjoin.txt")

        if (!file.exists()) return

        val text: String = withContext(Dispatchers.IO) {
            file.readText()
        }

        val component = mm.deserialize(text)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            player.sendMessage(component)
        }, 5)
    }

    private suspend fun sendJoinMessage(player: Player) {
        val file = File(plugin.dataFolder, "joinmessage.txt")

        if (!file.exists()) return

        val text: String = withContext(Dispatchers.IO) {
            file.readText()
        }

        val component = mm.deserialize(
            text,
            Placeholder.component(
                "name",
                Component.text(player.name, NamedTextColor.DARK_RED)
            ),
            Placeholder.component(
                "players-online",
                Component.text(plugin.server.onlinePlayers.size, NamedTextColor.RED)
            )
        )

        plugin.server.scheduler.runTask(plugin, Runnable {
            player.sendMessage(component)
        })

        sendNewsMessage(player)
    }

    private fun sendNewsMessage(player: Player) {

    }
}