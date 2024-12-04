package net.bandit.darkdoppelganger.entity;

import net.bandit.darkdoppelganger.Config;
import net.bandit.darkdoppelganger.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.Blocks;



public class DarkDoppelgangerEntity extends PathfinderMob {

    private Player summonerPlayer;
    private final ServerBossEvent bossEvent;
    private boolean secondPhaseTriggered = false;
    private boolean thirdPhaseTriggered = false;
    private boolean isClone = false;
    private boolean musicPlaying = false;
    private int teleportCooldown = 80;
    private int shockwaveCooldown = 200;
    private int minionSummonCooldown = 300;
    private int lifeDrainCooldown = 150;
    private int roarSoundCooldown = 800;
    private int laughSoundCooldown = 800;
    private int meteorShowerCooldown = 400;
    private int levitationCooldown = 500;
    private int gravityPullCooldown = 300;
    private static final int MAX_MINIONS = 5;
    private static int currentMinionCount = 0;



    public DarkDoppelgangerEntity(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        this.setCustomName(Component.literal("Dark Doppelganger"));
        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        this.bossEvent = new ServerBossEvent(Component.literal("Dark Doppelganger"), ServerBossEvent.BossBarColor.PURPLE, ServerBossEvent.BossBarOverlay.PROGRESS);
    }
    public void setSummonerPlayer(Player summoner) {
        this.summonerPlayer = summoner;

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

        if (!this.level().isClientSide) {
            if (!this.isClone) {
                stopAllMusic();

                if (!musicPlaying) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            ModSounds.BOSS_FIGHT_MUSIC.get(), SoundSource.MUSIC, 0.5F, 1.0F);
                    musicPlaying = true;
                }
                adjustAttributesFromConfig();
            }
        } else {
            spawnSummoningParticles();
        }
    }
    private void adjustAttributesFromConfig() {
        if (Config.DOPPELGANGER_HEALTH != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Config.DOPPELGANGER_HEALTH.get());
        }
        if (Config.DOPPELGANGER_ATTACK_DAMAGE != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Config.DOPPELGANGER_ATTACK_DAMAGE.get());
        }
        if (Config.DOPPELGANGER_MOVEMENT_SPEED != null) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Config.DOPPELGANGER_MOVEMENT_SPEED.get());
        }
        if (Config.DOPPELGANGER_KNOCKBACK_RESISTANCE != null) {
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(Config.DOPPELGANGER_KNOCKBACK_RESISTANCE.get());
        }
        if (Config.DOPPELGANGER_ARMOR != null) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(Config.DOPPELGANGER_ARMOR.get());
        }
        if (Config.DOPPELGANGER_FOLLOW_RANGE != null) {
            this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(Config.DOPPELGANGER_FOLLOW_RANGE.get());
        }

        // Sync health attribute with current health
        this.setHealth(this.getMaxHealth());
    }
    private void spawnSummoningParticles() {
        for (int i = 0; i < 20; i++) {
            double xOffset = (this.random.nextDouble() - 0.5) * 2;
            double yOffset = this.random.nextDouble();
            double zOffset = (this.random.nextDouble() - 0.5) * 2;

            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset,
                    0, 0, 0);
        }
    }

    private void stopAllMusic() {
        if (!level().isClientSide && level().getServer() != null) {
            level().getServer().getPlayerList().getPlayers().forEach(player -> {
                player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
            });
        }
    }

    private void stopMinecraftAmbientMusic() {
        if (!level().isClientSide && level().getServer() != null) {
            // Loop through all players on the server
            for (ServerPlayer player : level().getServer().getPlayerList().getPlayers()) {
                // Stop specific Minecraft ambient music tracks
                player.connection.send(new ClientboundStopSoundPacket(new ResourceLocation("minecraft:music.game"), SoundSource.MUSIC));
                player.connection.send(new ClientboundStopSoundPacket(new ResourceLocation("minecraft:music.creative"), SoundSource.MUSIC));
                player.connection.send(new ClientboundStopSoundPacket(new ResourceLocation("minecraft:music.menu"), SoundSource.MUSIC));
                player.connection.send(new ClientboundStopSoundPacket(new ResourceLocation("minecraft:music.overworld.day"), SoundSource.MUSIC));
                player.connection.send(new ClientboundStopSoundPacket(new ResourceLocation("minecraft:music.overworld.night"), SoundSource.MUSIC));
                player.connection.send(new ClientboundStopSoundPacket(new ResourceLocation("minecraft:music.overworld.hills"), SoundSource.MUSIC));
                player.connection.send(new ClientboundStopSoundPacket(new ResourceLocation("minecraft:music.overworld.water"), SoundSource.MUSIC));
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.isClone) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (!this.isClone) {
            this.bossEvent.removePlayer(player);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isClone) return;

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (musicPlaying) {
            stopMinecraftAmbientMusic();
        }

        // Phase triggers
        if (!secondPhaseTriggered && this.getHealth() < this.getMaxHealth() * 0.2) {
            triggerSecondPhase();
        }
        if (!thirdPhaseTriggered && this.getHealth() < this.getMaxHealth() * 0.2) {
            triggerThirdPhase();
        }

        // Abilities
        if (teleportCooldown-- <= 0) {
            shadowTeleport();
            teleportCooldown = 100;
        }

        if (shockwaveCooldown-- <= 0) {
            shockwaveAttack();
            shockwaveCooldown = 200;
        }

        if (thirdPhaseTriggered) {
            if (minionSummonCooldown-- <= 0) {
                summonMinions();
                minionSummonCooldown = 300;
            }
            if (lifeDrainCooldown-- <= 0) {
                lifeDrainAttack();
                lifeDrainCooldown = 150;
            }
            if (levitationCooldown-- <= 0) {
                levitationAttack();
                levitationCooldown = 500;
            }
            if (meteorShowerCooldown-- <= 0) {
                meteorShowerAttack();
                meteorShowerCooldown = 400;
            }
            if (gravityPullCooldown-- <= 0) {
                gravitationalPull();
                gravityPullCooldown = 300;
            }
        }
        // Handle sound cooldowns
        if (roarSoundCooldown > 0) roarSoundCooldown--;
        if (laughSoundCooldown > 0) laughSoundCooldown--;
    }

    private void triggerSecondPhase() {
        secondPhaseTriggered = true;
        setHealth(getMaxHealth());

        for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(50))) {
            player.sendSystemMessage(Component.literal("The Dark Doppelganger has entered its Second Phase!").withStyle(ChatFormatting.DARK_PURPLE));
            player.playSound(ModSounds.BOSS_ROAR.get(), 1.0F, 1.0F);
        }
    }

    private void triggerThirdPhase() {
        thirdPhaseTriggered = true;
        setHealth(getMaxHealth());
        bossEvent.setName(Component.literal("Dark Doppelganger - Final Phase"));

        for (ServerPlayer player : level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(50))) {
            player.playSound(ModSounds.BOSS_ROAR.get(), 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("Final Form! Prepare yourself!").withStyle(ChatFormatting.RED), true);
        }

        for (Player player : level().players()) {
            player.getCooldowns().addCooldown(Items.TOTEM_OF_UNDYING, 60);
        }

        teleportCooldown = 60;
        shockwaveCooldown = 100;
        minionSummonCooldown = 150;
        lifeDrainCooldown = 200;
        meteorShowerCooldown = 300;
    }

    private void meteorShowerAttack() {
        // Enhanced meteor shower with explosions, lightning, and fire
        for (int i = 0; i < 6; i++) {
            double xOffset = random.nextDouble() * 30 - 15;
            double zOffset = random.nextDouble() * 30 - 15;
            double targetX = getX() + xOffset;
            double targetZ = getZ() + zOffset;
            double targetY = level().getHeight() - 1;

            // Create a large explosion
            level().explode(null, targetX, targetY, targetZ, 4.0F, Level.ExplosionInteraction.BLOCK);
            level().playSound(null, targetX, targetY, targetZ, ModSounds.BOSS_ROAR.get(), SoundSource.HOSTILE, 1.0F, 0.8F);

            // Summon a lightning bolt
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level());
            if (lightning != null) {
                lightning.moveTo(targetX, targetY, targetZ);
                level().addFreshEntity(lightning);
            }

            // Set blocks on fire
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (level().isEmptyBlock(new BlockPos((int) (targetX + x), (int) targetY, (int) (targetZ + z)))) {
                        level().setBlock(new BlockPos((int) (targetX + x), (int) targetY, (int) (targetZ + z)), Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }

            // Add particles
            level().addParticle(ParticleTypes.EXPLOSION, targetX, targetY, targetZ, 0, 0, 0);
            level().addParticle(ParticleTypes.SMOKE, targetX, targetY, targetZ, 0, 0.1, 0);
        }
        meteorShowerCooldown = 600; // Reset cooldown
    }

    private void levitationAttack() {
        if (getTarget() != null && !level().isClientSide) {
            // Boss levitates into the air
            teleportTo(getX(), getY() + 10, getZ());
            level().playSound(null, getX(), getY(), getZ(), ModSounds.BOSS_ROAR.get(), SoundSource.HOSTILE, 1.5F, 1.0F);

            // Apply levitation effect to nearby players
            level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(15)).forEach(player -> {
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 1));
                player.hurt(level().damageSources().magic(), 5.0F);
            });

            // Shoot fireballs at players
            for (int i = 0; i < 3; i++) {
                if (getTarget() != null) {
                    LargeFireball fireball = new LargeFireball(level(), this, getTarget().getX() - getX(), getTarget().getY() - getY(), getTarget().getZ() - getZ(), 2);
                    fireball.setPos(getX(), getY() + 2, getZ());
                    level().addFreshEntity(fireball);
                }
            }
            levitationCooldown = 800; // Reset cooldown
        }
    }

    private void gravitationalPull() {
        // Enhanced gravitational pull with stronger force and particles
        level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(20)).forEach(player -> {
            double dx = getX() - player.getX();
            double dz = getZ() - player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double pullStrength = 1.2 / (distance + 0.1); // Stronger pull
            player.setDeltaMovement(dx * pullStrength, 0.5, dz * pullStrength);
            player.hurt(level().damageSources().magic(), 8.0F);

            // Add swirling particles
            level().addParticle(ParticleTypes.PORTAL, player.getX(), player.getY(), player.getZ(), 0, 1, 0);
        });

        level().playSound(null, getX(), getY(), getZ(), ModSounds.BOSS_ROAR.get(), SoundSource.HOSTILE, 1.0F, 0.5F);
        gravityPullCooldown = 400; // Reset cooldown
    }


    private void shadowTeleport() {
        if (getTarget() != null && !level().isClientSide) {
            teleportTo(getTarget().getX(), getTarget().getY(), getTarget().getZ());
            // Play laugh sound only if cooldown is over
            if (laughSoundCooldown <= 0) {
                level().playSound(null, getX(), getY(), getZ(), ModSounds.BOSS_LAUGH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
                laughSoundCooldown = 300; // Reset cooldown
            }
        }
    }

    private void shockwaveAttack() {
        level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(12)).forEach(player -> {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            player.knockback(2.0F, dx, dz);
            player.hurt(level().damageSources().mobAttack(this), 6.0F);
        });
        // Play roar sound only if cooldown is over
        if (roarSoundCooldown <= 0) {
            level().playSound(null, getX(), getY(), getZ(), ModSounds.BOSS_ROAR.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            roarSoundCooldown = 400; // Reset cooldown
        }
    }

    private void summonMinions() {
        // Check if this is a clone or if the minion count has reached the limit
        if (isClone || currentMinionCount >= MAX_MINIONS) return;

        for (int i = 0; i < 2; i++) {
            if (currentMinionCount >= MAX_MINIONS) break; // Stop if max minions reached

            DarkDoppelgangerEntity minion = EntityRegistry.DARK_DOPPELGANGER.get().create(level());
            if (minion != null) {
                minion.setPos(getX() + random.nextInt(5) - 2, getY(), getZ() + random.nextInt(5) - 2);
                minion.setHealth(minion.getMaxHealth() * 0.3F);
                minion.isClone = true;
                level().addFreshEntity(minion);
                currentMinionCount++;
            }
        }
    }

    private void lifeDrainAttack() {
        level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(8)).forEach(player -> {
            player.hurt(level().damageSources().magic(), 4.0F);
            heal(4.0F);
        });
        level().playSound(null, getX(), getY(), getZ(), ModSounds.BOSS_LAUGH.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.6D, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1.2D, 64.0F));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.8F));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.5D));
    }
    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (isClone) {
            synchronized (DarkDoppelgangerEntity.class) {
                currentMinionCount = Math.max(0, currentMinionCount - 1);
            }
            return;
        }
        musicPlaying = false;

        if (!this.level().isClientSide) {
            if (cause.getEntity() instanceof ServerPlayer serverPlayer) {
                Advancement advancement = serverPlayer.getServer().getAdvancements()
                        .getAdvancement(new ResourceLocation("darkdoppelganger", "kill_dark_doppelganger"));

                if (advancement != null) {
                    serverPlayer.getAdvancements().award(advancement, "kill");
                    serverPlayer.sendSystemMessage(Component.literal("You have slain the Dark Doppelganger!"));
                }

                this.spawnAtLocation(Items.NETHER_STAR);
                this.spawnAtLocation(Items.DIAMOND_BLOCK, 3);
                this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY(), this.getZ(), 1500));
            }

            // Stop the music for the real boss only
            this.level().getServer().getPlayerList().getPlayers().forEach(player -> {
                player.connection.send(new ClientboundStopSoundPacket(ModSounds.BOSS_FIGHT_MUSIC.get().getLocation(), SoundSource.MUSIC));
            });
            this.bossEvent.removeAllPlayers();
        }
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6000.0) // Default value
                .add(Attributes.ATTACK_DAMAGE, 20.0) // Default value
                .add(Attributes.MOVEMENT_SPEED, 0.36) // Default value
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6) // Default value
                .add(Attributes.ARMOR, 20.0) // Default value
                .add(Attributes.FOLLOW_RANGE, 64.0); // Default value
    }

}
