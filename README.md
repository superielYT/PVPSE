🔥 PVPSE Client
PVPSE Client is a next-generation Minecraft Fabric client focused on performance, customization, and a premium PvP experience. Built with a modern design philosophy, PVPSE combines a powerful HUD system, smooth animations, and a sleek interface inspired by the best Minecraft clients while keeping its own unique identity.
Featuring a fully customizable HUD, advanced ClickGUI, dynamic themes, and optimized rendering systems, PVPSE gives players complete control over their Minecraft experience.
Features:
⚡ Modern ClickGUI
A clean, animated interface with custom themes, smooth transitions, and easy module management.
🎨 Custom HUD System
Move, resize, and customize your HUD elements including FPS, CPS, Armor, Coordinates, Keystrokes, and more.
🔥 PVPSE Ignite Theme
A unique dark interface with orange/fire accents, glowing effects, and a premium client aesthetic.
🚀 Performance Focused
Optimized rendering, lightweight systems, and smooth animations designed for high FPS gameplay.
⚙️ Advanced Customization
Save configurations, change colors, edit layouts, and create your own personal setup.
✨ Professional Experience
Designed for players who want a clean, powerful, and customizable Minecraft client.
PVPSE Client — Your Minecraft. Your Style. Your Control. 🔥

## Installation (v1.9.0)

Requires Minecraft **1.21.11**, Fabric Loader **0.19.3+**, Fabric API for 1.21.11 and Java **21**. Place PVPSE-1.9.0.jar in your Minecraft mods folder alongside Fabric API.

## Controls and settings

- Right Shift: open the client interface
- H: toggle the HUD
- Z: hold to zoom; B: add waypoint; V: toggle Fullbright
- Left-click a module to toggle it; right-click to expand settings
- HUD modules: scale, X/Y position and reset layout
- HUD EDITOR: drag widgets to arrange your layout
- Search filters modules; mouse wheel scrolls the module list
- Changes save when leaving the main interface and periodically during play
- Profiles live in config/pvpse/profiles/default.json; controls can be rebound in Minecraft settings

## Waypoints and radar (1.7.0)

Open WAYPOINTS from the Right Shift menu. ADD HERE saves your position; select a row to rename, delete, TRACK, change its COLOR, SHOW/HIDE it or COPY XYZ. Clearing all points asks for confirmation.

RANGE cycles 32/64/128/256/512 blocks from the radar center to its edge. NORTH UP/ROTATING changes orientation. LABELS controls on-screen destination labels separately from radar dots. DEATH saves a marker once when you die. Tracked destinations render first; otherwise the nearest visible point is used. Labels show horizontal distance and vertical offset.

New waypoints are isolated by server/world and dimension. Older waypoints stay accessible and display "legacy". The minimap is a waypoint radar with grid and heading, without terrain or entity scanning. Right-click Waypoint in the module list for position, scale and reset controls.

## General settings (1.9.0)

Open Right Shift > SETTINGS & PROFILES for global switches, profile switching, copying your current HUD/theme to a new profile, and resetting HUD positions/scales. Profiles do not include global feedback, waypoint or custom crosshair files. Your active profile is remembered; the former default.json migrates automatically to the profiles folder.

Drag module sliders to adjust them. In HUD EDITOR, select a widget and use arrow keys to move it one pixel, or Shift+arrow for ten. R resets its layout; Ctrl+Z restores positions, scales and visibility from the start of the editor session. Utilities stay in the module list rather than appearing as editor widgets.

During keybind capture, Escape cancels and Backspace/Delete unbinds. Bindings save when Minecraft closes normally. F1 hides overlays; "Hide on F3" controls debug-screen visibility. "No damage tilt" is now optional.

## Ownership and permissions

Copyright (c) 2026 superielYT. All rights reserved for owner-controlled material
covered by [LICENSE](LICENSE). Reuse, redistribution, and building derivative
projects require prior written permission from superielYT, subject to the
exceptions in that notice.

ChatGPT/Codex and GitHub Copilot may assist only on superielYT's instructions
for this project; this is not permission for others to reuse the project.

Previously granted MIT rights and third-party licenses remain unaffected.
A public GitHub repository can still be viewed and forked under GitHub's
terms. A copyright notice does not technically prevent copying.

## Cleaner customization and PvP visibility (1.9.0)

The Right Shift menu now has a navigation sidebar, search and ALL / HUD /
TOOLS / ON module filters. Use the + control or right-click for module settings.

Settings are split into General, Combat, Effects and Profiles:
- Attack cooldown bar: appears beneath the crosshair only while recharging.
- Low-health alert: choose 2–16 HP (2 HP = one heart); absorption is not counted.
- Armor alert: choose 5–50% remaining durability; checks equipped, damageable armor.
- Hit marker size: 3–10 pixels; damage-flash opacity: 10–100%; particles: 4–48.
- Minimal Effects disables the damage flash and kill particles.
- Click numeric values to increase them; after the maximum they wrap to the minimum.
- Alerts and effects are global; profiles retain their existing HUD/theme scope.
- Combat indicators hide in menus, spectator mode, F1 and (if enabled) F3.

## PVPSE Ignite 16x resource pack

Download [PVPSE-Ignite-16x-1.21.11.zip](dist/PVPSE-Ignite-16x-1.21.11.zip).
Place the ZIP in your Minecraft resourcepacks folder and enable it above other packs.

Includes seven short swords, a lucky-cat totem, golden carrot, golden and enchanted
apples, ender pearl, XP bottle, end crystal and a crisp cobweb. The shield is lowered
in both hands, at rest and while blocking. Shield banners still use vanilla rendering.
All pixel art is original. Unchanged textures and shield rendering come from Minecraft.

Targets **Minecraft Java 1.21.11 / resource pack format 75.0**. It changes appearance,
not damage, reach or attack timing. Other packs overriding the same items may conflict.

![Ignite item preview](dist/PVPSE-Ignite-preview.png)

Regenerate and validate the textures and ZIP with Node 22:
`node tools/build-pvp-pack.mjs`. GitHub Actions builds the mod and uploads the pack.
The pack ZIP is also committed for direct downloading.
