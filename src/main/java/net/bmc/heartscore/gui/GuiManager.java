package net.bmc.heartscore.gui;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.data.PlayerData;
import net.bmc.heartscore.util.SchedulerUtil;
import net.bmc.heartscore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GuiManager {

    private final HeartsCore plugin;

    public GuiManager(HeartsCore plugin) {
        this.plugin = plugin;
    }

    public void openMain(Player player) {
        HeartsGuiHolder holder = new HeartsGuiHolder(GuiType.MAIN, player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(
                holder,
                27,
                Text.mm(plugin.getMessages().raw("gui.main-title"))
        );
        holder.setInventory(inventory);

        PlayerData data = plugin.getDataStore().getOrCreate(player.getUniqueId());

        inventory.setItem(11, heartIcon(
                data.getHearts(),
                plugin.getConfigManager().maxHearts(),
                data.getKills(),
                data.getDeaths()
        ));

        ItemStack withdraw = button(
                Material.RED_DYE,
                plugin.getMessages().raw("gui.withdraw-name"),
                List.of(plugin.getMessages().replace(
                        plugin.getMessages().raw("gui.withdraw-lore"),
                        Map.of()
                ))
        );

        if (data.getHearts() <= plugin.getConfigManager().minHearts()) {
            withdraw = button(
                    Material.GRAY_DYE,
                    plugin.getMessages().raw("gui.withdraw-name"),
                    List.of(plugin.getMessages().replace(
                            plugin.getMessages().raw("gui.withdraw-blocked"),
                            Map.of("min", String.valueOf(plugin.getConfigManager().minHearts()))
                    ))
            );
        }

        inventory.setItem(13, withdraw);

        if (plugin.getEconomyHook().isReady() && plugin.getConfigManager().shopEnabled()) {
            inventory.setItem(15, button(
                    Material.EMERALD,
                    plugin.getMessages().raw("gui.shop-name"),
                    List.of(plugin.getMessages().raw("gui.shop-lore"))
            ));
        }

        inventory.setItem(22, button(
                Material.BARRIER,
                plugin.getMessages().raw("gui.close-name"),
                List.of(plugin.getMessages().raw("gui.close-lore"))
        ));

        if (player.hasPermission("heartscore.admin.gui")) {
            inventory.setItem(26, button(
                    Material.COMMAND_BLOCK,
                    plugin.getMessages().raw("gui.admin-name"),
                    List.of(plugin.getMessages().raw("gui.admin-lore"))
            ));
        }

        SchedulerUtil.open(player, inventory);
    }

    public void openAdminList(Player player) {
        HeartsGuiHolder holder = new HeartsGuiHolder(GuiType.ADMIN_LIST, null);
        Inventory inventory = Bukkit.createInventory(
                holder,
                54,
                Text.mm(plugin.getMessages().raw("gui.admin-title"))
        );
        holder.setInventory(inventory);

        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        int slot = 0;
        for (Player target : online) {
            if (slot >= 45) {
                break;
            }

            int hearts = plugin.getHeartsService().getHearts(target);
            inventory.setItem(slot, playerHead(
                    target,
                    plugin.getMessages().replace(
                            plugin.getMessages().raw("gui.player-entry-name"),
                            Map.of("player", target.getName())
                    ),
                    List.of(
                            plugin.getMessages().replace(
                                    plugin.getMessages().raw("gui.player-entry-lore-1"),
                                    Map.of("hearts", String.valueOf(hearts))
                            ),
                            plugin.getMessages().raw("gui.player-entry-lore-2")
                    )
            ));
            holder.getTargets().put(slot, target.getUniqueId());
            slot++;
        }

        inventory.setItem(49, button(
                Material.BARRIER,
                plugin.getMessages().raw("gui.close-name"),
                List.of(plugin.getMessages().raw("gui.close-lore"))
        ));

        SchedulerUtil.open(player, inventory);
    }

    public void openAdminManage(Player player, OfflinePlayer target) {
        UUID targetId = target.getUniqueId();
        HeartsGuiHolder holder = new HeartsGuiHolder(GuiType.ADMIN_MANAGE, targetId);
        Inventory inventory = Bukkit.createInventory(
                holder,
                45,
                Text.mm(plugin.getMessages().replace(
                        plugin.getMessages().raw("gui.manage-title"),
                        Map.of("player", safeName(target))
                ))
        );
        holder.setInventory(inventory);

        int hearts = plugin.getDataStore().getHearts(targetId);

        inventory.setItem(4, playerHead(
                target,
                plugin.getMessages().replace(
                        plugin.getMessages().raw("gui.target-name"),
                        Map.of("player", safeName(target))
                ),
                List.of(
                        plugin.getMessages().replace(
                                plugin.getMessages().raw("gui.target-lore-1"),
                                Map.of("hearts", String.valueOf(hearts))
                        ),
                        plugin.getMessages().raw("gui.target-lore-2")
                )
        ));

        inventory.setItem(19, addButton(1));
        inventory.setItem(20, addButton(5));
        inventory.setItem(21, addButton(10));
        inventory.setItem(23, removeButton(1));
        inventory.setItem(24, removeButton(5));
        inventory.setItem(25, removeButton(10));

        inventory.setItem(36, button(
                Material.ARROW,
                plugin.getMessages().raw("gui.back-name"),
                List.of(plugin.getMessages().raw("gui.back-lore"))
        ));

        inventory.setItem(40, button(
                Material.YELLOW_DYE,
                plugin.getMessages().raw("gui.reset-name"),
                List.of(plugin.getMessages().raw("gui.reset-lore"))
        ));

        inventory.setItem(44, button(
                Material.BARRIER,
                plugin.getMessages().raw("gui.close-name"),
                List.of(plugin.getMessages().raw("gui.close-lore"))
        ));

        SchedulerUtil.open(player, inventory);
    }

    private ItemStack addButton(int amount) {
        return button(
                Material.LIME_DYE,
                plugin.getMessages().replace(
                        plugin.getMessages().raw("gui.add-name"),
                        Map.of("amount", String.valueOf(amount))
                ),
                List.of(plugin.getMessages().raw("gui.add-lore"))
        );
    }

    private ItemStack removeButton(int amount) {
        return button(
                Material.RED_DYE,
                plugin.getMessages().replace(
                        plugin.getMessages().raw("gui.remove-name"),
                        Map.of("amount", String.valueOf(amount))
                ),
                List.of(plugin.getMessages().raw("gui.remove-lore"))
        );
    }

    private ItemStack heartIcon(int hearts, int max, int kills, int deaths) {
        ItemStack item = plugin.getHeartItemFactory().create(1);

        item.editMeta(meta -> {
            meta.displayName(Text.mm(plugin.getMessages().raw("gui.heart-name")));
            meta.lore(List.of(
                    Text.mm(plugin.getMessages().replace(
                            plugin.getMessages().raw("gui.heart-lore-1"),
                            Map.of("hearts", String.valueOf(hearts), "max", String.valueOf(max))
                    )),
                    Text.mm(plugin.getMessages().replace(
                            plugin.getMessages().raw("gui.heart-lore-2"),
                            Map.of("kills", String.valueOf(kills))
                    )),
                    Text.mm(plugin.getMessages().replace(
                            plugin.getMessages().raw("gui.heart-lore-3"),
                            Map.of("deaths", String.valueOf(deaths))
                    )),
                    Text.mm(plugin.getMessages().raw("gui.heart-lore-4"))
            ));
        });

        return item;
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

    @SuppressWarnings("deprecation")
    private ItemStack playerHead(OfflinePlayer player, String name, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        item.editMeta(SkullMeta.class, meta -> {
            meta.setOwningPlayer(player);
            meta.displayName(Text.mm(name));
            meta.lore(lore.stream().map(Text::mm).toList());
        });
        return item;
    }

    private String safeName(OfflinePlayer player) {
        return player.getName() == null ? player.getUniqueId().toString() : player.getName();
    }
}