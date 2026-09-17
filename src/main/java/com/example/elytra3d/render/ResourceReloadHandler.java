package com.example.elytra3d.render;

import com.example.elytra3d.Elytra3D;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * Drops cached meshes when resources reload (resource pack change, F3+T), so a new elytra
 * texture doesn't keep rendering with the old mesh.
 */
@EventBusSubscriber(modid = Elytra3D.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ResourceReloadHandler {

    private ResourceReloadHandler() {
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(
                (preparationBarrier, resourceManager, preparationProfiler, reloadProfiler,
                        backgroundExecutor, gameExecutor) ->
                        preparationBarrier.wait(null).thenRunAsync(
                                ElytraMeshCache::clear, gameExecutor));
    }
}
