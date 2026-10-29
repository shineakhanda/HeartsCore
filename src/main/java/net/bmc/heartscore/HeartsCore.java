package net.bmc.heartscore;

import net.bmc.heartscore.combat.AntiFarmService;
import net.bmc.heartscore.combat.CombatListener;
import net.bmc.heartscore.combat.CombatTracker;
import net.bmc.heartscore.combat.TrapListener;
import net.bmc.heartscore.combat.TrapTracker;
import net.bmc.heartscore.command.GuiCommand;
import net.bmc.heartscore.command.HeartsCommand;
import net.bmc.heartscore.command.ShopCommand;
import net.bmc.heartscore.config.ConfigManager;
import net.bmc.heartscore.config.Messages;
import net.bmc.heartscore.data.PlayerDataStore;
import net.bmc.heartscore.death.DeathListener;
import net.bmc.heartscore.economy.EconomyHook;
import net.bmc.heartscore.gui.GuiListener;
import net.bmc.heartscore.gui.GuiManager;
import net.bmc.heartscore.heart.HeartItemFactory;
import net.bmc.heartscore.heart.HeartListener;
import net.bmc.heartscore.heart.HeartsService;
import net.bmc.heartscore.hooks.GeyserHook;
import net.bmc.heartscore.shop.ShopListener;
import net.bmc.heartscore.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class HeartsCore extends JavaPlugin {

    private static HeartsCore instance;

    private ConfigManager configManager;
    private Messages messages;
    private PlayerDataStore dataStore;
    private HeartItemFactory heartItemFactory;
    private HeartsService heartsService;
    private CombatTracker combatTracker;
    private TrapTracker trapTracker;
    private AntiFarmService antiFarmService;
    private EconomyHook economyHook;
    private GuiManager guiManager;
    private ShopManager shopManager;

    public static HeartsCore get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        messages = new Messages(this);
        dataStore = new PlayerDataStore(this);
        dataStore.load();

        heartItemFactory = new HeartItemFactory(this);
        heartsService = new HeartsService(this);
        combatTracker = new CombatTracker(this);
        trapTracker = new TrapTracker(this);
        antiFarmService = new AntiFarmService(this);
        economyHook = new EconomyHook(this);
        guiManager = new GuiManager(this);
        shopManager = new ShopManager(this);

        new GeyserHook(this).install();

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new TrapListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new HeartListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        getCommand("hc").setExecutor(new HeartsCommand(this));
        getCommand("hc").setTabCompleter(new HeartsCommand(this));
        getCommand("hshop").setExecutor(new ShopCommand(this));
        getCommand("hcgui").setExecutor(new GuiCommand(this));

        getServer().getAsyncScheduler().runAtFixedRate(
                this,
                task -> dataStore.saveAsync(),
                5L, 5L, TimeUnit.MINUTES
        );

        getServer().getGlobalRegionScheduler().runAtFixedRate(
                this,
                task -> {
                    combatTracker.cleanup();
                    trapTracker.cleanup();
                    antiFarmService.cleanup();
                },
                20L * 30L,
                20L * 30L
        );

        getLogger().info("HeartsCore enabled.");
    }

    @Override
    public void onDisable() {
        if (dataStore != null) {
            dataStore.saveSync();
        }
    }

    public ConfigManager getConfigManager() { return configManager; }
    public Messages getMessages() { return messages; }
    public PlayerDataStore getDataStore() { return dataStore; }
    public HeartItemFactory getHeartItemFactory() { return heartItemFactory; }
    public HeartsService getHeartsService() { return heartsService; }
    public CombatTracker getCombatTracker() { return combatTracker; }
    public TrapTracker getTrapTracker() { return trapTracker; }
    public AntiFarmService getAntiFarmService() { return antiFarmService; }
    public EconomyHook getEconomyHook() { return economyHook; }
    public GuiManager getGuiManager() { return guiManager; }
    public ShopManager getShopManager() { return shopManager; }
}