package io.github.tkinggidra.viewhands;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;

public final class ViewHands implements ClientModInitializer {
    /** Set by /viewhands. Opened one tick later, because the chat screen closes itself after a command. */
    private static boolean openRequested;

    @Override
    public void onInitializeClient() {
        ViewConfig.load();

        // Shows up in Options > Controls > Key Binds under its own "ViewHands" category.
        KeyMapping open = Compat.registerKey("key.viewhands.open", InputConstants.KEY_V);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(Compat.command("viewhands").executes(context -> {
                    openRequested = true;
                    return 1;
                })));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean pressed = false;
            while (open.consumeClick()) pressed = true;
            if (pressed || openRequested) {
                openRequested = false;
                Compat.setScreen(new ViewHandsScreen(null));
            }
        });
    }
}
