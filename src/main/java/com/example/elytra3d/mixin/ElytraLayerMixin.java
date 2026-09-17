package com.example.elytra3d.mixin;

import com.example.elytra3d.Elytra3DConfig;
import com.example.elytra3d.render.ElytraMeshCache;
import com.example.elytra3d.render.ElytraTextureResolver;
import com.example.elytra3d.render.ElytraWingRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles 3D elytra rendering for the vanilla ElytraLayer (chest slot path).
 *
 * Two cases:
 * 1. Elytra in the chest slot: draw the 3D mesh here and cancel vanilla's flat render.
 * 2. Elytra in a Curios slot: ElytraSlotLayerMixin handles the real draw. Cancel here
 *    unconditionally to suppress the duplicate flat render that vanilla still attempts.
 *    Note: ElytraLayer reads the wing ModelParts before setupAnim has run for this entity's
 *    current state, so we must NOT draw from here when the Curios layer will handle it.
 */
@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin {

    @Shadow
    @Final
    private ElytraModel<?> elytraModel;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void elytra3d$renderElytra(PoseStack poseStack, MultiBufferSource buffer, int light,
            LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {

        if (!Elytra3DConfig.ENABLED.get()) {
            return;
        }

        // If no recognised elytra is equipped anywhere, let vanilla handle it normally.
        if (!ElytraTextureResolver.isElytraEquippedAnywhere(entity)) {
            return;
        }

        ResourceLocation texture = ElytraTextureResolver.resolveChestSlotOnly(entity);
        if (texture != null) {
            // Elytra in the chest slot - draw the 3D mesh here.
            ElytraMeshCache.Wings wings = ElytraMeshCache.getOrBuild(texture);
            if (wings != null) {
                ElytraModelAccessor accessor = (ElytraModelAccessor) this.elytraModel;
                VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));
                ElytraWingRenderer.render(wings,
                        accessor.elytra3d$getLeftWing(), accessor.elytra3d$getRightWing(),
                        poseStack, consumer, light);
            }
        }
        // Whether chest slot or Curios slot, always cancel vanilla's flat render.
        // For the Curios case, ElytraSlotLayerMixin draws the proper animated mesh.
        ci.cancel();
    }
}
