package net.bmc.heartscore.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HeartsGuiHolder implements InventoryHolder {

    private final GuiType type;
    private final UUID target;
    private final Map<Integer, UUID> targets = new HashMap<>();
    private Inventory inventory;

    public HeartsGuiHolder(GuiType type, UUID target) {
        this.type = type;
        this.target = target;
    }

    public GuiType getType() {
        return type;
    }

    public UUID getTarget() {
        return target;
    }

    public Map<Integer, UUID> getTargets() {
        return targets;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}