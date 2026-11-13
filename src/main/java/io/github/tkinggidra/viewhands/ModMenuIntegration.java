package io.github.tkinggidra.viewhands;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Listed as the "modmenu" entrypoint in fabric.mod.json. Only Mod Menu ever loads this class,
 * so the mod still runs fine when Mod Menu is not installed.
 */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ViewHandsScreen::new; // Mod Menu hands us its own screen as the parent to return to
    }
}
