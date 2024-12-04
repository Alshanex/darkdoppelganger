package net.bandit.darkdoppelganger.entity;

import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DarkDoppelgangerMod.MOD_ID);


    public static final RegistryObject<EntityType<DarkDoppelgangerEntity>> DARK_DOPPELGANGER =
            ENTITY_TYPES.register("dark_doppelganger",
                    () -> EntityType.Builder.of(DarkDoppelgangerEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .setTrackingRange(80)
                            .setUpdateInterval(3)
                            .setShouldReceiveVelocityUpdates(true)
                            .build(DarkDoppelgangerMod.MOD_ID + ":dark_doppelganger"));

}
