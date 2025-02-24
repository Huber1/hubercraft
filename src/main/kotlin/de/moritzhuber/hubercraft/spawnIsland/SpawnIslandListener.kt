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

    private val ISLAND_LOCATION = Location(Bukkit.getWorld(NamespacedKey.minecraft("overworld")), 0.5, 175.0, 0.5)
    private val ISLAND_ELYTRA_DISTANCE = 6

    private val BOTTOM_LOCATION = Location(Bukkit.getWorld(NamespacedKey.minecraft("overworld")), 0.5, 68.0, 0.5)
    private val BOTTOM_LEVITATE_DISTANCE = 0.5
    private val BOTTOM_LEVITATE_DURATION = 93L // 4.75 seconds

    private val preventLevitateAchievementPlayers = mutableListOf<UUID>()

    val savedChestplates = SavedChestplates(plugin)

    @EventHandler
    fun onAdvancement(event: PlayerAdvancementCriterionGrantEvent) {
        val p = event.player

        if (event.criterion !in listOf("levitated", "elytra")) return

        when (event.criterion) {
            "levitated" -> if (preventLevitateAchievementPlayers.contains(p.uniqueId)) {
                event.isCancelled = true
            }

            "elytra" -> {
                val chestplate = p.inventory.chestplate

                if (chestplate?.isEmpty == false) {
                    if ((chestplate.itemMeta.itemName() as TextComponent).content() == ITEM_NAME) {
                        event.isCancelled = true
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val p = event.player

        if (ISLAND_LOCATION.world.key != p.world.key) return

        if (BOTTOM_LOCATION.distance(p.location) < BOTTOM_LEVITATE_DISTANCE) levitateToIsland(p)
        else if (ISLAND_LOCATION.distance(p.location) < ISLAND_ELYTRA_DISTANCE) {
            plugin.logger.info("Detected Player ${p.name} on Spawn Island")
            giveElytra(p)
        }

    }

    @EventHandler
    fun onGliding(event: EntityToggleGlideEvent) {
        if (event.entityType != EntityType.PLAYER) return

        val p = event.entity as Player

        // Stopped Gliding
        if (!event.isGliding) {
            plugin.logger.info("Player ${p.name} stopped gliding. Removing Elytra")
            removeElytra(p)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        plugin.logger.info("Player ${event.player.name} died. Removing Elytra and Levitation Achievement Blocker")
        val p = event.player
        preventLevitateAchievementPlayers.remove(p.uniqueId)
        removeElytra(p)
    }

    private fun giveElytra(p: Player) {
        val chestPlate = p.inventory.chestplate

        // Check if SpawnElytra is already given
        if (chestPlate == null || (chestPlate.itemMeta.itemName() as TextComponent).content() != ITEM_NAME) {
            if (chestPlate == null) {
                plugin.logger.info("Giving an Elytra to Player ${p.name}")
                p.inventory.chestplate = getElytra()
            } else {
                plugin.logger.info("Giving an Elytra to  Player ${p.name} replacing ${(chestPlate.itemMeta.displayName() as TextComponent).content()}")
                savedChestplates.save(p.uniqueId, ItemStack(chestPlate))
                p.inventory.chestplate = getElytra()
            }
            p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F)
        }
    }

    private fun removeElytra(p: Player) {
        val chestPlate = p.inventory.chestplate
        if (chestPlate == null || (chestPlate.itemMeta.itemName() as TextComponent).content() != ITEM_NAME) return

        val previous = savedChestplates.remove(p.uniqueId)

        p.inventory.chestplate = previous
        p.playSound(p, Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F)
    }

    private fun getElytra(): ItemStack {
        val elytra = ItemStack(Material.ELYTRA)
        val meta = elytra.itemMeta

        meta.displayName(Component.text(ITEM_NAME))
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
        plugin.logger.info("Levitating player ${p.name} to Spawn Island")
        p.removePotionEffect(PotionEffectType.LEVITATION)
        val levitation = PotionEffect(PotionEffectType.LEVITATION, 20 * 5, 25, false, false)
        p.addPotionEffect(levitation)
        preventLevitateAchievementPlayers.add(p.uniqueId)

        val scheduler = plugin.server.scheduler
        scheduler.runTaskLater(plugin, Runnable {
            p.removePotionEffect(PotionEffectType.LEVITATION)
            p.teleport(ISLAND_LOCATION.apply {
                direction = p.location.direction
            })
            preventLevitateAchievementPlayers.remove(p.uniqueId)
        }, BOTTOM_LEVITATE_DURATION)
    }
}
