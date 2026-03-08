# EvoChat — Fabric Mod for Minecraft 1.21.4

A sleek private-message overlay mod.  
Every chat message that starts with **ЛС** appears in a separate floating window.

---

## Features

| Feature | Details |
|---|---|
| ЛС filter | Captures any message whose text starts with `ЛС` (case-insensitive) |
| Overlay window | Always-on-top dark-glass panel rendered on the HUD |
| Settings screen | Press `]` / `Ъ` (re-bindable in Options → Controls → EvoChat) |
| Transparency | Two sliders: chat opacity & background opacity |
| Position | 4 preset corner buttons + arrow-nudge buttons (2 % steps) |
| Size | Width and height sliders |
| Sound | Toggle notification ping on/off |
| Fade-in animation | New messages fade in with a highlight on the newest line |
| Badge | Unread count badge in the header |

---

## Building

**Requirements:** JDK 21, internet connection (Gradle downloads dependencies automatically).

```bash
cd evochat
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

The compiled JAR will be at:
```
evochat/build/libs/evochat-1.0.0.jar
```

Place it in your `.minecraft/mods/` folder alongside **Fabric API**.

---

## File Structure

```
evochat/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradle/wrapper/
└── src/main/
    ├── java/com/evochat/
    │   ├── EvoChatClient.java          ← entry point, key binding, HUD hook
    │   ├── chat/
    │   │   ├── ChatMessage.java        ← message model with alpha animation
    │   │   └── EvoChatWindow.java      ← HUD overlay renderer
    │   ├── config/
    │   │   └── EvoConfig.java          ← JSON config persisted to .minecraft/config/
    │   ├── gui/
    │   │   ├── EvoButton.java          ← custom dark button widget
    │   │   ├── EvoSettingsScreen.java  ← settings screen (Screen subclass)
    │   │   ├── EvoSlider.java          ← custom slider widget
    │   │   └── EvoToggle.java          ← pill toggle widget
    │   ├── mixin/
    │   │   └── ChatHudMixin.java       ← intercepts incoming chat messages
    │   └── sound/
    │       └── EvoChatSound.java       ← notification sound with debounce
    └── resources/
        ├── fabric.mod.json
        ├── evochat.mixins.json
        └── assets/evochat/lang/
            ├── en_us.json
            └── ru_ru.json
```

---

## Configuration

Config is stored at `.minecraft/config/evochat.json` and saved automatically
whenever you change a setting in the GUI.

```json
{
  "chatX": 0.70,
  "chatY": 0.05,
  "chatWidth": 320,
  "chatHeight": 200,
  "chatOpacity": 0.85,
  "backgroundOpacity": 0.65,
  "soundEnabled": true,
  "maxMessages": 100,
  "fontScale": 1.0,
  "windowVisible": true
}
```

---

## Required Dependencies (runtime)

- **Fabric Loader** >= 0.16.0
- **Fabric API** (any version compatible with 1.21.4)
- **Java 21**
