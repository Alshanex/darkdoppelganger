package net.bandit.darkdoppelganger.client.renderer;


import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;


public class DarkDoppelgangerRenderer extends LivingEntityRenderer<DarkDoppelgangerEntity, PlayerModel<DarkDoppelgangerEntity>> {

    // Path to the custom skin file
    private static final ResourceLocation CUSTOM_SKIN = new ResourceLocation("darkdoppelganger", "textures/entity/dark_doppelganger.png");


    public DarkDoppelgangerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);

        // Add armor layers to render the equipped armor
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));

        // Add the item in hand layer to render held weapons and shields
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
    @Override
    public ResourceLocation getTextureLocation(DarkDoppelgangerEntity entity) {
        return CUSTOM_SKIN;
    }
}
