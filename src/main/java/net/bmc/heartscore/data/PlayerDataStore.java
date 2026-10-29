package net.bmc.heartscore.data;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public final class PlayerDataStore {

    private final HeartsCore plugin;
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final File file;
    private final AtomicBoolean asyncSaving = new AtomicBoolean(false);
    private final ReentrantLock writeLock = new ReentrantLock();

    public PlayerDataStore(HeartsCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        players.clear();

        if (!file.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("players");
        if (root == null) {
            return;
        }

        for (String key : root.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            PlayerData data = new PlayerData(uuid, plugin.getConfigManager().startingHearts());
            data.setHearts(root.getInt(key + ".hearts", plugin.getConfigManager().startingHearts()));
            data.setKills(root.getInt(key + ".kills", 0));
            data.setDeaths(root.getInt(key + ".deaths", 0));
            players.put(uuid, data);
        }
    }

    public PlayerData getOrCreate(UUID uuid) {
        return players.computeIfAbsent(
                uuid,
                id -> new PlayerData(id, plugin.getConfigManager().startingHearts())
        );
    }

    public int getHearts(UUID uuid) {
        return getOrCreate(uuid).getHearts();
    }

    public void setHearts(UUID uuid, int amount) {
        getOrCreate(uuid).setHearts(amount);
    }

    public void saveAsync() {
        if (!asyncSaving.compareAndSet(false, true)) {
            return;
        }

        YamlConfiguration snapshot = snapshot();

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                write(snapshot);
            } finally {
                asyncSaving.set(false);
            }
        });
    }

    public void saveSync() {
        write(snapshot());
    }

    private YamlConfiguration snapshot() {
        YamlConfiguration yaml = new YamlConfiguration();

        for (PlayerData data : players.values()) {
            String path = "players." + data.uuid() + ".";
            yaml.set(path + "hearts", data.getHearts());
            yaml.set(path + "kills", data.getKills());
            yaml.set(path + "deaths", data.getDeaths());
        }

        return yaml;
    }

    private void write(YamlConfiguration yaml) {
        writeLock.lock();
        try {
            Path path = file.toPath();
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Path temp = path.resolveSibling("data.yml.tmp");
            yaml.save(temp.toFile());

            try {
                Files.move(
                        temp,
                        path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException ignored) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save player data: " + exception.getMessage());
        } finally {
            writeLock.unlock();
        }
    }
}