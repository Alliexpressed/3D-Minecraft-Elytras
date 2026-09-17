package com.example.elytra3d;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client config. NeoForge renders this automatically in the standard in-game mod config screen
 * (Mods -> 3D Elytra -> Config), so there's no custom screen to maintain.
 */
public final class Elytra3DConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue THICKNESS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("3D Elytra settings").push("general");

        ENABLED = builder
                .comment("Render the elytra as a 3D model. Off falls back to vanilla's flat elytra.")
                .define("enabled", true);

        THICKNESS = builder
                .comment("How far the elytra is extruded, in pixels. 1.0 matches the thickness",
                        "3D Skin Layers uses for skin layers. Higher is chunkier.")
                .defineInRange("thickness", 1.0D, 0.25D, 4.0D);

        builder.pop();

        SPEC = builder.build();
    }

    private Elytra3DConfig() {
    }
}
