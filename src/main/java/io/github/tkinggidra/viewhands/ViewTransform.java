package io.github.tkinggidra.viewhands;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;

/** The actual viewmodel maths. Pure JOML + PoseStack, so it rarely breaks between versions. */
public final class ViewTransform {
    private static final float PI = (float) Math.PI;
    private static final float DEG = PI / 180f;

    // Preview camera. Only used while the editor is open and never saved.
    public static boolean previewOpen;
    public static boolean camFocus = true;
    public static float camX, camY, camZoom;

    private ViewTransform() {}

    /** The hand you are "working with": main hand, unless it is empty and the off hand is not. */
    public static InteractionHand activeHand(LocalPlayer player) {
        return player.getMainHandItem().isEmpty() && !player.getOffhandItem().isEmpty()
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    /** +1 when the given hand is drawn on the right of the screen, -1 for the left. */
    private static float side(LocalPlayer player, InteractionHand hand) {
        return (player.getMainArm() == HumanoidArm.RIGHT) == (hand == InteractionHand.MAIN_HAND) ? 1f : -1f;
    }

    /** Final swing speed multiplier for this player right now. 0 means "never swing". */
    public static float swingSpeed(LocalPlayer player) {
        ViewConfig c = ViewConfig.get();
        if (!c.speedByEfficiency) return c.swingSpeed;
        // Efficiency N gives the player a mining_efficiency attribute of N*N + 1 while the tool is held.
        double attribute = player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        float level = attribute > 1.0 ? (float) Math.sqrt(attribute - 1.0) : 0f;
        return c.swingSpeed * (1f + c.efficiencyBonus * level);
    }

    /**
     * Called at the very start of vanilla's arm+item method, after we pushed a fresh pose.
     * Whatever we put on the pose here wraps everything vanilla draws for this hand.
     *
     * @param swing vanilla swing progress, 0..1
     * @param equip equip progress as vanilla will use it, 0 = fully raised, 1 = fully lowered
     * @return the swing progress vanilla should use (0 when we animate the swing ourselves)
     */
    public static float apply(PoseStack pose, LocalPlayer player, InteractionHand hand, ItemStack stack,
                              float swing, float equip) {
        ViewConfig c = ViewConfig.get();

        if (previewOpen) {
            // "Moving the camera" = moving both hands the opposite way. Same shift for both hands.
            float fx = 0f, fy = 0f, fz = 0f;
            if (camFocus) {
                fx = -side(player, activeHand(player)) * 0.38f; // pull the active arm to the centre
                fy = 0.30f;
                fz = 0.15f;
            }
            pose.translate(fx - camX, fy - camY, fz + camZoom * 0.5f);
        }

        if (!c.enabled) return swing;
        if (swingSpeed(player) <= 0f) swing = 0f; // speed 0: the hand simply never swings
        // Empty hand and held maps have their own vanilla layout, leave the rest alone.
        if (stack.isEmpty() || stack.is(Items.FILLED_MAP)) return swing;

        float side = side(player, hand);

        // Where vanilla parks a held item (ItemInHandRenderer#applyItemArmTransform). We use it as pivot.
        float px = side * 0.56f;
        float py = -0.52f - 0.6f * equip;
        float pz = -0.72f;

        // Eating, drawing a bow, blocking... vanilla does not swing there, so neither do we.
        boolean using = player.isUsingItem() && player.getUsedItemHand() == hand;
        boolean customSwing = c.hasCustomSwing() && !using;

        if (customSwing && swing > 0f) {
            // Vanilla's swing formulas, with our two multipliers applied.
            float root = (float) Math.sqrt(swing);
            float d = c.swingDistance;
            pose.translate(
                    side * -0.4f * sin(root * PI) * d,
                    0.2f * sin(root * 2f * PI) * d,
                    -0.2f * sin(swing * PI) * d);

            float r = c.swingRotation;
            float f = sin(swing * swing * PI);
            float g = sin(root * PI);
            pose.translate(px, py, pz); // rotate around the item, not around the camera
            pose.mulPose(new Quaternionf()
                    .rotationY(side * (45f - 20f * f * r) * DEG)
                    .rotateZ(side * g * -20f * r * DEG)
                    .rotateX(g * -80f * r * DEG)
                    .rotateY(side * -45f * DEG));
            pose.translate(-px, -py, -pz);
        }

        // X is mirrored for the left arm so positive X always means "away from the screen centre".
        pose.translate(side * c.offsetX, c.offsetY, c.offsetZ);

        boolean rotated = c.rotX != 0f || c.rotY != 0f || c.rotZ != 0f;
        if (rotated || c.scale != 1f) {
            // Rotating/scaling around the camera would fling the item away, so pivot on the item's spot.
            pose.translate(px, py, pz);
            if (rotated) {
                pose.mulPose(new Quaternionf().rotationXYZ(c.rotX * DEG, side * c.rotY * DEG, side * c.rotZ * DEG));
            }
            pose.scale(c.scale, c.scale, c.scale);
            pose.translate(-px, -py, -pz);
        }

        return customSwing ? 0f : swing;
    }

    private static float sin(float radians) {
        return (float) Math.sin(radians);
    }
}
