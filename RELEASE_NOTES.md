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
