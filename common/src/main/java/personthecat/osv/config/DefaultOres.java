package personthecat.osv.config;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import personthecat.osv.util.Group;

@UtilityClass
public class DefaultOres {

    public static final Group[] LISTED = {
        Group.named("create").withEntries("zinc_ore").implicitNamespace(),
        Group.named("minecraft").withEntries("coal_ore", "copper_ore", "diamond_ore", "emerald_ore", "gold_ore", "iron_ore", "lapis_ore", "redstone_ore")
    };

    public static final Group[] UNLISTED = {
        Group.named("minecraft").withEntries("gilded_blackstone_ore", "nether_gold_ore", "quartz_ore", "infested_stone")
    };

    public static final String[] NAMES = { "minecraft", "create" };

    public static boolean canIgnoreEntry(final String mod) {
        return ArrayUtils.contains(NAMES, mod) && !Cfg.modEnabled(mod);
    }
}
