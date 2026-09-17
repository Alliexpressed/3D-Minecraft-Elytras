package com.example.elytra3d.mixin;

import com.example.elytra3d.Elytra3DConfig;
import com.example.elytra3d.render.ElytraMeshCache;
import com.example.elytra3d.render.ElytraTextureResolver;
import com.example.elytra3d.render.ElytraWingRenderer;
import com.illusivesoulworks.elytraslot.client.ElytraRenderResult;
import com.illusivesoulworks.elytraslot.client.ElytraSlotLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
 * Replaces Elytra Slot's flat elytra render with the 3D mesh version.
 *
 * Elytra Slot registers ElytraSlotLayer for BOTH player model variants (default and slim),
 * so this mixin would fire twice per frame per player without deduplication. A per-frame
 * UUID set ensures only the first call draws; the second just cancels without drawing again.
 * The set is cleared on each outer render() call so it resets every frame.
 */
@Mixin(ElytraSlotLayer.class)
public abstract class ElytraSlotLayerMixin {

    /** Entities already rendered this frame - cleared at the start of each render() call. */
    private static final Set<UUID> RENDERED_THIS_FRAME = new HashSet<>();

    @Shadow
    @Final
    private ElytraModel<LivingEntity> elytraModel;

    /** Clear the dedup set at the start of each outer render() call. */
    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void elytra3d$clearDedup(PoseStack poseStack, MultiBufferSource buffer, int light,
            LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        // Only clear for this entity, not globally, so other players render correctly.
        RENDERED_THIS_FRAME.remove(entity.getUUID());
    }

    @Inject(
            method = "lambda$render$0(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFFFLnet/minecraft/client/renderer/MultiBufferSource;ILcom/illusivesoulworks/elytraslot/client/ElytraRenderResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ElytraModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    remap = true
            ),
            cancellable = true,
            remap = false
    )
    private void elytra3d$renderElytraSlot(
            LivingEntity entity, PoseStack poseStack,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw,
            MultiBufferSource buffer, int light,
            ElytraRenderResult elytraRenderResult,
            CallbackInfo ci) {

        if (!Elytra3DConfig.ENABLED.get()) {
            return;
        }

        ResourceLocation texture = ElytraTextureResolver.resolve(entity);
        if (texture == null) {
            return;
        }

        // Already rendered for this entity this frame (second model variant call) -
        // cancel without drawing to suppress the duplicate.
        boolean alreadyRendered = !RENDERED_THIS_FRAME.add(entity.getUUID());
        if (alreadyRendered) {
            poseStack.popPose();
            ci.cancel();
            return;
        }

        ElytraMeshCache.Wings wings = ElytraMeshCache.getOrBuild(texture);
        if (wings == null) {
            return;
        }

        ElytraModelAccessor accessor = (ElytraModelAccessor) this.elytraModel;
        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));

        ElytraWingRenderer.render(wings,
                accessor.elytra3d$getLeftWing(), accessor.elytra3d$getRightWing(),
                poseStack, consumer, light);

        poseStack.popPose();
        ci.cancel();
    }
}
