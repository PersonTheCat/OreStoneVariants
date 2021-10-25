package personthecat.osv.command;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hjson.JsonValue;
import personthecat.catlib.command.CommandContextWrapper;
import personthecat.catlib.command.annotations.ModCommand;
import personthecat.catlib.command.annotations.Node;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.osv.init.PresetLoadingContext;
import personthecat.osv.io.ModFolders;
import personthecat.osv.preset.data.OreSettings;

public class CommandOsv {

    @ModCommand(name = "debug", branch = @Node(name = "test"))
    private static void debugTest(final CommandContextWrapper ctx) {
        ctx.sendMessage("Hello, world!");
    }

    @ModCommand(
        name = "cleanup",
        arguments = "loot",
        description = {
            "Removes all of the unnecessary loot tables from your ore presets.",
            "This will allow the variants to pull directly from their background blocks."
        },
        branch = @Node(name = "loot")
    )
    private static void cleanupTables(final CommandContextWrapper ctx) {
        final MutableInt count = new MutableInt(0);
        FileIO.listFilesRecursive(ModFolders.ORE_DIR).forEach(preset -> {
            if (PresetLoadingContext.isPreset(preset)) {
                HjsonUtils.updateJson(preset, json -> {
                    final JsonValue loot = json.get(OreSettings.Fields.loot);
                    if (loot != null && loot.isString() && ResourceLocation.tryParse(loot.asString()) != null) {
                        json.remove(OreSettings.Fields.loot);
                        count.increment();
                    }
                }).unwrap();
            }
        });
        ctx.sendMessage("Updated {} presets", count.getValue());
    }
}
