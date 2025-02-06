package de.moritzhuber.hubercraft.beaconFly

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Beacon
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ceil

class BeaconFlyListener : Listener {
    @EventHandler
    fun onPlayerMovement(event: PlayerMoveEvent) {
        val p = event.player

        val location = p.location
        location.y = ceil(location.y) // ceil to account for semi-height blocks

        for (i in 1..2) {
            // for Some reason location.subtract() doesn't work
            val location1 = Location(location.world, location.x, location.y - i, location.z)
            val block = location1.block
            if (block.state is Beacon) {
                val beacon = block.state as Beacon
                val tier = beacon.tier

                if (tier < 1) return

                // Only work if elytra is equipped
                if (p.inventory.chestplate == null || p.inventory.chestplate!!.type != Material.ELYTRA) return

                giveEffect(p, tier)
                return
            }
        }
    }

    private fun giveEffect(p: Player, tier: Int) {
        // Seconds, Amplifier
        val stats = when (tier) {
            1 -> Pair(5, 5)
            2 -> Pair(5, 10)
            3 -> Pair(5, 15)
            4 -> Pair(5, 28)
            else -> null
        }

        if (stats == null) return

        if (p.hasPotionEffect(PotionEffectType.LEVITATION)) p.removePotionEffect(PotionEffectType.LEVITATION)

        val levitation = PotionEffect(PotionEffectType.LEVITATION, 20 * stats.first, stats.second, false, false)
        p.addPotionEffect(levitation)
    }
}
