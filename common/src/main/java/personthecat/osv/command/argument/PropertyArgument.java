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
import personthecat.osv.util.Group;
import personthecat.osv.util.Reference;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static personthecat.catlib.exception.Exceptions.cmdSyntax;

public class PropertyArgument implements ArgumentType<Group> {

    public static void register() {
        ArgumentTypes.register(Reference.MOD_ID + ":property_argument", PropertyArgument.class,
            new EmptyArgumentSerializer<>(PropertyArgument::new));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> ctx, final SuggestionsBuilder builder) {
        final Stream.Builder<String> suggestions = Stream.builder();
        ModRegistries.PROPERTY_GROUPS.forEach((name, entries) -> suggestions.add(name));
        ModRegistries.ORE_PRESETS.forEach((name, preset) -> suggestions.add(name));
        return SharedSuggestionProvider.suggest(suggestions.build(), builder);
    }

    @Override
    public Group parse(final StringReader reader) throws CommandSyntaxException {
        final String name = reader.readString();
        final Group group = ModRegistries.PROPERTY_GROUPS.get(name);
        if (group != null) {
            return group;
        } else if (ModRegistries.ORE_PRESETS.containsKey(name)) {
            return Group.synthetic(name);
        }
        throw cmdSyntax(reader, "No such group or preset");
    }
}
