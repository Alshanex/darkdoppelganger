package net.bandit.darkdoppelganger.entity;

import com.mojang.authlib.GameProfile;
import net.bandit.darkdoppelganger.registry.ModSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class DarkDoppelgangerEntity extends PathfinderMob {

    private Player summonerPlayer; // Store the Player object directly
    private final ServerBossEvent bossEvent;
    private boolean musicPlaying = false;
    private boolean secondPhaseTriggered = false;
    private boolean thirdPhaseTriggered = false;
    private boolean isClone = false;

    public DarkDoppelgangerEntity(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        this.setCustomName(Component.literal("Dark Doppelganger"));
        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        // Create the boss bar with the name of the entity and color
        this.bossEvent = new ServerBossEvent(Component.literal("Dark Doppelganger"), ServerBossEvent.BossBarColor.PURPLE, ServerBossEvent.BossBarOverlay.PROGRESS);
    }

    public void setSummonerPlayer(Player summoner) {
        this.summonerPlayer = summoner;

        // Immediately copy the summoner's equipment after setting the player
        if (summoner != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                this.setItemSlot(slot, summoner.getItemBySlot(slot));
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.setPersistenceRequired();

        if (!this.level().isClientSide && !this.isClone) { // Ensure this only runs on the server
            // Play Ender Dragon boss music if not already playing
            if (!musicPlaying) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.BOSS_FIGHT_MUSIC.get(), SoundSource.MUSIC, 0.5F, 1.0F);
                musicPlaying = true;
            }
        }

        // Summoning sequence with particles (client-side only)
        if (this.level().isClientSide) {
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * 2, this.getY() + this.random.nextDouble(), this.getZ() + (this.random.nextDouble() - 0.5) * 2, 0, 0, 0);
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.isClone) {
            this.bossEvent.addPlayer(player);  // Add player to boss bar only for real boss
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (!this.isClone) {
            this.bossEvent.removePlayer(player);  // Remove player from boss bar only for real boss
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isClone) {
            // Update the boss bar
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (!secondPhaseTriggered && this.getHealth() / this.getMaxHealth() < 0.2) {
                triggerSecondPhase();
            }

            if (!thirdPhaseTriggered && this.getHealth() / this.getMaxHealth() < 0.1) {
                triggerThirdPhase();
            }
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.isClone) {
            return;  // Prevent clones from affecting music or dropping items
        }

        musicPlaying = false; // Reset the flag to allow music to play again next time

        if (!this.level().isClientSide) {
            // Check if the cause of death is from a player
            if (cause.getEntity() instanceof ServerPlayer serverPlayer) {
                // Award advancement
                Advancement advancement = serverPlayer.getServer().getAdvancements()
                        .getAdvancement(new ResourceLocation("darkdoppelganger", "kill_dark_doppelganger"));

                if (advancement != null) {
                    serverPlayer.getAdvancements().award(advancement, "kill");
                    serverPlayer.sendSystemMessage(Component.literal("You have slain the Dark Doppelganger!"));
                }

                // Drop items directly upon death
                this.spawnAtLocation(Items.NETHER_STAR);
                this.spawnAtLocation(Items.DIAMOND_BLOCK, 3);
                this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY(), this.getZ(), 1500));
            }

            // Stop the music for the real boss only
            this.level().getServer().getPlayerList().getPlayers().forEach(player -> {
                player.connection.send(new ClientboundStopSoundPacket(ModSounds.BOSS_FIGHT_MUSIC.get().getLocation(), SoundSource.MUSIC));
            });

            // Remove the boss bar when the entity dies
            this.bossEvent.removeAllPlayers();
        }
    }

    private void triggerSecondPhase() {
        this.secondPhaseTriggered = true;

        // Fully regenerate health
        this.setHealth(this.getMaxHealth());

        // Send message to nearby players
        for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(50))) {
            player.sendSystemMessage(Component.literal("The Dark Doppelganger has entered its second phase!"));
        }

        // Add the fireball attack goal
        this.goalSelector.addGoal(2, new FireballAttackGoal(this));
    }

    private void triggerThirdPhase() {
        this.thirdPhaseTriggered = true;

        // Fully regenerate health
        this.setHealth(this.getMaxHealth());
        this.bossEvent.setName(Component.literal("Dark Doppelganger - Final Phase"));

        for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(50))) {
            player.sendSystemMessage(Component.literal("The Dark Doppelganger has entered its final phase!"));
        }

        // Spawn exactly two clones
        spawnShadowClones(2);
    }

    private void spawnShadowClones(int number) {
        if (this.summonerPlayer == null) return; // Prevent issues if summonerPlayer is null

        for (int i = 0; i < number; i++) {
            DarkDoppelgangerEntity clone = EntityRegistry.DARK_DOPPELGANGER.get().create(this.level());
            if (clone != null) {
                clone.setPos(this.getX() + random.nextInt(5) - 2, this.getY(), this.getZ() + random.nextInt(5) - 2);
                clone.setHealth((float) (clone.getMaxHealth() * 0.3));  // Clones have 30% of health
                clone.isClone = true;  // Mark it as a clone

                // Copy armor and weapons from the summoner
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    clone.setItemSlot(slot, summonerPlayer.getItemBySlot(slot));
                }

                this.level().addFreshEntity(clone);
            }
        }
    }


    // Custom goal for shooting fireballs
    private static class FireballAttackGoal extends Goal {
        private final DarkDoppelgangerEntity boss;
        private int cooldown = 0;

        public FireballAttackGoal(DarkDoppelgangerEntity boss) {
            this.boss = boss;
        }

        @Override
        public boolean canUse() {
            // Only shoot fireballs if there's a target
            return this.boss.getTarget() != null && cooldown-- <= 0;
        }

        @Override
        public void start() {
            LivingEntity target = this.boss.getTarget();
            if (target != null) {
                double d0 = 4.0;
                double x = target.getX() - this.boss.getX();
                double y = target.getY(0.5) - this.boss.getY(0.5);
                double z = target.getZ() - this.boss.getZ();
                this.boss.level().addFreshEntity(new LargeFireball(this.boss.level(), this.boss, x, y, z, 1));
            }
            cooldown = 100; // Cooldown before the next fireball (adjust as needed)
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 1F));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new MoveTowardsTargetGoal(this, 1.2D, 48.0F));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    // Create default attributes for Dark Doppelganger
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.45)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag dataTag) {
        return super.finalizeSpawn(world, difficulty, spawnType, groupData, dataTag);
    }

    // Method to get the GameProfile from the summonerPlayer
    public GameProfile getSummonerProfile() {
        if (this.summonerPlayer != null) {
            return this.summonerPlayer.getGameProfile();
        }
        return null; // Return null if summonerPlayer is not set
    }
}
