package io.github.tkinggidra.viewhands.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.tkinggidra.viewhands.ViewConfig;
import io.github.tkinggidra.viewhands.ViewTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * getCurrentSwingDuration() says how many ticks one swing lasts (item's swing duration, then haste /
 * mining fatigue). Dividing it by our speed multiplier keeps all of those vanilla rules intact.
 * Only your own player is touched, and only on your client: it is purely visual.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyReturnValue(method = "getCurrentSwingDuration", at = @At("RETURN"))
    private int viewhands$swingSpeed(int ticks) {
        LocalPlayer player = Minecraft.getInstance().player;
        if ((Object) this != player || !ViewConfig.get().enabled) return ticks;
        float speed = ViewTransform.swingSpeed(player);
        // Speed 0 is handled in ViewTransform (the swing is hidden), so the duration stays vanilla.
        if (speed <= 0f || speed == 1f) return ticks;
        return Math.max(1, Math.round(ticks / speed));
    }
}
