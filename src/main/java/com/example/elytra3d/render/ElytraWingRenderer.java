package com.example.elytra3d.render;

import com.example.elytra3d.Elytra3DConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.tr7zw.skinlayers.api.Mesh;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Draws the two wing meshes lined up with vanilla's wing ModelParts.
 *
 * Mesh has a render(ModelPart, ...) overload that copies a part's pose automatically, but it
 * gives no chance to correct for the mesh's own origin or orientation. So we apply the part's
 * transform ourselves with translateAndRotate, then apply the configured per-wing correction,
 * then draw with the plain render overload.
 */
public final class ElytraWingRenderer {

    private ElytraWingRenderer() {
    }

    public static void render(ElytraMeshCache.Wings wings, ModelPart leftWing, ModelPart rightWing,
            PoseStack poseStack, VertexConsumer consumer, int light) {

        renderWing(wings.left(), leftWing, poseStack, consumer, light, Elytra3DConfig.LEFT);
        renderWing(wings.right(), rightWing, poseStack, consumer, light, Elytra3DConfig.RIGHT);
    }

    private static void renderWing(Mesh mesh, ModelPart part, PoseStack poseStack,
            VertexConsumer consumer, int light, Elytra3DConfig.Wing cfg) {

        poseStack.pushPose();

        // The part's current animated position and rotation - this is what makes the mesh
        // follow vanilla's poses (and any physics mod driving the same part).
        part.translateAndRotate(poseStack);

        // Offset first, in the part's own (unrotated) space, so the numbers stay intuitive.
        // Model units are 1/16 of a block.
        poseStack.translate(
                cfg.offsetX.get() / 16.0,
                cfg.offsetY.get() / 16.0,
                cfg.offsetZ.get() / 16.0);

        // Then rotation, spinning the mesh about that offset point.
        float rotX = cfg.rotateX.get().floatValue();
        float rotY = cfg.rotateY.get().floatValue();
        float rotZ = cfg.rotateZ.get().floatValue();

        if (rotX != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        }
        if (rotY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        }
        if (rotZ != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        }

        mesh.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}
