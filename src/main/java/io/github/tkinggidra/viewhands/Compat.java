package io.github.tkinggidra.viewhands;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

//? if >=26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}

/**
 * Everything that differs between Minecraft versions lives here (plus one method in ViewHandsScreen).
 * The "//? if" comments are Stonecutter conditions: the branch matching the active version is real
 * code, the other branch sits inside a block comment. Stonecutter flips them when you switch version.
 */
public final class Compat {
    private Compat() {}

    /** Name of the first-person "draw one arm + its item" method. Renamed in 26.2. */
    //? if >=26.2 {
    public static final String ARM_METHOD = "submitArmWithItem";
    //?} else {
    /*public static final String ARM_METHOD = "renderArmWithItem";
    *///?}

    /** 26.2 moved setScreen from Minecraft into Minecraft.gui. */
    public static void setScreen(Screen screen) {
        //? if >=26.2 {
        Minecraft.getInstance().gui.setScreen(screen);
        //?} else {
        /*Minecraft.getInstance().setScreen(screen);
        *///?}
    }

    /** Registers a rebindable key in its own "ViewHands" category of the Key Binds screen. */
    public static KeyMapping registerKey(String translationKey, int keyCode) {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("viewhands", "main"));
        KeyMapping key = new KeyMapping(translationKey, InputConstants.Type.KEYSYM, keyCode, category);
        //? if >=26.1 {
        return KeyMappingHelper.registerKeyMapping(key);
        //?} else {
        /*return KeyBindingHelper.registerKeyBinding(key);
        *///?}
    }

    /** Start of a client-side command, e.g. command("viewhands") for /viewhands. */
    public static LiteralArgumentBuilder<FabricClientCommandSource> command(String name) {
        //? if >=26.1 {
        return ClientCommands.literal(name);
        //?} else {
        /*return ClientCommandManager.literal(name);
        *///?}
    }
}
