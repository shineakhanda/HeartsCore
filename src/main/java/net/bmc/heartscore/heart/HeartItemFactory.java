package net.bmc.heartscore.heart;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.util.SkullUtil;
import net.bmc.heartscore.util.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class HeartItemFactory {

    private final HeartsCore plugin;
    private final NamespacedKey heartKey;

    public HeartItemFactory(HeartsCore plugin) {
        this.plugin = plugin;
        this.heartKey = new NamespacedKey(plugin, "heart_item");
    }

    public ItemStack create(int amount) {
        List<ItemStack> items = createMany(amount);
        return items.isEmpty() ? new ItemStack(Material.AIR) : items.getFirst();
    }

    public List<ItemStack> createMany(int amount) {
        List<ItemStack> items = new ArrayList<>();
        int remaining = Math.max(0, amount);

        while (remaining > 0) {
            int stackAmount = Math.min(64, remaining);
            ItemStack item = single();
            item.setAmount(stackAmount);
            items.add(item);
            remaining -= stackAmount;
        }

        return items;
    }

    public boolean isHeart(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        return item.getPersistentDataContainer().has(heartKey, PersistentDataType.BYTE);
    }

    private ItemStack single() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        item.editMeta(SkullMeta.class, meta -> {
            meta.displayName(Text.mm("<red><bold>Heart</bold></red>"));
            meta.lore(List.of(
                    Text.mm("<gray>Right-click to absorb 1 heart."),
                    Text.mm("<dark_gray>Stored lifesteal heart")
            ));
            SkullUtil.applyTexture(meta, plugin.getConfigManager().heartTexture());
            meta.getPersistentDataContainer().set(heartKey, PersistentDataType.BYTE, (byte) 1);
        });

        return item;
    }
}