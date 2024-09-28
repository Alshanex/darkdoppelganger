package net.bandit.darkdoppelganger.item;

import net.bandit.darkdoppelganger.DarkDoppelgangerMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DarkDoppelgangerMod.MOD_ID);

    // Register the SummonScrollItem
    public static final RegistryObject<Item> SUMMON_SCROLL = ITEMS.register("summon_scroll",
            () -> new SummonScrollItem(new Item.Properties().stacksTo(1)));
}
