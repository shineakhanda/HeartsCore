package net.bmc.heartscore.shop;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class ShopListener implements Listener {

    private final HeartsCore plugin;

    public ShopListener(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGuiHolder holder)) {
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

        if (slot == 22) {
            player.closeInventory();
            return;
        }

        ShopGuiHolder.ShopOffer offer = holder.getOffers().get(slot);
        if (offer != null) {
            plugin.getShopManager().buy(player, offer);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ShopGuiHolder) {
            event.setCancelled(true);
        }
    }
}