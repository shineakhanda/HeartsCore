package net.bmc.heartscore.util;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class SchedulerUtil {

    private SchedulerUtil() {
    }

    public static void open(Player player, Inventory inventory) {
        player.getScheduler().run(
                HeartsCore.get(),
                task -> player.openInventory(inventory),
                null
        );
    }

    public static void run(Player player, Runnable runnable) {
        player.getScheduler().run(
                HeartsCore.get(),
                task -> runnable.run(),
                null
        );
    }

    public static void runDelayed(Player player, Runnable runnable, long delayTicks) {
        player.getScheduler().runDelayed(
                HeartsCore.get(),
                task -> runnable.run(),
                null,
                delayTicks
        );
    }
}