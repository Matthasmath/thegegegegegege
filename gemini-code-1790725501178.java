package net.nite.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class NiteClient implements ClientModInitializer {
    public static NiteClient INSTANCE;
    public static KeyBinding openGuiKey;
    public List<CategoryPanel> panels = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nite.opengui",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEYSYM.getCode(),
                "category.nite.title"
        ));

        // PVP CATEGORY
        CategoryPanel pvp = new CategoryPanel("PvP", 20, 20);
        Module triggerBot = new Module("Trigger Bot");
        triggerBot.addSetting(new Setting("Hit Delay", 50, 0, 200));
        triggerBot.addSetting(new Setting("Reach", 3.0f, 1.0f, 6.0f));
        pvp.addModule(triggerBot);

        Module autoCrystal = new Module("Auto Crystal");
        autoCrystal.addSetting(new Setting("Break Speed", 15, 1, 20));
        autoCrystal.addSetting(new Setting("Place Delay", 0, 0, 10));
        pvp.addModule(autoCrystal);

        pvp.addModule(new Module("Auto Totem"));
        pvp.addModule(new Module("Auto Anchor"));
        pvp.addModule(new Module("Mace Bomber"));
        pvp.addModule(new Module("Shield Breaker"));

        // VISUALS CATEGORY
        CategoryPanel visuals = new CategoryPanel("Visuals", 150, 20);
        Module playerEsp = new Module("Player ESP");
        playerEsp.addSetting(new Setting("Box Alpha", 180, 0, 255));
        visuals.addModule(playerEsp);
        visuals.addModule(new Module("Storage ESP"));
        visuals.addModule(new Module("Block ESP"));
        visuals.addModule(new Module("No Render"));
        visuals.addModule(new Module("X-Ray"));
        visuals.addModule(new Module("Fullbright"));
        visuals.addModule(new Module("Spawner Notifier"));
        visuals.addModule(new Module("Tracers"));
        visuals.addModule(new Module("Mob ESP"));

        // MOVEMENT CATEGORY
        CategoryPanel movement = new CategoryPanel("Movement", 280, 20);
        Module fly = new Module("Flight");
        fly.addSetting(new Setting("Speed", 2.5f, 0.5f, 10.0f));
        movement.addModule(fly);
        movement.addModule(new Module("Entity Control"));
        movement.addModule(new Module("NoFall"));
        movement.addModule(new Module("Jesus"));
        movement.addModule(new Module("Spider"));
        movement.addModule(new Module("NoClip"));
        movement.addModule(new Module("Sneak"));
        movement.addModule(new Module("Speed"));

        // CLIENT SETTINGS CATEGORY
        CategoryPanel settings = new CategoryPanel("Settings", 410, 20);
        Module uiCustomization = new Module("GUI Style");
        uiCustomization.addSetting(new Setting("Opacity", 220, 50, 255));
        uiCustomization.addSetting(new Setting("Red Tone", 180, 50, 255));
        settings.addModule(uiCustomization);

        Module hudOverlay = new Module("Active HUD");
        hudOverlay.addSetting(new Setting("FPS Scale", 1.0f, 0.5f, 2.0f));
        settings.addModule(hudOverlay);

        panels.add(pvp);
        panels.add(visuals);
        panels.add(movement);
        panels.add(settings);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen(Text.literal("Nite Client")));
            }
        });
    }

    public static class Setting {
        public String name;
        public float value;
        public float min;
        public float max;

        public Setting(String name, float value, float min, float max) {
            this.name = name;
            this.value = value;
            this.min = min;
            this.max = max;
        }
    }

    public static class Module {
        private final String name;
        private boolean toggled;
        private boolean expanded;
        private final List<Setting> settings = new ArrayList<>();

        public Module(String name) {
            this.name = name;
            this.toggled = false;
            this.expanded = false;
        }

        public String getName() { return name; }
        public boolean isToggled() { return toggled; }
        public void toggle() { this.toggled = !this.toggled; }
        public boolean isExpanded() { return expanded; }
        public void toggleExpanded() { this.expanded = !this.expanded; }
        public void addSetting(Setting s) { this.settings.add(s); }
        public List<Setting> getSettings() { return settings; }
    }

    public static class CategoryPanel {
        public String title;
        public int x, y, width = 115, headerHeight = 22;
        public List<Module> modules = new ArrayList<>();
        public boolean dragging;
        public int dragX, dragY;

        public CategoryPanel(String title, int x, int y) {
            this.title = title;
            this.x = x;
            this.y = y;
        }

        public void addModule(Module module) {
            modules.add(module);
        }

        public void render(DrawContext context, int mouseX, int mouseY) {
            if (dragging) {
                x = mouseX - dragX;
                y = mouseY - dragY;
            }

            // Category Header
            context.fill(x, y, x + width, y + headerHeight, new Color(20, 4, 8, 245).getRGB());
            context.fill(x, y + headerHeight - 2, x + width, y + headerHeight, new Color(180, 10, 30).getRGB());
            context.drawText(MinecraftClient.getInstance().textRenderer, title.toUpperCase(), x + 8, y + 7, 0xFFFFFFFF, true);

            int currentY = y + headerHeight;
            for (Module m : modules) {
                int bgColor = m.isToggled() ? new Color(110, 10, 25, 220).getRGB() : new Color(14, 14, 18, 230).getRGB();
                int textColor = m.isToggled() ? 0xFFFFFFFF : 0xFFA0A0A0;

                context.fill(x, currentY, x + width, currentY + 18, bgColor);
                context.drawText(MinecraftClient.getInstance().textRenderer, m.getName(), x + 6, currentY + 5, textColor, false);

                int indicatorColor = m.isToggled() ? 0xFFFF1E38 : 0xFF333340;
                context.fill(x + width - 12, currentY + 5, x + width - 5, currentY + 13, indicatorColor);

                currentY += 18;

                // Render Sub-Settings Menu when right-clicked (expanded)
                if (m.isExpanded() && !m.getSettings().isEmpty()) {
                    for (Setting s : m.getSettings()) {
                        context.fill(x, currentY, x + width, currentY + 16, new Color(8, 8, 12, 240).getRGB());
                        String valText = s.name + ": " + String.format("%.1f", s.value);
                        context.drawText(MinecraftClient.getInstance().textRenderer, valText, x + 10, currentY + 4, 0xFF888899, false);
                        currentY += 16;
                    }
                }
            }

            // Outline Border
            context.fill(x, y, x + 1, currentY, new Color(180, 10, 30, 120).getRGB());
            context.fill(x + width - 1, y, x + width, currentY, new Color(180, 10, 30, 120).getRGB());
            context.fill(x, currentY, x + width, currentY + 1, new Color(180, 10, 30, 120).getRGB());
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight) {
                if (button == 0) { // Left Click to Drag Header
                    dragging = true;
                    dragX = (int) mouseX - x;
                    dragY = (int) mouseY - y;
                    return true;
                }
            }

            int currentY = y + headerHeight;
            for (Module m : modules) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 18) {
                    if (button == 0) { // Left Click: Toggle Module
                        m.toggle();
                        return true;
                    } else if (button == 1) { // Right Click: Open Module Settings Menu
                        m.toggleExpanded();
                        return true;
                    }
                }
                currentY += 18;

                if (m.isExpanded() && !m.getSettings().isEmpty()) {
                    currentY += (m.getSettings().size() * 16);
                }
            }
            return false;
        }

        public void mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) dragging = false;
        }
    }

    public static class ClickGuiScreen extends Screen {
        protected ClickGuiScreen(Text title) {
            super(title);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            for (CategoryPanel panel : NiteClient.INSTANCE.panels) {
                panel.render(context, mouseX, mouseY);
            }
            context.drawText(this.textRenderer, "NITE CLIENT • RIGHT-CLICK MODULE FOR SETTINGS", 8, this.height - 16, 0xFFFF1E38, true);
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (CategoryPanel panel : NiteClient.INSTANCE.panels) {
                if (panel.mouseClicked(mouseX, mouseY, button)) return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (CategoryPanel panel : NiteClient.INSTANCE.panels) {
                panel.mouseReleased(mouseX, mouseY, button);
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }
}