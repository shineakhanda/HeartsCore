package net.bmc.heartscore.gui;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.UUID;

public final class GuiListener implements Listener {

    private final HeartsCore plugin;

    public GuiListener(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof HeartsGuiHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getInventory())) {
            return;
        }

        int slot = event.getRawSlot();

        switch (holder.getType()) {
            case MAIN -> handleMain(player, slot);
            case ADMIN_LIST -> handleAdminList(player, holder, slot);
            case ADMIN_MANAGE -> handleAdminManage(player, holder, slot);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof HeartsGuiHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMain(Player player, int slot) {
        switch (slot) {
            case 13 -> plugin.getHeartsService().withdraw(player, 1);
            case 15 -> plugin.getShopManager().open(player);
            case 22 -> player.closeInventory();
            case 26 -> {
                if (player.hasPermission("heartscore.admin.gui")) {
                    plugin.getGuiManager().openAdminList(player);
                }
            }
            default -> {
            }
        }
    }

    private void handleAdminList(Player player, HeartsGuiHolder holder, int slot) {
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        UUID targetId = holder.getTargets().get(slot);
        if (targetId == null) {
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        plugin.getGuiManager().openAdminManage(player, target);
    }

    private void handleAdminManage(Player player, HeartsGuiHolder holder, int slot) {
        UUID targetId = holder.getTarget();
        if (targetId == null) {
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);

        switch (slot) {
            case 19 -> modify(player, target, 1);
            case 20 -> modify(player, target, 5);
            case 21 -> modify(player, target, 10);
            case 23 -> modify(player, target, -1);
            case 24 -> modify(player, target, -5);
            case 25 -> modify(player, target, -10);
            case 36 -> plugin.getGuiManager().openAdminList(player);
            case 40 -> reset(player, target);
            case 44 -> player.closeInventory();
            default -> {
            }
        }
    }

    private void modify(Player admin, OfflinePlayer target, int amount) {
        Player online = target.getPlayer();

        if (online != null) {
            online.getScheduler().run(plugin, task -> {
                if (amount >= 0) {
                    plugin.getHeartsService().addHearts(online, amount);
                } else {
                    plugin.getHeartsService().removeHearts(online, -amount);
                }
            }, null);
        } else {
            if (amount >= 0) {
                plugin.getHeartsService().addHearts(target.getUniqueId(), amount);
            } else {
                plugin.getHeartsService().removeHearts(target.getUniqueId(), -amount);
            }
        }

        refresh(admin, target);
    }

    private void reset(Player admin, OfflinePlayer target) {
        int starting = plugin.getConfigManager().startingHearts();
        Player online = target.getPlayer();

        if (online != null) {
            online.getScheduler().run(plugin, task ->
                    plugin.getHeartsService().setHearts(online, starting), null);
        } else {
            plugin.getHeartsService().setHearts(target.getUniqueId(), starting);
        }

        refresh(admin, target);
    }

    private void refresh(Player admin, OfflinePlayer target) {
        admin.getScheduler().runDelayed(plugin, task -> {
            if (admin.isOnline()) {
                plugin.getGuiManager().openAdminManage(admin, target);
            }
        }, null, 2L);
    }
}