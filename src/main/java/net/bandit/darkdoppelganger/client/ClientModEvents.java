package net.bandit.darkdoppelganger.client;

import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.bandit.darkdoppelganger.entity.EntityRegistry;
import net.bandit.darkdoppelganger.client.renderer.DarkDoppelgangerRenderer;

@Mod.EventBusSubscriber(modid = DarkDoppelgangerMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register the entity renderer
        EntityRenderers.register(EntityRegistry.DARK_DOPPELGANGER.get(), DarkDoppelgangerRenderer::new);
    }
}
