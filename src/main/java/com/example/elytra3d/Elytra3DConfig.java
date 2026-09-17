package com.example.elytra3d;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client config, rendered automatically in the standard in-game screen
 * (Mods -> 3D Elytra -> Config).
 *
 * The offset/rotation values exist because the mesh's local origin has to be lined up by hand
 * with vanilla's wing ModelPart. The defaults are derived from vanilla's ElytraModel geometry,
 * but they're exposed so they can be nudged in-game rather than requiring a rebuild.
 */
public final class Elytra3DConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue DEPTH;
    public static final ModConfigSpec.DoubleValue OFFSET_X;
    public static final ModConfigSpec.DoubleValue OFFSET_Y;
    public static final ModConfigSpec.DoubleValue OFFSET_Z;
    public static final ModConfigSpec.BooleanValue MIRROR_RIGHT;
    public static final ModConfigSpec.BooleanValue TOP_PIVOT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("3D Elytra settings").push("general");

        ENABLED = builder
                .comment("Render the elytra as a 3D model. Off falls back to vanilla's flat elytra.")
                .define("enabled", true);

        DEPTH = builder
                .comment("Extrusion depth in pixels. Vanilla's elytra wing box is 2 deep.")
                .defineInRange("depth", 2, 1, 8);

        builder.pop();

        builder.comment("Alignment of the 3D mesh against vanilla's wing model part.",
                        "Units are pixels (1/16 block). Adjust if the wings sit in the wrong",
                        "place or face the wrong way.")
                .push("alignment");

        OFFSET_X = builder
                .comment("Sideways offset. Vanilla's left wing box starts at x=-10 from its part.")
                .defineInRange("offsetX", -10.0D, -32.0D, 32.0D);

        OFFSET_Y = builder
                .comment("Vertical offset.")
                .defineInRange("offsetY", 0.0D, -32.0D, 32.0D);

        OFFSET_Z = builder
                .comment("Depth offset (away from the player's back).")
                .defineInRange("offsetZ", 0.0D, -32.0D, 32.0D);

        MIRROR_RIGHT = builder
                .comment("Mirror the right wing, matching how vanilla draws it from the same UVs.")
                .define("mirrorRightWing", true);

        TOP_PIVOT = builder
                .comment("Build the mesh pivoting from its top edge rather than its origin.",
                        "Try flipping this if the wings are upside down.")
                .define("topPivot", false);

        builder.pop();

        SPEC = builder.build();
    }

    private Elytra3DConfig() {
    }
}
