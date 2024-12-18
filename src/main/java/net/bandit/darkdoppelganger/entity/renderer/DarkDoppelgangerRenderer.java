package net.bandit.darkdoppelganger.entity.renderer;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.bandit.darkdoppelganger.entity.model.DarkDopplegangerEntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DarkDoppelgangerRenderer extends AbstractSpellCastingMobRenderer {

    public DarkDoppelgangerRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkDopplegangerEntityModel());

    }
}
