package personthecat.osv.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.reader.ComponentReader;
import personthecat.osv.util.StateMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VariantItem extends BlockItem {

    private final BlockState state;
    private final OrePreset preset;
    private final Map<Property<?>, Comparable<?>> diffs;
    private final Component display;

    public VariantItem(final String key, final BlockState state, final Properties properties) {
        super(state.getBlock(), properties);
        this.state = state;
        this.preset = ((OreVariant) state.getBlock()).getPreset();
        this.diffs = calculateDiffs(state);
        this.display = this.createDisplay(getFormatters(this.preset, key));
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

    private static List<Component> getFormatters(final OrePreset preset, final String key) {
        StateMap<List<Component>> formatters = preset.getItem().getFormatters();
        if (formatters == null) formatters = Cfg.getItemFormatters();
        final List<Component> components = formatters.get(key);
        return components != null ? components : Collections.emptyList();
    }

    private Component createDisplay(final List<Component> formatters) {
        final MutableComponent display = new TextComponent("");
        for (final Component component : formatters) {
            final String text = component.getString()
                .replace("{bg}", "{" + this.getBg().getDescriptionId() + "}")
                .replace("{fg}", "{" + this.getFg().getDescriptionId() + "}");
            display.append(new TextComponent(ComponentReader.translateAny(text)).withStyle(component.getStyle()));
        }
        return display;
    }

    @Override
    public Component getName(final ItemStack stack) {
        return this.display;
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
