package de.moritzhuber.hubercraft.devmode

import de.moritzhuber.hubercraft.DEV_MODE
import de.moritzhuber.hubercraft.Hubercraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

object DevmodeService {
    private val playerData: MutableMap<UUID, SavedPlayerData> = mutableMapOf()

    fun isInDevmode(player: Player): Boolean =
        player.persistentDataContainer.getOrDefault(
            NamespacedKey(Hubercraft.instance, DEV_MODE),
            PersistentDataType.BOOLEAN,
            false
        )

    fun toggleDevmode(player: Player) = when {
        isInDevmode(player) -> deactivateDevmode(player)
        else -> activateDevmode(player)
    }

    private fun activateDevmode(player: Player) {
        // Store EssentialsX money

        val savedPlayerData = SavedPlayerData(
            uuid = player.uniqueId,
            gameMode = player.gameMode,
            inventory = player.inventory.contents.map { itemStack ->
                if (itemStack != null) ItemStack(itemStack) else null
            },
            level = player.level,
            exp = player.exp,
            health = player.health,
            foodLevel = player.foodLevel,
            isFlying = player.isFlying,
            potionEffects = player.activePotionEffects,
        )

        playerData[player.uniqueId] = savedPlayerData

        player.persistentDataContainer.set(
            NamespacedKey(Hubercraft.instance, DEV_MODE),
            PersistentDataType.BOOLEAN,
            true,
        )

        player.inventory.clear()
        player.gameMode = GameMode.CREATIVE

        player.sendMessage(
            Component
                .text("Devmode wurde ", NamedTextColor.GOLD)
                .append(Component.text("aktiviert", NamedTextColor.RED))
        )

    }

    fun deactivateDevmode(player: Player) {
        val savedData = playerData[player.uniqueId]
        if (savedData != null) {

            player.gameMode = savedData.gameMode
            player.inventory.contents = savedData.inventory.toTypedArray()
            player.level = savedData.level
            player.exp = savedData.exp
            player.health = savedData.health
            player.foodLevel = savedData.foodLevel
            player.isFlying = savedData.isFlying

            // PotionEffects
            player.clearActivePotionEffects()
            player.addPotionEffects(savedData.potionEffects)
        }

        player.persistentDataContainer.remove(NamespacedKey(Hubercraft.instance, DEV_MODE))
        playerData.remove(player.uniqueId)

        player.sendMessage(
            Component
                .text("Devmode wurde ", NamedTextColor.GOLD)
                .append(Component.text("deaktiviert", NamedTextColor.RED))
        )
    }
}
