package personthecat.osv.preset.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import lombok.Value;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.List;

import static personthecat.catlib.serialization.CodecUtils.easyList;

@Value
public class GenerationSettings {

    @Nullable List<DecoratedFeatureSettings<?, ?>> features;

    public static final GenerationSettings EMPTY = new GenerationSettings(null);

    public static final Codec<GenerationSettings> CODEC =
        Codec.either(Codec.unit(Unit.INSTANCE), easyList(DecoratedFeatureSettings.CODEC)).xmap(
            either -> either.map(unit -> EMPTY, GenerationSettings::new),
            gen -> gen.features == null ? Either.left(Unit.INSTANCE) : Either.right(gen.features)
        );
}
