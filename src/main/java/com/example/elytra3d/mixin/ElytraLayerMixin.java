package com.example.elytra3d.mixin;

import com.example.elytra3d.Elytra3DConfig;
import com.example.elytra3d.render.ElytraMeshCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces vanilla's flat elytra rendering with the 3D mesh version.
 *
 * Injects at HEAD and cancels the vanilla render when we successfully draw our own, so the
 * flat plane doesn't show through the solid model. Any failure path (config off, no mesh
 * built, unexpected texture) simply returns without cancelling, letting vanilla draw normally.
 */
@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin {

    private static final ResourceLocation ELYTRA_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");

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

        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(Items.ELYTRA)) {
            // Only the vanilla elytra for now - modded elytras keep vanilla rendering.
            return;
        }

        ElytraMeshCache.Wings wings = ElytraMeshCache.getOrBuild(ELYTRA_TEXTURE);
        if (wings == null) {
            return;
        }

        ElytraModelAccessor accessor = (ElytraModelAccessor) this.elytraModel;

        poseStack.pushPose();
        // Vanilla offsets the elytra slightly back from the body before rendering; match it.
        poseStack.translate(0.0F, 0.0F, 0.125F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(ELYTRA_TEXTURE));

        wings.left().render(accessor.elytra3d$getLeftWing(), poseStack, consumer, light,
                OverlayTexture.NO_OVERLAY);
        wings.right().render(accessor.elytra3d$getRightWing(), poseStack, consumer, light,
                OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        ci.cancel();
    }
}
