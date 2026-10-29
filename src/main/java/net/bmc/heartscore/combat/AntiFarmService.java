package net.bmc.heartscore.combat;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AntiFarmService {

    private final HeartsCore plugin;
    private final Map<UUID, Map<UUID, Long>> lastKills = new ConcurrentHashMap<>();

    public AntiFarmService(HeartsCore plugin) {
        this.plugin = plugin;
    }

    public boolean canSteal(Player killer, Player victim) {
        if (!plugin.getConfigManager().antiFarmEnabled()) {
            return true;
        }

        if (killer.hasPermission("heartscore.bypass.antifarm")) {
            return true;
        }

        if (killer.getUniqueId().equals(victim.getUniqueId())) {
            return false;
        }

        if (plugin.getConfigManager().blockSameIp() && sameIp(killer, victim)) {
            return false;
        }

        return canSteal(killer.getUniqueId(), victim.getUniqueId());
    }

    public boolean canSteal(UUID killerId, UUID victimId) {
        if (!plugin.getConfigManager().antiFarmEnabled()) {
            return true;
        }

        long cooldown = plugin.getConfigManager().killCooldownSeconds() * 1000L;
        if (cooldown <= 0) {
            return true;
        }

        Map<UUID, Long> victims = lastKills.get(killerId);
        if (victims == null) {
            return true;
        }

        Long last = victims.get(victimId);
        return last == null || System.currentTimeMillis() - last >= cooldown;
    }

    public void recordKill(UUID killerId, UUID victimId) {
        lastKills.computeIfAbsent(killerId, ignored -> new ConcurrentHashMap<>())
                .put(victimId, System.currentTimeMillis());
    }

    public void cleanup() {
        long cooldown = plugin.getConfigManager().killCooldownSeconds() * 1000L;
        if (cooldown <= 0) {
            lastKills.clear();
            return;
        }

        long now = System.currentTimeMillis();
        for (Map<UUID, Long> victims : lastKills.values()) {
            victims.entrySet().removeIf(entry -> now - entry.getValue() > cooldown * 2);
        }
        lastKills.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private boolean sameIp(Player first, Player second) {
        InetSocketAddress firstAddress = first.getAddress();
        InetSocketAddress secondAddress = second.getAddress();

        if (firstAddress == null || secondAddress == null) {
            return false;
        }

        if (firstAddress.getAddress() == null || secondAddress.getAddress() == null) {
            return false;
        }

        return firstAddress.getAddress().getHostAddress()
                .equals(secondAddress.getAddress().getHostAddress());
    }
}