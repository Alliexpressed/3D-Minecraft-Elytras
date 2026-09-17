package com.example.elytra3d;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * 3D Elytra - renders the elytra as an extruded voxel model instead of a flat textured plane,
 * using 3D Skin Layers' mesh-building API.
 *
 * Client-side only; purely visual.
 */
@Mod(value = Elytra3D.MOD_ID, dist = net.neoforged.api.distmarker.Dist.CLIENT)
public class Elytra3D {

    public static final String MOD_ID = "elytra3d";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Elytra3D(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Elytra3DConfig.SPEC);
    }
}
