package net.bandit.darkdoppelganger.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.bandit.darkdoppelganger.entity.DarkDoppelgangerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;

public class DarkDoppelgangerRenderer extends LivingEntityRenderer<DarkDoppelgangerEntity, PlayerModel<DarkDoppelgangerEntity>> {

    private static final MinecraftSessionService sessionService = Minecraft.getInstance().getMinecraftSessionService();

    public DarkDoppelgangerRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DarkDoppelgangerEntity entity) {
        UUID summonerUUID = entity.getSummonerUUID();
        String summonerName = entity.getSummonerName();

        // Check if summonerUUID and summonerName are valid
        if (summonerUUID != null && summonerName != null) {
            GameProfile profile = new GameProfile(summonerUUID, summonerName);
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = sessionService.getTextures(profile, false);

            // If a custom skin is available, use it
            if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                return Minecraft.getInstance().getSkinManager().registerTexture(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            }
        }

        // Fallback to the default skin if summonerUUID is null or no custom skin is found
        return DefaultPlayerSkin.getDefaultSkin(entity.getUUID() != null ? entity.getUUID() : UUID.randomUUID());
    }
}
