package net.bandit.darkdoppelganger.entity.renderer;

import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.bandit.darkdoppelganger.entity.PortalJoinEntity;
import net.bandit.darkdoppelganger.entity.model.PortalJoinModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalJoinRenderer extends GeoEntityRenderer<PortalJoinEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DarkDoppelgangerMod.MOD_ID, "textures/entity/portal.png");

    public PortalJoinRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PortalJoinModel());
        this.shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(PortalJoinEntity animatable) {
        return TEXTURE;
    }
}
