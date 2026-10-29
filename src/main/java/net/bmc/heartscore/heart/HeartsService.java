package net.bmc.heartscore.heart;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.data.PlayerData;
import net.bmc.heartscore.util.Text;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HeartsService {

    private final HeartsCore plugin;
    private final Set<UUID> eliminated = ConcurrentHashMap.newKeySet();

    public HeartsService(HeartsCore plugin) {
        this.plugin = plugin;
    }

    public int getHearts(Player player) {
        return getHearts(player.getUniqueId());
    }

    public int getHearts(UUID uuid) {
        return plugin.getDataStore().getHearts(uuid);
    }

    public void setHearts(Player player, int amount) {
        int clamped = clamp(amount);
        plugin.getDataStore().setHearts(player.getUniqueId(), clamped);
        applyHealth(player);

        if (clamped <= 0) {
            handleZero(player);
        } else {
            eliminated.remove(player.getUniqueId());
            restoreGameMode(player);
        }
    }

    public void setHearts(UUID uuid, int amount) {
        plugin.getDataStore().setHearts(uuid, clamp(amount));
    }

    public int addHearts(Player player, int amount) {
        int added = addHearts(player.getUniqueId(), amount);
        applyHealth(player);

        if (getHearts(player) <= 0) {
            handleZero(player);
        } else {
            eliminated.remove(player.getUniqueId());
            restoreGameMode(player);
        }

        return added;
    }

    public int addHearts(UUID uuid, int amount) {
        int current = getHearts(uuid);
        int next = clamp(current + Math.max(0, amount));
        plugin.getDataStore().setHearts(uuid, next);
        return next - current;
    }

    public int removeHearts(Player player, int amount) {
        int removed = removeHearts(player.getUniqueId(), amount);
        applyHealth(player);

        if (getHearts(player) <= 0) {
            handleZero(player);
        }

        return removed;
    }

    public int removeHearts(UUID uuid, int amount) {
        int current = getHearts(uuid);
        int next = clamp(current - Math.max(0, amount));
        plugin.getDataStore().setHearts(uuid, next);
        return current - next;
    }

    public void withdraw(Player player, int amount) {
        if (amount <= 0) {
            plugin.getMessages().send(player, "withdraw-invalid");
            return;
        }

        if (amount > plugin.getConfigManager().maxWithdraw()) {
            plugin.getMessages().send(player, "withdraw-limit", Map.of(
                    "max", String.valueOf(plugin.getConfigManager().maxWithdraw())
            ));
            return;
        }

        player.getScheduler().run(plugin, task -> {
            if (!player.isOnline()) {
                return;
            }

            int current = getHearts(player);
            if (current - amount < plugin.getConfigManager().minHearts()) {
                plugin.getMessages().send(player, "withdraw-too-many");
                return;
            }

            plugin.getDataStore().setHearts(player.getUniqueId(), current - amount);
            applyHealth(player);

            List<ItemStack> items = plugin.getHeartItemFactory().createMany(amount);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(
                    items.toArray(ItemStack[]::new)
            );

            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }

            plugin.getMessages().send(player, "withdraw-success", Map.of(
                    "amount", String.valueOf(amount)
            ));

            if (getHearts(player) <= 0) {
                handleZero(player);
            }
        }, null);
    }

    public void applyHealth(Player player) {
        if (!player.isOnline() || player.isDead()) {
            return;
        }

        int hearts = getHearts(player);
        double maxHealth = Math.max(2.0, hearts * 2.0);

        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(maxHealth);
        }

        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }

        if (plugin.getConfigManager().healthScale()) {
            player.setHealthScale(Math.min(
                    plugin.getConfigManager().maxDisplayHealth(),
                    maxHealth
            ));
        }
    }

    public void handleZeroIfNeeded(Player player) {
        if (getHearts(player) <= 0) {
            handleZero(player);
        }
    }

    private void handleZero(Player player) {
        UUID uuid = player.getUniqueId();
        if (!eliminated.add(uuid)) {
            return;
        }

        String policy = plugin.getConfigManager().zeroHeartPolicy();

        switch (policy) {
            case "BAN" -> {
                String reason = plugin.getConfigManager().zeroHeartBanReason();
                Bukkit.getBanList(BanList.Type.NAME).addBan(
                        player.getName(),
                        reason,
                        (Date) null,
                        "HeartsCore"
                );
                player.kick(Text.mm(reason));
            }
            case "KICK" -> player.kick(
                    Text.mm(plugin.getConfigManager().zeroHeartKickReason())
            );
            default -> {
                player.setGameMode(GameMode.SPECTATOR);
                plugin.getMessages().send(player, "zero-spectator");
            }
        }
    }

    private void restoreGameMode(Player player) {
        if (!plugin.getConfigManager().restoreGamemode()) {
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    private int clamp(int amount) {
        int min = plugin.getConfigManager().minHearts();
        int max = plugin.getConfigManager().maxHearts();
        return Math.max(min, Math.min(max, amount));
    }
}