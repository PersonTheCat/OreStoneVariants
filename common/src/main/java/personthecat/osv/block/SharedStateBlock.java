package personthecat.osv.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import personthecat.osv.mixin.BlockStateBuilderAccessor;

import java.util.Map;

public class SharedStateBlock extends Block {

    private static final ThreadLocal<StateConfig> INIT_CACHE = new ThreadLocal<>();

    public SharedStateBlock(final Properties properties, final StateConfig config) {
        super(preInit(properties, config));
        INIT_CACHE.remove();
    }

    private static Properties preInit(final Properties properties, final StateConfig config) {
        INIT_CACHE.set(config);
        return properties;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        final StateConfig config = INIT_CACHE.get();
        this.copyStates(builder, config.bg);
        this.copyStates(builder, config.fg);
        builder.add(config.mixins);
    }

    private void copyStates(final StateDefinition.Builder<Block, BlockState> builder, final Block from) {
        final Map<String, Property<?>> builderProperties = ((BlockStateBuilderAccessor) builder).getProperties();
        for (final Property<?> property : from.getStateDefinition().getProperties()) {
            if (!builderProperties.containsValue(property)) {
                builder.add(property);
            }
        }
    }
}

