package com.example.elytra3d.render;

import com.example.elytra3d.Elytra3D;
import com.example.elytra3d.Elytra3DConfig;
import com.mojang.blaze3d.platform.NativeImage;
import dev.tr7zw.skinlayers.api.Mesh;
import dev.tr7zw.skinlayers.api.SkinLayersAPI;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/**
 * Builds and caches the 3D wing meshes for an elytra texture.
 *
 * Vanilla's elytra texture lays each wing out as a flat region; 3D Skin Layers' create3DMesh
 * turns such a region into a solid extruded box mesh (handling the edge wrapping and skipping
 * fully-transparent pixels for us). We build both wings once per texture and cache them,
 * since rebuilding per-frame would be very expensive.
 */
public final class ElytraMeshCache {

    /**
     * Vanilla's elytra texture layout. Each wing occupies a 10x20 region; the left wing starts
     * at (22,0) and the right at (22,0) mirrored - vanilla's ElytraModel uses texOffs(22,0)
     * for both wings, drawing the right one mirrored.
     */
    private static final int WING_TEX_U = 22;
    private static final int WING_TEX_V = 0;
    private static final int WING_WIDTH = 10;
    private static final int WING_HEIGHT = 20;

    private static final Map<ResourceLocation, Wings> CACHE = new HashMap<>();
    private static final Set<ResourceLocation> FAILED = new HashSet<>();

    private ElytraMeshCache() {
    }

    /** A built pair of wing meshes. */
    public record Wings(Mesh left, Mesh right) {
    }

    public static Wings getOrBuild(ResourceLocation texture) {
        Wings cached = CACHE.get(texture);
        if (cached != null) {
            return cached;
        }
        if (FAILED.contains(texture)) {
            return null;
        }

        try {
            NativeImage image = readTexture(texture);
            if (image == null) {
                FAILED.add(texture);
                return null;
            }

            float thickness = Elytra3DConfig.THICKNESS.get().floatValue();

            // Real parameter order (from 3D Skin Layers' own MeshHelperImplementation):
            //   create3DMesh(natImage, width, height, depth, textureU, textureV,
            //                topPivot, rotationOffset, mirror)
            // Depth is the extrusion thickness in pixels; vanilla's elytra wing box is 1 deep,
            // so we scale that by the configured thickness. Mirror flips the right wing, the
            // same way vanilla's ElytraModel draws the right wing mirrored from the same UVs.
            int depth = Math.max(1, Math.round(thickness));

            Mesh left = SkinLayersAPI.getMeshHelper().create3DMesh(
                    image, WING_WIDTH, WING_HEIGHT, depth, WING_TEX_U, WING_TEX_V,
                    false, 0.0F, false);
            Mesh right = SkinLayersAPI.getMeshHelper().create3DMesh(
                    image, WING_WIDTH, WING_HEIGHT, depth, WING_TEX_U, WING_TEX_V,
                    false, 0.0F, true);

            Wings wings = new Wings(left, right);
            CACHE.put(texture, wings);
            Elytra3D.LOGGER.info("Built 3D elytra mesh for {}", texture);
            return wings;
        } catch (Exception e) {
            Elytra3D.LOGGER.error("Failed to build 3D elytra mesh for " + texture, e);
            FAILED.add(texture);
            return null;
        }
    }

    private static NativeImage readTexture(ResourceLocation texture) throws Exception {
        Optional<Resource> resource =
                Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream stream = resource.get().open()) {
            return NativeImage.read(stream);
        }
    }

    /** Called on resource reload - textures may have changed, so drop everything. */
    public static void clear() {
        CACHE.clear();
        FAILED.clear();
    }
}
