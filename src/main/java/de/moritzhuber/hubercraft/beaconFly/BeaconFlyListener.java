package de.moritzhuber.hubercraft.beaconFly;


import de.moritzhuber.hubercraft.Tuple;
import org.bukkit.Location;
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
        for (int i = 0; i < 3; i++) {
            Block block = location.subtract(0, i, 0).getBlock();
            if (block.getState() instanceof Beacon) {
                Beacon beacon = (Beacon) block.getState();
                int tier = beacon.getTier();

                if (tier < 1) return;

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

        if (p.hasPotionEffect(PotionEffectType.LEVITATION))
            p.removePotionEffect(PotionEffectType.LEVITATION);

        PotionEffect levitation = new PotionEffect(PotionEffectType.LEVITATION, 20 * stats.$1, stats.$2, false, false);
        p.addPotionEffect(levitation);
    }
}
