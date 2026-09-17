package com.example.elytra3d.render;

import com.example.elytra3d.Elytra3DConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import dev.tr7zw.skinlayers.api.Mesh;

/**
 * Draws the two wing meshes lined up with vanilla's wing ModelParts.
 *
 * Mesh has a render(ModelPart, ...) overload that copies a part's pose automatically, but it
 * gives no chance to correct for the mesh's own origin. Vanilla's wing cubes aren't built at
 * their part's origin - the left wing's box spans x -10..0 and the mirrored right wing spans
 * 0..10 - while Skin Layers builds its mesh from 0..width. So instead we apply the part's
 * transform ourselves with translateAndRotate, insert the corrective offset, then draw with
 * the plain render overload.
 */
public final class ElytraWingRenderer {

    /** Vanilla's wing box width, used to mirror the offset for the right wing. */
    private static final float WING_WIDTH = 10.0F;

    private ElytraWingRenderer() {
    }

    public static void render(ElytraMeshCache.Wings wings, ModelPart leftWing, ModelPart rightWing,
            PoseStack poseStack, VertexConsumer consumer, int light) {

        float offsetX = Elytra3DConfig.OFFSET_X.get().floatValue();
        float offsetY = Elytra3DConfig.OFFSET_Y.get().floatValue();
        float offsetZ = Elytra3DConfig.OFFSET_Z.get().floatValue();

        // Left wing: vanilla's box starts offsetX pixels left of the part origin (-10 default).
        renderWing(wings.left(), leftWing, poseStack, consumer, light,
                offsetX, offsetY, offsetZ);

        // Right wing: mirrored, so its box starts at the part origin instead. Deriving it from
        // the same config value keeps one knob meaningful for both wings.
        renderWing(wings.right(), rightWing, poseStack, consumer, light,
                offsetX + WING_WIDTH, offsetY, offsetZ);
    }

    private static void renderWing(Mesh mesh, ModelPart part, PoseStack poseStack,
            VertexConsumer consumer, int light, float offsetX, float offsetY, float offsetZ) {

        poseStack.pushPose();

        // Applies the part's current animated position and rotation - this is what makes the
        // mesh follow vanilla's flight/standing/crouching poses (and any physics mod driving
        // the same part) without us reimplementing any animation.
        part.translateAndRotate(poseStack);

        // Model units are 1/16 of a block.
        poseStack.translate(offsetX / 16.0F, offsetY / 16.0F, offsetZ / 16.0F);

        mesh.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}
