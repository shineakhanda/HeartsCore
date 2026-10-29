package net.bmc.heartscore.combat;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TrapTracker {

    private record BlockPos(String world, int x, int y, int z) {
    }

    private record TrapRecord(UUID owner, long expiresAt) {
    }

    private static final Set<Material> TRAP_BLOCKS = EnumSet.of(
            Material.LAVA,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.MAGMA_BLOCK,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
            Material.COBWEB,
            Material.POWDER_SNOW,
            Material.TNT
    );

    private final HeartsCore plugin;
    private final Map<BlockPos, TrapRecord> traps = new ConcurrentHashMap<>();

    public TrapTracker(HeartsCore plugin) {
        this.plugin = plugin;
    }

    public void record(Block block, Player player) {
        if (!TRAP_BLOCKS.contains(block.getType())) {
            return;
        }

        long expiresAt = System.currentTimeMillis()
                + (plugin.getConfigManager().combatTagSeconds() * 2_000L);

        traps.put(
                key(block),
                new TrapRecord(player.getUniqueId(), expiresAt)
        );
    }

    public Optional<UUID> findOwner(Location deathLocation) {
        if (deathLocation.getWorld() == null) {
            return Optional.empty();
        }

        World world = deathLocation.getWorld();
        int baseX = deathLocation.getBlockX();
        int baseY = deathLocation.getBlockY();
        int baseZ = deathLocation.getBlockZ();

        UUID bestOwner = null;
        long bestExpiry = 0L;

        for (int x = baseX - 2; x <= baseX + 2; x++) {
            for (int y = baseY - 2; y <= baseY + 2; y++) {
                for (int z = baseZ - 2; z <= baseZ + 2; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    TrapRecord record = traps.get(key(block));

                    if (record == null) {
                        continue;
                    }

                    if (System.currentTimeMillis() > record.expiresAt()) {
                        traps.remove(key(block));
                        continue;
                    }

                    if (record.expiresAt() > bestExpiry) {
                        bestExpiry = record.expiresAt();
                        bestOwner = record.owner();
                    }
                }
            }
        }

        return Optional.ofNullable(bestOwner);
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        traps.entrySet().removeIf(entry -> now > entry.getValue().expiresAt());
    }

    private BlockPos key(Block block) {
        return new BlockPos(
                block.getWorld().getName(),
                block.getX(),
                block.getY(),
                block.getZ()
        );
    }
}