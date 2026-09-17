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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles 3D elytra rendering for the vanilla ElytraLayer (chest slot path).
 *
 * Two cases:
 * 1. Elytra in the chest slot: draw the 3D mesh here and cancel vanilla's flat render.
 * 2. Elytra in a Curios slot: ElytraSlotLayerMixin handles the real draw. Cancel here
 *    unconditionally to suppress the duplicate flat render vanilla still attempts.
 *
 * ElytraLayer is registered for both player model variants (default and slim), so this mixin
 * fires twice per frame per player. A per-frame UUID set deduplicates it so only the first
 * call draws; the second just cancels. The set entry is cleared when an entity with no
 * recognised elytra is seen, resetting it for the next frame.
 */
@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin {

    private static final Set<UUID> RENDERED_THIS_FRAME = new HashSet<>();

    @Shadow
    @Final
    private ElytraModel<?> elytraModel;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void elytra3d$renderElytra(PoseStack poseStack, MultiBufferSource buffer, int light,
            LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {

        if (!Elytra3DConfig.ENABLED.get()) {
            RENDERED_THIS_FRAME.remove(entity.getUUID());
            return;
        }

        // If no recognised elytra equipped anywhere, let vanilla handle it and reset the
        // dedup set for this entity so the next frame starts fresh.
        if (!ElytraTextureResolver.isElytraEquippedAnywhere(entity)) {
            RENDERED_THIS_FRAME.remove(entity.getUUID());
            return;
        }

        // Always cancel vanilla's flat render once we know an elytra is equipped.
        // For the Curios case, ElytraSlotLayerMixin draws the proper animated mesh.
        // For the chest slot case, draw the 3D mesh on the first call only.
        ResourceLocation texture = ElytraTextureResolver.resolveChestSlotOnly(entity);
        boolean alreadyRendered = !RENDERED_THIS_FRAME.add(entity.getUUID());

        if (texture != null && !alreadyRendered) {
            ElytraMeshCache.Wings wings = ElytraMeshCache.getOrBuild(texture);
            if (wings != null) {
                ElytraModelAccessor accessor = (ElytraModelAccessor) this.elytraModel;
                // Pose the wings for this frame. At @At("HEAD") setupAnim hasn't run yet,
                // so we call it ourselves with the same args vanilla would use.
                // Cast through raw type to satisfy the wildcard capture.
                @SuppressWarnings("unchecked")
                ElytraModel<LivingEntity> model = (ElytraModel<LivingEntity>) (ElytraModel<?>) this.elytraModel;
                model.setupAnim(entity, limbSwing, limbSwingAmount,
                        ageInTicks, netHeadYaw, headPitch);
                VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));
                ElytraWingRenderer.render(wings,
                        accessor.elytra3d$getLeftWing(), accessor.elytra3d$getRightWing(),
                        poseStack, consumer, light);
            }
        }

        ci.cancel();
    }
}
