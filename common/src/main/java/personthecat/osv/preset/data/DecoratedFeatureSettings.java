package personthecat.osv.preset.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.Builder;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Value;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.serialization.CodecUtils;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.world.MappedFeature;
import personthecat.osv.world.decorator.DecoratorProvider;
import personthecat.osv.world.feature.FeatureProvider;

import java.util.Collections;
import java.util.List;

@Value
@Builder
@FieldNameConstants
public class DecoratedFeatureSettings<FS extends FeatureProvider<?>, DS extends DecoratorProvider<?>> {

    Type type;
    FS config;
    DS decorator;
    double denseRatio;
    @Exclude BiomePredicate biomes;
    @With @Nullable List<NestedSettings> nested;

    public static final Codec<DecoratedFeatureSettings<?, ?>> CODEC = new FeatureCodec();

    public DecoratedFeatureSettings<FS, DS> withDefaultContainers(final List<NestedSettings> containers) {
        return this.nested == null ? this.withNested(containers) : this;
    }

    public MappedFeature createOreFeature(final OrePreset preset) {
        final ConfiguredFeature<?, ?> feature = this.config.createOreFeature(preset, this);
        return new MappedFeature(this.biomes, this.decorator.decorate(feature));
    }

    public MappedFeature createStoneFeature(final StonePreset preset) {
        final ConfiguredFeature<?, ?> feature = this.config.createStoneFeature(preset, this);
        return new MappedFeature(this.biomes, this.decorator.decorate(feature));
    }

    public enum Type {
        CLUSTER(ClusterSettings.CODEC, FlexibleDecoratorSettings.CODEC);

        private static final Codec<Type> CODEC = CodecUtils.ofEnum(Type.class);
        private final Codec<FeatureProvider<?>> feature;
        private final Codec<DecoratorProvider<?>> decorator;

        Type(final Codec<? extends FeatureProvider<?>> feature, Codec<? extends DecoratorProvider<?>> decorator) {
            this.feature = CodecUtils.asParent(feature);
            this.decorator = CodecUtils.asParent(decorator);
        }
    }

    private static class FeatureCodec implements Codec<DecoratedFeatureSettings<?, ?>> {

        @Override
        public <T> DataResult<Pair<DecoratedFeatureSettings<?, ?>, T>> decode(final DynamicOps<T> ops, final T input) {
            return CodecUtils.easyReader(ops, input).runPaired(ctx -> {
                final Type type = ctx.read(Type.CODEC, Fields.type, () -> Type.CLUSTER);
                return builder().type(type)
                    .config(ctx.read(type.feature, Fields.config, () -> ctx.readThis(type.feature)))
                    .decorator(ctx.read(type.decorator, Fields.decorator, () -> ctx.readThis(type.decorator)))
                    .denseRatio(ctx.readDouble(Fields.denseRatio, Cfg::denseChance))
                    .biomes(ctx.read(BiomePredicate.CODEC, Fields.biomes, () -> BiomePredicate.builder().names(Collections.emptyList()).mods(Collections.emptyList()).types(Collections.emptyList()).build()))
                    .nested(ctx.read(NestedSettings.LIST, Fields.nested, () -> null))
                    .build();
            });
        }

        @Override
        public <T> DataResult<T> encode(final DecoratedFeatureSettings<?, ?> input, final DynamicOps<T> ops, final T prefix) {
            return ops.mapBuilder()
                .add(Fields.type, ops.createString(input.type.name()))
                .add(Fields.config, input.type.feature.encodeStart(ops, input.config))
                .add(Fields.decorator, input.type.decorator.encodeStart(ops, input.decorator))
                .add(Fields.denseRatio, ops.createDouble(input.denseRatio))
                .add(Fields.biomes, BiomePredicate.CODEC.encodeStart(ops, input.biomes))
                .add(Fields.nested, NestedSettings.LIST.encodeStart(ops, input.nested))
                .build(prefix);
        }
    }
}
