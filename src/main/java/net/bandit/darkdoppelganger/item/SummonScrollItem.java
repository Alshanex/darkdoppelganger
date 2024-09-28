package net.bandit.darkdoppelganger.item;

import net.bandit.darkdoppelganger.entity.EntityRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import org.jetbrains.annotations.NotNull;

public class SummonScrollItem extends Item {

    public SummonScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide) {
            Player player = context.getPlayer();
            ServerLevel serverWorld = (ServerLevel) world;

            // Create and spawn Dark Doppelganger entity
            DarkDoppelgangerEntity entity = new DarkDoppelgangerEntity(EntityRegistry.DARK_DOPPELGANGER.get(), serverWorld);
            assert player != null;
            entity.setPos(player.getX() + 2, player.getY(), player.getZ() + 2);  // Spawns next to the player

            // Set the entity's name and summoner information
            entity.setSummoner(player); // Store summoner's name and UUID
            entity.setCustomName(Component.literal(player.getName().getString())); // Set the entity name to the player's name
            entity.setCustomNameVisible(true); // Ensure the name is visible above the entity

            serverWorld.addFreshEntity(entity);

            // Play summon sound at player's location
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 0.2F);  // Use a fitting sound event

            // Shrink item stack for both survival and creative
            if (!player.isCreative()) {
                context.getItemInHand().shrink(1); // Reduce item stack by 1
            }
        }
        return InteractionResult.SUCCESS;
    }
}
