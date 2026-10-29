package net.bmc.heartscore.shop;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.util.SchedulerUtil;
import net.bmc.heartscore.util.Text;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public final class ShopManager {

    private final HeartsCore plugin;

    public ShopManager(HeartsCore plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        if (!plugin.getConfigManager().shopEnabled()) {
            plugin.getMessages().send(player, "shop-disabled");
            return;
        }

        if (!plugin.getEconomyHook().isReady()) {
            plugin.getMessages().send(player, "shop-no-economy");
            return;
        }

        ShopGuiHolder holder = new ShopGuiHolder();
        Inventory inventory = Bukkit.createInventory(
                holder,
                27,
                Text.mm(plugin.getMessages().raw("gui.shop-title"))
        );
        holder.setInventory(inventory);

        addOffer(holder, inventory, "one");
        addOffer(holder, inventory, "five");
        addOffer(holder, inventory, "ten");

        inventory.setItem(22, button(
                Material.BARRIER,
                plugin.getMessages().raw("gui.close-name"),
                List.of(plugin.getMessages().raw("gui.close-lore"))
        ));

        SchedulerUtil.open(player, inventory);
    }

    public void buy(Player player, ShopGuiHolder.ShopOffer offer) {
        player.getScheduler().run(plugin, task -> {
            if (!player.isOnline()) {
                return;
            }

            int current = plugin.getHeartsService().getHearts(player);
            if (current + offer.hearts() > plugin.getConfigManager().maxHearts()) {
                plugin.getMessages().send(player, "shop-max");
                return;
            }

            if (plugin.getEconomyHook().balance(player) < offer.price()) {
                plugin.getMessages().send(player, "shop-insufficient-funds", Map.of(
                        "price", plugin.getEconomyHook().format(offer.price())
                ));
                return;
            }

            EconomyResponse response = plugin.getEconomyHook().withdraw(player, offer.price());
            if (!response.transactionSuccess()) {
                plugin.getMessages().send(player, "shop-insufficient-funds", Map.of(
                        "price", plugin.getEconomyHook().format(offer.price())
                ));
                return;
            }

            plugin.getHeartsService().addHearts(player, offer.hearts());

            plugin.getMessages().send(player, "shop-bought", Map.of(
                    "amount", String.valueOf(offer.hearts()),
                    "price", plugin.getEconomyHook().format(offer.price())
            ));
        }, null);
    }

    private void addOffer(ShopGuiHolder holder, Inventory inventory, String key) {
        int slot = plugin.getConfigManager().shopSlot(key);
        int hearts = plugin.getConfigManager().shopHearts(key);
        double price = plugin.getConfigManager().shopPrice(key);

        ItemStack item = plugin.getHeartItemFactory().create(1);
        item.editMeta(meta -> {
            meta.displayName(Text.mm(plugin.getMessages().replace(
                    plugin.getMessages().raw("gui.buy-name"),
                    Map.of("amount", String.valueOf(hearts))
            )));
            meta.lore(List.of(
                    Text.mm(plugin.getMessages().replace(
                            plugin.getMessages().raw("gui.buy-lore-1"),
                            Map.of("price", plugin.getEconomyHook().format(price))
                    )),
                    Text.mm(plugin.getMessages().raw("gui.buy-lore-2"))
            ));
        });

        inventory.setItem(slot, item);
        holder.getOffers().put(slot, new ShopGuiHolder.ShopOffer(hearts, price));
    }

    private ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.mm(name));
            meta.lore(lore.stream().map(Text::mm).toList());
            item.setItemMeta(meta);
        }
        return item;
    }
}