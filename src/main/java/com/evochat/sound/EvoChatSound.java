package com.evochat.sound;

import com.evochat.config.EvoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Plays a notification sound when a new ЛС message arrives.
 *
 * We re-use vanilla's "entity.experience_orb.pickup" as the
 * notification chime — it is always present and has a pleasant
 * high-pitched 'ding' quality fitting for a private-message alert.
 *
 * Players can swap to any registered SoundEvent by changing
 * SOUND_ID below without recompiling, but for the default build
 * this vanilla sound requires no extra resource-pack entry.
 */
public class EvoChatSound {

    // Vanilla sound: short, clear ping — perfect for ЛС alerts
    private static final Identifier SOUND_ID =
            Identifier.ofVanilla("entity.experience_orb.pickup");

    // Debounce: don't spam the sound if many ЛС lines arrive at once
    private static long lastPlayTime = 0;
    private static final long DEBOUNCE_MS = 500;

    public static void playIfEnabled() {
        EvoConfig cfg = EvoConfig.get();
        if (!cfg.soundEnabled) return;

        long now = System.currentTimeMillis();
        if (now - lastPlayTime < DEBOUNCE_MS) return;
        lastPlayTime = now;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        SoundEvent sound = net.minecraft.registry.Registries.SOUND_EVENT.get(SOUND_ID);
        if (sound == null) return;

        mc.player.playSound(sound, 0.6f, 1.4f);
    }
}
