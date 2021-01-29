package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGenerator;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.server.ServerWorld;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.fresult.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.getMin;
import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;
import static com.personthecat.orestonevariants.util.CommonMethods.info;
import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.CommonMethods.toArray;
import static com.personthecat.orestonevariants.util.HjsonTools.FORMATTER;
import static com.personthecat.orestonevariants.util.HjsonTools.getPaths;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;
import static com.personthecat.orestonevariants.util.HjsonTools.getValueFromPath;
import static com.personthecat.orestonevariants.util.HjsonTools.setValueFromPath;
import static com.personthecat.orestonevariants.util.HjsonTools.writeJson;

public class CommandOSV {

    /** A suggestion provider suggesting an optional name parameter. */
    private static final SuggestionProvider<CommandSource> OPTIONAL_NAME = createNameSuggestion();

    /** A suggestion provider suggesting all of the supported mod names or "all." */
    private static final SuggestionProvider<CommandSource> MOD_NAMES = createModNames();

    /** A suggestion provider suggesting all files in the preset directory. */
    private static final SuggestionProvider<CommandSource> STONE_PRESET_NAMES = createStonePresetNames();

    /** A suggestion provider that provides file paths OTF. Requires `file` arg. */
    private static final SuggestionProvider<CommandSource> FILE_SUGGESTION = createFileSuggestion();

    /** A suggestion provider that provides json paths OTF. Requires 'file' and `path` args. */
    private static final SuggestionProvider<CommandSource> JSON_SUGGESTION = createJsonSuggestion();

    /** A suggestion provider suggesting that any value is acceptable. */
    private static final SuggestionProvider<CommandSource> ANY_VALUE = createAnySuggestion();

    /** A suggestion provider suggesting example integers. */
    private static final SuggestionProvider<CommandSource> INTEGER_SUGGESTION = createIntegerSuggestion();

    /** A suggestion provider suggestion example decimals. */
    private static final SuggestionProvider<CommandSource> DECIMAL_SUGGESTION = createDecimalSuggestion();

    /** The text formatting to be used for the command usage header. */
    private static final Style USAGE_HEADER_STYLE = Style.EMPTY
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
            "update <dir> <cfg> <key_path> <value>",
            "Manually update a preset value."
        }, {
            "put <ore> [<ore> [...]] in <block>",
            "Places any number of new variants in the block list"
        }, {
            "group <type> [<entry> [<entry> [...]]] in <group>",
            "Adds any number of properties or blocks into a group."
        }, {
            "display <properties>",
            "Outputs the contents of any presets to the chat."
        }
    };

    /** the number of lines to occupy each page of the help message. */
    private static final int USAGE_LENGTH = 5;

    /** The header to be used by the help message /  usage text. */
    private static final String USAGE_HEADER = " --- OSV Command Usage ({} / {}) ---";

    /** The help message / usage text. */
    private static final TextComponent[] USAGE_MSG = createHelpMessage();

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
        info("Successfully registered /osv with Commands.");
    }

    private static SuggestionProvider<CommandSource> register(String name, SuggestionProvider<ISuggestionProvider> provider) {
        return SuggestionProviders.register(osvLocation(name), provider);
    }

    private static SuggestionProvider<CommandSource> register(String name, String... suggestions) {
        return register(name, (ctx, builder) -> ISuggestionProvider.suggest(suggestions, builder));
    }

    /** Generates the top level command used by this mod. */
    private static LiteralArgumentBuilder<CommandSource> createCommandOSV() {
        return literal("osv")
            .executes(wrap(CommandOSV::help))
            .then(createHelp())
            .then(createGenerate())
            .then(createEditConfig())
            .then(createSetStoneLayer())
            .then(createUpdate());
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
            .then(jsonArg()
            .then(greedyArg("value", ANY_VALUE)
                .executes(wrap(CommandOSV::update)))));
    }

    /** Wraps a standard consumer so that all errors will be forwarded to the user. */
    private static Command<CommandSource> wrap(Consumer<CommandContext<CommandSource>> fn) {
        return ctx -> (int) personthecat.fresult.Result.of(() -> fn.accept(ctx))
            .ifErr(Result::WARN)
            .ifErr(e -> sendError(ctx, e.getMessage()))
            .map(v -> 1)
            .orElse(-1);
    }

    /** Generates the optional name suggestion provider. */
    private static SuggestionProvider<CommandSource> createNameSuggestion() {
        return register("optional_name_suggestion", "[<named_manually>]");
    }

    /** Generates the possible mod name suggestion provider. */
    private static SuggestionProvider<CommandSource> createModNames() {
        return register("mod_names_suggestion", "all", "[<mod_name>]");
    }

    /** Generates the preset name provider. */
    private static SuggestionProvider<CommandSource> createStonePresetNames() {
        return register("presets_suggestion", (ctx, builder) -> {
            final Stream<String> names = PathTools.getSimpleContents(StoneProperties.DIR);
            return ISuggestionProvider.suggest(names, builder);
        });
    }

    /** Generates file suggestions on the fly. Requires argument `file`. */
    private static SuggestionProvider<CommandSource> createFileSuggestion() {
        return register("file_suggestion", (ctx, builder) -> {
            final Stream<String> neighbors = tryGetArgument(ctx, "file", HjsonArgument.Result.class)
                .map(HjsonArgument.Result::getNeighbors)
                .orElse(PathTools.getSimpleContents(getOSVDir()));
            return ISuggestionProvider.suggest(neighbors, builder);
        });
    }

    /** Generates json path suggestions on the fly. Requires arguments 'file` and `path`. */
    private static SuggestionProvider<CommandSource> createJsonSuggestion() {
        return register("json_suggestion", (ctx, builder) -> {
            final JsonObject json = ctx.getArgument("file", HjsonArgument.Result.class).json.get();
            final PathArgument.Result result = tryGetArgument(ctx, "path", PathArgument.Result.class)
                .orElse(new PathArgument.Result(new ArrayList<>()));
            return ISuggestionProvider.suggest(getPaths(json, result), builder);
        });
    }

    /** Generates the "any value" suggestion provider. */
    private static SuggestionProvider<CommandSource> createAnySuggestion() {
        return register("any_suggestion", "[<any_value>]");
    }

    /** Generates the integer suggestion provider. */
    private static SuggestionProvider<CommandSource> createIntegerSuggestion() {
        return register("integer_suggestion", "[<integer>]", "-1", "0", "1");
    }

    /** Generates the decimal suggestion provider. */
    private static SuggestionProvider<CommandSource> createDecimalSuggestion() {
        return register("decimal_suggestion", "[<decimal>]", "0.5", "1.0", "1.5");
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
        size = getMin(52, size); // >52 -> cascading gen lag.
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
            .orElse(toVal); // Inefficient double read from path here...
        final String fromLit = fromVal.toString(FORMATTER);
        final String fromEsc = escape(fromLit);
        // Write the new value.
        setValueFromPath(preset.json.get(), path, toVal);
        writeJson(preset.json.get(), preset.file).expectF("Error writing toVal file: {}", preset.file.getName());
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

    /** Generates the help message, displaying usage for each sub-command. */
    private static TextComponent[] createHelpMessage() {
        final List<StringTextComponent> msgs = new ArrayList<>();
        final int numLines = getNumElements(USAGE_TEXT) - USAGE_TEXT.length;
        final int numPages = (int) Math.ceil((double) numLines / (double) USAGE_LENGTH) - 1;
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
        return toArray(msgs, StringTextComponent.class);
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
        headerSTC.setStyle(USAGE_HEADER_STYLE);
        full.append(headerSTC);
        return full;
    }

    /** A slightly neater way to append so many components to the help message. */
    private static void appendUsageText(TextComponent msg, String command, String usage) {
        msg.append(usageText(command, usage));
    }

    /** Formats the input text to nicely display a command'spawnStructure usage. */
    private static TextComponent usageText(String command, String usage) {
        TextComponent msg = stc(""); // Parent has no formatting.
        msg.append(stc(command));
        msg.append(stc(" :\n " + usage).setStyle(USAGE_STYLE));
        return msg;
    }

    /** Executes additional commands internally. */
    private static int execute(CommandContext<CommandSource> ctx, String command) {
        final Commands manager = ctx.getSource().getServer().getCommandManager();
        return manager.handleCommand(ctx.getSource(), command);
    }
    
    /** Shorthand for sending a message to the input user. */
    private static void sendMessage(CommandContext<CommandSource> ctx, String msg) {
        ctx.getSource().sendFeedback(stc(msg), true);
    }

    /** Shorthand for sending an error to the input user. */
    private static void sendError(CommandContext<CommandSource> ctx, String msg) {
        ctx.getSource().sendErrorMessage(stc(msg));
    }

    /** Shorthand method for creating StringTextComponents. */
    private static StringTextComponent stc(String s) {
        return new StringTextComponent(s);
    }

    /** Shorthand for creating a literal argument. */
    private static LiteralArgumentBuilder<CommandSource> literal(String name) {
        return Commands.literal(name);
    }

    /** Retrieves an argument which may not be present. */
    private static <T> Optional<T> tryGetArgument(CommandContext<?> ctx, String name, Class<T> clazz) {
        return Result.of(() -> ctx.getArgument(name, clazz))
            .get(Result::IGNORE);
    }

    /** Shorthand method for creating an integer argument. */
    private static RequiredArgumentBuilder<CommandSource, Integer> arg(String name, int min, int max) {
        return Commands.argument(name, IntegerArgumentType.integer(min, max))
            .suggests(INTEGER_SUGGESTION);
    }

    /** Shorthand method for creating a decimal argument. */
    private static RequiredArgumentBuilder<CommandSource, Double> arg(String name, double min, double max) {
        return Commands.argument(name, DoubleArgumentType.doubleArg(min, max))
            .suggests(DECIMAL_SUGGESTION);
    }

    /** Shorthand method for creating a string argument. */
    private static RequiredArgumentBuilder<CommandSource, String> arg(String name, SuggestionProvider<CommandSource> suggests) {
        return Commands.argument(name, StringArgumentType.string())
            .suggests(suggests);
    }

    /** Shorthand method for creating a greedy string argument. */
    private static RequiredArgumentBuilder<CommandSource, String> greedyArg(String name, SuggestionProvider<CommandSource> suggests) {
        return Commands.argument(name, StringArgumentType.greedyString())
            .suggests(suggests);
    }

    /** Shorthand method for creating a block argument. */
    private static RequiredArgumentBuilder<CommandSource, BlockStateInput> blkArg(String name) {
        return Commands.argument(name, BlockStateArgument.blockState());
    }

    private static RequiredArgumentBuilder<CommandSource, HjsonArgument.Result> fileArg() {
        return Commands.argument("file", HjsonArgument.OSV())
            .suggests(FILE_SUGGESTION);
    }

    private static RequiredArgumentBuilder<CommandSource, PathArgument.Result> jsonArg() {
        return Commands.argument("path", new PathArgument())
            .suggests(JSON_SUGGESTION);
    }
}