package de.moritzhuber.hubercraft.spawnElytra

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

class SpawnElytraListener : Listener {
    val ITEM_NAME: String = "Einwegelytra"

    val LOCATION = Location(Bukkit.getWorld(NamespacedKey.minecraft("overworld")), 0.5, 175.0, 0.0)
    val DISTANCE = 6

    private val chestPlateData = mutableMapOf<UUID, ItemStack?>()

    @EventHandler
    fun onAdvancement(event: PlayerAdvancementCriterionGrantEvent) {
        val p = event.player

        p.sendMessage("CRITERION: ${event.criterion}")
        p.sendMessage("ADVANCEMENT: ${event.advancement.key}")

        if (event.advancement.key != NamespacedKey.minecraft("end/elytra")) return

        p.sendMessage("CORRECT KEY")

        val chestplate = p.inventory.chestplate

        if (chestplate?.isEmpty == false) {
            p.sendMessage("CHESTPLATE NOT EMPTY")
            p.sendMessage(chestplate.itemMeta.itemName().toString())
            if ((chestplate.itemMeta.itemName() as TextComponent).content() == ITEM_NAME) {
                p.sendMessage("CANCEL EVENT")
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val p = event.player

        if (LOCATION.world.key == p.location.world.key && LOCATION.distance(p.location) < DISTANCE) {
            giveElytra(p)
        }
    }

    @EventHandler
    fun onGliding(event: EntityToggleGlideEvent) {
        if (event.entityType != EntityType.PLAYER) return

        val p = event.entity as Player

        // Stopped Gliding
        if (!event.isGliding) removeElytra(p)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) = removeElytra(event.player)

    private fun giveElytra(p: Player) {
        val chestPlate = p.inventory.chestplate

        // Check if SpawnElytra is already given
        if (chestPlateData.containsKey(p.uniqueId)) return

        chestPlateData[p.uniqueId] = chestPlate

        p.inventory.chestplate = getElytra()
        p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F)
    }

    private fun removeElytra(p: Player) {
        val chestPlate = p.inventory.chestplate
        if (chestPlate == null || (chestPlate.itemMeta.itemName() as TextComponent).content() != ITEM_NAME) return

        val previous = chestPlateData[p.uniqueId]

        p.inventory.chestplate = previous
        p.playSound(p, Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F)
        chestPlateData.remove(p.uniqueId)
    }

    private fun getElytra(): ItemStack {
        val elytra = ItemStack(Material.ELYTRA)
        val meta = elytra.itemMeta

        meta.displayName(Component.text("DisplayName"))
        meta.isUnbreakable = true
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        meta.lore(listOf(Component.text("Verschwindet nach der ersten Landung")))
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta.itemName(Component.text(ITEM_NAME))

        elytra.itemMeta = meta

        return elytra
    }
}
