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
 * The cache key includes the settings the mesh geometry depends on, so changing them in-game
 * rebuilds rather than silently keeping the old mesh (which it used to do).
 */
public final class ElytraMeshCache {

    /**
     * Vanilla's ElytraModel: texOffs(22, 0), box 10 wide x 20 tall x 2 deep. The right wing
     * uses the same UVs drawn mirrored.
     */
    private static final int WING_TEX_U = 22;
    private static final int WING_TEX_V = 0;
    private static final int WING_WIDTH = 10;
    private static final int WING_HEIGHT = 20;

    /**
     * Always on - building the mesh from its top edge is what gives the wings their correct
     * facing, so it's baked in rather than being a toggle.
     */
    private static final boolean TOP_PIVOT = true;

    private static final Map<Key, Wings> CACHE = new HashMap<>();
    private static final Set<Key> FAILED = new HashSet<>();

    private ElytraMeshCache() {
    }

    /** Everything the built geometry depends on. */
    private record Key(ResourceLocation texture, int depth, boolean mirrorRight) {
    }

    /** A built pair of wing meshes. */
    public record Wings(Mesh left, Mesh right) {
    }

    public static Wings getOrBuild(ResourceLocation texture) {
        Key key = new Key(
                texture,
                Elytra3DConfig.DEPTH.get(),
                Elytra3DConfig.MIRROR_RIGHT.get());

        Wings cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (FAILED.contains(key)) {
            return null;
        }

        try {
            NativeImage image = readTexture(texture);
            if (image == null) {
                FAILED.add(key);
                return null;
            }

            // create3DMesh(natImage, width, height, depth, textureU, textureV,
            //              topPivot, rotationOffset, mirror)
            Mesh left = SkinLayersAPI.getMeshHelper().create3DMesh(
                    image, WING_WIDTH, WING_HEIGHT, key.depth(), WING_TEX_U, WING_TEX_V,
                    TOP_PIVOT, 0.0F, false);
            Mesh right = SkinLayersAPI.getMeshHelper().create3DMesh(
                    image, WING_WIDTH, WING_HEIGHT, key.depth(), WING_TEX_U, WING_TEX_V,
                    TOP_PIVOT, 0.0F, key.mirrorRight());

            Wings wings = new Wings(left, right);
            CACHE.put(key, wings);
            Elytra3D.LOGGER.info("Built 3D elytra mesh for {} (depth={}, mirror={})",
                    texture, key.depth(), key.mirrorRight());
            return wings;
        } catch (Exception e) {
            Elytra3D.LOGGER.error("Failed to build 3D elytra mesh for " + texture, e);
            FAILED.add(key);
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
