package personthecat.osv.preset.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import lombok.Value;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

@Value
public class DropSettings implements DynamicSerializable<DropSettings> {

    @Nullable Either<ResourceLocation, Dynamic<?>> value;

    private static final Codec<DropSettings> DECODER =
        Codec.either(ResourceLocation.CODEC, Codec.PASSTHROUGH).xmap(DropSettings::new, s -> s.value);

    private static final Encoder<DropSettings> ENCODER = new DropSettingsEncoder();

    public static final Codec<DropSettings> CODEC = Codec.of(ENCODER, DECODER);

    public static final DropSettings EMPTY = new DropSettings(null);

    @Override
    public Codec<DropSettings> codec() {
        return CODEC;
    }

    private static class DropSettingsEncoder implements Encoder<DropSettings> {
        @Override
        public <T> DataResult<T> encode(final DropSettings input, final DynamicOps<T> ops, final T prefix) {
            return input.getValue() == null ? DataResult.success(ops.empty()) : DECODER.encode(input, ops, prefix);
        }
    };
}
