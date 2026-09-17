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
 * Replaces vanilla's flat elytra rendering with the 3D mesh version, for an elytra worn
 * in the vanilla chest slot.
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

        ResourceLocation texture = ElytraTextureResolver.resolve(entity);
        if (texture == null) {
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

        ci.cancel();
    }
}
