package de.moritzhuber.hubercraft.devmode

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import de.moritzhuber.hubercraft.Hubercraft
import org.bukkit.block.SculkSensor
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockReceiveGameEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRecipeDiscoverEvent
import org.bukkit.event.player.PlayerStatisticIncrementEvent
import org.bukkit.event.raid.RaidTriggerEvent

class DevmodeListener : Listener {
    // Sculk Sensor is triggered
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onBlockReceiveGameEvent(event: BlockReceiveGameEvent) {
        if (
            event.block.state is SculkSensor
            && event.entity is Player
            && DevmodeService.isInDevmode(event.entity as Player)
        ) {
            event.isCancelled = true
        }
    }

    // Prevent xp orb targeting devmode player
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onEntityTargetLivingEntity(event: EntityTargetLivingEntityEvent) {
        Hubercraft.instance.logger.warning("ENTITY_TYPE: ${event.entityType}")
        Hubercraft.instance.logger.warning("TARGET: ${event.target}")
        Hubercraft.instance.logger.warning("TARGET_IS_PLAYER: ${event.target is Player}")

        if (
            event.entityType == EntityType.EXPERIENCE_ORB
            && event.target is Player
            && DevmodeService.isInDevmode(event.target as Player)
        ) {
            Hubercraft.instance.logger.warning("Cancelling Event")
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onPlayerAdvancementCriterionGrant(event: PlayerAdvancementCriterionGrantEvent) {
        if (DevmodeService.isInDevmode(event.player)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onPlayerPickupExperience(event: PlayerPickupExperienceEvent) {
        if (DevmodeService.isInDevmode(event.player)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onPlayerRecipeDiscover(event: PlayerRecipeDiscoverEvent) {
        if (DevmodeService.isInDevmode(event.player)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onPlayerStatisticIncrement(event: PlayerStatisticIncrementEvent) {
        if (DevmodeService.isInDevmode(event.player)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onRaidTrigger(event: RaidTriggerEvent) {
        if (DevmodeService.isInDevmode(event.player)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // restore and remove devmode
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onQuit(event: PlayerQuitEvent) {
        DevmodeService.deactivateDevmode(event.player)
    }
}
