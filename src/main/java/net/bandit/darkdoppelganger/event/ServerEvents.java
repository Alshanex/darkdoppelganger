package net.bandit.darkdoppelganger.event;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.network.spell.ClientboundSyncTargetingData;
import io.redspace.ironsspellbooks.setup.Messages;
import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = DarkDoppelgangerMod.MOD_ID)
public class ServerEvents {
    @SubscribeEvent
    public static void onSpellCasted(SpellPreCastEvent event) {
        if(event.getSpellId().equals(SpellRegistry.ROOT_SPELL.get().getSpellId()) || event.getSpellId().equals(SpellRegistry.SLOW_SPELL.get().getSpellId())
                || event.getSpellId().equals(SpellRegistry.BLIGHT_SPELL.get().getSpellId()) || event.getSpellId().equals(SpellRegistry.ACID_ORB_SPELL.get().getSpellId())){
            if(event.getEntity() != null){
                boolean doppelClose = event.getEntity().level().getEntitiesOfClass(DarkDoppelgangerEntity.class, event.getEntity().getBoundingBox().inflate(50, 50, 50)).isEmpty();
                if(!doppelClose){
                    event.setCanceled(true);
                    if(event.getEntity() instanceof ServerPlayer player){
                        if(event.getSpellId().equals(SpellRegistry.ROOT_SPELL.get().getSpellId()) || event.getSpellId().equals(SpellRegistry.BLIGHT_SPELL.get().getSpellId()) || event.getSpellId().equals(SpellRegistry.SLOW_SPELL.get().getSpellId())){
                            Messages.sendToPlayer(new ClientboundSyncTargetingData(SpellRegistry.getSpell(event.getSpellId()), new ArrayList<>()), player);
                        }
                        if(event.getSpellId().equals(SpellRegistry.SLOW_SPELL.get().getSpellId())){
                            event.getEntity().level().getEntitiesOfClass(TargetedAreaEntity.class, event.getEntity().getBoundingBox().inflate(50, 50, 50)).forEach(Entity::discard);
                        }
                    }
                }
            }
        }
    }
}
