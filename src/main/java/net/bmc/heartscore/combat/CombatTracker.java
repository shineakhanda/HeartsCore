package net.bmc.heartscore.combat;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatTracker {

    private record CombatTag(UUID attackerId, String attackerName, long expiresAt) {
    }

    private final HeartsCore plugin;
    private final Map<UUID, CombatTag> tags = new ConcurrentHashMap<>();

    public CombatTracker(HeartsCore plugin) {
        this.plugin = plugin;
    }

    public void tag(Player victim, Player attacker) {
        if (victim.getUniqueId().equals(attacker.getUniqueId())) {
            return;
        }

        long expiresAt = System.currentTimeMillis()
                + (plugin.getConfigManager().combatTagSeconds() * 1000L);

        tags.put(
                victim.getUniqueId(),
                new CombatTag(attacker.getUniqueId(), attacker.getName(), expiresAt)
        );
    }

    public Optional<UUID> getAttacker(UUID victimId) {
        CombatTag tag = tags.get(victimId);
        if (tag == null) {
            return Optional.empty();
        }

        if (System.currentTimeMillis() > tag.expiresAt()) {
            tags.remove(victimId);
            return Optional.empty();
        }

        return Optional.of(tag.attackerId());
    }

    public boolean isTagged(Player player) {
        return getAttacker(player.getUniqueId()).isPresent();
    }

    public void clear(UUID victimId) {
        tags.remove(victimId);
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        tags.entrySet().removeIf(entry -> now > entry.getValue().expiresAt());
    }
}