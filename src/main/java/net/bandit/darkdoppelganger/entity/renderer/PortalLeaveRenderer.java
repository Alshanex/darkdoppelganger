package net.bandit.darkdoppelganger.entity.renderer;

import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.bandit.darkdoppelganger.entity.PortalJoinEntity;
import net.bandit.darkdoppelganger.entity.PortalLeaveEntity;
import net.bandit.darkdoppelganger.entity.model.PortalLeaveModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PortalLeaveRenderer extends GeoEntityRenderer<PortalLeaveEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DarkDoppelgangerMod.MOD_ID, "textures/entity/portal.png");

    public PortalLeaveRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PortalLeaveModel());
        this.shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(PortalLeaveEntity animatable) {
        return TEXTURE;
    }
}
