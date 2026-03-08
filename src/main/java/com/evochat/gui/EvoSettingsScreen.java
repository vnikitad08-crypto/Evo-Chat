package com.evochat.gui;

import com.evochat.chat.EvoChatWindow;
import com.evochat.config.EvoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * EvoChat Settings screen.
 *
 * Visual identity: dark-glass panel (navy/charcoal), cyan-teal accents,
 * slim section dividers, custom sliders and toggle widgets.
 * No vanilla widgets for the main panel — pure custom rendering.
 */
public class EvoSettingsScreen extends Screen {

    private enum Section {
        CHAT,
        AUTO_MESSAGE
    }

    // ─── Colors ──────────────────────────────────────────────────────────────
    private static final int COL_BG        = 0xFF0D1117;
    private static final int COL_PANEL     = 0xFF161B22;
    private static final int COL_ACCENT    = 0xFF00D4C8;
    private static final int COL_ACCENT2   = 0xFF58A6FF;
    private static final int COL_TEXT      = 0xFFE6EDF3;
    private static final int COL_MUTED     = 0xFF8B949E;
    private static final int COL_DIVIDER   = 0xFF21262D;
    private static final int COL_BTN       = 0xFF21262D;
    private static final int COL_BTN_HOV   = 0xFF2D333B;
    private static final int COL_BTN_TEXT  = 0xFFE6EDF3;

    // ─── Layout ──────────────────────────────────────────────────────────────
    private static final int PANEL_W = 400;
    private static final int PANEL_H = 380;

    private final Screen parent;
    private EvoConfig cfg;

    private final Section section;

    // Panel origin (computed in init)
    private int px, py;

    // Position preset buttons track selected
    private int selectedPreset = -1;

    // Arrow step buttons for fine position tuning (chat window)
    private EvoButton btnLeft, btnRight, btnUp, btnDown;

    // Auto-message text field
    private TextFieldWidget autoMessageField;

    public EvoSettingsScreen(Screen parent) {
        this(parent, Section.CHAT);
    }

    private EvoSettingsScreen(Screen parent, Section section) {
        super(Text.of("EvoChat Settings"));
        this.parent = parent;
        this.section = section;
    }

    @Override
    protected void init() {
        cfg = EvoConfig.get();

        px = (width  - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;

        int sliderX = px + 20;
        int sliderW = PANEL_W - 40;

        // Left-side section selector
        int navX = px - 110;
        int navY = py + 70;
        int navW = 100;
        int navH = 18;

        addDrawableChild(new EvoButton(
                navX, navY, navW, navH,
                "Настройки чата",
                btn -> client.setScreen(new EvoSettingsScreen(parent, Section.CHAT))
        ));

        addDrawableChild(new EvoButton(
                navX, navY + navH + 6, navW, navH,
                "Автосообщение",
                btn -> client.setScreen(new EvoSettingsScreen(parent, Section.AUTO_MESSAGE))
        ));

        if (section == Section.CHAT) {

        // ── CHAT SECTION UI ────────────────────────────────────────────────

            // Row 1: Opacity slider
            int row1 = py + 80;
            addDrawableChild(new EvoSlider(
                    sliderX, row1, sliderW, 20,
                    "Прозрачность чата", 0.1f, 1.0f, cfg.chatOpacity,
                    v -> String.format("%.0f%%", v * 100),
                    v -> { cfg.chatOpacity = v; cfg.save(); }
            ));

            // Row 2: Background opacity slider
            int row2 = row1 + 60;
            addDrawableChild(new EvoSlider(
                    sliderX, row2, sliderW, 20,
                    "Прозрачность фона", 0.0f, 1.0f, cfg.backgroundOpacity,
                    v -> String.format("%.0f%%", v * 100),
                    v -> { cfg.backgroundOpacity = v; cfg.save(); }
            ));

            // Row 3: Size sliders
            int row3 = row2 + 60;
            int halfW = (sliderW - 12) / 2;

            addDrawableChild(new EvoSlider(
                    sliderX, row3, halfW, 20,
                    "Ширина", 160f, 600f, cfg.chatWidth,
                    v -> (int) v + "px",
                    v -> { cfg.chatWidth = Math.round(v); cfg.save(); }
            ));

            addDrawableChild(new EvoSlider(
                    sliderX + halfW + 12, row3, halfW, 20,
                    "Высота", 80f, 400f, cfg.chatHeight,
                    v -> (int) v + "px",
                    v -> { cfg.chatHeight = Math.round(v); cfg.save(); }
            ));

            // Row 4: Position presets + arrow nudge
            int row4 = row3 + 65;
            int btnW = 80;
            int gap  = 8;

            String[] presetLabels = { "Лево-верх", "Право-верх", "Лево-низ", "Право-низ" };
            float[][] presets = {
                    { 0.01f, 0.05f }, { 0.70f, 0.05f },
                    { 0.01f, 0.75f }, { 0.70f, 0.75f }
            };

            for (int i = 0; i < 4; i++) {
                final int idx = i;
                final float px2 = presets[i][0], py2 = presets[i][1];
                addDrawableChild(new EvoButton(
                        sliderX + i * (btnW + gap), row4, btnW, 18,
                        presetLabels[i],
                        btn -> {
                            cfg.chatX = px2;
                            cfg.chatY = py2;
                            cfg.save();
                            selectedPreset = idx;
                        }
                ));
            }

            // Arrow nudge
            int arrowY = row4 + 28;
            int arrowSize = 18;
            int midX = sliderX + sliderW / 2;

            btnUp    = addDrawableChild(new EvoButton(midX - 10, arrowY,
                    arrowSize, arrowSize, "\u25B2",
                    b -> { cfg.chatY = Math.max(0f, cfg.chatY - 0.02f); cfg.save(); }));
            btnDown  = addDrawableChild(new EvoButton(midX - 10, arrowY + arrowSize + 2,
                    arrowSize, arrowSize, "\u25BC",
                    b -> { cfg.chatY = Math.min(0.9f, cfg.chatY + 0.02f); cfg.save(); }));
            btnLeft  = addDrawableChild(new EvoButton(midX - 10 - arrowSize - 2, arrowY + (arrowSize + 2),
                    arrowSize, arrowSize, "\u25C4",
                    b -> { cfg.chatX = Math.max(0f, cfg.chatX - 0.02f); cfg.save(); }));
            btnRight = addDrawableChild(new EvoButton(midX - 10 + arrowSize + 2, arrowY + (arrowSize + 2),
                    arrowSize, arrowSize, "\u25BA",
                    b -> { cfg.chatX = Math.min(0.9f, cfg.chatX + 0.02f); cfg.save(); }));

            // Row 5: Sound toggle
            int row5 = row4 + 32 + arrowSize * 2 + 14;
            addDrawableChild(new EvoToggle(
                    sliderX, row5,
                    "Звук сообщения",
                    cfg.soundEnabled,
                    v -> { cfg.soundEnabled = v; cfg.save(); }
            ));
        } else {
            // ── AUTO-MESSAGE SECTION UI ─────────────────────────────────────

            int row1 = py + 90;

            // Master toggle
            addDrawableChild(new EvoToggle(
                    sliderX, row1,
                    "Включить автосообщение",
                    cfg.autoMessageEnabled,
                    v -> { cfg.autoMessageEnabled = v; cfg.save(); }
            ));

            // Interval sliders: minutes & seconds
            int row2 = row1 + 40;
            int halfW = (sliderW - 12) / 2;
            int currentMinutes = cfg.autoMessageIntervalSeconds / 60;
            int currentSeconds = cfg.autoMessageIntervalSeconds % 60;

            addDrawableChild(new EvoSlider(
                    sliderX, row2, halfW, 20,
                    "Минуты", 0f, 30f, currentMinutes,
                    v -> (int) v + " мин",
                    v -> {
                        int mins = Math.round(v);
                        cfg.autoMessageIntervalSeconds = mins * 60 + (cfg.autoMessageIntervalSeconds % 60);
                        cfg.save();
                    }
            ));

            addDrawableChild(new EvoSlider(
                    sliderX + halfW + 12, row2, halfW, 20,
                    "Секунды", 0f, 59f, currentSeconds,
                    v -> (int) v + " сек",
                    v -> {
                        int secs = Math.round(v);
                        int mins = cfg.autoMessageIntervalSeconds / 60;
                        cfg.autoMessageIntervalSeconds = mins * 60 + secs;
                        cfg.save();
                    }
            ));

            // Auto-message text field
            int row3 = row2 + 50;
            autoMessageField = new TextFieldWidget(
                    client.textRenderer,
                    sliderX, row3, sliderW, 20,
                    Text.of("AutoMessage"));
            autoMessageField.setText(cfg.autoMessageText == null ? "" : cfg.autoMessageText);
            autoMessageField.setChangedListener(text -> {
                cfg.autoMessageText = text;
                cfg.save();
            });
            addSelectableChild(autoMessageField);
            addDrawableChild(autoMessageField);

            // Widget visibility toggle
            int row4 = row3 + 40;
            addDrawableChild(new EvoToggle(
                    sliderX, row4,
                    "Показывать виджет на экране",
                    cfg.autoMessageWidgetVisible,
                    v -> { cfg.autoMessageWidgetVisible = v; cfg.save(); }
            ));

            // Widget position nudge using arrows
            int row5 = row4 + 32;
            int arrowSize = 18;
            int midX = sliderX + sliderW / 2;

            addDrawableChild(new EvoButton(midX - 10, row5,
                    arrowSize, arrowSize, "\u25B2",
                    b -> { cfg.autoMessageWidgetY = Math.max(0f, cfg.autoMessageWidgetY - 0.02f); cfg.save(); }));
            addDrawableChild(new EvoButton(midX - 10, row5 + arrowSize + 2,
                    arrowSize, arrowSize, "\u25BC",
                    b -> { cfg.autoMessageWidgetY = Math.min(0.95f, cfg.autoMessageWidgetY + 0.02f); cfg.save(); }));
            addDrawableChild(new EvoButton(midX - 10 - arrowSize - 2, row5 + (arrowSize + 2),
                    arrowSize, arrowSize, "\u25C4",
                    b -> { cfg.autoMessageWidgetX = Math.max(0f, cfg.autoMessageWidgetX - 0.02f); cfg.save(); }));
            addDrawableChild(new EvoButton(midX - 10 + arrowSize + 2, row5 + (arrowSize + 2),
                    arrowSize, arrowSize, "\u25BA",
                    b -> { cfg.autoMessageWidgetX = Math.min(0.95f, cfg.autoMessageWidgetX + 0.02f); cfg.save(); }));
        }
        // ── Close button ──────────────────────────────────────────────────
        int closeY = py + PANEL_H - 30;
        addDrawableChild(new EvoButton(
                px + PANEL_W / 2 - 60, closeY, 120, 20,
                "Закрыть",
                btn -> close()
        ));

        // Clear chat button
        addDrawableChild(new EvoButton(
                px + PANEL_W / 2 + 66, closeY, 80, 20,
                "Очистить",
                btn -> EvoChatWindow.getInstance().clear()
        ));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim background
        ctx.fill(0, 0, width, height, 0xAA000000);

        // Outer glow / shadow (semi-transparent cyan border)
        ctx.fill(px - 2, py - 2, px + PANEL_W + 2, py + PANEL_H + 2,
                0x1F00D4C8);

        // Main panel
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, COL_PANEL);

        // Top accent bar
        ctx.fill(px, py, px + PANEL_W, py + 3, COL_ACCENT);

        // Corner dots
        ctx.fill(px,              py,              px + 6,           py + 6,           COL_ACCENT);
        ctx.fill(px + PANEL_W - 6, py,              px + PANEL_W,     py + 6,           COL_ACCENT);
        ctx.fill(px,              py + PANEL_H - 6, px + 6,           py + PANEL_H,     COL_ACCENT);
        ctx.fill(px + PANEL_W - 6, py + PANEL_H - 6, px + PANEL_W,   py + PANEL_H,     COL_ACCENT);

        // Panel border
        ctx.fill(px, py, px + 1,           py + PANEL_H, 0xFF21262D);
        ctx.fill(px + PANEL_W - 1, py, px + PANEL_W, py + PANEL_H, 0xFF21262D);
        ctx.fill(px, py + PANEL_H - 1, px + PANEL_W, py + PANEL_H, 0xFF21262D);

        // ── Header ────────────────────────────────────────────────────────
        var tr = client.textRenderer;

        // Big title
        String title = "EvoChat";
        int titleX = px + PANEL_W / 2 - tr.getWidth(title) * 2 / 2;
        ctx.getMatrices().push();
        ctx.getMatrices().scale(2f, 2f, 1f);
        ctx.drawText(tr, title, titleX / 2, (py + 16) / 2, COL_ACCENT, false);
        ctx.getMatrices().pop();

        // Sub-label
        String sub = section == Section.CHAT
                ? "Настройки приватного чата"
                : "Настройки автосообщения";
        ctx.drawText(tr, sub,
                px + PANEL_W / 2 - tr.getWidth(sub) / 2,
                py + 48, COL_MUTED, false);

        // Divider after header
        ctx.fill(px + 20, py + 60, px + PANEL_W - 20, py + 61, COL_DIVIDER);

        // ── Section labels ────────────────────────────────────────────────
        drawSectionLabel(ctx, tr, "", px + 20, py + 68);
        drawSectionLabel(ctx, tr, "",
                px + 20, py + 68 + 55 + 55 + 5);
        drawSectionLabel(ctx, tr, "",
                px + 20, py + 68 + 55 + 55 + 55 + 5);
        drawSectionLabel(ctx, tr, "ЗВУК",
                px + 20, py + 68 + 55 + 55 + 55 + 90);

        // Dividers between sections
        int[] divYOffsets = { 130, 185, 240 };
        for (int dy : divYOffsets) {
            ctx.fill(px + 20, py + dy, px + PANEL_W - 20, py + dy + 1, COL_DIVIDER);
        }

        // ── Live position readout ─────────────────────────────────────────
        String pos = String.format("X: %.2f  Y: %.2f", cfg.chatX, cfg.chatY);
        ctx.drawText(tr, pos,
                px + PANEL_W / 2 + 30,
                py + 68 + 55 + 55 + 55 + 30 + 4,
                COL_MUTED, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawSectionLabel(DrawContext ctx,
                                   net.minecraft.client.font.TextRenderer tr,
                                   String text, int x, int y) {
        ctx.drawText(tr, text, x, y, COL_ACCENT2, false);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
