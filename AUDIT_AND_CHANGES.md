# PvP HUD Client — Audit and Improvements

## Implemented

- Added smooth 320 ms theme color transitions for accent, text, muted, danger, and glow colors.
- Added player hit detection and a fading center-screen hit marker.
- Added damage detection and a brief red edge flash.
- Added a lightweight center-screen particle burst when a hit kills a player.
- Added settings toggles for animated themes, hit marker, damage flash, and kill particles.
- Added working mouse-wheel scrolling with bounded offsets for small settings screens.
- Replaced the hand-built 5x5 pixel-block logo with a cleaner scaled Minecraft-font logo.
- Changed the default editor key from Left Shift to Right Shift to avoid opening the editor while sneaking.
- Replaced CPS front-removal lists with `ArrayDeque` queues.
- Restored missing CPS/movement config fields referenced by the settings screen.
- Made the CPS and right-CPS settings affect the combat widget.
- Added English and Dutch translations for all new settings.

## Bugs found

1. The settings screen had no `mouseScrolled` override, so scrolling was impossible.
2. Four settings fields (`showCps`, `showRightCps`, `showSprint`, `showReach`) were referenced but missing from `HudConfig`, which is a compile-breaking source mismatch.
3. Theme colors were read directly from the enum everywhere, making animation impossible.
4. Left Shift was an unsafe default keybind for a PvP settings screen because it conflicts with sneaking.
5. CPS tracking removed index zero repeatedly, causing unnecessary list shifting.
6. Several settings existed in the UI but did not affect rendering; CPS toggles now work.
7. The PVPES header was rendered using many individual fill calls and a very limited 5x5 bitmap alphabet.

## Build status

The source was patched and structurally checked. A full Gradle build could not run in the editing environment because the wrapper attempted to download Gradle 9.5.0 from `services.gradle.org`, while outbound network access was unavailable.

Build locally with Java 21:

```powershell
.\gradlew.bat build
```

The finished JAR should appear at:

```text
build/libs/pvp-hud-client-1.0.0.jar
```

## Notes

- Hit confirmation is detected client-side by watching the targeted player's hurt timer shortly after an attack click. This avoids mixins and keeps the feature lightweight.
- Kill particles trigger when the confirmed target is no longer alive or reaches zero health.
- The logo is cleaner but is not a custom PNG asset because no finished logo artwork was supplied.
