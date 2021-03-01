package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.CommonMethods;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import personthecat.fresult.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.commands.CommandSuggestions.DECIMAL_SUGGESTION;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.FILE_SUGGESTION;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.INTEGER_SUGGESTION;
import static com.personthecat.orestonevariants.commands.CommandSuggestions.JSON_SUGGESTION;
import static com.personthecat.orestonevariants.io.SafeFileIO.safeListFiles;

public class CommandUtils {

    /** Returns a list of all current blocks and block groups */
    static Stream<String> getValidBlocks() {
        final Stream<String> primary = Stream.concat(Cfg.blockGroups.keySet().stream(),
            ForgeRegistries.BLOCKS.getKeys().stream().map(ResourceLocation::toString));
        return Stream.concat(Stream.of("all", "default"), primary);
    }

    /** Returns a list of all current properties and property groups. */
    static Stream<String> getValidProperties() {
        final Stream<String> primary = Stream.concat(Cfg.propertyGroups.keySet().stream(),
            Stream.of(safeListFiles(OreProperties.DIR)).map(CommonMethods::noExtension));
        return Stream.concat(Stream.of("all", "default"), primary);
    }

    static boolean isValidBlock(String block) {
        final String registry = block.split("\\[")[0];
        return getValidBlocks().anyMatch(s -> registry.equals(s) || registry.equals("minecraft:" + s));
    }

    static boolean isValidProperty(String property) {
        return getValidProperties().anyMatch(s -> s.equals(property));
    }

    /** Executes additional commands internally. */
    static int execute(CommandContext<CommandSource> ctx, String command) {
        final Commands manager = ctx.getSource().getServer().getCommandManager();
        return manager.handleCommand(ctx.getSource(), command);
    }

    /** Shorthand for sending a message to the input user. */
    static void sendMessage(CommandContext<CommandSource> ctx, String msg) {
        sendMessage(ctx, stc(msg));
    }

    /** Shorthand for sending a message to the input user. */
    static void sendMessage(CommandContext<CommandSource> ctx, ITextComponent msg) {
        ctx.getSource().sendFeedback(msg, true);
    }

    /** Shorthand for sending an error to the input user. */
    static void sendError(CommandContext<CommandSource> ctx, String msg) {
        ctx.getSource().sendErrorMessage(stc(msg));
    }

    /** Shorthand method for creating StringTextComponents. */
    static StringTextComponent stc(String s) {
        return new StringTextComponent(s);
    }

    /** Shorthand for creating a literal argument. */
    static LiteralArgumentBuilder<CommandSource> literal(String name) {
        return Commands.literal(name);
    }

    /** Retrieves an argument which may not be present. */
    static <T> Optional<T> tryGetArgument(CommandContext<?> ctx, String name, Class<T> clazz) {
        return Result.of(() -> ctx.getArgument(name, clazz))
            .get(Result::IGNORE);
    }

    /** Retrieves series of numbered arguments by name. */
    static <T> List<T> getListArgument(CommandContext<?> ctx, String name, Class<T> clazz) {
        final List<T> list = new ArrayList<>();
        for (int i = 0; true; i++) {
            final Optional<T> arg = tryGetArgument(ctx, name + i, clazz);
            if (arg.isPresent()) {
                list.add(arg.get());
            } else {
                return list;
            }
        }
    }

    /** Shorthand method for creating an integer argument. */
    static RequiredArgumentBuilder<CommandSource, Integer> arg(String name, int min, int max) {
        return Commands.argument(name, IntegerArgumentType.integer(min, max))
            .suggests(INTEGER_SUGGESTION);
    }

    /** Shorthand method for creating a decimal argument. */
    static RequiredArgumentBuilder<CommandSource, Double> arg(String name, double min, double max) {
        return Commands.argument(name, DoubleArgumentType.doubleArg(min, max))
            .suggests(DECIMAL_SUGGESTION);
    }

    /** Shorthand method for creating a string argument. */
    static RequiredArgumentBuilder<CommandSource, String> arg(String name, SuggestionProvider<CommandSource> suggests) {
        return Commands.argument(name, StringArgumentType.string())
            .suggests(suggests);
    }

    /** Shorthand method for creating a greedy string argument. */
    static RequiredArgumentBuilder<CommandSource, String> greedyArg(String name, SuggestionProvider<CommandSource> suggests) {
        return Commands.argument(name, StringArgumentType.greedyString())
            .suggests(suggests);
    }

    /** Shorthand method for creating a block argument. */
    static RequiredArgumentBuilder<CommandSource, BlockStateInput> blkArg(String name) {
        return Commands.argument(name, BlockStateArgument.blockState());
    }

    /** Shorthand method for creating an Hjson file argument. */
    static RequiredArgumentBuilder<CommandSource, HjsonArgument.Result> fileArg() {
        return Commands.argument("file", HjsonArgument.OSV())
            .suggests(FILE_SUGGESTION);
    }

    /** Shorthand method for creating an Hjson path argument. */
    static RequiredArgumentBuilder<CommandSource, PathArgument.Result> jsonArg() {
        return Commands.argument("path", new PathArgument())
            .suggests(JSON_SUGGESTION);
    }
}
