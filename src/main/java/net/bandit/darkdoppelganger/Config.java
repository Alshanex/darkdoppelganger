package net.bandit.darkdoppelganger;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class Config {
    public static ForgeConfigSpec.DoubleValue DOPPELGANGER_HEALTH;
    public static ForgeConfigSpec.DoubleValue DOPPELGANGER_ATTACK_DAMAGE;
    public static ForgeConfigSpec.DoubleValue DOPPELGANGER_MOVEMENT_SPEED;

    public static ConfigValue<String> DOPPELGANGER_MAINHAND;
    public static ConfigValue<String> DOPPELGANGER_HELMET;
    public static ConfigValue<String> DOPPELGANGER_CHESTPLATE;
    public static ConfigValue<String> DOPPELGANGER_LEGGINGS;
    public static ConfigValue<String> DOPPELGANGER_BOOTS;

    public static boolean CONFIG_LOADED = false;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // Define entity attributes
        DOPPELGANGER_HEALTH = builder
                .comment("Health of the Dark Doppelganger")
                .defineInRange("doppelganger_health", 300.0, 1.0, 1000.0);

        DOPPELGANGER_ATTACK_DAMAGE = builder
                .comment("Attack Damage of the Dark Doppelganger")
                .defineInRange("doppelganger_attack_damage", 15.0, 1.0, 50.0);

        DOPPELGANGER_MOVEMENT_SPEED = builder
                .comment("Movement Speed of the Dark Doppelganger")
                .defineInRange("doppelganger_movement_speed", 0.35, 0.1, 1.0);

        // Define equipment through config
        DOPPELGANGER_MAINHAND = builder
                .comment("Mainhand weapon of the Dark Doppelganger (e.g., 'netherite_sword')")
                .define("doppelganger_mainhand", "netherite_sword");

        DOPPELGANGER_HELMET = builder
                .comment("Helmet of the Dark Doppelganger (e.g., 'netherite_helmet')")
                .define("doppelganger_helmet", "netherite_helmet");

        DOPPELGANGER_CHESTPLATE = builder
                .comment("Chestplate of the Dark Doppelganger (e.g., 'netherite_chestplate')")
                .define("doppelganger_chestplate", "netherite_chestplate");

        DOPPELGANGER_LEGGINGS = builder
                .comment("Leggings of the Dark Doppelganger (e.g., 'netherite_leggings')")
                .define("doppelganger_leggings", "netherite_leggings");

        DOPPELGANGER_BOOTS = builder
                .comment("Boots of the Dark Doppelganger (e.g., 'netherite_boots')")
                .define("doppelganger_boots", "netherite_boots");

        SPEC = builder.build();
    }

    // Helper method to get the item from config
    public static Item getItemFromConfig(ConfigValue<String> configValue) {
        switch (configValue.get()) {
            case "netherite_sword": return Items.NETHERITE_SWORD;
            case "netherite_helmet": return Items.NETHERITE_HELMET;
            case "netherite_chestplate": return Items.NETHERITE_CHESTPLATE;
            case "netherite_leggings": return Items.NETHERITE_LEGGINGS;
            case "netherite_boots": return Items.NETHERITE_BOOTS;
            // Add more items if needed
            default: return Items.AIR; // Default to empty
        }
    }
}
