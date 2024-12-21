package net.bandit.darkdoppelganger.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.spell.ClientboundTeleportParticles;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.util.Log;
import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.bandit.darkdoppelganger.entity.EntityRegistry;
import net.bandit.darkdoppelganger.entity.PortalJoinEntity;
import net.bandit.darkdoppelganger.entity.PortalLeaveEntity;
import net.bandit.darkdoppelganger.registry.ModAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class DoppelPortalSpell extends AbstractSpell {
    private Vec3 dest = null;

    private final ResourceLocation spellId = new ResourceLocation(DarkDoppelgangerMod.MOD_ID, "doppel_portal");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(5)
            .build();

    public DoppelPortalSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 3;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public int getCastTime(int spellLevel) {
        return castTime + 5 * spellLevel;
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ENDERMAN_TELEPORT);
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return ModAnimations.PLAYER_JOIN;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return ModAnimations.PLAYER_LEAVE;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var teleportData = (TeleportSpell.TeleportData) playerMagicData.getAdditionalCastData();

        this.dest = null;
        if (teleportData != null) {
            var potentialTarget = teleportData.getTeleportTargetPosition();
            if (potentialTarget != null) {
                this.dest = potentialTarget;
            }
        }

        if (this.dest == null) {
            this.dest = findTeleportLocation(level, entity, getDistance(spellLevel, entity));
        }

        PortalLeaveEntity portal = new PortalLeaveEntity(EntityRegistry.PORTAL_LEAVE_ENTITY.get(), level);
        portal.setPos(entity.position());
        portal.setXRot(entity.getXRot());
        portal.setYRot(entity.getYRot());
        level.addFreshEntity(portal);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData != null && (playerMagicData.getCastDurationRemaining() + 1) == 5) {
            Messages.sendToPlayersTrackingEntity(new ClientboundTeleportParticles(entity.position(), this.dest), entity, true);
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            entity.teleportTo(this.dest.x, this.dest.y, this.dest.z);
            entity.resetFallDistance();

            playerMagicData.resetAdditionalCastData();

            entity.playSound(getCastFinishSound().get(), 2.0f, 1.0f);

            PortalJoinEntity portal = new PortalJoinEntity(EntityRegistry.PORTAL_JOIN_ENTITY.get(), level);
            portal.setPos(this.dest);
            portal.setXRot(entity.getXRot());
            portal.setYRot(entity.getYRot());
            level.addFreshEntity(portal);
        }
    }

    public static Vec3 findTeleportLocation(Level level, LivingEntity entity, float maxDistance) {
        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, maxDistance);
        var pos = blockHitResult.getBlockPos();

        Vec3 bbOffset = entity.getForward().normalize().multiply(entity.getBbWidth() / 3, 0, entity.getBbHeight() / 3);
        Vec3 bbImpact = blockHitResult.getLocation().subtract(bbOffset);
        //        Vec3 lower = level.clip(new ClipContext(start, start.add(0, maxSteps * -2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation();
        int ledgeY = (int) level.clip(new ClipContext(Vec3.atBottomCenterOf(pos).add(0, 3, 0), Vec3.atBottomCenterOf(pos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation().y;
        boolean isAir = level.getBlockState(new BlockPos(new Vec3i(pos.getX(), ledgeY, pos.getZ()))).isAir();
        boolean los = level.clip(new ClipContext(bbImpact, bbImpact.add(0, ledgeY - pos.getY(), 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;

        if (isAir && los && Math.abs(ledgeY - pos.getY()) <= 3) {
            Vec3 correctedPos = new Vec3(pos.getX(), ledgeY, pos.getZ());
            return correctedPos.add(0.5, 0.076, 0.5);
        } else {
            return level.clip(new ClipContext(bbImpact, bbImpact.add(0, -entity.getBbHeight(), 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getLocation().add(0, 0.076, 0);
        }
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return (float) (Utils.softCapFormula(getEntityPowerMultiplier(sourceEntity)) * getSpellPower(spellLevel, null));
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getDistance(spellLevel, caster), 1)));
    }
}
