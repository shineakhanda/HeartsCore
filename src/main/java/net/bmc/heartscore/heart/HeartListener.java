package net.bmc.heartscore.heart;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.util.Text;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class HeartListener implements Listener {

    private final HeartsCore plugin;

    public HeartListener(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.getScheduler().runDelayed(plugin, task -> {
            if (!player.isOnline()) {
                return;
            }
            plugin.getHeartsService().applyHealth(player);
            plugin.getHeartsService().handleZeroIfNeeded(player);
        }, null, 2L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        player.getScheduler().runDelayed(plugin, task -> {
            if (!player.isOnline()) {
                return;
            }
            plugin.getHeartsService().applyHealth(player);
            plugin.getHeartsService().handleZeroIfNeeded(player);
        }, null, 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!plugin.getHeartItemFactory().isHeart(item)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        player.getScheduler().run(plugin, task -> {
            if (!player.isOnline()) {
                return;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (!plugin.getHeartItemFactory().isHeart(inHand)) {
                return;
            }

            int hearts = plugin.getHeartsService().getHearts(player);
            int max = plugin.getConfigManager().maxHearts();

            if (hearts >= max) {
                plugin.getMessages().send(player, "max-hearts");
                return;
            }

            inHand.setAmount(inHand.getAmount() - 1);
            plugin.getDataStore().setHearts(player.getUniqueId(), hearts + 1);
            plugin.getHeartsService().applyHealth(player);

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            player.getWorld().spawnParticle(
                    Particle.HEART,
                    player.getLocation().add(0, 1.0, 0),
                    8,
                    0.5, 0.5, 0.5, 0.01
            );

            plugin.getMessages().send(player, "consumed", Map.of(
                    "hearts", String.valueOf(hearts + 1)
            ));
        }, null);
    }
}