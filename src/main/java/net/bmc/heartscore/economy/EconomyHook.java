package net.bmc.heartscore.economy;

import net.bmc.heartscore.HeartsCore;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyHook {

    private final HeartsCore plugin;
    private Economy economy;

    public EconomyHook(HeartsCore plugin) {
        this.plugin = plugin;
        setup();
    }

    private void setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found. /hshop will be disabled until an economy provider is installed.");
            return;
        }

        RegisteredServiceProvider<Economy> registration =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (registration == null) {
            plugin.getLogger().warning("No Vault economy provider found. Install EternalEconomy or another Vault economy.");
            return;
        }

        this.economy = registration.getProvider();
        plugin.getLogger().info("Hooked into Vault economy: " + economy.getName());
    }

    public boolean isReady() {
        return economy != null;
    }

    public double balance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public EconomyResponse withdraw(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount);
    }

    public String format(double amount) {
        return economy.format(amount);
    }
}