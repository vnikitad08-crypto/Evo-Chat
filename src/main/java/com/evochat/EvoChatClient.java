package com.evochat;

import com.evochat.chat.EvoChatWindow;
import com.evochat.config.EvoConfig;
import com.evochat.gui.AutoMessageWidget;
import com.evochat.gui.EvoSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Client entry-point for EvoChat.
 *
 * Responsibilities:
 *  1. Register the ] / ъ key-binding (GLFW_KEY_RIGHT_BRACKET = 93)
 *  2. Hook into HUD rendering to draw the EvoChatWindow overlay
 *  3. On each tick check if the key was pressed → open settings screen
 */
public class EvoChatClient implements ClientModInitializer {

    public static KeyBinding openMenuKey;
    public static KeyBinding toggleAutoMessageKey;
    public static KeyBinding scrollUpKey;
    public static KeyBinding scrollDownKey;
    private static long lastAutoMessageTime = 0L;

    @Override
    public void onInitializeClient() {
        // Load config early
        EvoConfig.load();

        // ── Key binding ────────────────────────────────────────────────────
        // Default: ] (right bracket)  — same physical key as Ъ on a Russian layout
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.evochat.open_menu",          // translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,      // ] on US layout == Ъ on RU layout
                "category.evochat"                // category shown in controls screen
        ));

        // Toggle auto-message on/off
        toggleAutoMessageKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.evochat.toggle_auto_message",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                "category.evochat"
        ));

        // Scroll EvoChat history up/down
        scrollUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.evochat.scroll_up",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_UP,
                "category.evochat"
        ));
        scrollDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.evochat.scroll_down",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_DOWN,
                "category.evochat"
        ));

        // ── HUD overlay ────────────────────────────────────────────────────
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            float delta = tickCounter.getTickDelta(true);
            EvoChatWindow.getInstance().render(drawContext, delta);
            AutoMessageWidget.render(drawContext, delta);
        });


        // ── Tick: handle key press ─────────────────────────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                // Open settings screen; pass current screen as parent
                // so ESC returns to game (parent == null → returns to game)
                client.setScreen(new EvoSettingsScreen(client.currentScreen));
            }

            while (toggleAutoMessageKey.wasPressed()) {
                EvoConfig cfgLocal = EvoConfig.get();
                cfgLocal.autoMessageEnabled = !cfgLocal.autoMessageEnabled;
                cfgLocal.save();
            }

            while (scrollUpKey.wasPressed()) {
                EvoChatWindow.getInstance().scroll(3);
            }
            while (scrollDownKey.wasPressed()) {
                EvoChatWindow.getInstance().scroll(-3);
            }

            // Auto-message ticking
            EvoConfig cfgLocal = EvoConfig.get();
            if (cfgLocal.autoMessageEnabled
                    && client.player != null
                    && client.world != null
                    && cfgLocal.autoMessageText != null
                    && !cfgLocal.autoMessageText.isEmpty()) {
                long now = System.currentTimeMillis();
                long intervalMs = Math.max(1, cfgLocal.autoMessageIntervalSeconds) * 1000L;
                if (now - lastAutoMessageTime >= intervalMs) {
                    client.player.networkHandler.sendChatMessage(cfgLocal.autoMessageText);
                    lastAutoMessageTime = now;
                }
            }
        });

        System.out.println("[EvoChat] Initialized. Press ] / Ъ to open settings.");
    }
}
