# 3D Elytra

Renders the elytra as a solid, extruded 3D model instead of a flat textured plane, using
3D Skin Layers' mesh-building API.

Client-side only, purely visual. Requires [3D Skin Layers](https://modrinth.com/mod/3dskinlayers).

## How it works

3D Skin Layers exposes a public `MeshHelper.create3DMesh(...)` that turns a rectangular region
of a texture into a solid voxel mesh (handling edge wrapping and skipping transparent pixels).
This mod:

1. Reads `textures/entity/elytra.png` from the resource manager into a `NativeImage`.
2. Builds one mesh per wing from the elytra texture's 10x20 wing region, cached per texture.
3. Cancels vanilla's flat `ElytraLayer` render and draws the meshes instead.

The meshes are rendered against vanilla's own `leftWing`/`rightWing` `ModelPart`s, which vanilla
has already animated for the current pose - so flight, standing and crouching animations are
inherited for free rather than reimplemented.

## Config

In-game via **Mods -> 3D Elytra -> Config** (NeoForge generates the screen automatically):

- `enabled` - turn the 3D rendering off to fall back to vanilla's flat elytra.
- `thickness` - how far the model is extruded, in pixels (default 1.0).

## Scope

Currently handles the **vanilla elytra only**. Modded elytras (Soul Elytra etc.) keep vanilla's
flat rendering - support for those is a planned follow-up once the base rendering is confirmed
correct.

## Building

```bash
gradle build
```

Output: `build/libs/elytra3d-1.0.0.jar`. Client-side only - no need to install it on servers.
