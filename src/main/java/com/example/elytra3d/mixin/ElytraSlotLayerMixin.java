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

/**
 * Same replacement as ElytraLayerMixin, but for Elytra Slot's own render layer, used when
 * the elytra is worn in a Curios accessory slot.
 */
@Mixin(ElytraSlotLayer.class)
public abstract class ElytraSlotLayerMixin {

    @Shadow
    @Final
    private ElytraModel<LivingEntity> elytraModel;

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
