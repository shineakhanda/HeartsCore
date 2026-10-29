package net.bmc.heartscore.config;

import net.bmc.heartscore.HeartsCore;
import net.bmc.heartscore.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class Messages {

    private final HeartsCore plugin;
    private String prefix = "";

    public Messages(HeartsCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.prefix = plugin.getConfig().getString("messages.prefix", "");
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public Component get(String key, Map<String, String> placeholders) {
        return Text.mm(replace(raw(key), placeholders));
    }

    public String raw(String key) {
        return plugin.getConfig().getString("messages." + key, "<red>Missing message: " + key);
    }

    public String replace(String raw, Map<String, String> placeholders) {
        String out = raw == null ? "" : raw;
        out = out.replace("%prefix%", prefix == null ? "" : prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            out = out.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return out;
    }
}