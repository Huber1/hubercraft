package de.moritzhuber.hubercraft.spawnIsland

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class SpawnIslandListener(private val plugin: JavaPlugin) : Listener {
    private val ELYTRA_KEY = NamespacedKey(plugin, "einwegelytra")
    private val BOOSTER_KEY = NamespacedKey(plugin, "elytraboost")
    private val BOOSTER_SLOT = 4

    private val ISLAND_LOCATION = Location(Bukkit.getWorld(NamespacedKey.minecraft("overworld")), 0.5, 175.0, 0.5)
    private val ISLAND_ELYTRA_DISTANCE = 6

    private val BOTTOM_LOCATION = Location(Bukkit.getWorld(NamespacedKey.minecraft("overworld")), 0.5, 68.0, 0.5)
    private val BOTTOM_LEVITATE_DISTANCE = 0.5
    private val BOTTOM_LEVITATE_DURATION = 93L // 4.75 seconds

    private val preventLevitateAchievementPlayers = mutableListOf<UUID>()

    val savedInventorySlots = SavedInventorySlots(plugin)

    /**
     * Prevent Advancements because of the spawn area
     */
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

                if (chestplate?.isEmpty == false && isElytra(chestplate)) {
                    event.isCancelled = true
                }
            }
        }
    }

    /**
     * Listen to Player Moves to check if player is in spawn Area
     */
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val p = event.player

        if (ISLAND_LOCATION.world.key != p.world.key) return

        if (BOTTOM_LOCATION.distance(p.location) < BOTTOM_LEVITATE_DISTANCE) levitateToIsland(p)
        else if (ISLAND_LOCATION.distance(p.location) < ISLAND_ELYTRA_DISTANCE) {
            giveSpawnItems(p)
        }

    }

    /**
     * Detect if a player lands
     */
    @EventHandler
    fun onGliding(event: EntityToggleGlideEvent) {
        if (event.entityType != EntityType.PLAYER) return

        val p = event.entity as Player

        // Stopped Gliding
        if (!event.isGliding) {
            plugin.logger.info("Player ${p.name} stopped gliding. Removing spawn items")
            removeSpawnItems(p)
        }
    }

    /**
     * Remove spawn items on death
     */
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        plugin.logger.info("Player ${event.player.name} died. Removing Elytra and Levitation Achievement Blocker")
        val p = event.player
        preventLevitateAchievementPlayers.remove(p.uniqueId)
        removeSpawnItems(p)
    }

    /**
     * Prevent dropping the Booster
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDrop(event: PlayerDropItemEvent) {
        if (isBooster(event.itemDrop.itemStack) || isElytra(event.itemDrop.itemStack)) {
            event.isCancelled = true
        }
    }

    /**
     * Prevent moving elytra and booster in inventory
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryMove(event: InventoryClickEvent) {
        val item = event.currentItem ?: return
        if (isBooster(item) || isElytra(item)) event.isCancelled = true
    }

    /**
     * Boost player
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInteract(event: PlayerInteractEvent) {
        val p = event.player
        if (
            event.item != null
            && isBooster(event.item!!)
            && p.isGliding
            && event.action == Action.RIGHT_CLICK_AIR
        ) {
            val firework = ItemStack(Material.FIREWORK_ROCKET)
            val meta = firework.itemMeta as FireworkMeta
            meta.power = 5
            firework.itemMeta = meta
            p.fireworkBoost(firework)
            plugin.server.scheduler.runTask(plugin, Runnable {
                val previousBooster = savedInventorySlots.removeBooster(p.uniqueId)
                p.inventory.setItem(BOOSTER_SLOT, previousBooster)
            })
        }
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

    private fun giveSpawnItems(p: Player) {
        val chestPlate = p.inventory.chestplate
        val boosterSlot = p.inventory.getItem(BOOSTER_SLOT)

        // Elytra
        if (chestPlate == null || !isElytra(chestPlate)) {
            if (chestPlate == null) {
                plugin.logger.info("Giving an Elytra to Player ${p.name}")
            } else {
                plugin.logger.info("Giving an Elytra to  Player ${p.name} replacing ${chestPlate.itemMeta.javaClass.simpleName}")
                savedInventorySlots.saveChestplate(p.uniqueId, ItemStack(chestPlate))
            }
            p.inventory.chestplate = getElytra()
            p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 2.0F)
        }

        // Booster
        if (boosterSlot == null || !isBooster(boosterSlot)) {
            if (boosterSlot == null) {
                plugin.logger.info("Giving Booster to Player ${p.name}")
            } else {
                plugin.logger.info("Giving Booster to  Player ${p.name} replacing ${boosterSlot.itemMeta.javaClass.simpleName}")
                savedInventorySlots.saveBooster(p.uniqueId, ItemStack(boosterSlot))
            }
            p.inventory.setItem(BOOSTER_SLOT, getBooster())
            p.inventory.heldItemSlot = BOOSTER_SLOT
        }
    }

    private fun removeSpawnItems(p: Player) {
        val chestPlate = p.inventory.chestplate
        val booster = p.inventory.getItem(BOOSTER_SLOT)
        if (chestPlate == null && booster == null) return

        // Elytra
        if (chestPlate != null && isElytra(chestPlate)) {
            val previous = savedInventorySlots.removeChestplate(p.uniqueId)
            p.inventory.chestplate = previous
            p.playSound(p, Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 2.0F)
        }

        // Booster
        if (booster != null && isBooster(booster)) {
            val previous = savedInventorySlots.removeBooster(p.uniqueId)
            p.inventory.setItem(BOOSTER_SLOT, previous)
        }
    }

    private fun getElytra(): ItemStack {
        val elytra = ItemStack(Material.ELYTRA)

        elytra.editMeta { meta ->
            meta.itemName(Component.text("Einwegelytra"))
            meta.customName(Component.text("Einwegelytra"))
            meta.isUnbreakable = true
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
            meta.lore(listOf(Component.text("Verschwindet nach der ersten Landung")))
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

            meta.persistentDataContainer.set(ELYTRA_KEY, PersistentDataType.BOOLEAN, true)
        }

        return elytra
    }

    private fun getBooster(): ItemStack = ItemStack(Material.FEATHER).let {
        it.editMeta { meta ->
            meta.itemName(Component.text("Booster"))
            meta.customName(Component.text("Booster"))
            meta.lore(
                listOf(
                    Component.text("Gibt nen ordentlichen Boost"),
                    Component.text("Keine Sorge, du kriegst dein Item zurück")
                )
            )
            meta.setRarity(ItemRarity.EPIC)
            meta.setEnchantmentGlintOverride(true)

            meta.persistentDataContainer.set(BOOSTER_KEY, PersistentDataType.BOOLEAN, true)
        }

        it
    }

    private fun isElytra(itemStack: ItemStack): Boolean =
        itemStack.persistentDataContainer.getOrDefault(ELYTRA_KEY, PersistentDataType.BOOLEAN, false)

    private fun isBooster(itemStack: ItemStack): Boolean =
        itemStack.persistentDataContainer.getOrDefault(BOOSTER_KEY, PersistentDataType.BOOLEAN, false)
}
