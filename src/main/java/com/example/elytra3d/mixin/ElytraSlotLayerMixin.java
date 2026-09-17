package com.example.elytra3d.mixin;

import com.example.elytra3d.Elytra3DConfig;
import com.example.elytra3d.render.ElytraMeshCache;
import com.example.elytra3d.render.ElytraWingRenderer;
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

/**
 * Same replacement as ElytraLayerMixin, but for Elytra Slot's own render layer - used when the
 * elytra is worn in its Curios "back" slot, where vanilla's ElytraLayer never runs.
 */
@Mixin(ElytraSlotLayer.class)
public abstract class ElytraSlotLayerMixin {

    private static final ResourceLocation ELYTRA_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");

    @Shadow
    @Final
    private ElytraModel<LivingEntity> elytraModel;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void elytra3d$renderElytraSlot(PoseStack poseStack, MultiBufferSource buffer, int light,
            LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {

        if (!Elytra3DConfig.ENABLED.get()) {
            return;
        }

        ElytraMeshCache.Wings wings = ElytraMeshCache.getOrBuild(ELYTRA_TEXTURE);
        if (wings == null) {
            return;
        }

        ElytraModelAccessor accessor = (ElytraModelAccessor) this.elytraModel;
        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(ELYTRA_TEXTURE));

        ElytraWingRenderer.render(wings,
                accessor.elytra3d$getLeftWing(), accessor.elytra3d$getRightWing(),
                poseStack, consumer, light);

        ci.cancel();
    }
}
