package personthecat.osv.command;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hjson.JsonValue;
import personthecat.catlib.command.CommandContextWrapper;
import personthecat.catlib.command.CommandSide;
import personthecat.catlib.command.annotations.ModCommand;
import personthecat.catlib.command.annotations.Node;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.FeatureSupport;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.ResourceArrayLinter;
import personthecat.osv.ModRegistries;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.command.supplier.BackgroundSupplier;
import personthecat.osv.command.supplier.OrePresetSupplier;
import personthecat.osv.init.PresetLoadingContext;
import personthecat.osv.io.ModFolders;
import personthecat.osv.io.ResourceHelper;
import personthecat.osv.preset.data.OreSettings;
import personthecat.osv.util.Group;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandOsv {

    private static final int BACKUP_COUNT_WARNING = 10;

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
            "Backs up and regenerates the OSV resource pack. Resources will be ",
            "refreshed immediately."
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
        Minecraft.getInstance().reloadResourcePacks()
            .thenRun(() -> ctx.sendMessage("New resources generated successfully."));
    }

    @ModCommand(
        side = CommandSide.CLIENT,
        description = {
            "Reloads ore and stone presets. Only affects world generation and model",
            "assets. Requires world restart"
        })
    private void reload(final CommandContextWrapper ctx) {
        ModRegistries.resetAll();
        Minecraft.getInstance().reloadResourcePacks()
            .thenRun(() -> ctx.sendMessage("OSV resources reloaded successfully."));
    }

    @ModCommand
    private void debugTest(final CommandContextWrapper ctx) {
        ctx.sendMessage("Hello, world!");
    }

    @ModCommand(
        description = "Displays a list of all current biome features.",
        linter = ResourceArrayLinter.class,
        branch = @Node(name = "feature", registry = Feature.class))
    private void debugFeatures(final CommandContextWrapper ctx, final Feature<?> feature) {
        ctx.sendLintedMessage(Arrays.toString(FeatureSupport.getIds(feature).toArray()));
    }

    @ModCommand(
        description = "Displays all resource locations for variants matching the given preset.",
        linter = ResourceArrayLinter.class,
        branch = @Node(name = "name", descriptor = OrePresetSupplier.class))
    private void debugProperties(final CommandContextWrapper ctx, final String name) {
        final Group group = ModRegistries.PROPERTY_GROUPS.getOptional(name)
            .orElseGet(() -> Group.named(name).withEntries(name));

        final List<ResourceLocation> matching = new ArrayList<>();
        ModRegistries.BLOCK_LIST.forEach((entry, descriptors) -> {
            if (group.getEntries().contains(entry.getForeground())) {
                descriptors.forEach(descriptor -> matching.add(descriptor.getId()));
            }
        });
        ctx.sendLintedMessage(Arrays.toString(matching.toArray()));
    }

    @ModCommand(
        description = "Displays all resource locations for variants matching the given background.",
        linter = ResourceArrayLinter.class,
        branch = @Node(name = "name", descriptor = BackgroundSupplier.class))
    private void debugBlocks(final CommandContextWrapper ctx, final String name) {
        final Group group = ModRegistries.BLOCK_GROUPS.getOptional(name)
            .orElseGet(() -> Group.named(name).withEntries(name));

        final List<ResourceLocation> matching = new ArrayList<>();
        ModRegistries.BLOCK_LIST.forEach((entry, descriptors) -> {
            if (group.ids().contains(new ResourceLocation(entry.getBackground()))) {
                descriptors.forEach(descriptor -> matching.add(descriptor.getId()));
            }
        });
        ctx.sendLintedMessage(Arrays.toString(matching.toArray()));
    }
}
