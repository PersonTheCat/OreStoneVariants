package personthecat.osv.preset.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import lombok.Value;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

@Value
public class DropSettings implements DynamicSerializable<DropSettings> {

    @Nullable Either<ResourceLocation, Dynamic<?>> value;

    public static final Codec<DropSettings> CODEC =
        Codec.either(ResourceLocation.CODEC, Codec.PASSTHROUGH).xmap(DropSettings::new, s -> s.value);

    public static final DropSettings EMPTY = new DropSettings(null);

    @Override
    public Codec<DropSettings> codec() {
        return CODEC;
    }
}
