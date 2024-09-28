package net.bandit.darkdoppelganger.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.bandit.darkdoppelganger.DarkDoppelgangerMod;

@Mod.EventBusSubscriber(modid = DarkDoppelgangerMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSounds {

    // Create the Deferred Register for sound events
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DarkDoppelgangerMod.MOD_ID);

    // Register your custom boss music
    public static final RegistryObject<SoundEvent> BOSS_FIGHT_MUSIC = SOUND_EVENTS.register("boss_fight",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(DarkDoppelgangerMod.MOD_ID, "boss_fight")));

    // Method to register sound events
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);  // Register the sound events with the event bus
    }
}
