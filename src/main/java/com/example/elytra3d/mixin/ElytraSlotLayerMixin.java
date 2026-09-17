package com.example.elytra3d.mixin;

import com.example.elytra3d.Elytra3DConfig;
import com.example.elytra3d.render.ElytraMeshCache;
import com.illusivesoulworks.elytraslot.client.ElytraSlotLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Same 3D replacement as ElytraLayerMixin, but for Elytra Slot's own render layer.
 *
 * When an elytra is worn in Elytra Slot's Curios "back" slot rather than the vanilla chest
 * slot, vanilla's ElytraLayer never draws it - Elytra Slot's own layer does. So hooking only
 * vanilla's layer leaves curios-worn elytras rendering flat.
 *
 * Lives in its own optional mixin config (elytra3d.elytraslot.mixins.json) so this mod still
 * loads fine when Elytra Slot isn't installed.
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

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(ELYTRA_TEXTURE));

        final int noTint = 0xFFFFFFFF;

        wings.left().render(accessor.elytra3d$getLeftWing(), poseStack, consumer, light,
                OverlayTexture.NO_OVERLAY, noTint);
        wings.right().render(accessor.elytra3d$getRightWing(), poseStack, consumer, light,
                OverlayTexture.NO_OVERLAY, noTint);

        poseStack.popPose();

        ci.cancel();
    }
}
