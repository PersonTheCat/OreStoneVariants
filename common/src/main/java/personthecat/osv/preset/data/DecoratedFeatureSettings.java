package personthecat.osv.preset.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.Builder;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.serialization.CodecUtils;
import personthecat.osv.config.Cfg;
import personthecat.osv.world.decorator.DecoratorProvider;
import personthecat.osv.world.feature.FeatureProvider;

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
    @Nullable List<NestedSettings> containers;

    public static final Codec<DecoratedFeatureSettings<?, ?>> CODEC = new FeatureCodec();

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
                    .biomes(ctx.read(BiomePredicate.CODEC, Fields.biomes, () -> BiomePredicate.ALL_BIOMES))
                    .containers(ctx.read(NestedSettings.LIST, Fields.containers, () -> null))
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
                .add(Fields.containers, NestedSettings.LIST.encodeStart(ops, input.containers))
                .build(prefix);
        }
    }
}