package net.bmc.heartscore.combat;

import net.bmc.heartscore.HeartsCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class TrapListener implements Listener {

    private final HeartsCore plugin;

    public TrapListener(HeartsCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        plugin.getTrapTracker().record(event.getBlockPlaced(), event.getPlayer());
    }
}