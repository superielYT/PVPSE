# PVPSE Client v2.3.0

## Rebuilt PvP art
- Shorter broad-edged swords with material-matched guards and inset details.
- Dark forged mace with a luminous wind core and wrapped handle.
- Cute golden and enchanted apples; moon-themed ender pearl.
- Keeps the lucky-cat totem, low shields, eight bow charge stages and both 32x/16x editions.

## Visible client access and HUD recovery
- PVPSE button on the title and pause screens.
- Right Shift, F8 and /pvpse open the menu; /pvpse restore restores HUD visibility.
- Explicit HUD ON/OFF control and confirmed Restore HUD action in the sidebar.
- Restore enables and repositions the current profile's HUD, clears F1 hiding and disables hide-on-F3.
- HUD toggle feedback and a first-join message explain how to open the client.
- Menu closes back to its originating screen. Editor is clearly unavailable outside a world.
- Correct Minecraft version in the watermark.

## Buttons and checks
- Rounded, theme-aware buttons with animated hover, keyboard focus, native input and narration.
- Themed module cards with clear toggle thumbs.
- GUI and singleplayer HUD game tests capture screenshots and check actual watermark pixels.
- Minecraft Java 26.1.2; Java 25; Fabric Loader 0.19.3+; matching Fabric API.
- Install PVPSE-2.3.0.jar in mods. A resourcepack ZIP alone cannot add the GUI or HUD.

---

# PVPSE Client v2.2.0

## HUD themes and style
- Optional Clear Glass theme alongside every existing theme.
- Refined shared panel surfaces, softer glow, subtle accent lines and readable text.
- New STYLE tab: HUD opacity (20–100%), accent lines, panel shadows and text shadows.
- Minimal HUD preset and one-click style reset.
- Appearance preferences are saved per profile and copied with the current profile.
- Fixed overlapping translucent rounded panels and forced glass opacity.

## Downloads and requirements
- Minecraft Java 26.1.2, Java 25, Fabric Loader 0.19.3+ and matching Fabric API.
- Install PVPSE-2.2.0.jar in mods.
- Includes Ignite 32x Smooth and 16x Lite packs. Enable one edition.
- Automated build, regression, pack and client startup checks run before publication.
- Full gameplay and visual testing still needs to be done in Minecraft.

---

# PVPSE Client v2.1.0

## Cooler, smoother Ignite packs
- New original shaded art: short swords with beveled blades, glossy PvP food, lucky-cat totem, pearl, XP, crystal, mace and open cobweb.
- Smooth 32x and lightweight 16x editions share the same style. Enable one at a time.
- Eight native bow drawing stages, with green reserved for the full 20-tick draw.
- Low shields retain the Minecraft 26.1.2 transformation and vanilla banner renderer.

## Bug fixes
- Valid gamma/brightness range; existing profiles and keybind IDs preserved.
- Atomic settings, crosshair and theme saves, with malformed global-config backups.
- Invalid crosshair imports are validated completely before applying; imported colors stay opaque.
- Removed legacy low-health notifications that ignored the new switch.
- Reset feedback across player changes and watch delayed target death for particles.
- Confirm reach before displaying it; reset combos across targets.
- Reset incomplete profile layouts and missing themes to defaults.
- Fix percentage opacity and clamp invalid crosshair settings.
- Prevent clicks on controls hidden during expansion; constrain potion-card rows.

## Checks and installation
- Minecraft Java 26.1.2, Java 25, Fabric Loader 0.19.3+ and Fabric API 0.155.2+26.1.2.
- Put PVPSE-2.1.0.jar in mods and ONE Ignite ZIP in resourcepacks.
- Includes regression, geometry, pack integrity and real-client startup checks.
- Startup testing does not cover full gameplay, multiplayer or every mod combination.

---

# PVPSE Client v2.0.0

## Minecraft 26.1.2
- Requires Minecraft Java 26.1.2, Java 25, Fabric Loader 0.19.3+ and Fabric API 0.155.2+26.1.2.
- Migrated Minecraft references to the official unobfuscated names.
- Updated HUD, custom screens, key mappings and world rendering integration.
- Retains the cleaner menus, PvP indicators, sword/mace hitbox display, profiles and waypoints from 1.9.

## Ignite 16x for 26.1.2
- Resource pack format 84 with the updated shield coordinate transformation.
- Short swords, lowered shields, lucky-cat totem, PvP food and clear bow charge stages.
- Download the ZIP from this release and enable it in Resource Packs.

## Installation
- Put PVPSE-2.0.0.jar in the mods folder of a Fabric 26.1.2 instance alongside matching Fabric API.
- Put PVPSE-Ignite-16x-26.1.2.zip in resourcepacks and enable it.
- PVPSE 1.9.0 remains available for Minecraft 1.21.11.
- Minecraft in-game visual playtesting is still required.

---

# PVPSE Client v1.9.0

## Bow and melee follow-up
- Orange/gold/green bow charge textures, with green at full 20-tick draw.
- Interpolated sword/mace hitbox outlines, optional eye-level rings and 1–4 line width.
- Separate sword/mace switches, cooldown/falling colors and optional nearby-visible mode.
- Hitboxes respect HUD visibility and use eye-to-box display distance.


## Cleaner customization
- Navigation sidebar with HUD editor, crosshair studio, waypoints, settings and themes.
- Searchable ALL / HUD / TOOLS / ON module filters; counts and scroll reset per filter.
- Module names fit their rows. Settings use General, Combat, Effects and Profiles tabs.

## PvP visibility and settings
- Optional attack cooldown bar beneath the crosshair, visible only while recharging.
- Optional low-health alert (2–16 HP; two HP per heart, excluding absorption).
- Optional equipped-armor alert (5–50% remaining durability).
- Adjustable hit marker size, damage-flash opacity and kill-particle count.
- Minimal Effects preset turns off flash and particles while keeping hit markers.
- Combat feedback respects the HUD toggle and hides while menus are open.
- Alerts/effects are global; HUD and theme profiles keep their existing scope.

## Ignite 16x resource pack
- Minecraft Java 1.21.11 / pack format 75.0.
- Seven short swords, lucky-cat totem, golden carrot, golden and enchanted apples,
  ender pearl, XP bottle, end crystal and high contrast cobweb.
- Lower shield for both hands while resting and blocking, retaining banner rendering.
- Original pixel art, editable source, deterministic Node generator and ready-to-use ZIP.
- Download dist/PVPSE-Ignite-16x-1.21.11.zip or the resource-pack Actions artifact.

## Permissions
- All-rights-reserved notice for owner-controlled material, with owner-directed
  ChatGPT/Codex and Copilot assistance; prior MIT and third-party rights remain intact.

## Validation
- Java compilation, existing radar tests and pack generation run in GitHub Actions.
- ZIP integrity is checked independently with Python's zipfile.
- Minecraft in-game appearance and shield positioning still need visual playtesting.

---

# PVPSE Client v1.8.0

## General usability and stability

- Main navigation now uses keyboard-focusable buttons, retains search text, shows enabled module count and explains module controls.
- New SETTINGS & PROFILES screen: HUD, notifications, hide-on-debug, damage tilt, hit marker, damage flash, kill particles and crosshair switches.
- Copy the current HUD/theme to a named profile and cycle saved profiles. The active profile survives restarting.
- Profile files now live in a dedicated profiles directory, separate from waypoint and crosshair JSON. Existing default profiles migrate automatically.
- Malformed profile files are backed up before fallback recovery.
- Drag scale, position and utility-value sliders; the + button expands module settings.
- Keep HUD positions within the current screen. Utility modules no longer clutter the HUD editor.
- HUD editor: arrow keys nudge the selection, Shift moves 10 pixels, R restores the selected layout, Ctrl+Z restores the session's initial layout and visibility.
- F1 hides the overlays; F3 can hide them independently. Disabling the client HUD restores the vanilla crosshair.
- Zoom releases when opening a screen; original FOV, gamma and damage tilt are restored on client shutdown.
- Keybind capture consumes Escape to cancel, supports Backspace/Delete to unbind and saves bindings on clean shutdown.
- Target names and notification text fit their cards; notifications respect smaller screens.
- Added HUD boundary checks alongside radar direction, zoom and edge tests.

Build and automated checks pass. A full Minecraft gameplay/visual test has not been performed.

# PVPSE Client v1.7.0

## Waypoints and radar

- New waypoint controls: track a destination, cycle six marker colors, hide/show points, copy coordinates.
- Automatic death-position markers, with an on/off setting.
- New points belong to their server or singleplayer world and dimension. Legacy points remain available and are marked in the list.
- Fixed-scale waypoint radar with grid, player heading and range choices from 32 to 512 blocks.
- Choose rotating radar or north-up; distant points clamp to the edge without losing direction.
- Tracked destination takes priority; otherwise the nearest visible waypoint is shown first.
- On-screen labels show horizontal distance, vertical offset and behind-you indicators; labels can be disabled separately.
- Scrollable waypoint list uses standard keyboard-focusable action buttons and confirms clearing all points.
- Atomic waypoint saves protect against interrupted writes.

The minimap is a waypoint radar, without terrain or entity scanning.

# PVPSE Client v1.6.0

Final PvP HUD release for Fabric 1.21.11 and Java 21.

## Melee hitbox practice

- New **Melee Hitboxes** module in the scrollable HUD elements panel
- Shows a precise 3D wireframe only for the living entity under the crosshair
- Activates only while holding a sword or mace
- Adjustable visual range (2–12 blocks) in the module settings
- Client-side visualization only: no reach, attack, aiming, or packet changes

## Settings and polish

- Per-widget X and Y position controls
- Reset position and scale for individual HUD widgets
- Reject non-finite positions, scales and utility values
- Handle incomplete profile data safely
- Fix the release workflow to publish the 1.6.0 JAR

## Included

- Full draggable HUD and editor
- Target, armor, potion, CPS, combo, reach, ping, FPS, session and keystroke widgets
- Custom crosshair editor, themes, waypoints and profile-backed settings

---

# PVPSE Client v1.5.0

Waypoint and keybind release for Fabric 1.21.11 and Java 21.

## Waypoint system

- New WAYPOINTS button in the main ClickGUI
- Type a custom name before adding a waypoint
- Select an existing waypoint and change its name
- Delete individual waypoints or clear the complete list
- Scrollable waypoint list
- Persistent waypoints separated by dimension
- Labeled on-screen markers show waypoint name and distance
- Movable/scalable HUD with a rotating waypoint minimap

## Keybind settings

- Right-click Zoom, Waypoint, or Fullbright in the main GUI
- Click the Keybind setting
- The button displays LISTENING...
- Press any keyboard key to assign it
- Escape cancels key capture

## Included

- Draggable crosshair scrollbar
- Zoom, No Pumpkin, Fullbright, damage tilt 0
- Combo, reach, low-health and all existing HUD features
