package io.github.tkinggidra.viewhands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Locale;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

/**
 * The whole UI: three tabs in one narrow panel. Sliders write straight into the live config, and the
 * screen draws no background in a world, so your real first-person hand behind it is the preview.
 */
public final class ViewHandsScreen extends Screen {
    private static final int LEFT = 8, TOP = 8, WIDTH = 168, ROW = 22;
    /** Rows reserved for a tab's own widgets. The fixed buttons start below them. */
    private static final int TAB_ROWS = 7;

    private enum Tab { ITEM, SWING, CAMERA }

    /** Remembered between openings, like the preview camera. */
    private static Tab tab = Tab.ITEM;
    private static boolean autoSwing = true;

    private final Screen parent;
    /** Settings as they were when the screen opened (or last saved). Closing goes back to this. */
    private final ViewConfig snapshot = ViewConfig.get().copy();
    private int ticks;
    private int y;

    /** @param parent screen to return to (Mod Menu passes its own), or null to return to the game */
    public ViewHandsScreen(Screen parent) {
        super(Component.translatable("viewhands.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ViewTransform.previewOpen = true;
        ViewConfig c = ViewConfig.get();
        int third = (WIDTH - 4) / 3;

        // Tab bar. The current tab's button is greyed out.
        y = TOP;
        for (Tab t : Tab.values()) {
            Button button = Button.builder(Component.translatable("viewhands.tab." + t.name().toLowerCase(Locale.ROOT)), b -> {
                tab = t;
                rebuildWidgets();
            }).bounds(LEFT + t.ordinal() * (third + 2), y, third, 20).build();
            button.active = t != tab;
            addRenderableWidget(button);
        }
        y += ROW;

        switch (tab) {
            case ITEM -> {
                slider("viewhands.scale", 0f, 3f, 2, c.scale, v -> c.scale = v);
                slider("viewhands.offset_x", -2f, 2f, 2, c.offsetX, v -> c.offsetX = v);
                slider("viewhands.offset_y", -2f, 2f, 2, c.offsetY, v -> c.offsetY = v);
                slider("viewhands.offset_z", -2f, 2f, 2, c.offsetZ, v -> c.offsetZ = v);
                slider("viewhands.rot_x", -180f, 180f, 0, c.rotX, v -> c.rotX = v);
                slider("viewhands.rot_y", -180f, 180f, 0, c.rotY, v -> c.rotY = v);
                slider("viewhands.rot_z", -180f, 180f, 0, c.rotZ, v -> c.rotZ = v);
            }
            case SWING -> {
                button(speedMode(c), b -> {
                    c.speedByEfficiency = !c.speedByEfficiency;
                    rebuildWidgets(); // the "per level" slider appears / disappears
                });
                slider("viewhands.swing_speed", 0f, 5f, 2, c.swingSpeed, v -> c.swingSpeed = v);
                if (c.speedByEfficiency) {
                    slider("viewhands.efficiency_bonus", 0f, 1f, 2, c.efficiencyBonus, v -> c.efficiencyBonus = v);
                }
                slider("viewhands.swing_distance", 0f, 3f, 2, c.swingDistance, v -> c.swingDistance = v);
                slider("viewhands.swing_rotation", 0f, 3f, 2, c.swingRotation, v -> c.swingRotation = v);
                button(onOff("viewhands.equip_animation", c.equipAnimation), b -> {
                    c.equipAnimation = !c.equipAnimation;
                    b.setMessage(onOff("viewhands.equip_animation", c.equipAnimation));
                });
                button(onOff("viewhands.auto_swing", autoSwing), b -> {
                    autoSwing = !autoSwing;
                    b.setMessage(onOff("viewhands.auto_swing", autoSwing));
                });
            }
            case CAMERA -> {
                // Preview only: these never touch the config file and do nothing once the editor is closed.
                button(onOff("viewhands.cam_focus", ViewTransform.camFocus), b -> {
                    ViewTransform.camFocus = !ViewTransform.camFocus;
                    b.setMessage(onOff("viewhands.cam_focus", ViewTransform.camFocus));
                });
                slider("viewhands.cam_x", -1f, 1f, 2, ViewTransform.camX, v -> ViewTransform.camX = v);
                slider("viewhands.cam_y", -1f, 1f, 2, ViewTransform.camY, v -> ViewTransform.camY = v);
                slider("viewhands.cam_zoom", 0f, 1f, 2, ViewTransform.camZoom, v -> ViewTransform.camZoom = v);
                button(Component.translatable("viewhands.cam_reset"), b -> {
                    ViewTransform.camFocus = true;
                    ViewTransform.camX = ViewTransform.camY = ViewTransform.camZoom = 0f;
                    rebuildWidgets();
                });
            }
        }

        // Fixed bottom part, same place on every tab.
        y = TOP + (1 + TAB_ROWS) * ROW;
        button(onOff("viewhands.enabled", c.enabled), b -> {
            c.enabled = !c.enabled;
            b.setMessage(onOff("viewhands.enabled", c.enabled));
        });
        addRenderableWidget(Button.builder(Component.translatable("viewhands.save"), b -> {
            ViewConfig.save();
            snapshot.copyFrom(c); // so closing has nothing to undo
            onClose();
        }).bounds(LEFT, y, third, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("viewhands.reset"), b -> {
            c.copyFrom(new ViewConfig());
            rebuildWidgets(); // sliders must show the new values
        }).bounds(LEFT + third + 2, y, third, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("viewhands.cancel"), b -> onClose())
                .bounds(LEFT + 2 * (third + 2), y, third, 20).build());
    }

    /** Adds a full-width slider on the next row. */
    private void slider(String key, float min, float max, int decimals, float value, FloatSetter setter) {
        addRenderableWidget(new Slider(LEFT, y, WIDTH, key, min, max, decimals, value, setter));
        y += ROW;
    }

    /** Adds a full-width button on the next row. */
    private void button(Component label, Button.OnPress onPress) {
        addRenderableWidget(Button.builder(label, onPress).bounds(LEFT, y, WIDTH, 20).build());
        y += ROW;
    }

    private static Component onOff(String key, boolean on) {
        return Component.translatable(key, on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private static Component speedMode(ViewConfig c) {
        return Component.translatable("viewhands.speed_mode", Component.translatable(
                c.speedByEfficiency ? "viewhands.speed_mode.efficiency" : "viewhands.speed_mode.overall"));
    }

    @Override
    public void tick() {
        // Preview: swing the active hand locally every ~1.25s. swing(hand, false) sends nothing to the server.
        LocalPlayer player = Minecraft.getInstance().player;
        if (autoSwing && player != null && ++ticks % 25 == 0) {
            player.swing(ViewTransform.activeHand(player), false);
        }
    }

    /** Esc, Cancel and Save all end up here. */
    @Override
    public void onClose() {
        Compat.setScreen(parent);
    }

    /** Runs however the screen goes away (closed, replaced, death screen...): drop unsaved changes. */
    @Override
    public void removed() {
        ViewConfig.get().copyFrom(snapshot);
        ViewTransform.previewOpen = false;
        super.removed();
    }

    /** Keep the world running in singleplayer, otherwise the preview swing would freeze. */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // In a world: no blur and no dark overlay, so the hand stays visible. From the title screen
    // (Mod Menu) there is no hand to see, so keep the normal background. Renamed in 26.1.
    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (Minecraft.getInstance().level == null) super.extractBackground(graphics, mouseX, mouseY, delta);
    }
    //?} else {
    /*@Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (Minecraft.getInstance().level == null) super.renderBackground(graphics, mouseX, mouseY, delta);
    }
    *///?}

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }

    /** A vanilla slider mapped onto [min, max] and rounded to the given number of decimals. */
    private static final class Slider extends AbstractSliderButton {
        private final String key;
        private final float min, max, step;
        private final int decimals;
        private final FloatSetter setter;

        Slider(int x, int y, int width, String key, float min, float max, int decimals, float value, FloatSetter setter) {
            super(x, y, width, 20, Component.empty(), (value - min) / (max - min));
            this.key = key;
            this.min = min;
            this.max = max;
            this.decimals = decimals;
            this.step = (float) Math.pow(10, decimals);
            this.setter = setter;
            updateMessage();
        }

        private float current() {
            return Math.round((min + (float) value * (max - min)) * step) / step + 0f; // + 0f turns -0.0 into 0.0
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(key, String.format(Locale.ROOT, "%." + decimals + "f", current())));
        }

        @Override
        protected void applyValue() {
            setter.set(current());
        }
    }
}
