package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import personthecat.osv.preset.OrePreset;

public abstract class PlatformBlockSettings implements DynamicSerializable<PlatformBlockSettings> {

    @ExpectPlatform
    public static Codec<PlatformBlockSettings> getCodec() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static PlatformBlockSettings getEmpty() {
        throw new AssertionError();
    }

    public abstract void apply(final BlockBehaviour.Properties properties, final OrePreset preset, final Block bg, final Block fg);
}
