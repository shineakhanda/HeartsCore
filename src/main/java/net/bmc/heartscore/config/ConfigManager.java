package net.bmc.heartscore.config;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Locale;

public final class ConfigManager {

    private final HeartsCore plugin;
    private FileConfiguration config;

    public ConfigManager(HeartsCore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public int startingHearts() {
        return config.getInt("starting-hearts", 10);
    }

    public int maxHearts() {
        return config.getInt("max-hearts", 40);
    }

    public int minHearts() {
        return config.getInt("min-hearts", 0);
    }

    public int combatTagSeconds() {
        return Math.max(1, config.getInt("combat-tag-seconds", 15));
    }

    public boolean antiFarmEnabled() {
        return config.getBoolean("anti-farm.enabled", true);
    }

    public int killCooldownSeconds() {
        return Math.max(0, config.getInt("anti-farm.kill-cooldown-seconds", 300));
    }

    public boolean blockSameIp() {
        return config.getBoolean("anti-farm.block-same-ip", true);
    }

    public String pvpMode() {
        return config.getString("pvp.mode", "DROP").toUpperCase(Locale.ROOT);
    }

    public boolean pvpDropMode() {
        return !pvpMode().equals("DIRECT");
    }

    public int pvpHeartLoss() {
        return Math.max(0, config.getInt("pvp.heart-loss", 1));
    }

    public int killerGain() {
        return Math.max(0, config.getInt("pvp.killer-gain", 1));
    }

    public int naturalDeathHeartLoss() {
        return Math.max(0, config.getInt("natural-death.heart-loss", 1));
    }

    public boolean naturalDeathDropHeart() {
        return config.getBoolean("natural-death.drop-heart", false);
    }

    public int maxWithdraw() {
        return Math.max(1, config.getInt("withdraw.max-per-command", 64));
    }

    public String zeroHeartPolicy() {
        return config.getString("zero-heart.policy", "SPECTATOR").toUpperCase(Locale.ROOT);
    }

    public String zeroHeartBanReason() {
        return config.getString("zero-heart.ban-reason", "You ran out of hearts.");
    }

    public String zeroHeartKickReason() {
        return config.getString("zero-heart.kick-reason", "You ran out of hearts.");
    }

    public boolean restoreGamemode() {
        return config.getBoolean("zero-heart.restore-gamemode", true);
    }

    public boolean combatLogPunish() {
        return config.getBoolean("combat-log.punish", false);
    }

    public int combatLogHeartLoss() {
        return Math.max(0, config.getInt("combat-log.heart-loss", 1));
    }

    public boolean combatLogDropHeart() {
        return config.getBoolean("combat-log.drop-heart", false);
    }

    public boolean healthScale() {
        return config.getBoolean("health.health-scale", true);
    }

    public double maxDisplayHealth() {
        return Math.max(20.0, config.getDouble("health.max-display-health", 40.0));
    }

    public String heartTexture() {
        return config.getString("heart-item.texture", "");
    }

    public boolean shopEnabled() {
        return config.getBoolean("shop.enabled", true);
    }

    public int shopSlot(String key) {
        return config.getInt("shop.offers." + key + ".slot", 11);
    }

    public int shopHearts(String key) {
        return config.getInt("shop.offers." + key + ".hearts", 1);
    }

    public double shopPrice(String key) {
        return config.getDouble("shop.offers." + key + ".price", 50000.0);
    }

    public boolean geyserInstallCustomSkull() {
        return config.getBoolean("geyser.install-custom-skull", true);
    }
}