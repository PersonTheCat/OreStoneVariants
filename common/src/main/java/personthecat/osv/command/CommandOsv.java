package personthecat.osv.command;

import net.minecraft.client.Minecraft;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hjson.JsonValue;
import personthecat.catlib.command.CommandContextWrapper;
import personthecat.catlib.command.CommandSide;
import personthecat.catlib.command.annotations.ModCommand;
import personthecat.catlib.command.annotations.Node;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.ResourceArrayLinter;
import personthecat.osv.ModRegistries;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.init.PresetLoadingContext;
import personthecat.osv.io.ModFolders;
import personthecat.osv.io.ResourceHelper;
import personthecat.osv.preset.data.OreSettings;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class CommandOsv {

    private static final int BACKUP_COUNT_WARNING = 10;

    @ModCommand
    private void debugTest(final CommandContextWrapper ctx) {
        ctx.sendMessage("Hello, world!");
    }

    @ModCommand(
        description = "Displays a list of all current biome features.",
        linter = ResourceArrayLinter.class,
        branch = @Node(name = "feature", registry = Feature.class))
    private void debugFeatures(final CommandContextWrapper ctx) {
        final Feature<?> feature = ctx.get("feature", Feature.class);
        final Stream<ResourceLocation> features = BuiltinRegistries.CONFIGURED_FEATURE.entrySet().stream()
            .filter(e -> e.getValue().getFeatures().anyMatch(f -> feature.equals(f.feature)))
            .map(e -> e.getKey().location());
        ctx.sendLintedMessage(Arrays.toString(features.toArray()));
    }

    @ModCommand(
        description = {
            "Removes all of the unnecessary loot tables from your ore presets.",
            "This will allow the variants to pull directly from their background blocks."
        })
    private void removeLoot(final CommandContextWrapper ctx) {
        final MutableInt count = new MutableInt(0);
        FileIO.listFilesRecursive(ModFolders.ORE_DIR, PresetLoadingContext::isPreset).forEach(preset ->
            HjsonUtils.updateJson(preset, json -> {
                final JsonValue loot = json.get(OreSettings.Fields.loot);
                if (loot != null && loot.isString() && ResourceLocation.tryParse(loot.asString()) != null) {
                    json.remove(OreSettings.Fields.loot);
                    count.increment();
                }
            }).unwrap()
        );
        ctx.sendMessage("Updated {} presets", count.getValue());
    }

    @ModCommand(
        side = CommandSide.CLIENT,
        description = {
            "Backs up and regenerates the OSV resource pack.",
            "Resources will be refreshed immediately."
        })
    private void regenerate(final CommandContextWrapper ctx) {
        ctx.sendMessage("Creating backup of /resources");
        final File assets = ResourceHelper.file("assets");
        final int numBackups = FileIO.backup(ctx.getBackupsFolder(), assets);
        if (numBackups > BACKUP_COUNT_WARNING) {
            ctx.sendError("> {} backups detected. Consider cleaning your backups folder.", BACKUP_COUNT_WARNING);
        }
        ModRegistries.resetAll();
        FileIO.mkdirsOrThrow(assets);
        ModelHandler.generateOverlayModel();
        ModRegistries.ORE_PRESETS.forEach(TextureHandler::generateOverlays);
        ModRegistries.BLOCK_LIST.forEach(l -> l.forEach(ModelHandler::generateModels));
        Minecraft.getInstance().reloadResourcePacks();
        ctx.sendMessage("New resources generated successfully.");
    }
}
