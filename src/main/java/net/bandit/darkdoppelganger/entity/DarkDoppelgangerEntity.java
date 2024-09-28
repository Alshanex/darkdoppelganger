package net.bandit.darkdoppelganger.entity;

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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DarkDoppelgangerEntity extends PathfinderMob {

    private UUID summonerUUID;
    private String summonerName;
    private final ServerBossEvent bossEvent;
    private boolean musicPlaying = false; // Flag to track if music is playing
    private boolean secondPhaseTriggered = false; // Flag to track if second phase is triggered
    private boolean thirdPhaseTriggered = false; // Flag for third phase
    private boolean isClone = false;  // Flag to check if the entity is a clone

    public DarkDoppelgangerEntity(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        this.setCustomName(Component.literal("Dark Doppelganger"));

        // Create the boss bar with the name of the entity and color
        this.bossEvent = new ServerBossEvent(Component.literal("Dark Doppelganger"), ServerBossEvent.BossBarColor.PURPLE, ServerBossEvent.BossBarOverlay.PROGRESS);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.setPersistenceRequired();

        if (!this.isClone) {
            // Summoning sequence with particles
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * 2, this.getY() + this.random.nextDouble(), this.getZ() + (this.random.nextDouble() - 0.5) * 2, 0, 0, 0);
            }

            // Play Ender Dragon boss music if not already playing
            if (!musicPlaying) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.BOSS_FIGHT_MUSIC.get(), SoundSource.MUSIC, 0.5F, 1.0F);
                musicPlaying = true; // Set the flag to true
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
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());  // Update boss bar progress

            // Check if the boss is below 10% health and the second phase hasn't triggered yet
            if (!secondPhaseTriggered && this.getHealth() / this.getMaxHealth() < 0.5) {
                triggerSecondPhase();
            }

            // Check if the boss is below 10% health and the third phase hasn't triggered yet
            if (!thirdPhaseTriggered && this.getHealth() / this.getMaxHealth() < 0.1) {
                triggerThirdPhase();
            }
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        musicPlaying = false; // Reset the flag to allow music to play again next time

        if (!this.isClone) {
            // Ensure this runs only on the server side
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

                // Stop the music
                this.level().getServer().getPlayerList().getPlayers().forEach(player -> {
                    player.connection.send(new ClientboundStopSoundPacket(ModSounds.BOSS_FIGHT_MUSIC.get().getLocation(), SoundSource.MUSIC));
                });

                // Remove the boss bar when the entity dies
                this.bossEvent.removeAllPlayers();
            }
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
        for (int i = 0; i < number; i++) {
            DarkDoppelgangerEntity clone = EntityRegistry.DARK_DOPPELGANGER.get().create(this.level());
            if (clone != null) {
                clone.setPos(this.getX() + random.nextInt(5) - 2, this.getY(), this.getZ() + random.nextInt(5) - 2);  // Spawn near the boss
                clone.setHealth((float) (clone.getMaxHealth() * 0.3));  // Clones have 30% of real boss's health
                clone.isClone = true;  // Mark this as a clone
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
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    // Create default attributes for Dark Doppelganger
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag dataTag) {
        return super.finalizeSpawn(world, difficulty, spawnType, groupData, dataTag);
    }

    public void setSummoner(Player player) {
        this.summonerUUID = player.getUUID();
        this.summonerName = player.getName().getString();
    }

    public UUID getSummonerUUID() {
        return this.summonerUUID;
    }

    public String getSummonerName() {
        return this.summonerName;
    }
}
