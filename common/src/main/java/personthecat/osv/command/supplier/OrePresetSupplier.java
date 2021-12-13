package personthecat.osv.command.supplier;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.command.arguments.ArgumentDescriptor;
import personthecat.catlib.command.arguments.ArgumentSupplier;
import personthecat.osv.ModRegistries;
import personthecat.osv.util.Group;
import personthecat.osv.util.Reference;

import java.util.stream.Stream;

public class OrePresetSupplier implements ArgumentSupplier<String> {

    private static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "ore_preset_supplier");

    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS =
        SuggestionProviders.register(ID, (ctx, builder) -> {
            final Stream<String> names = ModRegistries.ORE_PRESETS.keySet().stream();
            final Stream<String> meta = Stream.of(Group.DEFAULT, Group.ALL);
            return SharedSuggestionProvider.suggest(Stream.concat(names, meta), builder);
        });

    @Override
    public ArgumentDescriptor<String> get() {
        return new ArgumentDescriptor<>(StringArgumentType.string(), SUGGESTIONS);
    }
}
