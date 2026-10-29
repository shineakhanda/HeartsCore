package net.bmc.heartscore.shop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public final class ShopGuiHolder implements InventoryHolder {

    public record ShopOffer(int hearts, double price) {
    }

    private final Map<Integer, ShopOffer> offers = new HashMap<>();
    private Inventory inventory;

    public Map<Integer, ShopOffer> getOffers() {
        return offers;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}