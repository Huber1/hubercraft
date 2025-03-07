package de.moritzhuber.hubercraft.devmode

import de.moritzhuber.hubercraft.helper.serializer.ItemStackSerializer
import de.moritzhuber.hubercraft.helper.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import org.bukkit.GameMode
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import java.util.*

data class SavedPlayerData(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val gameMode: GameMode,
    val inventory: List<@Serializable(with = ItemStackSerializer::class) ItemStack?>,
    val level: Int,
    val exp: Float,
    val health: Double,
    val foodLevel: Int,
    val isFlying: Boolean,
    val potionEffects: Collection<PotionEffect>,
)
