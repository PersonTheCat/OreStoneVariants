package com.personthecat.orestonevariants.commands;

import com.personthecat.orestonevariants.config.ModConfigSupport;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.PropertyGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

public class CommandOSV extends CommandBase  {
    /** The text formatting to be used for the command usage header. */
    private static final Style USAGE_HEADER_STYLE = new Style()
        .setColor(TextFormatting.GREEN)
        .setBold(true);
    /** The text formatting to be used for displaying command usage. */
    private static final Style USAGE_STYLE = new Style()
        .setColor(TextFormatting.GRAY);
    /** The actual text to be used by the help message. */
    private static final String[][] USAGE_TEXT = {
        { "generate <ore_name> [name]", "Generates an ore preset from the specified",
                                        "registry name. World gen is not included." },
        { "editConfig <mod_name|all>", "Attempts to disable all ore generation for",
                                        "the specified mod via its config file." },
        { "setStoneLayer <preset> <min> <max> <density>", "Attempts to generate world gen variables",
                                                          "based on a range of y values and a 0-1 density." },
        { "update <dir> <cfg> <key_path> <value>", "Manually update a preset value." }
    };
    /** the number of lines to occupy each page of the help message. */
    private static final int USAGE_LENGTH = 5;
    /** The header to be used by the help message / usage text. */
    private static final String USAGE_HEADER = " --- OSV Command Usage ({} / {}) ---";
    /** The help message / usage text. */
    private static final ITextComponent[] USAGE_MSG = createHelpMessage();
    /** New line character. */
    private static final String NEW_LINE = System.getProperty("line.separator");
    /** An expression representing an array access, grouping the name and index. */
    private static Pattern ARRAY_PATTERN = Pattern.compile("(.*)\\[(.*)\\]");

    @Override
    public String getName() {
        return "osv";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/osv <subcommand>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        // The user did not specify which command to run. Inform them and stop.
        if (args.length == 0) {
            displayHelp(sender, 1);
            return;
        }
        // Allow multiple commands to be separated by `&&`.
        final int splitIndex = ArrayUtils.lastIndexOf(args, "&&");
        if (splitIndex > 0 && args.length > splitIndex) {
            // Split the arguments into multiple arrays.
            String[] runFirst = ArrayUtils.subarray(args, 0, splitIndex);
            args = ArrayUtils.subarray(args, splitIndex + 1, args.length);
            this.execute(server, sender, runFirst);
        }
        try { // Process, forwarding errors to the user.
            String[] slice = ArrayUtils.subarray(args, 1, args.length);
            handle(server, sender, args[0], slice);
        } catch (RuntimeException e) {
            sendMessage(sender, e.getMessage());
        }
    }

    /** This directs each command into its own dedicated function with helpers. */
    private static void handle(MinecraftServer server, ICommandSender sender, String command, String[] args) {
        switch (command.toLowerCase()) {
            case "generate" : generate(server, sender, args); break;
            case "configedit" :
            case "editconfig" : editConfig(sender, args); break;
            case "setstonelayer" : setStoneLayer(sender, args); break;
            case "update" : update(sender, args); break;
            default : displayHelp(sender, 1);
        }
    }

    /** Run the generate command -> generate a new preset. */
    private static void generate(MinecraftServer server, ICommandSender sender, String[] args) {
        requireArgs(args, 1);
        final IBlockState ore = getBlockState(args[0])
            .orElseThrow(() -> runExF("There is no block named {}.", args[0]));
        final World world = server.getWorld(0);
        final Optional<String> name = safeGet(args, 1);
        final JsonObject json = PropertyGenerator.getBlockInfo(ore, world, name);
        final String fileName = getString(json, "name")
            .orElseThrow(() -> runEx("Unreachable."));
        final File file = new File(OreProperties.DIR, fileName + ".hjson");
        writeJson(json, file).expect("Error writing new hjson file.");
        sendMessage(sender, "Finished writing new preset.");
    }

    /** Run the editConfig command -> disable ore generation from another mod. */
    private static void editConfig(ICommandSender sender, String[] args) {
        requireArgs(args, 1);
        if (ModConfigSupport.updateConfig(args[0])) {
            sendMessage(sender, "Successfully updated mod config! Restart your game to see changes.");
        } else {
            sendMessage(sender, "Invalid or unloaded mod.");
        }
    }

    /** Run the setStoneLayer command -> generate a faux stone layer from a height range and density. */
    private static void setStoneLayer(ICommandSender sender, String[] args) {
        requireArgs(args, 4);
        final String cfg = args[0];
        final int min = Integer.parseInt(args[1]);
        final int max = Integer.parseInt(args[2]);
        final double density = Double.parseDouble(args[3]);
        final int size = (int) ((max - min) * density);
        // Lower density -> greater size -> lower count (invert)
        // 15 count per 20 blocks high
        // Minimum of 2
        final int count = (int) ((1.0 - density) * (max - min) * 15 / 20) + 2;
        // Individual updates bad.
        update(sender, "stone", cfg, "gen[0].height", f("[{},{}]", min, max));
        update(sender, "stone", cfg, "gen[0].size", String.valueOf(size));
        update(sender, "stone", cfg, "gen[0].count", String.valueOf(count));
    }

    /** Run the update command -> manually update a single value in a config file. */
    private static void update(ICommandSender sender, String... args) {
        requireArgs(args, 4);
        final File dir = new File(getConfigDir() + "/osv", args[0]);
        final File cfg = new File(dir, args[1] + ".hjson");
        final String path = args[2];
        final JsonValue value = JsonValue.readHjson(joinAfter(args, 3));
        final String filename = cfg.getName();
        final JsonObject preset = readJson(cfg)
            .orElseThrow(() -> runExF("Preset not found: ", filename));
        setValueFromPath(preset, path, value);
        writeJson(preset, cfg).expectF("Error updating ", filename);
        sendMessage(sender, "Successfully updated " + filename);
    }

    /** Joins every element in a string array on a single space, starting at `index`. */
    private static String joinAfter(String[] array, int index) {
        final StringBuilder sb = new StringBuilder(array[index]);
        for (int i = index + 1; i < array.length; i++) {
            sb.append(' ');
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /** Updates a single value in a json based on a full, dotted path.  */
    private static void setValueFromPath(JsonObject json, String path, JsonValue value) {
        final String[] split = path.split(Pattern.quote("."));
        if (split.length == 0) {
            return;
        }
        JsonObject current = json;
        for (int i = 0; i < split.length - 1; i++) {
            final Matcher matcher = ARRAY_PATTERN.matcher(split[i]);
            if (matcher.matches()) {
                final String key = matcher.group(1);
                final int index = Integer.parseInt(matcher.group(2));
                final JsonArray array = getOrNewArray(current, key);
                final JsonValue fromIndex = getOrNewObj(array, index);
                if (!fromIndex.isObject()) {
                    throw runEx("Only arrays containing objects can be updated by index.");
                }
                current = fromIndex.asObject();
            } else {
                current = getOrNewObj(current, split[i]);
            }
        }
        current.set(split[split.length - 1], value);
    }

    private static JsonObject getOrNewObj(JsonObject json, String key) {
        if (!json.has(key)) {
            json.set(key, new JsonObject());
        }
        // Throws if not an object. Error is forwarded to user in-game.
        return json.get(key).asObject();
    }

    private static JsonObject getOrNewObj(JsonArray array, int index) {
        if (array.size() <= index) {
            array.set(index, new JsonObject());
        }
        return array.get(index).asObject();
    }

    private static JsonArray getOrNewArray(JsonObject json, String key) {
        if (!json.has(key)) {
            json.set(key, new JsonArray());
        }
        return json.get(key).asArray();
    }

    /** Sends the formatted command usage to the user. */
    private static void displayHelp(ICommandSender sender, int page) {
        if (page > USAGE_MSG.length || page <= 0) {
            sendMessage(sender, "Invalid page #.");
            return;
        }
        sender.sendMessage(USAGE_MSG[page - 1]);
    }

    /** Generates the help message, displaying usage for each sub-command. */
    private static ITextComponent[] createHelpMessage() {
        final List<TextComponentString> msgs = new ArrayList<>();
        final int numLines = getNumElements(USAGE_TEXT) - USAGE_TEXT.length;
        final int numPages = (int) Math.ceil((double) numLines / (double) USAGE_LENGTH);
        // The actual pages.
        for (int i = 0; i < USAGE_TEXT.length; i += USAGE_LENGTH) {
            final TextComponentString header = getUsageHeader((i / USAGE_LENGTH) + 1, numPages);
            // The elements on each page.
            for (int j = i; j < i + USAGE_LENGTH; j++) {
                if (j >= USAGE_TEXT.length) { // ?
                    continue;
                }
                final String[] full = USAGE_TEXT[j];
                // Append the required elements;
                header.appendText("\n");
                appendUsageText(header, full[0], full[1]);
                // Append any extra lines below;
                for (int k = 2; k < full.length; k++) {
                    header.appendText(" ");
                    header.appendText((full[k]));
                }
            }
            msgs.add(header);
        }
        return toArray(msgs, TextComponentString.class);
    }

    private static int getNumElements(String[][] matrix) {
        int numElements = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                numElements++;
            }
        }
        return numElements;
    }

    private static TextComponentString getUsageHeader(int page, int max) {
        final String header = f(USAGE_HEADER, String.valueOf(page), String.valueOf(max));
        TextComponentString full = tcs("");
        TextComponentString headerTCS = tcs(header);
        headerTCS.setStyle(USAGE_HEADER_STYLE);
        full.appendSibling(headerTCS);
        return full;
    }

    /** A slightly neater way to append so many components to the help message. */
    private static void appendUsageText(ITextComponent msg, String command, String usage) {
        msg.appendSibling(usageText(command, usage));
    }

    /** Formats the input text to nicely display a command'spawnStructure usage. */
    private static ITextComponent usageText(String command, String usage) {
        ITextComponent msg = tcs(""); // Parent has no formatting.
        msg.appendSibling(tcs(command).setStyle(USAGE_STYLE));
        msg.appendSibling(tcs(" : " + usage));
        return msg;
    }

    /** Shorthand for sending a message to the input user. */
    private static void sendMessage(ICommandSender user, String msg) {
        user.sendMessage(tcs(msg));
    }

    /** Shorthand method for creating TextComponentStrings. */
    private static TextComponentString tcs(String s) {
        return new TextComponentString(s);
    }

    /** Ensures that at least `num` arguments are present. */
    private static void requireArgs(String[] args, int num) {
        if (args.length < num) {
            throw runEx("Insufficient arguments for this command.");
        }
    }
}