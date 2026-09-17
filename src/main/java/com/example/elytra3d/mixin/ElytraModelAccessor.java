package com.example.elytra3d.mixin;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes vanilla's private wing ModelParts. These are already animated by vanilla each frame
 * (flight pose, standing, crouching), so handing them to Mesh.render lets our 3D meshes follow
 * the exact same motion without reimplementing any of it.
 */
@Mixin(ElytraModel.class)
public interface ElytraModelAccessor {

    @Accessor("leftWing")
    ModelPart elytra3d$getLeftWing();

    @Accessor("rightWing")
    ModelPart elytra3d$getRightWing();
}
