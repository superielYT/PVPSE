# PVPSE v3.5.0 — Ignite Radiant

- 60 native enchantment variants across swords, axes and bows. Fire Aspect, Sharpness, Smite, Bane, Efficiency, Flame and Power have distinct accents. Existing mace/book variants and full bow charge stages remain.
- 18 ore models have native emissive mineral facets; surrounding stone stays normally shaded. No shader or extra mod required for ore glow. Covered ores remain covered, and the effect does not cast light.
- Expanded coordinated building textures include sandstone, quartz, prismarine, purpur, polished stone, copper and clearer glass.
- 27 smaller combat/potion/totem particle sprites preserve vanilla timing and colors.
- Three original quiet sound cues replace totem use, pearl throws and item breaks (tools and armor).
- Darker inventory, chest, crafting, shulker and furnace-family GUI, with readable native title chips.
- Refined mod backdrop, sidebar, module rows and button accents. Settings use two readable tab rows and compact spacing.
- Previous HUD layouts, visibility choices, previews and sword/mace-only hitboxes remain intact. Photographic sky still needs its separate custom-sky loader; native ore glow does not.
- Pack generation validates PNG/JSON/OGG data and enchantment predicates. World tests verify loaded assets, sound events, rendered ore glow in an unlit room, terrain, weapon variants and menu screenshots.

---

# PVPSE v3.4.0 - Clean Ember

- Cleaner HUD cards and key tiles with restrained PVPSE ember accents, flatter surfaces and lighter shadows. Existing layouts, scales, hidden cards and theme choices stay intact.
- Midnight becomes Midnight Ember: charcoal panels, warm accents, cool secondary details and more readable muted text. Select it through the theme picker or Settings > ITEMS > MIDNIGHT EMBER.
- Shulker hover previews preserve all 27 slots. Bundle previews show actual contents, selected stack and capacity. Hidden tooltip components remain hidden; normal bundle interactions stay vanilla.
- Held-food forecasts appear directly on the hunger bar: cyan hunger outlines, dashed pale-gold saturation preview, solid yellow current saturation and a numeric gain label. Uses actual food components and caps; unavailable multiplayer saturation stays unknown.
- Block Overlay is now a world-space outline and light tint on the actual selected block shape, with no extra HUD card. Toggle under GENERAL.
- The movable waypoint HUD shows distance, height difference and direction, without an extra floating card. Shift+B marks a temporary visible block target up to 128m away, expiring after five minutes or leaving the world/dimension. B still saves your current position.
- Armor shows per-slot percentage, exact remaining durability and a calm low-durability warning using the configured threshold.
- Clean hotbar frame, readable counts, durability strips and real cooldown seconds. Toggle under ITEMS.
- General hitboxes are clearer while not aiming and have adjustable background opacity. They appear only while holding an enabled sword or mace; food, empty hands and other items do not activate them. Target-only stays optional and normal visibility/depth checks remain active. Visual bounds do not change attack reach.
- Build, startup and world regression checks include previews, exact saturation forecasts, route math, non-aimed hitboxes, native tooltip composition and persistent controls.

---

# PVPSE v3.3.0 - Vanilla HUD integration

- Potion timers sit inside the existing vanilla effect icons, following vanilla ordering, beneficial/harmful rows and visibility. Infinite effects show infinity; short durations turn amber/red.
- Saturation appears as a yellow outline on the existing hunger icons, with half icons for odd values. Exact integrated-server values in singleplayer; no false saturation overlay on multiplayer servers that do not synchronize it.
- Removed the separate Food & Saturation and Potions cards. Other HUD visibility/layout choices stay intact.
- Settings > GENERAL: independent Potion timers and Yellow saturation switches, searchable in English and Dutch, saved between sessions.
- World checks cover integrated-server saturation, hunger-bar pixels and persistent toggles. Existing GUI, HUD, hitbox, fullbright and pack checks retained.

---

# PVPSE v3.2.0 - Food and general hitboxes

- Optional Food & Saturation card in HUD editor > HUDs. Move, scale or hide it. Exact saturation is available in singleplayer; multiplayer servers do not normally synchronize it, so unknown is shown there.
- Existing configs switch once from target-only to general hitbox visibility. The Target only setting remains available; later choices are preserved. Weapon filters and wall visibility checks remain active.

---

# PVPSE Client v3.1.2 - Bug audit

- Failed HUD auto-arrange attempts now leave existing positions and scales intact. The HUD picker keeps a newly enabled card off if even auto-arrange cannot make room.
- Fullbright remains stable while a real Night Vision effect fades; disabling fullbright preserves the actual potion effect.
- Hitbox view distance is searchable with English/Dutch distance and reach terms.
- Added world regression checks for impossible layouts and expiring real Night Vision. Existing HUD/editor, settings, texture-pack, hitbox distance, wall occlusion, dark-room and performance checks retained.

---

# PVPSE Client v3.1.1 - Fullbright and hitbox distance fixes

- Fix Target-only hitboxes using vanilla attack picking: independently select visible entities up to the configured view distance, stopping at blocks. Range now goes up to 32 blocks and is available in Settings > HITBOX. Actual attack reach is unchanged.
- Replace ordinary brightness adjustment with client rendering night vision. Does not create or remove potion effects or change the saved gamma option. Toggle with V or the Brightness module; strength remains adjustable.
- In-world checks cover targets at 12/32 blocks, shorter-range exclusion, wall occlusion, actual dark-room screenshot brightness, and fullbright disable restoration.

---

# PVPSE Client v3.1.0 - Ignite Reforged

Minecraft 26.1.2 / Fabric / Java 25.

- Resourcepack update inspired by the supplied screenshots: 43 illustrated enchantment books with large individual symbols and leather covers. Native component predicates work at any level, including combined books.
- Broad blue diamond blades and matching axes. Steel mace plus cyan Breach, red Density and violet Wind Burst variants; no renaming or CIT required.
- Brighter crystal icon and a new glass/magenta world-crystal texture.
- 149 upgraded common block textures: stone families, ores, wood planks/logs, natural ground/leaves, wool, concrete, terracotta and mineral blocks. Other blocks fall back to vanilla.
- Lower first-person shield while idle and blocking, with vanilla banner rendering retained.
- Animated low fire and soul fire. The upper 35% of every texture frame stays transparent, leaving the first-person fire low on screen; placed world fire also looks shorter.
- Round native sun and eight detailed moon phases using the Minecraft 26.1.2 celestial texture paths.
- Original photographic cloud sky supplied in OptiFine custom-sky format. **The full cloud sky requires a separate compatible sky loader, such as Skyboxify for Fabric 26.1.2.** Without a loader, Minecraft uses the upgraded native sun/moons and normal sky. No sky loader is bundled.
- Choose either Smooth 32x or Lite 16x. All HUD options and profiles from 3.0 remain available.

---
# PVPSE Client v3.0.0 - Combat polish

For Minecraft 26.1.2 / Fabric, Java 25.

- Subtle one-pixel attack cooldown circle around the crosshair, visible only while charging. Adjustable radius and independent toggle in Settings > AIM.
- Crosshair reactions: amber while charging, theme accent when ready, target color when aiming at a player. Includes custom drawn crosshairs.
- Optional Health Indicator HUD with current/max HP, low-health color and separate absorption points. Move, resize or hide it through the HUD editor; existing hidden cards stay hidden.
- Clearer target HP readout with maximum health and low-health color.
- Cleaner melee hitboxes: emphasized selected target, thin subdued nearby outlines and eye-level ring only on the selected target. Bounds, line-of-sight filtering and depth-tested rendering retained.
- Waypoint radar: north-up compass labels, clearer tracked destination frame, outlined waypoint dots and above/below markers. This remains a waypoint radar rather than a terrain map.
- Existing FPS presets, themes and resource packs included.

---
# PVPSE Client v2.7.0

- FPS tab with opt-in Balanced and Max FPS presets. Balanced caps view/simulation at 12/8; Max at 8/5. Both disable clouds/entity shadows and reduce particles, keeping already-lower distances.
- Restore previous settings keeps manual changes made after enabling a preset. The original snapshot survives restarts; VSync and frame limits are untouched.
- Optional Light HUD removes decorative panel shadows/glass layers while retaining themes, text, alerts, positions and visibility.
- Rounded corners use cached geometry and merged rows with identical pixel coverage and fewer fill rectangles, including outside performance mode.
- Inventory Counter caches icons/labels and counts all six item types in one pass per game tick instead of rescanning every frame.
- Opaque client menus skip hidden HUD rendering behind their panels.
- Settled animations skip repeated easing calculations. Unchanged settings/profile saves no longer rewrite files.
- Tests cover native options and restoration, restart snapshots, geometry coverage, unchanged saves and all existing GUI/HUD controls. Actual FPS gain depends on hardware and scene; no fixed FPS increase is claimed.

Minecraft Java 26.1.2 / Java 25 / Fabric Loader 0.19.3+ / matching Fabric API.
Replace the previous JAR with PVPSE-2.7.0.jar. Find performance presets at F8 > Settings & profiles > FPS.

---

# PVPSE Client v2.6.0

- Optional Held Items HUD: main/offhand names, stack counts, exact remaining durability and low-durability colors/bars.
- Optional Resource Packs HUD: active user file packs, highest-priority pack first, with a clear vanilla fallback.
- Both new cards start OFF, including on older profiles. Enable them in HUD editor > HUDs; first activation searches for free space.
- If the canvas is full, first activation offers to arrange shown cards or keep the new card off; custom layouts are never silently overwritten.
- Search the HUD picker by name or ID; filter All, Shown or Hidden. The heading shows how many cards are enabled.
- Auto arrange now only places visible cards and preserves hidden cards' positions and sizes.
- Gameplay rendering no longer clamps hidden cards into the viewport, preserving saved layouts and first-activation placement.
- Toolbar help and picker footer fit smaller windows; all earlier full-screen editing, close and restore controls remain.
- In-world tests cover optional defaults, hidden layout preservation, real typing/filtering, equipped item durability and active pack detection.

Minecraft Java 26.1.2 / Java 25 / Fabric Loader 0.19.3+ / matching Fabric API.
Replace the previous JAR with PVPSE-2.6.0.jar. Existing Ignite texturepacks remain compatible.

---

# PVPSE Client v2.5.1

- HUD editor now allows the full screen, including the exact bottom edge. Toolbar moved to the top; Tab hides it so every edge stays accessible.
- X on a card, Hide, Delete or right-click removes it immediately from the canvas and gameplay.
- HUDs opens an explicit ON/OFF picker with a Hidden filter to bring cards back. Visibility saves with your profile.
- Disabled cards are no longer forced visible in the editor and cannot intercept dragging, resizing or snapping.
- Restore HUD keeps your hidden-card choices instead of turning every card back on.
- Client game tests exercise bottom-edge dragging, hiding, restoring from the picker, close buttons and disabled rendering.

Minecraft Java 26.1.2 / Java 25 / Fabric Loader 0.19.3+ / matching Fabric API.
Replace the previous mod JAR with PVPSE-2.5.1.jar. Texturepacks are unchanged.

---

# PVPSE Client v2.5.0

- Block Overlay: a movable/resizable card with localized block name, item icon, registry ID, position and distance from the actual crosshair target. Theme-colored outlines follow block selection shapes, including slabs and stairs.
- Search for Block Overlay or "blok aankijken" in settings; one switch controls both card and outline. Included in all three PvP presets.
- New cards fit into free space when older profiles load, preserving existing card positions and scales.
- In-world checks cover target changes, slabs, looking at the sky and entity targets.

- Ignite Final: slimmer forged swords, matching axes, warmer apples and moonstone pearl; lucky-cat totem retained.
- Dark resource-pack GUI: hotbar, offhand frames, XP bar, vanilla buttons, inventory, chests and crafting table. Title strips preserve vanilla label contrast.
- Original covers for all 43 vanilla enchantments, including Lunge. Native component predicates support every level and combined books without CIT, OptiFine or renaming; BOOKS.txt lists combined-book priority.
- 55 coordinated block textures: stone, obsidian, ores, planks and all wool colors.
- 32x Smooth and 16x Lite packs share model routing; GUI uses native dimensions.
- CI loads the actual pack and captures book covers, dark inventory and placed blocks, alongside existing HUD/input checks.

Minecraft Java 26.1.2 / Java 25 / Fabric Loader 0.19.3+ / matching Fabric API.
Install PVPSE-2.5.0.jar in mods. Enable ONE Ignite ZIP in Resource Packs above other packs.

---

# PVPSE Client v2.4.0

- Sword, Crystal and Mace HUD presets with different card selections; utilities and theme are preserved.
- Live inventory counter for totems, pearls, golden/enchanted apples, wind charges and crystals, including offhand.
- Optional low-armor sound with volume control and a ten-second repeat interval while armor remains low.
- Search settings in English or Dutch, then jump directly to the matching control.
- HUD editor: drag to move; drag the corner, scroll, use +/- buttons or keys to resize.
- Scale stays within the usable viewport and closing the editor saves the layout.
- Regression checks cover inventory totals, presets, sound timing, search, dragging, scaling and saving.

Minecraft Java 26.1.2 / Java 25 / Fabric Loader 0.19.3+ / matching Fabric API.
Install PVPSE-2.4.0.jar in mods. Texturepack ZIPs remain optional and do not install the HUD.

---

# PVPSE Client v2.3.0

## Rebuilt PvP art
- Shorter broad-edged swords with material-matched guards and inset details.
- Dark forged mace with a luminous wind core and wrapped handle.
- Cute golden and enchanted apples; moon-themed ender pearl.
- Keeps the lucky-cat totem, low shields, eight bow charge stages and both 32x/16x editions.
- New wind-charge, arrow and spectral-arrow designs.

## Visible client access and HUD recovery
- PVPSE button on the title and pause screens.
- Right Shift, F8 and /pvpse open the menu; /pvpse restore restores HUD visibility.
- Explicit HUD ON/OFF control and confirmed Restore HUD action in the sidebar.
- Restore enables and repositions the current profile's HUD, clears F1 hiding and disables hide-on-F3.
- HUD toggle feedback and a first-join message explain how to open the client.
- Menu closes back to its originating screen. Editor is clearly unavailable outside a world.
- Correct Minecraft version in the watermark.
- Adaptive restored/new HUD layout keeps widgets apart on small windows.
- Armor slots now show remaining durability, low-durability warnings and clear empty-slot labels.
- Empty waypoint radar no longer covers the HUD during gameplay; editor toolbar moved below widgets.

## Buttons and checks
- PvP HUD preset removes the extra module list/session card; Auto Arrange keeps your switches.
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
- New STYLE tab: HUD opacity (20-100%), accent lines, panel shadows and text shadows.
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
- Interpolated sword/mace hitbox outlines, optional eye-level rings and 1-4 line width.
- Separate sword/mace switches, cooldown/falling colors and optional nearby-visible mode.
- Hitboxes respect HUD visibility and use eye-to-box display distance.


## Cleaner customization
- Navigation sidebar with HUD editor, crosshair studio, waypoints, settings and themes.
- Searchable ALL / HUD / TOOLS / ON module filters; counts and scroll reset per filter.
- Module names fit their rows. Settings use General, Combat, Effects and Profiles tabs.

## PvP visibility and settings
- Optional attack cooldown bar beneath the crosshair, visible only while recharging.
- Optional low-health alert (2-16 HP; two HP per heart, excluding absorption).
- Optional equipped-armor alert (5-50% remaining durability).
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
- Adjustable visual range (2-12 blocks) in the module settings
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
