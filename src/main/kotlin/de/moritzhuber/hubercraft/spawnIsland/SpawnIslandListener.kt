package de.moritzhuber.hubercraft.spawnIsland

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
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class SpawnIslandListener(val plugin: JavaPlugin) : Listener {
    private val ITEM_NAME: String = "Einwegelytra"

    private val ISLAND_LOCATION = Location(Bukkit.getWorld(NamespacedKey.minecraft("overworld")), 0.5, 175.0, 0.0)
    private val ISLAND_ELYTRA_DISTANCE = 6

    private val BOTTOM_LOCATION = Location(Bukkit.getWorld(NamespacedKey.minecraft("overworld")), 0.5, 68.0, 0.0)
    private val BOTTOM_LEVITATE_DISTANCE = 0.5
    private val BOTTOM_LEVITATE_DURATION = 95L // 4.75 seconds

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

        if (ISLAND_LOCATION.world.key != p.world.key) return

        if (BOTTOM_LOCATION.distance(p.location) < BOTTOM_LEVITATE_DISTANCE) levitateToIsland(p)
        else if (ISLAND_LOCATION.distance(p.location) < ISLAND_ELYTRA_DISTANCE) giveElytra(p)

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

    private fun levitateToIsland(p: Player) {
        p.removePotionEffect(PotionEffectType.LEVITATION)
        val levitation = PotionEffect(PotionEffectType.LEVITATION, 20 * 5, 25, false, false)
        p.addPotionEffect(levitation)

        val scheduler = plugin.server.scheduler
        scheduler.runTaskLater(plugin, Runnable {
            p.removePotionEffect(PotionEffectType.LEVITATION)
            p.teleport(ISLAND_LOCATION.apply {
                direction = p.location.direction
            })
        }, BOTTOM_LEVITATE_DURATION)
    }
}
