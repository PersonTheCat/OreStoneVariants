package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.properties.StoneProperties;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;
import org.hjson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.commands.CommandUtils.getValidBlocks;
import static com.personthecat.orestonevariants.commands.CommandUtils.getValidProperties;
import static com.personthecat.orestonevariants.commands.CommandUtils.tryGetArgument;
import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;
import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;
import static com.personthecat.orestonevariants.util.HjsonTools.getPaths;

public class CommandSuggestions {

    /** A suggestion provider suggesting an optional name parameter. */
    static final SuggestionProvider<CommandSource> OPTIONAL_NAME = createNameSuggestion();

    /** A suggestion provider suggesting all of the supported mod names or "all." */
    static final SuggestionProvider<CommandSource> MOD_NAMES = createModNames();

    /** A suggestion provider suggesting all files in the stone preset directory. */
    static final SuggestionProvider<CommandSource> STONE_PRESET_NAMES = createStonePresetNames();

    /** A suggestion provider suggesting all of the current block groups. */
    static final SuggestionProvider<CommandSource> BLOCK_GROUPS = createBlockGroupSuggestion();

    /** A suggestion provider suggesting all of the current property groups. */
    static final SuggestionProvider<CommandSource> PROPERTY_GROUPS = createPropertyGroupSuggestion();

    /** A suggestion provider suggesting all valid blocks and block groups. */
    static final SuggestionProvider<CommandSource> ALL_VALID_BLOCKS = createAllValidBlockSuggestion();

    /** A suggestion provider suggesting all valid properties and property groups. */
    static final SuggestionProvider<CommandSource> ALL_VALID_PROPERTIES = createAllValidPropertySuggestion();

    /** A suggestion provider suggesting all blocks and groups, minus all and default. */
    static final SuggestionProvider<CommandSource> VALID_BLOCKS = createValidBlockSuggestion();

    /** A suggestion provider suggesting all properties and groups, minus all and default. */
    static final SuggestionProvider<CommandSource> VALID_PROPERTIES = createValidPropertySuggestion();

    /** A suggestion provider that provides file paths OTF. Requires `file` arg. */
    static final SuggestionProvider<CommandSource> FILE_SUGGESTION = createFileSuggestion();

    /** A suggestion provider that provides json paths OTF. Requires 'file' and `path` args. */
    static final SuggestionProvider<CommandSource> JSON_SUGGESTION = createJsonSuggestion();

    /** A suggestion provider suggesting that any value is acceptable. */
    static final SuggestionProvider<CommandSource> ANY_VALUE = createAnySuggestion();

    /** A suggestion provider suggesting example integers. */
    static final SuggestionProvider<CommandSource> INTEGER_SUGGESTION = createIntegerSuggestion();

    /** A suggestion provider suggestion example decimals. */
    static final SuggestionProvider<CommandSource> DECIMAL_SUGGESTION = createDecimalSuggestion();

    /** Generates the optional name suggestion provider. */
    private static SuggestionProvider<CommandSource> createNameSuggestion() {
        return register("optional_name_suggestion", "[<named_manually>]");
    }

    /** Generates the possible mod name suggestion provider. */
    private static SuggestionProvider<CommandSource> createModNames() {
        return register("mod_names_suggestion", "all", "[<mod_name>]");
    }

    /** Generates the ore preset name provider. */
    private static SuggestionProvider<CommandSource> createOrePresetNames() {
        return register("ore_suggestion", (ctx, builder) -> {
            final Stream<String> names = PathTools.getSimpleContents(OreProperties.DIR);
            return ISuggestionProvider.suggest(names, builder);
        });
    }

    /** Generates the stone preset name provider. */
    private static SuggestionProvider<CommandSource> createStonePresetNames() {
        return register("stone_suggestion", (ctx, builder) -> {
            final Stream<String> names = PathTools.getSimpleContents(StoneProperties.DIR);
            return ISuggestionProvider.suggest(names, builder);
        });
    }

    /** Generates the block group name provider. */
    private static SuggestionProvider<CommandSource> createBlockGroupSuggestion() {
        return register("block_group_suggestion", (ctx, builder) -> {
            final Set<String> names = new HashSet<>();
            names.add("[<new group>]");
            names.addAll(Cfg.blockGroups.keySet());
            return ISuggestionProvider.suggest(names, builder);
        });
    }

    /** Generates the property group name provider. */
    private static SuggestionProvider<CommandSource> createPropertyGroupSuggestion() {
        return register("property_group_suggestion", (ctx, builder) -> {
            final Set<String> names = new HashSet<>();
            names.add("[<new group>]");
            names.addAll(Cfg.propertyGroups.keySet());
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

    /** Generates the valid background block suggestion provider. */
    private static SuggestionProvider<CommandSource> createValidBlockSuggestion() {
        // Todo: don't filter, add
        return register("background_suggestion", (ctx, builder) ->
            ISuggestionProvider.suggest(getValidBlocks()
                .filter(s -> !s.equals("all") && !s.equals("default")), builder));
    }

    /** Generates the valid ore property suggestion provider, minus all and default. */
    private static SuggestionProvider<CommandSource> createValidPropertySuggestion() {
        // Todo: don't filter, add
        return register("property_suggestion", (ctx, builder) ->
            ISuggestionProvider.suggest(getValidProperties()
                .filter(s -> !s.equals("all") && !s.equals("default")), builder));
    }

    /** Generates the valid background block suggestion provider. */
    private static SuggestionProvider<CommandSource> createAllValidBlockSuggestion() {
        return register("all_background_suggestion", (ctx, builder) ->
            ISuggestionProvider.suggest(getValidBlocks(), builder));
    }

    /** Generates the valid ore property suggestion provider. */
    private static SuggestionProvider<CommandSource> createAllValidPropertySuggestion() {
        return register("all_property_suggestion", (ctx, builder) ->
            ISuggestionProvider.suggest(getValidProperties(), builder));
    }

    private static SuggestionProvider<CommandSource> register(String name, SuggestionProvider<ISuggestionProvider> provider) {
        return SuggestionProviders.register(osvLocation(name), provider);
    }

    private static SuggestionProvider<CommandSource> register(String name, String... suggestions) {
        return register(name, (ctx, builder) -> ISuggestionProvider.suggest(suggestions, builder));
    }

}
