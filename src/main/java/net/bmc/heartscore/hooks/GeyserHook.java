package net.bmc.heartscore.hooks;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GeyserHook {

    private final HeartsCore plugin;

    public GeyserHook(HeartsCore plugin) {
        this.plugin = plugin;
    }

    public void install() {
        if (!plugin.getConfigManager().geyserInstallCustomSkull()) {
            return;
        }

        Plugin geyser = plugin.getServer().getPluginManager().getPlugin("Geyser-Spigot");
        if (geyser == null) {
            return;
        }

        try {
            Path mappings = geyser.getDataFolder().toPath().resolve("custom_mappings");
            Files.createDirectories(mappings);

            Path target = mappings.resolve("heartscore.json");
            if (Files.exists(target)) {
                return;
            }

            try (InputStream input = plugin.getResource("geyser/custom_skulls.json")) {
                if (input == null) {
                    plugin.getLogger().warning("Geyser custom skull resource is missing.");
                    return;
                }
                Files.copy(input, target);
                plugin.getLogger().info(
                        "Installed Geyser heart-skull mapping. Restart Geyser/server for Bedrock clients to receive it."
                );
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not install Geyser mapping: " + exception.getMessage());
        }
    }
}