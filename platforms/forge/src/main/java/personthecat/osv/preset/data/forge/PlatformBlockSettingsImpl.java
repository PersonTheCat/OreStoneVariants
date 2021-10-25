package personthecat.osv.preset.data.forge;

import com.mojang.serialization.Codec;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolType;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.serialization.CodecUtils;
import personthecat.catlib.serialization.FieldDescriptor;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.data.PlatformBlockSettings;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
@EqualsAndHashCode(callSuper = false)
public class PlatformBlockSettingsImpl extends PlatformBlockSettings {

    @Nullable Integer harvestLevel;
    @Nullable ToolType harvestTool;

    private static Codec<ToolType> TOOL_TYPE_CODEC = Codec.STRING.xmap(ToolType::get, ToolType::getName);

    public static final Codec<PlatformBlockSettingsImpl> CODEC = codecOf(
        FieldDescriptor.nullable(Codec.INT, Fields.harvestLevel, PlatformBlockSettingsImpl::getHarvestLevel),
        FieldDescriptor.nullable(TOOL_TYPE_CODEC, Fields.harvestTool, PlatformBlockSettingsImpl::getHarvestTool),
        PlatformBlockSettingsImpl::new
    );

    public static final PlatformBlockSettingsImpl EMPTY = new PlatformBlockSettingsImpl(null, null);

    public static Codec<PlatformBlockSettings> getCodec() {
        return CodecUtils.asParent(CODEC);
    }

    public static PlatformBlockSettings getEmpty() {
        return EMPTY;
    }

    @Override
    public Codec<PlatformBlockSettings> codec() {
        return CodecUtils.asParent(CODEC);
    }

    @Override
    public void apply(final BlockBehaviour.Properties properties, final OrePreset preset, final Block bg, final Block fg) {
        final BlockState bgState = bg.defaultBlockState();
        final BlockState fgState = fg.defaultBlockState();

        if (this.harvestLevel != null) {
            properties.harvestLevel(this.harvestLevel);
        } else if (preset.getVariant().isBgImitation()) {
            properties.harvestLevel(Math.max(bgState.getHarvestLevel(), fgState.getHarvestLevel()));
        } else {
            properties.harvestLevel(fgState.getHarvestLevel());
        }

        if (this.harvestTool != null) {
            properties.harvestTool(this.harvestTool);
        } else if (preset.getVariant().isBgImitation()) {
            properties.harvestTool(bgState.getHarvestTool());
        } else {
            properties.harvestTool(fgState.getHarvestTool());
        }
    }
}
