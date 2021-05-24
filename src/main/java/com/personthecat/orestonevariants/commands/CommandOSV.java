package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGenerator;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.fresult.Result;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.CommonMethods.safeGet;
import static com.personthecat.orestonevariants.util.HjsonTools.FORMATTER;
import static com.personthecat.orestonevariants.util.HjsonTools.getObjectOrNew;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;
import static com.personthecat.orestonevariants.util.HjsonTools.getValueFromPath;
import static com.personthecat.orestonevariants.util.HjsonTools.setValueFromPath;
import static com.personthecat.orestonevariants.util.HjsonTools.updateJson;
import static com.personthecat.orestonevariants.util.HjsonTools.writeJson;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.ALL_VALID_BLOCKS;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.ALL_VALID_PROPERTIES;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.ANY_VALUE;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.BLOCK_GROUPS;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.MOD_NAMES;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.OPTIONAL_NAME;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.PROPERTY_GROUPS;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.STONE_PRESET_NAMES;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.VALID_PROPERTIES;
import static com.personthecat.orestonevariants.commands.CommandUtils.arg;
import static com.personthecat.orestonevariants.commands.CommandUtils.blkArg;
import static com.personthecat.orestonevariants.commands.CommandUtils.execute;
import static com.personthecat.orestonevariants.commands.CommandUtils.fileArg;
import static com.personthecat.orestonevariants.commands.CommandUtils.getListArgument;
import static com.personthecat.orestonevariants.commands.CommandUtils.getValidProperties;
import static com.personthecat.orestonevariants.commands.CommandUtils.greedyArg;
import static com.personthecat.orestonevariants.commands.CommandUtils.isValidBlock;
import static com.personthecat.orestonevariants.commands.CommandUtils.isValidProperty;
import static com.personthecat.orestonevariants.commands.CommandUtils.jsonArg;
import static com.personthecat.orestonevariants.commands.CommandUtils.literal;
import static com.personthecat.orestonevariants.commands.CommandUtils.sendError;
import static com.personthecat.orestonevariants.commands.CommandUtils.sendMessage;
import static com.personthecat.orestonevariants.commands.CommandUtils.stc;
import static com.personthecat.orestonevariants.commands.CommandUtils.tryGetArgument;

@Log4j2
public class CommandOSV {

    /** The text formatting to be used for the command usage header. */
    private static final Style HEADER_STYLE = Style.EMPTY
        .setColor(Color.fromTextFormatting(TextFormatting.GREEN))
        .setBold(true);

    /** The text formatting to be used for displaying command usage. */
    private static final Style USAGE_STYLE = Style.EMPTY
        .setColor(Color.fromTextFormatting(TextFormatting.GRAY));

    /** The actual text to be used by the help message. */
    private static final String[][] USAGE_TEXT = {
        {
            "generate <ore_name> [name]",
            "Generates an ore preset from the specified",
            "registry name. World gen is not included."
        }, {
            "editConfig <mod_name|all>",
            "Attempts to disable all ore generation for",
            "the specified mod via its config file."
        }, {
            "setStoneLayer <preset> <min> <max> <density>",
            "Attempts to generate world gen variables",
            "based on a range of y values and a 0-1 density."
        }, {
            "update <preset> <path> <value>",
            "Manually update a preset value. Omit the value or",
            "path to display the current values."
        }, {
            "display <preset> [<path>]",
            "Outputs the contents of any presets to the chat."
        }, {
            "put <ore> [<ore> [...]] in <block>",
            "Places any number of new variants in the block list"
        }, {
            "group <type> [<entry> [<entry> [...]]] in <group>",
            "Adds any number of properties or blocks into a group."
        }, {
            "list <type> [<group>]",
            "Displays all of the entries in the current registry",
            "of the given type."
        }, {
            "clear <type> [<group>]",
            "Clears all of the entries in the given registry."
        }, {
            "delete <type> [<group>]",
            "Deletes the given registry. This will reset it to",
            "its default state after restart."
        }
    };

    /** the number of lines to occupy each page of the help message. */
    private static final int USAGE_LENGTH = 5;

    /** The header to be used by the help message /  usage text. */
    private static final String USAGE_HEADER = " --- OSV Command Usage ({} / {}) ---";

    /** The help message / usage text. */
    private static final TextComponent[] USAGE_MSG = createHelpMessage();

    /** The maximum number of values in a list argument. */
    private static final int LIST_DEPTH = 32;

    /** The text formatting used to indicate values being deleted. */
    private static final Style DELETED_VALUE_STYLE = Style.EMPTY
        .setColor(Color.fromTextFormatting(TextFormatting.RED));

    /** The text formatting use to indicate values being replaced. */
    private static final Style REPLACED_VALUE_STYLE = Style.EMPTY
        .setColor(Color.fromTextFormatting(TextFormatting.GREEN));

    /** The text formatting used for the undo button. */
    private static final Style UNDO_STYLE = Style.EMPTY
        .setColor(Color.fromTextFormatting(TextFormatting.GRAY))
        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, stc("Click to undo.")))
        .setUnderlined(true)
        .setBold(true);

    /** Creates and registers the parent OSV command. */
    public static void register(Commands manager) {
        manager.getDispatcher().register(createCommandOSV());
        log.info("Successfully registered /osv with Commands.");
    }

    /** Generates the top level command used by this mod. */
    private static LiteralArgumentBuilder<CommandSource> createCommandOSV() {
        return literal("osv")
            .executes(wrap(CommandOSV::help))
            .then(createHelp())
            .then(createGenerate())
            .then(createEditConfig())
            .then(createSetStoneLayer())
            .then(createUpdate())
            .then(createDisplay())
            .then(createPut())
            .then(createGroup())
            .then(createList())
            .then(createClear())
            .then(createDelete());
    }

    /** Generates the help sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createHelp() {
        return literal("help")
            .executes(wrap(CommandOSV::help))
            .then(arg("page", 1, USAGE_MSG.length)
                .executes(wrap(CommandOSV::helpPage)));
    }

    /** Generates the generate sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createGenerate() {
        return literal("generate")
            .then(blkArg("ore")
                .executes(wrap(CommandOSV::generate))
            .then(arg("name", OPTIONAL_NAME)
                .executes(wrap(CommandOSV::generate))));
    }

    /** Generates the editConfig sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createEditConfig() {
        return literal("editConfig")
            .then(arg("mod", MOD_NAMES)
                .executes(wrap(CommandOSV::editConfig)));
    }

    /** Generates the setStoneLayer sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createSetStoneLayer() {
        return literal("setStoneLayer")
            .then(arg("preset", STONE_PRESET_NAMES)
            .then(arg("min", 0, 255)
            .then(arg("max", 0, 255)
            .then(arg("density", 0.0, 1.0)
                .executes(wrap(CommandOSV::setStoneLayer))))));
    }

    /** Generates the update sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createUpdate() {
        return literal("update")
            .then(fileArg()
                .executes(wrap(CommandOSV::display))
            .then(jsonArg()
                .executes(wrap(CommandOSV::display))
            .then(greedyArg("value", ANY_VALUE)
                .executes(wrap(CommandOSV::update)))));
    }

    /** Generates the display sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createDisplay() {
        return literal("display")
            .then(fileArg()
                .executes(wrap(CommandOSV::display))
            .then(jsonArg()
                .executes(wrap(CommandOSV::display))));
    }

    /** Generates the put sub-command. */
    private static ArgumentBuilder<CommandSource, ?> createPut() {
        ArgumentBuilder<CommandSource, ?> nextOre = blkInBg("ore" + LIST_DEPTH);
        for (int i = LIST_DEPTH - 1; i > 0; i--) {
            nextOre = blkInBg("ore" + i).then(nextOre);
        }
        // All and default are only suggested for the first entry.
        return literal("put")
            .then(inBg(arg("ore0", ALL_VALID_PROPERTIES))
            .then(nextOre));
    }

    /** Generates the group sub-command. */
    private static ArgumentBuilder<CommandSource, ?> createGroup() {
        return literal("group")
            .then(literal(GroupType.PROPERTIES.key())
                .then(createGroupProperties()))
            .then(literal(GroupType.BLOCKS.key())
                .then(createGroupBlocks()));
    }

    /** A sub-command for placing properties in a property group. */
    private static ArgumentBuilder<CommandSource, ?> createGroupProperties() {
        ArgumentBuilder<CommandSource, ?> nextProperty = propsInGroup("val" + LIST_DEPTH);
        for (int i = LIST_DEPTH - 1; i > 0; i--) {
            nextProperty = propsInGroup("val" + i).then(nextProperty);
        }
        return propsInGroup("val0").then(nextProperty);
    }

    /** A sub-command for placing blocks in a block group. */
    private static ArgumentBuilder<CommandSource, ?> createGroupBlocks() {
        ArgumentBuilder<CommandSource, ?> nextBlock = blocksInGroup("val" + LIST_DEPTH);
        for (int i = LIST_DEPTH - 1; i > 0; i--) {
            nextBlock = blocksInGroup("val" + i).then(nextBlock);
        }
        return blocksInGroup("val0").then(nextBlock);
    }

    /** Generates the list sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createList() {
        return literal("list")
            .then(literal(RegistryOperation.VALUES.key())
                .executes(wrap(ctx -> list(ctx, RegistryOperation.VALUES))))
            .then(literal(RegistryOperation.BLOCKS.key())
                .then(arg("name", BLOCK_GROUPS)
                    .executes(wrap(ctx -> list(ctx, RegistryOperation.BLOCKS)))))
            .then(literal(RegistryOperation.PROPERTIES.key())
                .then(arg("name", PROPERTY_GROUPS)
                    .executes(wrap(ctx -> list(ctx, RegistryOperation.PROPERTIES)))));
    }

    /** Generates the clear sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createClear() {
        return literal("clear")
            .then(literal(RegistryOperation.VALUES.key())
                .executes(wrap(ctx -> clear(ctx, RegistryOperation.VALUES))))
            .then(literal(RegistryOperation.BLOCKS.key())
                .then(arg("name", BLOCK_GROUPS)
                    .executes(wrap(ctx -> clear(ctx, RegistryOperation.BLOCKS)))))
            .then(literal(RegistryOperation.PROPERTIES.key())
                .then(arg("name", PROPERTY_GROUPS)
                    .executes(wrap(ctx -> clear(ctx, RegistryOperation.PROPERTIES)))));
    }

    /** Generates the delete sub-command. */
    private static LiteralArgumentBuilder<CommandSource> createDelete() {
        return literal("delete")
            .then(literal(RegistryOperation.VALUES.key())
                .executes(wrap(ctx -> delete(ctx, RegistryOperation.VALUES))))
            .then(literal(RegistryOperation.BLOCKS.key())
                .then(arg("name", BLOCK_GROUPS)
                    .executes(wrap(ctx -> delete(ctx, RegistryOperation.BLOCKS)))))
            .then(literal(RegistryOperation.PROPERTIES.key())
                .then(arg("name", PROPERTY_GROUPS)
                    .executes(wrap(ctx -> delete(ctx, RegistryOperation.PROPERTIES)))));
    }

    /** Accepts any block, excluding all and default. */
    private static ArgumentBuilder<CommandSource, ?> blkInBg(String name) {
        return inBg(arg(name, VALID_PROPERTIES));
    }

    /** Generates an argument node which may or may not be the end of a group. */
    private static ArgumentBuilder<CommandSource, ?> inBg(ArgumentBuilder<CommandSource, ?> arg) {
        return arg.then(literal("in")
            .then(greedyArg("bg", ALL_VALID_BLOCKS)
                .executes(wrap(CommandOSV::put))));
    }

    /** Places any number of properties into a group at this point in the tree. */
    private static ArgumentBuilder<CommandSource, ?> propsInGroup(String name) {
        return arg(name, VALID_PROPERTIES)
            .then(literal("in")
            .then(arg("group", PROPERTY_GROUPS)
                .executes(wrap(ctx -> group(ctx, GroupType.PROPERTIES)))));
    }

    /** Places any number of blocks into a group at this point in the tree. */
    private static ArgumentBuilder<CommandSource, ?> blocksInGroup(String name) {
        return blkArg(name)
            .then(literal("in")
            .then(arg("group", BLOCK_GROUPS)
                .executes(wrap(ctx -> group(ctx, GroupType.BLOCKS)))));
    }

    /** Wraps a standard consumer so that all errors will be forwarded to the user. */
    private static Command<CommandSource> wrap(Consumer<CommandContext<CommandSource>> fn) {
        return ctx -> (int) Result.of(() -> fn.accept(ctx))
            .ifErr(Result::WARN)
            .ifErr(e -> sendError(ctx, e.getMessage()))
            .map(v -> 1)
            .orElse(-1);
    }

    /** Displays the help page with no arguments passed. */
    private static void help(CommandContext<CommandSource> ctx) {
        doHelp(ctx.getSource(), 1);
    }

    /** Displays the help page with a page number passed. */
    private static void helpPage(CommandContext<CommandSource> ctx) {
        doHelp(ctx.getSource(), ctx.getArgument("page", Integer.class));
    }

    /** Runs the actual help argument. */
    private static void doHelp(CommandSource source, int page) {
        source.sendFeedback(USAGE_MSG[page - 1], true);
    }


    /** Executes the generate command. */
    private static void generate(CommandContext<CommandSource> ctx) {
        final Optional<String> name = tryGetArgument(ctx, "name", String.class);
        final BlockState ore = ctx.getArgument("ore", BlockStateInput.class).getState();
        final ServerWorld world = ctx.getSource().getWorld();
        final JsonObject json = PropertyGenerator.getBlockInfo(ore, world, name);
        final String fileName = getString(json, "name")
            .orElseThrow(() -> runEx("Unreachable."));
        final File file = new File(OreProperties.DIR, fileName + ".hjson");
        writeJson(json, file).expect("Error writing new hjson file.");
        sendMessage(ctx, f("Finished writing {}.", fileName + ".hjson"));
    }

    /** Executes the edit config command. */
    private static void editConfig(CommandContext<CommandSource> ctx) {
        sendMessage(ctx, "No mods are yet supported, as of this version.");
    }

    /** Executes the set stone layer command. */
    private static void setStoneLayer(CommandContext<CommandSource> ctx) {
        final String preset = ctx.getArgument("preset", String.class);
        final int min = ctx.getArgument("min", Integer.class);
        final int max = ctx.getArgument("max", Integer.class);
        if (min > max) {
            throw runEx("max > min");
        }
        final double density = ctx.getArgument("density", Double.class);
        int size = (int) (((max - min) + 25) * density);
        size = Math.min(52, size); // >52 -> cascading gen lag.
        // Lower density -> greater size -> lower count (invert)
        // 15 count per 5 blocks high
        // Minimum of 15
        final int count = (int) ((1.0 - density) * (max - min) * 15 / 5) + 15;

        final JsonObject json = new JsonObject()
            .set("height", new JsonArray().add(min).add(max).setCondensed(true))
            .set("size", size)
            .set("count", count)
            .setCondensed(false);
        final String value = escape(json.toString(FORMATTER));
        execute(ctx, f("/osv update stone/{} gen[0] {}", preset, value));
    }

    /** Executes the update command. */
    private static void update(CommandContext<CommandSource> ctx) {
        final HjsonArgument.Result preset = ctx.getArgument("file", HjsonArgument.Result.class);
        final PathArgument.Result path = ctx.getArgument("path", PathArgument.Result.class);
        // Read the new and old values.
        final String toEsc = ctx.getArgument("value", String.class);
        final String toLit = toLiteral(toEsc);
        final JsonValue toVal = JsonValue.readHjson(toLit);
        final JsonValue fromVal = getValueFromPath(preset.json.get(), path)
            .orElse(JsonValue.valueOf(null));
        final String fromLit = fromVal.toString(FORMATTER);
        final String fromEsc = escape(fromLit);
        // Write the new value.
        setValueFromPath(preset.json.get(), path, toVal);
        writeJson(preset.json.get(), preset.file).expectF("Error writing to file: {}", preset.file.getName());
        // Send feedback.
        final IFormattableTextComponent message = stc(f("Successfully updated {}.\n", preset.file.getName()))
            .append(stc(fromLit.replace("\r", "")).setStyle(DELETED_VALUE_STYLE))
            .append(stc(" -> "))
            .append(stc(toLit).setStyle(REPLACED_VALUE_STYLE))
            .append(stc(" "))
            .append(generateUndo(ctx.getInput(), fromEsc, toEsc));
        ctx.getSource().sendFeedback(message, true);
    }

    /** Generates the undo text and click event. */
    private static IFormattableTextComponent generateUndo(String input, String from, String to) {
        final int index = input.lastIndexOf(to); // Inefficient.
        final String cmd = (input.substring(0, index) + from).replace("\"\"", "\"");
        final ClickEvent undoClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd);
        return stc("[UNDO]").setStyle(UNDO_STYLE.setClickEvent(undoClick));
    }

    private static String escape(String literal) {
        return literal.replaceAll("\r?\n", "\\\\n") // newline -> \n
            .replace("\"", "\\\""); // " -> \"
    }

    private static String toLiteral(String escaped) {
        return escaped.replace("\\n", "\n")
            .replace("\\\"", "\"");
    }

    /** Displays the specified preset in the chat. */
    private static void display(CommandContext<CommandSource> ctx) {
        final HjsonArgument.Result preset = ctx.getArgument("file", HjsonArgument.Result.class);
        final JsonValue json = tryGetArgument(ctx, "path", PathArgument.Result.class)
            .flatMap(result -> getValueFromPath(preset.json.get(), result))
            .orElseGet(preset.json::get);

        final IFormattableTextComponent msg = stc("")
            .append(stc(f("--- {} ---\n", preset.file.getName()))
                .setStyle(HEADER_STYLE))
            .appendString(json.toString(FORMATTER));
        sendMessage(ctx, msg);
    }

    /** Puts any number of ore properties to spawn in the given background block. */
    private static void put(CommandContext<CommandSource> ctx) {
        final List<String> ores = getListArgument(ctx, "ore", String.class);
        final String bg = ctx.getArgument("bg", String.class);
        // Make sure this background block is valid.
        if (!isValidBlock(bg)) {
            throw runExF("Invalid block type: {}", bg);
        }
        // Update the block entries and update them in memory and on the disk.
        updateRegistryValues(generateRegistryValues(ores, bg));
        // Display the updated values to the user.
        final ITextComponent values = stc(Arrays.toString(Cfg.blockEntries.get().toArray(new String[0])))
            .setStyle(USAGE_STYLE);
        sendMessage(ctx, stc("Updated block list:\n").append(values));
        sendMessage(ctx, "Restart to see changes.");
    }

    /** Generates an updated set of block entries from the preset arguments. */
    private static List<String> generateRegistryValues(List<String> ores, String formatted) {
        final List<String> valid = getValidProperties().collect(Collectors.toList());
        final List<String> entries = Cfg.blockEntries.get();
        // Don't use "all" with other entries.
        if (ores.contains("all") && ores.size() > 1) {
            throw runEx("Too many entries. Don't put all with extra values.");
        }
        for (String ore : ores) {
            if (!valid.contains(ore)) {
                throw runExF("Invalid property type: {}", ore);
            }
            entries.add(f("{} {}", ore, formatted));
        }
        return entries;
    }

    /** Updates all of the block registry values in the config file and in memory. */
    private static void updateRegistryValues(List<String> entries) {
        Cfg.blockEntries.set(entries);
        updateJson(Cfg.getCommon(), json -> {
            final JsonArray values = new JsonArray();
            entries.forEach(value -> values.add(JsonValue.valueOf(value)));
            final JsonObject registry = getObjectOrNew(json, "blockRegistry");
            registry.set("values", values);
        }).expect("Error writing to file.");
    }

    /** Displays the specified element in the config file. */
    private static void list(CommandContext<CommandSource> ctx, RegistryOperation operation) {
        final String name = tryGetArgument(ctx, "name", String.class).orElse("values");
        final Optional<List<String>> list;
        if (operation == RegistryOperation.VALUES) {
            list = full(Cfg.blockEntries.get());
        } else if (operation == RegistryOperation.BLOCKS) {
            list = safeGet(Cfg.blockGroups, name);
        } else {
            list = safeGet(Cfg.propertyGroups, name);
        }
        if (list.isPresent()) {
            final String formatted = Arrays.toString(list.get().toArray(new String[0]));
            sendMessage(ctx, "Entries in " + name + ":");
            sendMessage(ctx, stc(formatted).setStyle(USAGE_STYLE));
        } else {
            sendError(ctx, "Invalid group name: " + name);
        }
    }

    /** Clears the specified element in the config file. */
    private static void clear(CommandContext<CommandSource> ctx, RegistryOperation operation) {
        final String arg = tryGetArgument(ctx, "name", String.class).orElse("values");
        updateJson(Cfg.getCommon(), json -> {
            final JsonObject registry = getObjectOrNew(json, "blockRegistry");
            if (operation == RegistryOperation.VALUES) {
                Cfg.blockEntries.get().clear();
                registry.set("values", new JsonArray());
            } else if (operation == RegistryOperation.BLOCKS) {
                clearGroup(Cfg.blockGroups, registry, arg, "blockGroups");
            } else {
                clearGroup(Cfg.propertyGroups, registry, arg, "propertyGroups");
            }
        }).expect("Error writing to file.");
        sendMessage(ctx, "Successfully cleared values. Restart to see changes.");
    }

    /** Removes the specified element from the config file. */
    private static void delete(CommandContext<CommandSource> ctx, RegistryOperation operation) {
        final String name = tryGetArgument(ctx, "name", String.class).orElse("values");
        updateJson(Cfg.getCommon(), json -> {
            final JsonObject registry = getObjectOrNew(json, "blockRegistry");
            if (operation == RegistryOperation.VALUES) {
                Cfg.blockEntries.get().clear();
                registry.remove("values");
            } else if (operation == RegistryOperation.BLOCKS) {
                Cfg.blockGroups.remove(name);
                getObjectOrNew(registry, "blockGroups").remove(name);
            } else {
                Cfg.propertyGroups.remove(name);
                getObjectOrNew(registry, "propertyGroups").remove(name);
            }
        }).expect("Error writing to file.");
        sendMessage(ctx, "Successfully cleared values. Entries will be reset on restart.");
    }

    /** Clears a group both in memory and in a JSON object. */
    private static void clearGroup(Map<String, List<String>> groups, JsonObject json, String arg, String field) {
        if (!groups.containsKey(arg)) {
            groups.put(arg, new ArrayList<>());
        } else {
            groups.get(arg).clear();
        }
        getObjectOrNew(json, field).set(arg, new JsonArray());
    }

    /** Puts a list of properties or blocks into a group. */
    private static void group(CommandContext<CommandSource> ctx, GroupType type) {
        final List<String> values = getGroupArguments(ctx, type);
        final String group = ctx.getArgument("group", String.class);
        for (String val : values) {
            if (type == GroupType.BLOCKS && !isValidBlock(val)) {
                throw runExF("Invalid block group entry: {}", val);
            } else if (type == GroupType.PROPERTIES && !isValidProperty(val)) {
                throw runExF("Invalid property group entry: {}", val);
            }
        }
        // Place the new entries at the bottom of the list.s
        final List<String> entries = getGroupValues(type, group);
        entries.addAll(values);
        updateGroupValues(entries, type, group);

        final String formatted = Arrays.toString(entries.toArray(new String[0]));
        final ITextComponent text = stc(group + ": " + formatted)
            .setStyle(USAGE_STYLE);
        sendMessage(ctx, stc("Updated group list:\n").append(text));
        sendMessage(ctx, "Restart to see changes.");
    }

    /** Gets all of the values currently belonging to a group. */
    private static List<String> getGroupValues(GroupType type, String group) {
        if (type == GroupType.BLOCKS) {
            if (!Cfg.blockGroups.containsKey(group)) {
                Cfg.blockGroups.put(group, new ArrayList<>());
            }
            return safeGet(Cfg.blockGroups, group).orElseThrow(() -> runEx("Unreachable."));
        }
        if (!Cfg.propertyGroups.containsKey(group)) {
            Cfg.propertyGroups.put(group, new ArrayList<>());
        }
        return safeGet(Cfg.propertyGroups, group).orElseThrow(() -> runEx("Unreachable."));
    }

    /** Updates all of the specified values in the config file and in memory. */
    private static void updateGroupValues(List<String> entries, GroupType type, String group) {
        final String field;
        if (type == GroupType.BLOCKS) {
            Cfg.blockGroups.put(group, entries);
            field = "blockGroups";
        } else {
            Cfg.propertyGroups.put(group, entries);
            field = "propertyGroups";
        }
        updateJson(Cfg.getCommon(), json -> {
            final JsonArray values = new JsonArray();
            entries.forEach(value -> values.add(JsonValue.valueOf(value)));
            final JsonObject registry = getObjectOrNew(json, "blockRegistry");
            final JsonObject groups = getObjectOrNew(registry, field);
            groups.set(group, values);
        }).expect("Error writing to file.");
    }

    /** Generates the help message, displaying usage for each sub-command. */
    private static TextComponent[] createHelpMessage() {
        final List<StringTextComponent> msgs = new ArrayList<>();
        final int numLines = getNumElements(USAGE_TEXT) - USAGE_TEXT.length;
        final int numPages = (int) Math.floor((double) numLines / (double) USAGE_LENGTH) - 1;
        // The actual pages.
        for (int i = 0; i < USAGE_TEXT.length; i += USAGE_LENGTH) {
            final StringTextComponent header = getUsageHeader((i / USAGE_LENGTH) + 1, numPages);
            // The elements on each page.
            for (int j = i; j < i + USAGE_LENGTH; j++) {
                if (j >= USAGE_TEXT.length) { // ?
                    continue;
                }
                final String[] full = USAGE_TEXT[j];
                // Append the required elements;
                header.appendString("\n");
                appendUsageText(header, full[0], full[1]);
                // Append any extra lines below;
                for (int k = 2; k < full.length; k++) {
                    header.appendString(" ");
                    header.append(stc(full[k]).setStyle(USAGE_STYLE));
                }
            }
            msgs.add(header);
        }
        return msgs.toArray(new StringTextComponent[0]);
    }

    private static int getNumElements(String[][] matrix) {
        int numElements = 0;
        for (String[] a : matrix) {
            numElements += a.length;
        }
        return numElements;
    }

    private static StringTextComponent getUsageHeader(int page, int max) {
        final String header = f(USAGE_HEADER, String.valueOf(page), String.valueOf(max));
        StringTextComponent full = stc("");
        StringTextComponent headerSTC = stc(header);
        headerSTC.setStyle(HEADER_STYLE);
        full.append(headerSTC);
        return full;
    }

    /** A slightly neater way to append so many components to the help message. */
    private static void appendUsageText(TextComponent msg, String command, String usage) {
        msg.append(usageText(command, usage));
    }

    /** Formats the input text to nicely display a command's usage. */
    private static TextComponent usageText(String command, String usage) {
        TextComponent msg = stc(""); // Parent has no formatting.
        msg.append(stc(command));
        msg.append(stc(" :\n " + usage).setStyle(USAGE_STYLE));
        return msg;
    }

    /** Retrieves a series of group values, either block names or property types. */
    private static List<String> getGroupArguments(CommandContext<?> ctx, GroupType type) {
        if (type == GroupType.PROPERTIES) {
            return getListArgument(ctx, "val", String.class);
        }
        return getListArgument(ctx, "val", BlockStateInput.class).stream()
            .map(input -> BlockStateParser.toString(input.getState()))
            .collect(Collectors.toList());
    }

    /** The type of config element to operated on with /osv list and /osv clear. */
    private enum RegistryOperation {
        VALUES, PROPERTIES, BLOCKS;

        private String key() {
            return name().toLowerCase();
        }
    }

    /** The type of group being operated on by /osv group. */
    private enum GroupType {
        PROPERTIES, BLOCKS;

        private String key() {
            return name().toLowerCase();
        }
    }
}