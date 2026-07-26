package com.codex.pvphud.waypoint;

import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.TextFieldWidget;

public final class WaypointScreen extends Screen {
    private final Screen parent;
    private final WaypointManager manager = WaypointManager.getInstance();
    private int scroll;
    private int selected = -1;
    private TextFieldWidget nameField;

    public WaypointScreen(Screen parent) {
        super(Text.literal("PVPSE Waypoints"));
        this.parent = parent;
    }

    protected void init() {
        int panelX = width / 2 - 170;
        nameField = addDrawableChild(new TextFieldWidget(textRenderer, panelX + 10, 46, 178, 20,
                Text.literal("Waypoint name")));
        nameField.setPlaceholder(Text.literal("Waypoint name..."));
        nameField.setMaxLength(24);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        int panelX = width / 2 - 170;
        RenderUtil.panel(context, panelX, 38, 340, height - 58, theme);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, theme.accent());
        button(context, panelX + 194, 46, 66, "ADD", mouseX, mouseY, theme);
        button(context, panelX + 264, 46, 66, "DONE", mouseX, mouseY, theme);
        button(context, panelX + 10, 72, 88, "SAVE NAME", mouseX, mouseY, theme);
        button(context, panelX + 104, 72, 76, "CLEAR ALL", mouseX, mouseY, theme);
        context.enableScissor(panelX + 5, 100, panelX + 335, height - 25);
        int y = 104 - scroll;
        var points = manager.waypoints();
        for (int i = 0; i < points.size(); i++) {
            var point = points.get(i);
            RoundedRenderer.fill(context, panelX + 10, y, 320, 32, 6,
                    i == selected ? theme.accent() : theme.panelHeader());
            context.drawTextWithShadow(textRenderer, Text.literal(point.name()), panelX + 18, y + 6, theme.text());
            context.drawTextWithShadow(textRenderer,
                    Text.literal("X " + point.x() + "  Y " + point.y() + "  Z " + point.z()),
                    panelX + 18, y + 18, theme.mutedText());
            button(context, panelX + 264, y + 6, 58, "DELETE", mouseX, mouseY, theme);
            y += 38;
        }
        context.disableScissor();
        if (points.isEmpty()) context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("No waypoints yet — type a name and press ADD"), width / 2, 116, theme.mutedText());
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        int panelX = width / 2 - 170;
        if (inside(click.x(), click.y(), panelX + 194, 46, 66, 20)) {
            manager.addCurrent(client, nameField == null ? "" : nameField.getText());
            if (nameField != null) nameField.setText("");
        }
        else if (inside(click.x(), click.y(), panelX + 264, 46, 66, 20)) close();
        else if (inside(click.x(), click.y(), panelX + 10, 72, 88, 20)) {
            if (nameField != null) manager.rename(selected, nameField.getText());
        }
        else if (inside(click.x(), click.y(), panelX + 104, 72, 76, 20)) {
            manager.clear();
            selected = -1;
        }
        else {
            int index = ((int) click.y() - 104 + scroll) / 38;
            int rowY = 104 - scroll + index * 38;
            if (inside(click.x(), click.y(), panelX + 264, rowY + 6, 58, 20)) {
                manager.remove(index);
                selected = -1;
            } else if (index >= 0 && index < manager.waypoints().size()
                    && inside(click.x(), click.y(), panelX + 10, rowY, 246, 32)) {
                selected = index;
                if (nameField != null) nameField.setText(manager.waypoints().get(index).name());
            }
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maximum = Math.max(0, manager.waypoints().size() * 38 - (height - 134));
        scroll = Math.clamp(scroll - (int) Math.round(verticalAmount * 28), 0, maximum);
        return true;
    }

    public void close() { if (client != null) client.setScreen(parent); }
    public boolean shouldPause() { return false; }

    private void button(DrawContext c, int x, int y, int w, String label, int mx, int my, Theme t) {
        RoundedRenderer.fill(c, x, y, w, 20, 5, inside(mx, my, x, y, w, 20) ? t.accentSecondary() : t.panelHeader());
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + 6, t.text());
    }
    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }
}
