package personthecat.osv.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.command.arguments.ArgumentDescriptor;
import personthecat.catlib.command.arguments.ArgumentSupplier;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.osv.util.Group;
import personthecat.osv.util.Reference;

import java.util.stream.Stream;

public class BackgroundSupplier implements ArgumentSupplier<String> {

    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "background_supplier");

    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS =
        SuggestionProviders.register(ID, (ctx, builder) -> {
            final Stream.Builder<String> suggestions = Stream.builder();
            CommonRegistries.BLOCKS.forEach((id, b) -> {
                if ("minecraft".equals(id.getNamespace())) suggestions.add(id.getPath());
                suggestions.add(id.toString());
            });
            suggestions.add(Group.DEFAULT);
            suggestions.add(Group.ALL);
            return SharedSuggestionProvider.suggest(suggestions.build(), builder);
        });

    @Override
    public ArgumentDescriptor<String> get() {
        return new ArgumentDescriptor<>(StringArgumentType.string(), SUGGESTIONS);
    }
}
