package net.bmc.heartscore.command;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HeartsCommand implements CommandExecutor, TabCompleter {

    private final HeartsCore plugin;

    public HeartsCommand(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "hearts" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                showHearts(sender, player);
            }
            case "withdraw" -> {
                if (!(sender instanceof Player player)) {
                    plugin.getMessages().send(sender, "player-only");
                    return true;
                }
                if (!player.hasPermission("heartscore.withdraw")) {
                    plugin.getMessages().send(player, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    plugin.getMessages().send(player, "withdraw-invalid");
                    return true;
                }
                Integer amount = parseInt(args[1]);
                if (amount == null) {
                    plugin.getMessages().send(player, "withdraw-invalid");
                    return true;
                }
                plugin.getHeartsService().withdraw(player, amount);
            }
            case "admin" -> handleAdmin(sender, args);
            case "reload" -> {
                if (!sender.hasPermission("heartscore.admin")) {
                    plugin.getMessages().send(sender, "no-permission");
                    return true;
                }
                plugin.getConfigManager().reload();
                plugin.getMessages().reload();
                plugin.getMessages().send(sender, "reloaded");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartscore.admin")) {
            plugin.getMessages().send(sender, "no-permission");
            return;
        }

        if (args.length < 3) {
            sendAdminHelp(sender);
            return;
        }

        OfflinePlayer target = findTarget(args[2]);
        if (target == null) {
            plugin.getMessages().send(sender, "admin-target-not-found");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "hearts" -> {
                int hearts = plugin.getDataStore().getHearts(target.getUniqueId());
                plugin.getMessages().send(sender, "hearts-other", Map.of(
                        "player", safeName(target),
                        "hearts", String.valueOf(hearts)
                ));
            }
            case "add", "revoke" -> {
                if (args.length < 4) {
                    plugin.getMessages().send(sender, "admin-invalid-amount");
                    return;
                }

                Integer amount = parseInt(args[3]);
                if (amount == null || amount <= 0) {
                    plugin.getMessages().send(sender, "admin-invalid-amount");
                    return;
                }

                Player online = target.getPlayer();
                boolean add = args[1].equalsIgnoreCase("add");

                if (online != null) {
                    online.getScheduler().run(plugin, task -> {
                        if (add) {
                            plugin.getHeartsService().addHearts(online, amount);
                        } else {
                            plugin.getHeartsService().removeHearts(online, amount);
                        }
                    }, null);
                } else {
                    if (add) {
                        plugin.getHeartsService().addHearts(target.getUniqueId(), amount);
                    } else {
                        plugin.getHeartsService().removeHearts(target.getUniqueId(), amount);
                    }
                }

                plugin.getMessages().send(sender, add ? "admin-added" : "admin-removed", Map.of(
                        "amount", String.valueOf(amount),
                        "player", safeName(target)
                ));
            }
            case "set" -> {
                if (args.length < 4) {
                    plugin.getMessages().send(sender, "admin-invalid-amount");
                    return;
                }

                Integer amount = parseInt(args[3]);
                if (amount == null || amount < plugin.getConfigManager().minHearts()) {
                    plugin.getMessages().send(sender, "admin-invalid-amount");
                    return;
                }

                Player online = target.getPlayer();
                if (online != null) {
                    online.getScheduler().run(plugin, task ->
                            plugin.getHeartsService().setHearts(online, amount), null);
                } else {
                    plugin.getHeartsService().setHearts(target.getUniqueId(), amount);
                }

                plugin.getMessages().send(sender, "admin-set", Map.of(
                        "amount", String.valueOf(amount),
                        "player", safeName(target)
                ));
            }
            case "reset" -> {
                int starting = plugin.getConfigManager().startingHearts();
                Player online = target.getPlayer();

                if (online != null) {
                    online.getScheduler().run(plugin, task ->
                            plugin.getHeartsService().setHearts(online, starting), null);
                } else {
                    plugin.getHeartsService().setHearts(target.getUniqueId(), starting);
                }

                plugin.getMessages().send(sender, "admin-reset", Map.of(
                        "amount", String.valueOf(starting),
                        "player", safeName(target)
                ));
            }
            default -> sendAdminHelp(sender);
        }
    }

    private void showHearts(CommandSender sender, Player player) {
        PlayerData data = plugin.getDataStore().getOrCreate(player.getUniqueId());
        plugin.getMessages().send(sender, "hearts", Map.of(
                "hearts", String.valueOf(data.getHearts()),
                "max", String.valueOf(plugin.getConfigManager().maxHearts())
        ));
    }

    private OfflinePlayer findTarget(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }

        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null) {
            return cached;
        }

        try {
            return Bukkit.getOfflinePlayer(name);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String safeName(OfflinePlayer player) {
        return player.getName() == null ? player.getUniqueId().toString() : player.getName();
    }

    private void sendHelp(CommandSender sender) {
        plugin.getMessages().send(sender, "hearts");
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hc withdraw <amount> <dark_gray>- withdraw hearts"
        ));
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hshop <dark_gray>- buy hearts"
        ));
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hcgui <dark_gray>- open the heart GUI"
        ));
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hc admin hearts <player>"
        ));
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hc admin add <player> <amount>"
        ));
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hc admin revoke <player> <amount>"
        ));
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hc admin set <player> <amount>"
        ));
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hc admin reset <player>"
        ));
        sender.sendMessage(net.bmc.heartscore.util.Text.mm(
                "<gray>/hc reload"
        ));
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            return filter(List.of("hearts", "withdraw", "admin", "reload"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return filter(List.of("hearts", "add", "revoke", "set", "reset"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(names, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("admin")) {
            return filter(List.of("1", "5", "10"), args[3]);
        }

        return List.of();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase();
        return values.stream()
                .filter(value -> value.toLowerCase().startsWith(lower))
                .toList();
    }
}