package net.bmc.heartscore.command;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ShopCommand implements CommandExecutor {

    private final HeartsCore plugin;

    public ShopCommand(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().send(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("heartscore.shop")) {
            plugin.getMessages().send(player, "no-permission");
            return true;
        }

        plugin.getShopManager().open(player);
        return true;
    }
}