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

## Installation (v1.8.0)

Requires Minecraft **1.21.11**, Fabric Loader **0.19.3+**, Fabric API for 1.21.11 and Java **21**. Place PVPSE-1.8.0.jar in your Minecraft mods folder alongside Fabric API.

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

## General settings (1.8.0)

Open Right Shift > SETTINGS & PROFILES for global switches, profile switching, copying your current HUD/theme to a new profile, and resetting HUD positions/scales. Profiles do not include global feedback, waypoint or custom crosshair files. Your active profile is remembered; the former default.json migrates automatically to the profiles folder.

Drag module sliders to adjust them. In HUD EDITOR, select a widget and use arrow keys to move it one pixel, or Shift+arrow for ten. R resets its layout; Ctrl+Z restores positions, scales and visibility from the start of the editor session. Utilities stay in the module list rather than appearing as editor widgets.

During keybind capture, Escape cancels and Backspace/Delete unbinds. Bindings save when Minecraft closes normally. F1 hides overlays; "Hide on F3" controls debug-screen visibility. "No damage tilt" is now optional.
