package net.bandit.darkdoppelganger;

import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DarkDoppelgangerMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

//    @SubscribeEvent
//    public static void onLivingDeath(LivingDeathEvent event) {
//        if (event.getEntity() instanceof DarkDoppelgangerEntity) {
//            if (event.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
//                Advancement advancement = serverPlayer.getServer().getAdvancements()
//                        .getAdvancement(new ResourceLocation("darkdoppelganger", "kill_dark_doppelganger"));
//
//                if (advancement != null) {
//                    serverPlayer.getAdvancements().award(advancement, "kill");
//                }
//            }
//        }
//    }
}
