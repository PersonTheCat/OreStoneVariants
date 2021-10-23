package personthecat.osv.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.OreVariant;
import personthecat.osv.preset.OrePreset;

import java.util.Collections;
import java.util.Map;

import static personthecat.osv.block.AdditionalProperties.DENSE;

public class VariantItem extends BlockItem {

    private final BlockState state;
    private final OrePreset preset;
    private final Map<Property<?>, Comparable<?>> diffs;

    public VariantItem(final BlockState state, final Properties properties) {
        super(state.getBlock(), properties);
        this.state = state;
        this.preset = ((OreVariant) state.getBlock()).getPreset();
        this.diffs = calculateDiffs(state);
    }

    public Block getBg() {
        return ((OreVariant) this.getBlock()).getBg();
    }

    public Block getFg() {
        return ((OreVariant) this.getBlock()).getFg();
    }

    public BlockState getState() {
        return this.state;
    }

    @Override
    public SoundEvent getEatingSound() {
        return this.preset.getItem().getEatingSound();
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(final BlockPlaceContext ctx) {
        return this.applyDiffs(super.getPlacementState(ctx));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private BlockState applyDiffs(BlockState state) {
        for (final Map.Entry<Property<?>, Comparable<?>> entry : this.diffs.entrySet()) {
            state = state.setValue((Property) entry.getKey(), (Comparable) entry.getValue());
        }
        return state;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<Property<?>, Comparable<?>> calculateDiffs(final BlockState state) {
        final BlockState defaultState = state.getBlock().defaultBlockState();
        if (defaultState == state) return Collections.emptyMap();

        final ImmutableMap.Builder<Property, Comparable> builder = ImmutableMap.builder();
        for (final Property property : state.getProperties()) {
            final Comparable stateValue = state.getValue(property);
            final Comparable defaultValue = defaultState.getValue(property);
            if (stateValue != defaultValue) {
                builder.put(property, stateValue);
            }
        }
        return (Map<Property<?>, Comparable<?>>) (Object) builder.build();
    }
}
