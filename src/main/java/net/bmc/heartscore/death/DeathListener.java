package net.bmc.heartscore.death;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DeathListener implements Listener {

    private final HeartsCore plugin;

    public DeathListener(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        UUID victimId = victim.getUniqueId();

        Player directKiller = victim.getKiller();
        UUID attackerId = resolveAttackerId(victim, directKiller);
        Player onlineAttacker = attackerId == null ? null : Bukkit.getPlayer(attackerId);

        boolean pvp = attackerId != null && !attackerId.equals(victimId);
        boolean allowed = pvp && (onlineAttacker != null
                ? plugin.getAntiFarmService().canSteal(onlineAttacker, victim)
                : plugin.getAntiFarmService().canSteal(attackerId, victimId));

        int current = plugin.getHeartsService().getHearts(victim);
        int min = plugin.getConfigManager().minHearts();

        int loss;
        if (pvp) {
            loss = allowed
                    ? Math.min(plugin.getConfigManager().pvpHeartLoss(), Math.max(0, current - min))
                    : 0;
        } else {
            loss = Math.min(
                    plugin.getConfigManager().naturalDeathHeartLoss(),
                    Math.max(0, current - min)
            );
        }

        int newHearts = current - loss;
        plugin.getDataStore().setHearts(victimId, newHearts);

        plugin.getDataStore().getOrCreate(victimId).incrementDeaths();

        if (pvp && !allowed) {
            plugin.getMessages().send(victim, "anti-farm-blocked");
        } else if (pvp && loss > 0) {
            handlePvpDeath(event, victim, attackerId, onlineAttacker, loss);
        } else if (!pvp && loss > 0) {
            plugin.getMessages().send(victim, "natural-death", Map.of(
                    "amount", String.valueOf(loss)
            ));
        }

        plugin.getCombatTracker().clear(victimId);

        if (newHearts <= 0) {
            victim.getScheduler().runDelayed(
                    plugin,
                    task -> {
                        if (victim.isOnline()) {
                            plugin.getHeartsService().handleZeroIfNeeded(victim);
                        }
                    },
                    null,
                    40L
            );
        }
    }

    private UUID resolveAttackerId(Player victim, Player directKiller) {
        if (directKiller != null) {
            return directKiller.getUniqueId();
        }

        Optional<UUID> combatAttacker = plugin.getCombatTracker().getAttacker(victim.getUniqueId());
        if (combatAttacker.isPresent()) {
            return combatAttacker.get();
        }

        return plugin.getTrapTracker().findOwner(victim.getLocation()).orElse(null);
    }

    private void handlePvpDeath(
            PlayerDeathEvent event,
            Player victim,
            UUID attackerId,
            Player onlineAttacker,
            int loss
    ) {
        plugin.getAntiFarmService().recordKill(attackerId, victim.getUniqueId());
        plugin.getDataStore().getOrCreate(attackerId).incrementKills();

        if (plugin.getConfigManager().pvpDropMode()) {
            event.getDrops().addAll(plugin.getHeartItemFactory().createMany(loss));
        } else {
            int gain = plugin.getConfigManager().killerGain();
            int gained = plugin.getHeartsService().addHearts(attackerId, gain);

            if (gained < gain) {
                event.getDrops().addAll(
                        plugin.getHeartItemFactory().createMany(gain - gained)
                );
            }

            if (onlineAttacker != null) {
                onlineAttacker.getScheduler().run(plugin, task -> {
                    plugin.getHeartsService().applyHealth(onlineAttacker);
                }, null);
            }
        }

        if (onlineAttacker != null) {
            String key = plugin.getConfigManager().pvpDropMode()
                    ? "killer-drop"
                    : "killer-stole";

            onlineAttacker.getScheduler().run(plugin, task -> {
                plugin.getMessages().send(onlineAttacker, key, Map.of(
                        "amount", String.valueOf(loss),
                        "player", victim.getName()
                ));
            }, null);
        }

        plugin.getMessages().send(victim, "pvp-death", Map.of(
                "player", onlineAttacker != null ? onlineAttacker.getName() : "another player",
                "amount", String.valueOf(loss)
        ));
    }
}