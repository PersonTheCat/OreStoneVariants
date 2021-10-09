package personthecat.osv.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.exception.InvalidBlockEntryException;
import personthecat.osv.init.PresetLoadingContext;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.util.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockEntry {

    @EqualsAndHashCode.Exclude String raw;
    String foreground;
    String background;

    public static BlockEntry create(final String raw) throws InvalidBlockEntryException {
        final String[] split = ArrayUtils.removeAllOccurences(raw.split(",\\s*|\\s+"), "");
        if (split.length != 2) throw new InvalidBlockEntryException(raw);
        return new BlockEntry(raw, split[0], split[1]);
    }

    List<VariantDescriptor> resolve() {
        final Group oreGroup = ModRegistries.PROPERTY_GROUPS.getOptional(this.foreground)
            .orElseGet(() -> new Group(this.foreground, this.foreground));
        final Group blockGroup = ModRegistries.BLOCK_GROUPS.getOptional(this.background)
            .orElseGet(() -> new Group(this.background, this.background));

        final List<VariantDescriptor> descriptors = new ArrayList<>();
        for (final String path : oreGroup.getEntries()) {
            final Optional<OrePreset> ore = PresetLoadingContext.loadOre(path);
            if (!ore.isPresent()) continue;

            for (final ResourceLocation id : blockGroup.ids()) {
                descriptors.add(new VariantDescriptor(path, ore.get(), id));
            }
        }
        return descriptors;
    }

    @Override
    public String toString() {
        return "BlockEntry{ " + this.foreground + " -> " + this.background + " }";
    }
}
