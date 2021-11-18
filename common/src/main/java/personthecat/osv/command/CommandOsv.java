package personthecat.osv.command;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.catlib.command.CommandContextWrapper;
import personthecat.catlib.command.CommandSide;
import personthecat.catlib.command.annotations.ModCommand;
import personthecat.catlib.command.annotations.Node;
import personthecat.catlib.command.annotations.Node.ListInfo;
import personthecat.catlib.data.JsonPath;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.exception.UnreachableException;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.*;
import personthecat.osv.ModRegistries;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.command.argument.*;
import personthecat.osv.config.*;
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
        final int numBackups = FileIO.backup(ctx.getBackupsFolder(), assets, false);
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
        ModRegistries.VARIANTS.forEach((id, variant) -> {
            if (group.getEntries().contains(variant.getPreset().getName())) {
                matching.add(id);
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
        ModRegistries.VARIANTS.forEach((id, variant) -> {
            if (group.ids().contains(CommonRegistries.BLOCKS.getKey(variant.getBg()))) {
                matching.add(id);
            }
        });
        ctx.sendLintedMessage(Arrays.toString(matching.toArray()));
    }

    @ModCommand(
        description = "Places any number of new variants on the block list.",
        linter = GenericArrayLinter.class,
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
            .append(ctx.lintMessage(Arrays.toString(raw.toArray())))
            .append("\nRestart to see changes in game.")
            .sendMessage();
    }

    @ModCommand(
        description = "Adds any number of properties into an existing or new group.",
        linter = GenericArrayLinter.class,
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
        linter = GenericArrayLinter.class,
        branch = {
            @Node(name = "entry", type = BlockStateArgument.class, intoList = @ListInfo),
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

    @ModCommand(
        description = "Displays the current contents of the block list.",
        linter = GenericArrayLinter.class)
    private void listValues(final CommandContextWrapper ctx) {
        ctx.sendLintedMessage(Arrays.toString(Cfg.blockEntries().toArray()));
    }

    @ModCommand(
        description = "Displays the current contents of a property group.",
        linter = GenericArrayLinter.class,
        branch = @Node(name = "group", type = PropertyGroupArgument.class))
    private void listProperties(final CommandContextWrapper ctx, final Group group) {
        ctx.sendLintedMessage(Arrays.toString(group.getEntries().toArray()));
    }

    @ModCommand(
        description = "Displays the current contents of a block group.",
        linter = GenericArrayLinter.class,
        branch = @Node(name = "group", type = BlockGroupArgument.class))
    private void listBlocks(final CommandContextWrapper ctx, final Group group) {
        ctx.sendLintedMessage(Arrays.toString(group.getEntries().toArray()));
    }

    @ModCommand(description = "Clears all entries from the block list.")
    private void clearValues(final CommandContextWrapper ctx) {
        updateEntries(Collections.emptyList());
        ctx.sendMessage("Successfully cleared values. Restart to see changes.");
    }

    @ModCommand(
        description = "Clears all entries from the given property group.",
        branch = @Node(name = "group", type = PropertyGroupArgument.class))
    private void clearProperties(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            clearAllGroups(true);
        } else if (group.getName().equals(Group.DEFAULT)) {
            clearDefaultGroups(true);
        } else {
            updateGroup(new Group(group.getName(), Collections.emptySet()), true);
        }
        ctx.sendMessage("Successfully cleared values. Restart to see changes.");
    }

    @ModCommand(
        description = "Clears all entries from the given block group.",
        branch = @Node(name = "group", type = BlockGroupArgument.class))
    private void clearBlocks(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            clearAllGroups(false);
        } else if (group.getName().equals(Group.DEFAULT)) {
            clearDefaultGroups(false);
        } else {
            updateGroup(new Group(group.getName(), Collections.emptySet()), false);
        }
        ctx.sendMessage("Successfully cleared values. Restart to see changes.");
    }

    @ModCommand(
        description = "Deletes all entries from the given property group",
        branch = @Node(name = "group", type = PropertyGroupArgument.class))
    private void removeProperties(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            deleteAllGroups(true);
        } else if (group.getName().equals(Group.DEFAULT)) {
            deleteDefaultGroups(true);
        } else {
            deleteGroup(group, true);
        }
        ctx.sendMessage("Successfully deleted values. Restart to see changes.");
    }

    @ModCommand(
        description = "Deletes all entries from the given block group",
        branch = @Node(name = "group", type = BlockGroupArgument.class))
    private void removeBlocks(final CommandContextWrapper ctx, final Group group) {
        if (group.getName().equals(Group.ALL)) {
            deleteAllGroups(false);
        } else if (group.getName().equals(Group.DEFAULT)) {
            deleteDefaultGroups(false);
        } else {
            deleteGroup(group,  false);
        }
        ctx.sendMessage("Successfully deleted values. Restart to see changes.");
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
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly("blockRegistry.values").setValue(json, HjsonUtils.stringArray(entries))
        ).expect("Could not update config file.");
        ModRegistries.BLOCK_LIST.reset();
    }

    private static void group(final CommandContextWrapper ctx, final Group updated, final boolean ore) {
        updateGroup(updated, ore);
        ctx.generateMessage("Updated group list:\n")
            .append(ctx.lintMessage(Arrays.toString(updated.getEntries().toArray())))
            .append("\nRestart to see changes.")
            .sendMessage();
    }

    private static void updateGroup(final Group group, final boolean ore) {
        if (ore) {
            Cfg.propertyGroups().put(group.getName(), new ArrayList<>(group.getEntries()));
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            Cfg.blockGroups().put(group.getName(), new ArrayList<>(group.getEntries()));
            ModRegistries.BLOCK_GROUPS.reset();
        }
        final String path = "blockRegistry." + (ore ? "propertyGroups." : "blockGroups.") + group.getName();
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly(path).setValue(json, HjsonUtils.stringArray(group.getEntries()))
        ).expect("Could not update config file.");
    }

    private static void clearAllGroups(final boolean ore) {
        if (ore) {
            for (final Map.Entry<String, List<String>> group : Cfg.propertyGroups().entrySet()) {
                group.setValue(Collections.emptyList());
            }
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            for (final Map.Entry<String, List<String>> group : Cfg.blockGroups().entrySet()) {
                group.setValue(Collections.emptyList());
            }
            ModRegistries.BLOCK_GROUPS.reset();
        }
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final String path = ore ? "propertyGroups" : "blockGroups";
            final JsonObject blockRegistry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = HjsonUtils.getObjectOrNew(blockRegistry, path);
            for (final String name : groups.names()) {
                groups.set(name, new JsonArray());
            }
        }).expect("Could not update config file.");
    }

    private static void clearDefaultGroups(final boolean ore) {
        if (ore) {
            for (final String group : DefaultOres.NAMES) {
                Cfg.propertyGroups().put(group, Collections.emptyList());
            }
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            for (final String group : DefaultStones.NAMES) {
                Cfg.blockGroups().put(group, Collections.emptyList());
            }
            ModRegistries.BLOCK_GROUPS.reset();
        }
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final String path = ore ? "propertyGroups" : "blockGroups";
            final String[] names = ore ? DefaultOres.NAMES : DefaultStones.NAMES;
            final JsonObject blockRegistry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = HjsonUtils.getObjectOrNew(blockRegistry, path);
            for (final String name : names) {
                groups.set(name, new JsonArray());
            }
        }).expect("Could not update config file.");
    }

    private static void deleteGroup(final Group group, final boolean ore) {
        if (ore) {
            Cfg.propertyGroups().remove(group.getName());
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            Cfg.blockGroups().remove(group.getName());
            ModRegistries.BLOCK_GROUPS.reset();
        }
        final String path = "blockRegistry." + (ore ? "propertyGroups." : "blockGroups.") + group.getName();
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly(path).setValue(json, null)
        ).expect("Could not update config file.");
    }

    private static void deleteAllGroups(final boolean ore) {
        if (ore) {
            Cfg.propertyGroups().clear();
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            Cfg.blockGroups().clear();
            ModRegistries.BLOCK_GROUPS.reset();
        }
        final String path = "blockRegistry." + (ore ? "propertyGroups" : "blockGroups");
        HjsonUtils.updateJson(Cfg.getCommon(), json ->
            JsonPath.objectOnly(path).setValue(json, new JsonObject())
        ).expect("Could not update config file.");
    }

    private static void deleteDefaultGroups(final boolean ore) {
        if (ore) {
            for (final String group : DefaultOres.NAMES) {
                Cfg.propertyGroups().remove(group);
            }
            ModRegistries.PROPERTY_GROUPS.reset();
        } else {
            for (final String group : DefaultStones.NAMES) {
                Cfg.blockGroups().remove(group);
            }
            ModRegistries.BLOCK_GROUPS.reset();
        }
        HjsonUtils.updateJson(Cfg.getCommon(), json -> {
            final String path = ore ? "propertyGroups" : "blockGroups";
            final String[] names = ore ? DefaultOres.NAMES : DefaultStones.NAMES;
            final JsonObject blockRegistry = HjsonUtils.getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = HjsonUtils.getObjectOrNew(blockRegistry, path);
            for (final String name : names) {
                groups.remove(name);
            }
        }).expect("Could not update config file.");
    }
}
