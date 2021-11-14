package personthecat.osv.config;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import personthecat.osv.util.Group;

@UtilityClass
public class DefaultOres {

    public static final Group[] LISTED = {
        Group.named("byg").withEntries("pendorite_ore"),
        Group.named("create").withEntries("copper_ore", "zinc_ore"),
        Group.named("druidcraft").withEntries("amber_ore", "fiery_glass_ore", "moonstone_ore", "rockroot_ore"),
        Group.named("iceandfire").withEntries("amethyst_ore", "copper_ore", "sapphire_ore", "silver_ore"),
        Group.named("minecraft").withEntries("coal_ore", "diamond_ore", "emerald_ore", "gold_ore", "iron_ore", "lapis_ore", "redstone_ore"),
        Group.named("simpleores").withEntries("tin_ore", "mythril_ore", "copper_ore", "adamantium_ore")
    };

    public static final Group[] UNLISTED = {
        Group.named("byg").withEntries("ametrine_ore", "anthracite_ore", "budding_ametrine_ore", "cryptic_redstone_ore", "lignite_ore", "pervaded_netherrack"),
        Group.named("druidcraft").withEntries("brightstone_ore"),
        Group.named("minecraft").withEntries("gilded_blackstone_ore", "nether_gold_ore", "quartz_ore", "infested_stone"),
        Group.named("quark").withEntries("biotite_ore"),
        Group.named("simpleores").withEntries("onyx_ore")
    };

    public static final String[] NAMES = { "byg", "create", "druidcraft", "iceandfire", "minecraft", "quark", "simpleores" };
}
