package net.bmc.heartscore.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public final class SkullUtil {

    private SkullUtil() {
    }

    public static void applyTexture(SkullMeta meta, String base64) {
        if (base64 == null || base64.isBlank()) {
            return;
        }

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "Heart");
        profile.setProperty(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(profile);
    }
}