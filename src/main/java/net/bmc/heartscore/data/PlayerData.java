package net.bmc.heartscore.data;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class PlayerData {

    private final UUID uuid;
    private final AtomicInteger hearts;
    private final AtomicInteger kills = new AtomicInteger();
    private final AtomicInteger deaths = new AtomicInteger();

    public PlayerData(UUID uuid, int startingHearts) {
        this.uuid = uuid;
        this.hearts = new AtomicInteger(startingHearts);
    }

    public UUID uuid() {
        return uuid;
    }

    public int getHearts() {
        return hearts.get();
    }

    public void setHearts(int value) {
        hearts.set(value);
    }

    public int getKills() {
        return kills.get();
    }

    public void setKills(int value) {
        kills.set(value);
    }

    public int incrementKills() {
        return kills.incrementAndGet();
    }

    public int getDeaths() {
        return deaths.get();
    }

    public void setDeaths(int value) {
        deaths.set(value);
    }

    public int incrementDeaths() {
        return deaths.incrementAndGet();
    }
}