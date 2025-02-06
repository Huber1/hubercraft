package de.moritzhuber.hubercraft.beaconFly;


import de.moritzhuber.hubercraft.Tuple;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BeaconFlyListener implements Listener {
    @EventHandler
    public void onPlayerMovement(final PlayerMoveEvent event) {
        Player p = event.getPlayer();

        Location location = p.getLocation();
        location.setY(Math.ceil(location.getY())); // ceil to account for semi-height blocks

        for (int i = 1; i <= 2; i++) {
            // for Some reason location.subtract() doesn't work
            Location location1 = new Location(location.getWorld(), location.getX(), location.getY() - i, location.getZ());
            Block block = location1.getBlock();
            if (block.getState() instanceof Beacon) {
                Beacon beacon = (Beacon) block.getState();
                int tier = beacon.getTier();

                if (tier < 1) return;
                // Only work if elytra is equipped
                if (p.getInventory().getChestplate() == null || p.getInventory().getChestplate().getType() != Material.ELYTRA)
                    return;

                giveEffect(p, tier);
                return;
            }
        }
    }

    private void giveEffect(Player p, int tier) {
        // Seconds, Amplifier
        Tuple<Integer, Integer> stats = switch (tier) {
            case 1 -> new Tuple<>(5, 5);
            case 2 -> new Tuple<>(5, 10);
            case 3 -> new Tuple<>(5, 15);
            case 4 -> new Tuple<>(5, 28);
            default -> null;
        };

        if (stats == null) return;

        if (p.hasPotionEffect(PotionEffectType.LEVITATION)) p.removePotionEffect(PotionEffectType.LEVITATION);

        PotionEffect levitation = new PotionEffect(PotionEffectType.LEVITATION, 20 * stats.$1, stats.$2, false, false);
        p.addPotionEffect(levitation);
    }
}
