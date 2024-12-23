package net.bandit.darkdoppelganger.entity.model;

import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.bandit.darkdoppelganger.entity.PortalJoinEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PortalJoinModel extends GeoModel<PortalJoinEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DarkDoppelgangerMod.MOD_ID, "textures/entity/portal.png");
    private static final ResourceLocation MODEL = new ResourceLocation(DarkDoppelgangerMod.MOD_ID, "geo/portal.geo.json");
    public static final ResourceLocation ANIMS = new ResourceLocation(DarkDoppelgangerMod.MOD_ID, "animations/model_portal_join.animation.json");


    public PortalJoinModel() {
    }

    @Override
    public ResourceLocation getTextureResource(PortalJoinEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(PortalJoinEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(PortalJoinEntity animatable) {
        return ANIMS;
    }
}
