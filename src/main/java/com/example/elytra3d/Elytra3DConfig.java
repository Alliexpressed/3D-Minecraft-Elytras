package com.example.elytra3d;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client config, rendered automatically in the standard in-game screen
 * (Mods -> 3D Elytra -> Config).
 *
 * Each wing has its own independent offset and rotation. The mirrored right wing doesn't
 * simply sit opposite the left one, so sharing values between them can't line both up.
 */
public final class Elytra3DConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.IntValue DEPTH;
    public static final ModConfigSpec.BooleanValue MIRROR_RIGHT;

    public static final Wing LEFT;
    public static final Wing RIGHT;

    /** Per-wing alignment values. */
    public static final class Wing {
        public final ModConfigSpec.DoubleValue offsetX;
        public final ModConfigSpec.DoubleValue offsetY;
        public final ModConfigSpec.DoubleValue offsetZ;
        public final ModConfigSpec.DoubleValue rotateX;
        public final ModConfigSpec.DoubleValue rotateY;
        public final ModConfigSpec.DoubleValue rotateZ;

        private Wing(ModConfigSpec.Builder builder, String name, double defaultX) {
            builder.comment("Alignment for the " + name + " wing. Offsets are in pixels",
                            "(1/16 block) and applied first; rotations are in degrees and",
                            "applied after, spinning the mesh about that offset point.")
                    .push(name);

            offsetX = builder.defineInRange("offsetX", defaultX, -32.0D, 32.0D);
            offsetY = builder.defineInRange("offsetY", 0.0D, -32.0D, 32.0D);
            offsetZ = builder.defineInRange("offsetZ", 0.0D, -32.0D, 32.0D);
            rotateX = builder.defineInRange("rotateX", 0.0D, -360.0D, 360.0D);
            rotateY = builder.defineInRange("rotateY", 0.0D, -360.0D, 360.0D);
            rotateZ = builder.defineInRange("rotateZ", 0.0D, -360.0D, 360.0D);

            builder.pop();
        }
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("3D Elytra settings").push("general");

        ENABLED = builder
                .comment("Render the elytra as a 3D model. Off falls back to vanilla's flat elytra.")
                .define("enabled", true);

        DEPTH = builder
                .comment("Extrusion depth in pixels. Vanilla's elytra wing box is 2 deep.")
                .defineInRange("depth", 2, 1, 8);

        MIRROR_RIGHT = builder
                .comment("Mirror the right wing, matching how vanilla draws it from the same UVs.")
                .define("mirrorRightWing", true);

        builder.pop();

        builder.comment("Per-wing alignment against vanilla's wing model parts.").push("alignment");
        LEFT = new Wing(builder, "left", 0.0D);
        RIGHT = new Wing(builder, "right", 0.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private Elytra3DConfig() {
    }
}
