package com.gmail.nossr50.runnables.items;

import com.gmail.nossr50.datatypes.player.BukkitMMOPlayer;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.ChimaeraWing;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ChimaeraWingWarmup extends BukkitRunnable {
    private mcMMO pluginRef;
    private BukkitMMOPlayer mcMMOPlayer;

    public ChimaeraWingWarmup(mcMMO pluginRef, BukkitMMOPlayer mcMMOPlayer) {
        this.pluginRef = pluginRef;
        this.mcMMOPlayer = mcMMOPlayer;
    }

    @Override
    public void run() {
        checkChimaeraWingTeleport();
    }

    private void checkChimaeraWingTeleport() {
        Player player = mcMMOPlayer.getNative();
        Location previousLocation = mcMMOPlayer.getTeleportCommenceLocation();
        ChimaeraWing chimaeraWing = new ChimaeraWing(pluginRef, mcMMOPlayer);

        if (player.getLocation().distanceSquared(previousLocation) > 1.0 || !player.getInventory().containsAtLeast(pluginRef.getChimaeraWing(), 1)) {
            player.sendMessage(pluginRef.getLocaleManager().getString("Teleport.Cancelled"));
            mcMMOPlayer.setTeleportCommenceLocation(null);
            return;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();

        if (!pluginRef.getItemTools().isChimaeraWing(inHand) || inHand.getAmount() < pluginRef.getConfigManager().getConfigItems().getChimaeraWingUseCost()) {
            player.sendMessage(pluginRef.getLocaleManager().getString("Skills.NeedMore", pluginRef.getLocaleManager().getString("Item.ChimaeraWing.Name")));
            return;
        }

        long recentlyHurt = mcMMOPlayer.getRecentlyHurt();
        int hurtCooldown = pluginRef.getConfigManager().getConfigItems().getRecentlyHurtCooldown();

        if (hurtCooldown > 0) {
            int timeRemaining = pluginRef.getSkillTools().calculateTimeLeft(recentlyHurt * pluginRef.getMiscTools().TIME_CONVERSION_FACTOR, hurtCooldown, player);

            if (timeRemaining > 0) {
                player.sendMessage(pluginRef.getLocaleManager().getString("Item.Injured.Wait", timeRemaining));
                return;
            }
        }

        chimaeraWing.chimaeraExecuteTeleport();
    }
}
