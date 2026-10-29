package net.bmc.heartscore.combat;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public final class CombatListener implements Listener {

    private final HeartsCore plugin;

    public CombatListener(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        plugin.getCombatTracker().tag(victim, attacker);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!plugin.getConfigManager().combatLogPunish()) {
            return;
        }

        Player player = event.getPlayer();
        if (!plugin.getCombatTracker().isTagged(player)) {
            return;
        }

        int current = plugin.getHeartsService().getHearts(player);
        int min = plugin.getConfigManager().minHearts();
        int loss = Math.min(
                plugin.getConfigManager().combatLogHeartLoss(),
                Math.max(0, current - min)
        );

        if (loss > 0) {
            plugin.getHeartsService().setHearts(player.getUniqueId(), current - loss);

            if (plugin.getConfigManager().combatLogDropHeart()) {
                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        plugin.getHeartItemFactory().create(loss)
                );
            }

            plugin.getMessages().send(player, "combat-log-punished");
        }

        plugin.getCombatTracker().clear(player.getUniqueId());
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
            return player;
        }

        if (damager instanceof TNTPrimed tnt
                && tnt.getSource() instanceof Player player) {
            return player;
        }

        if (damager instanceof AreaEffectCloud cloud
                && cloud.getSource() instanceof Player player) {
            return player;
        }

        if (damager instanceof Tameable tameable
                && tameable.getOwner() instanceof Player player) {
            return player;
        }

        return null;
    }
}