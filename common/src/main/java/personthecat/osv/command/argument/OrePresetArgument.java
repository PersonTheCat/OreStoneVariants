package personthecat.osv.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import personthecat.osv.ModRegistries;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.util.Reference;

import java.util.concurrent.CompletableFuture;

import static personthecat.catlib.exception.Exceptions.cmdSyntax;

public class OrePresetArgument implements ArgumentType<OrePreset> {

    public static void register() {
        ArgumentTypes.register(Reference.MOD_ID + ":ore_preset_argument", OrePresetArgument.class,
            new EmptyArgumentSerializer<>(OrePresetArgument::new));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ModRegistries.ORE_PRESETS.keySet(), builder);
    }

    @Override
    public OrePreset parse(final StringReader reader) throws CommandSyntaxException {
        final OrePreset preset = ModRegistries.ORE_PRESETS.get(reader.readString());
        if (preset == null) throw cmdSyntax(reader, "No such preset");
        return preset;
    }
}
