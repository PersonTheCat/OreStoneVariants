package personthecat.osv.command;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hjson.CommentType;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.catlib.command.CommandContextWrapper;
import personthecat.catlib.command.CommandSide;
import personthecat.catlib.command.annotations.ModCommand;
import personthecat.catlib.command.annotations.Node;
import personthecat.catlib.command.annotations.Node.ListInfo;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.exception.UnreachableException;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.FeatureSupport;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.ResourceArrayLinter;
import personthecat.catlib.util.Shorthand;
import personthecat.osv.ModRegistries;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.command.argument.*;
import personthecat.osv.config.BlockEntry;
import personthecat.osv.config.BlockList;
import personthecat.osv.config.Cfg;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.exception.InvalidBlockEntryException;
import personthecat.osv.init.PresetLoadingContext;
import personthecat.osv.io.ModFolders;
import personthecat.osv.io.ResourceHelper;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.OreSettings;
import personthecat.osv.util.Group;

import java.io.File;
import java.util.*;

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
        branch = @Node(name = "group", type = PropertyArgument.class))
    private void getProperties(final CommandContextWrapper ctx, final Group group) {
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
        branch = @Node(name = "group", type = BackgroundArgument.class))
    private void getBlocks(final CommandContextWrapper ctx, final Group group) {
        final List<ResourceLocation> matching = new ArrayList<>();
        ModRegistries.BLOCK_LIST.forEach((entry, descriptors) -> {
            if (group.ids().contains(new ResourceLocation(entry.getBackground()))) {
                descriptors.forEach(descriptor -> matching.add(descriptor.getId()));
            }
        });
        ctx.sendLintedMessage(Arrays.toString(matching.toArray()));
    }

    @ModCommand(
        description = "Places any number of new variants on the block list.",
        branch = {
            @Node(name = "ore", type = PropertyArgument.class, intoList = @ListInfo),
            @Node(name = "in"),
            @Node(name = "bg", type = BackgroundArgument.class, intoList = @ListInfo)
        })
    private void put(final CommandContextWrapper ctx, final List<Group> ore, final List<Group> bg) {
        final Map<BlockEntry, List<VariantDescriptor>> entries = createEntries(ore, bg);
        if (Cfg.testForDuplicates() && checkDuplicates(ctx, entries)) {
            return;
        }
        final List<String> raw = new ArrayList<>();
        for (final Map.Entry<BlockEntry, ?> entry : entries.entrySet()) {
            raw.add(entry.getKey().getRaw());
        }
        updateEntries(raw);
        ctx.generateMessage("Updated block list:\n")
            .append(Arrays.toString(raw.toArray()))
            .append("\nRestart to see changes in game.")
            .sendMessage();
    }

    private static Map<BlockEntry, List<VariantDescriptor>> createEntries(final List<Group> ores, final List<Group> bgs) {
        final Map<BlockEntry, List<VariantDescriptor>> entries = new HashMap<>(ModRegistries.BLOCK_LIST);
        for (final Group ore : ores) {
            for (final Group bg : bgs) {
                try {
                    final BlockEntry entry = BlockEntry.create(ore.getName() + " " + bg.getName());
                    entries.put(entry, entry.resolve());
                } catch (final InvalidBlockEntryException ignored) {
                    throw new UnreachableException();
                }
            }
        }
        return entries;
    }

    private static boolean checkDuplicates(final CommandContextWrapper ctx, final Map<BlockEntry, List<VariantDescriptor>> entries) {
        final Map<VariantDescriptor, Set<BlockEntry>> duplicates = BlockList.getDuplicates(entries);
        if (!duplicates.isEmpty()) {
            ctx.sendError("Refusing to update block list. Found {} duplicates.", duplicates.size());
            for (final Map.Entry<VariantDescriptor, Set<BlockEntry>> duplicate : duplicates.entrySet()) {
                final Set<String> values = Shorthand.map(duplicate.getValue(), BlockEntry::getRaw);
                ctx.sendError("Found {} in {}", duplicate.getKey().getId(), Arrays.toString(values.toArray()));
            }
            return true;
        }
        return false;
    }

    private static void updateEntries(final List<String> entries) {
        Cfg.setBlockEntries(entries);
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final JsonObject registry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonValue oldValues = registry.get("values");
            final JsonArray values = new JsonArray();
            values.setFullComment(CommentType.BOL, oldValues.getBOLComment());
            entries.forEach(values::add);
            registry.set("values", values);
        }).expect("Could not update config file.");
        ModRegistries.BLOCK_LIST.reset();
    }

    @ModCommand(
        description = "Adds any number of properties into an existing or new group.",
        branch = {
            @Node(name = "entry", type = OrePresetArgument.class, intoList = @ListInfo),
            @Node(name = "in"),
            @Node(name = "group", type = PropertyGroupArgument.class)
        })
    private void groupProperties(final CommandContextWrapper ctx, final List<OrePreset> entry, final Group group) {
        final Set<String> output = new HashSet<>(group.getEntries());
        entry.forEach(preset -> output.add(preset.getName()));
        group(ctx, new Group(group.getName(), output), true);
    }

    @ModCommand(
        description = "Adds any number of blocks into an existing or new group.",
        branch = {
            @Node(name = "entry", type = BackgroundArgument.class, intoList = @ListInfo),
            @Node(name = "in"),
            @Node(name = "group", type = BlockGroupArgument.class)
        })
    private void groupBlocks(final CommandContextWrapper ctx, final List<BlockState> entry, final Group group) {
        final Set<String> output = new HashSet<>(group.getEntries());
        entry.forEach(state -> {
            final ResourceLocation id = Objects.requireNonNull(CommonRegistries.BLOCKS.getKey(state.getBlock()));
            output.add(id.toString());
        });
        group(ctx, new Group(group.getName(), output), false);
    }

    private static void group(final CommandContextWrapper ctx, final Group updated, final boolean ore) {
        updateGroup(updated, ore);
        ctx.generateMessage("Updated group list:\n")
            .append(Arrays.toString(updated.getEntries().toArray()))
            .append("\nRestart to see changes.")
            .sendMessage();
    }

    private static void updateGroup(final Group group, final boolean ore) {
        (ore ? Cfg.propertyGroups() : Cfg.blockGroups())
            .put(group.getName(), new ArrayList<>(group.getEntries()));
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final JsonObject registry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = registry.get(ore ? "propertyGroups" : "blockGroups").asObject();
            final JsonValue oldValues = groups.get(group.getName());
            final JsonArray newValues = new JsonArray();
            newValues.setFullComment(CommentType.BOL, oldValues.getBOLComment());
            group.getEntries().forEach(newValues::add);
            groups.set(group.getName(), newValues);
        }).expect("Could not update config file.");
    }
}
