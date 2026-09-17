package com.example.elytra3d;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client config, rendered automatically in the standard in-game screen
 * (Mods -> 3D Elytra -> Config).
 *
 * Wing offsets and rotations use plain define() with a validator rather than defineInRange()
 * so NeoForge renders them as free text fields rather than sliders. Sliders produced by
 * defineInRange() clamp to 0 and reject negative typed values, even when the lower bound
 * is explicitly set negative.
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
        public final ModConfigSpec.ConfigValue<Double> offsetX;
        public final ModConfigSpec.ConfigValue<Double> offsetY;
        public final ModConfigSpec.ConfigValue<Double> offsetZ;
        public final ModConfigSpec.ConfigValue<Double> rotateX;
        public final ModConfigSpec.ConfigValue<Double> rotateY;
        public final ModConfigSpec.ConfigValue<Double> rotateZ;

        private Wing(ModConfigSpec.Builder builder, String name, double defaultX, double defaultY, double defaultZ) {
            builder.comment("Alignment for the " + name + " wing. Offsets are in pixels",
                            "(1/16 block); rotations in degrees.")
                    .push(name);

            offsetX = builder.comment("Sideways offset.")
                    .define("offsetX", defaultX, v -> v instanceof Double d && d >= -32 && d <= 32);
            offsetY = builder.comment("Vertical offset. Positive values move DOWN in Minecraft coordinates.")
                    .define("offsetY", defaultY, v -> v instanceof Double d && d >= -32 && d <= 32);
            offsetZ = builder.comment("Depth offset. Positive values push wings further from the body.")
                    .define("offsetZ", defaultZ, v -> v instanceof Double d && d >= -32 && d <= 32);
            rotateX = builder.comment("Rotation around X axis in degrees.")
                    .define("rotateX", (double) 0, v -> v instanceof Double d && d >= -360 && d <= 360);
            rotateY = builder.comment("Rotation around Y axis in degrees.")
                    .define("rotateY", (double) 0, v -> v instanceof Double d && d >= -360 && d <= 360);
            rotateZ = builder.comment("Rotation around Z axis in degrees.")
                    .define("rotateZ", (double) 0, v -> v instanceof Double d && d >= -360 && d <= 360);

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
        LEFT  = new Wing(builder, "left",  -3.0D, -1.0D, 2.0D);
        RIGHT = new Wing(builder, "right",  3.0D, -1.0D, 2.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private Elytra3DConfig() {
    }
}
