package personthecat.osv.preset.data;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.BiomePredicate;
import personthecat.catlib.data.DimensionPredicate;
import personthecat.catlib.serialization.codec.CodecUtils;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.OrePreset;
import personthecat.osv.preset.StonePreset;
import personthecat.osv.world.MappedFeature;
import personthecat.osv.world.carver.GlobalFeatureProvider;
import personthecat.osv.world.placement.DimensionPlacementModifier;
import personthecat.osv.world.placement.PlacementProvider;
import personthecat.osv.world.feature.FeatureProvider;

import java.util.List;

@Value
@Builder
@FieldNameConstants
public class PlacedFeatureSettings<FS extends FeatureProvider<?>, DS extends PlacementProvider<?>> {

    Type type;
    FS config;
    DS placement;
    double denseRatio;
    @Exclude BiomePredicate biomes;
    @Exclude DimensionPredicate dimensions;
    @With @Nullable List<NestedSettings> nested;

    public static final Codec<PlacedFeatureSettings<?, ?>> CODEC = new FeatureCodec();

    public PlacedFeatureSettings<FS, DS> withDefaultContainers(final List<NestedSettings> containers) {
        return this.nested == null || this.nested.isEmpty() ? this.withNested(containers) : this;
    }

    public boolean isGlobal() {
        return this.config instanceof GlobalFeatureProvider;
    }

    public MappedFeature createOreFeature(final OrePreset preset) {
        final ConfiguredFeature<?, ?> feature = this.config.createOreFeature(preset, this);
        return new MappedFeature(this.biomes, this.wrap(this.placement.place(feature)));
    }

    public MappedFeature createStoneFeature(final StonePreset preset) {
        final ConfiguredFeature<?, ?> feature = this.config.createStoneFeature(preset, this);
        return new MappedFeature(this.biomes, this.wrap(this.placement.place(feature)));
    }

    private PlacedFeature wrap(final PlacedFeature feature) {
        if (this.dimensions.isEmpty()) {
            return feature;
        }
        final ImmutableList.Builder<PlacementModifier> modifiers = ImmutableList.builder();
        modifiers.add(new DimensionPlacementModifier(this.dimensions));
        modifiers.addAll(feature.placement());
        return new PlacedFeature(feature.feature(), modifiers.build());
    }

    public enum Type {
        CLUSTER(ClusterSettings.CODEC, FlexiblePlacementSettings.CODEC),
        GIANT_CLUSTER(GiantClusterSettings.CODEC, SimpleDecoratorSettings.CODEC),
        SPHERE(SphereSettings.CODEC, FlexiblePlacementSettings.CODEC),
        GIANT_SPHERE(GiantSphereSettings.CODEC, SimpleDecoratorSettings.CODEC);

        private static final Codec<Type> CODEC = CodecUtils.ofEnum(Type.class);
        private final Codec<FeatureProvider<?>> feature;
        private final Codec<PlacementProvider<?>> decorator;

        Type(final Codec<? extends FeatureProvider<?>> feature, Codec<? extends PlacementProvider<?>> decorator) {
            this.feature = CodecUtils.asParent(feature);
            this.decorator = CodecUtils.asParent(decorator);
        }
    }

    private static class FeatureCodec implements Codec<PlacedFeatureSettings<?, ?>> {

        @Override
        public <T> DataResult<Pair<PlacedFeatureSettings<?, ?>, T>> decode(final DynamicOps<T> ops, final T input) {
            return CodecUtils.easyReader(ops, input).runPaired(ctx -> {
                final Type type = ctx.read(Type.CODEC, Fields.type, () -> Type.CLUSTER);
                return builder().type(type)
                    .config(ctx.read(type.feature, Fields.config, () -> ctx.readThis(type.feature)))
                    .placement(ctx.read(type.decorator, Fields.placement, () -> ctx.readThis(type.decorator)))
                    .denseRatio(ctx.readDouble(Fields.denseRatio, Cfg::denseChance))
                    .biomes(ctx.read(BiomePredicate.CODEC, Fields.biomes, () -> BiomePredicate.builder().build()))
                    .dimensions(ctx.read(DimensionPredicate.CODEC, Fields.dimensions, () -> DimensionPredicate.builder().build()))
                    .nested(ctx.read(NestedSettings.LIST, Fields.nested, () -> null))
                    .build();
            });
        }

        @Override
        public <T> DataResult<T> encode(final PlacedFeatureSettings<?, ?> input, final DynamicOps<T> ops, final T prefix) {
            return ops.mapBuilder()
                .add(Fields.type, ops.createString(input.type.name()))
                .add(Fields.config, input.type.feature.encodeStart(ops, input.config))
                .add(Fields.placement, input.type.decorator.encodeStart(ops, input.placement))
                .add(Fields.denseRatio, ops.createDouble(input.denseRatio))
                .add(Fields.biomes, BiomePredicate.CODEC.encodeStart(ops, input.biomes))
                .add(Fields.dimensions, DimensionPredicate.CODEC.encodeStart(ops, input.dimensions))
                .add(Fields.nested, NestedSettings.LIST.encodeStart(ops, input.nested))
                .build(prefix);
        }
    }
}
