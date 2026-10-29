package net.bmc.heartscore.command;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GuiCommand implements CommandExecutor {

    private final HeartsCore plugin;

    public GuiCommand(HeartsCore plugin) {
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

        if (!player.hasPermission("heartscore.gui")) {
            plugin.getMessages().send(player, "no-permission");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("heartscore.admin.gui")) {
                plugin.getMessages().send(player, "no-permission");
                return true;
            }
            plugin.getGuiManager().openAdminList(player);
            return true;
        }

        plugin.getGuiManager().openMain(player);
        return true;
    }
}