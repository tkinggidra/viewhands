package io.github.tkinggidra.viewhands.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.tkinggidra.viewhands.Compat;
import io.github.tkinggidra.viewhands.ViewConfig;
import io.github.tkinggidra.viewhands.ViewTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Target method (26.2), called once per hand per frame:
 *   submitArmWithItem(player, float frameInterp, float xRot, InteractionHand hand, float attack,
 *                     ItemStack stack, float inverseArmHeight, PoseStack pose, collector, int light)
 *
 * We never write that parameter list down. Instead each value is picked out by type with @Local,
 * so the mixin does not care that e.g. the collector parameter had another type before 1.21.9.
 * Floats are ambiguous (there are four), so those are picked by position: ordinal 2 = attack/swing
 * progress, ordinal 3 = equip progress.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    /** At the top of the method: open our own pose, apply the viewmodel, optionally replace swing. */
    @ModifyVariable(method = Compat.ARM_METHOD, at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private float viewhands$begin(float swing,
                                  @Local(argsOnly = true) InteractionHand hand,
                                  @Local(argsOnly = true) ItemStack stack,
                                  @Local(argsOnly = true, ordinal = 3) float equip,
                                  @Local(argsOnly = true) PoseStack pose) {
        pose.pushPose(); // always, because viewhands$end always pops
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return swing;
        // viewhands$equip may run before or after us, so apply the same rule here by hand.
        if (viewhands$noEquip()) equip = 0f;
        return ViewTransform.apply(pose, player, hand, stack, swing, equip);
    }

    /** Equip animation off: tell vanilla the item is always fully raised. */
    @ModifyVariable(method = Compat.ARM_METHOD, at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float viewhands$equip(float equip) {
        return viewhands$noEquip() ? 0f : equip;
    }

    /** On the way out: close the pose again, otherwise our transform would leak into the other hand. */
    @Inject(method = Compat.ARM_METHOD, at = @At("RETURN"))
    private void viewhands$end(CallbackInfo ci, @Local(argsOnly = true) PoseStack pose) {
        pose.popPose();
    }

    @Unique
    private static boolean viewhands$noEquip() {
        ViewConfig c = ViewConfig.get();
        return c.enabled && !c.equipAnimation;
    }
}
