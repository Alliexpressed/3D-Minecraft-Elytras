package com.example.elytra3d.render;

import com.example.elytra3d.Elytra3D;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the entity texture to use for the 3D elytra mesh, checking both the vanilla
 * chest slot and (if Curios is loaded) all Curios accessory slots.
 *
 * Returns null if no recognised elytra is equipped, letting the caller fall back to vanilla.
 */
public final class ElytraTextureResolver {

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    private static final ResourceLocation VANILLA_ELYTRA =
            ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");

    /**
     * Known modded elytras: item registry name -> entity texture path.
     * Add new entries here as more modded elytras are supported.
     */
    private static final Map<ResourceLocation, ResourceLocation> MODDED_ELYTRAS = new HashMap<>();

    static {
        MODDED_ELYTRAS.put(
                ResourceLocation.fromNamespaceAndPath("deeperdarker", "soul_elytra"),
                ResourceLocation.fromNamespaceAndPath("deeperdarker",
                        "textures/entity/soul_elytra.png"));
    }

    private ElytraTextureResolver() {
    }

    /**
     * True if any recognised elytra is equipped anywhere (chest or Curios slots). Used by
     * ElytraLayerMixin to suppress vanilla's flat render even when the elytra isn't in the
     * chest slot, preventing duplicates when ElytraSlotLayerMixin handles the actual draw.
     */
    public static boolean isElytraEquippedAnywhere(LivingEntity entity) {
        return resolve(entity) != null;
    }

    /**
     * Returns the entity texture for the elytra the entity is wearing, or null if none of
     * our supported elytras are equipped anywhere.
     */
    public static ResourceLocation resolve(LivingEntity entity) {
        // Vanilla chest slot first.
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        ResourceLocation tex = textureFor(chest);
        if (tex != null) {
            return tex;
        }

        // Curios accessory slots (e.g. Elytra Slot's "back" slot).
        if (CURIOS_LOADED) {
            tex = resolveFromCurios(entity);
            if (tex != null) {
                return tex;
            }
        }

        return null;
    }

    private static ResourceLocation resolveFromCurios(LivingEntity entity) {
        try {
            Optional<?> inventory = top.theillusivec4.curios.api.CuriosApi
                    .getCuriosInventory(entity);
            if (inventory.isEmpty()) {
                return null;
            }
            // Walk every curio slot looking for a known elytra.
            var handler = (top.theillusivec4.curios.api.type.capability.ICuriosItemHandler)
                    inventory.get();
            // findFirstCurio returns Optional<SlotResult>; we just need the stack.
            var result = handler.findFirstCurio(stack -> textureFor(stack) != null);
            if (result.isPresent()) {
                return textureFor(result.get().stack());
            }
        } catch (Exception e) {
            Elytra3D.LOGGER.error("Failed to read Curios inventory for 3D elytra texture", e);
        }
        return null;
    }

    private static ResourceLocation textureFor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ElytraItem)) {
            return null;
        }
        if (stack.is(Items.ELYTRA)) {
            return VANILLA_ELYTRA;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return MODDED_ELYTRAS.get(id);
    }
}
