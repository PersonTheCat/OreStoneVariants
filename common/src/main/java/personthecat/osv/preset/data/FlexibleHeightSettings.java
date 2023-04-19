package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Encoder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.VerticalAnchor.AboveBottom;
import net.minecraft.world.level.levelgen.VerticalAnchor.Absolute;
import net.minecraft.world.level.levelgen.VerticalAnchor.BelowTop;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import personthecat.catlib.data.Range;
import personthecat.catlib.serialization.codec.SimpleAnyCodec;
import personthecat.catlib.serialization.codec.SimpleEitherCodec;
import personthecat.osv.preset.reader.CommonHeightAccessor;
import personthecat.osv.world.providers.SeaLevelVerticalAnchor;
import personthecat.osv.world.providers.SimpleHeight;

import java.util.List;

import static personthecat.catlib.serialization.codec.CodecUtils.easyList;

@Value
@FieldNameConstants
public class FlexibleHeightSettings {

    public static final String BOTTOM = "bottom";
    public static final String TOP = "top";
    public static final String SEA = "sea";
    public static final String ABSOLUTE = "absolute";

    private static final VerticalAnchor NEVER = VerticalAnchor.aboveBottom(Integer.MIN_VALUE);

    private static final Codec<FlexibleHeightSettings> SINGLE_NAME_DECODER =
        NamedOffset.CODEC.xmap(
            NamedOffset::toSimpleHeight, FlexibleHeightSettings::toNamedOffsetUnreachable);

    private static final Codec<FlexibleHeightSettings> MULTI_NAME_DECODER =
        NamedOffset.CODEC.listOf().xmap(
            FlexibleHeightSettings::fromNamedOffsets, FlexibleHeightSettings::toNamedOffsets);

    private static final Encoder<FlexibleHeightSettings> ABSOLUTE_RANGE_ENCODER =
        Range.CODEC.comap(FlexibleHeightSettings::toAbsoluteRange);

    private static final Encoder<FlexibleHeightSettings> AUTO_LIST_ENCODER =
        easyList(NamedOffset.CODEC).xmap(
            FlexibleHeightSettings::fromNamedOffsets, FlexibleHeightSettings::toNamedOffsets);

    public static final Codec<FlexibleHeightSettings> CODEC =
        new SimpleEitherCodec<>(SINGLE_NAME_DECODER, MULTI_NAME_DECODER)
            .withEncoder(height -> height.isAbsoluteRange() ? ABSOLUTE_RANGE_ENCODER : AUTO_LIST_ENCODER);

    public static final Codec<HeightProvider> HEIGHT_PROVIDER_CODEC =
        CODEC.xmap(FlexibleHeightSettings::toHeightProvider, FlexibleHeightSettings::fromHeightProvider);

    VerticalAnchor min;
    VerticalAnchor max;

    public static boolean isSupportedProvider(final HeightProvider provider) {
        return provider instanceof ConstantHeight
            || provider instanceof UniformHeight
            || provider instanceof TrapezoidHeight
            || provider instanceof BiasedToBottomHeight
            || provider instanceof VeryBiasedToBottomHeight
            || provider instanceof SimpleHeight;
    }

    public static FlexibleHeightSettings fromNamedOffsets(final List<NamedOffset> offsets) {
        if (offsets.isEmpty()) {
            return new FlexibleHeightSettings(NEVER, NEVER);
        } else if (offsets.size() == 1) {
            return offsets.get(0).toSimpleHeight();
        }
        return new FlexibleHeightSettings(
            offsets.get(0).getMinAnchor(),
            offsets.get(offsets.size() - 1).getMaxAnchor());
    }

    private NamedOffset toNamedOffsetUnreachable() { // should be unreachable
        throw new UnsupportedOperationException("loss of data");
    }

    public List<NamedOffset> toNamedOffsets() {
        if (this.min.equals(NEVER) && this.max.equals(NEVER)) {
            return List.of();
        }
        final String minType = typeOf(this.min);
        final int min = valueOf(this.min);
        final String maxType = typeOf(this.max);
        final int max = valueOf(this.max);
        if (minType.equals(maxType)) {
            return List.of(new NamedOffset(minType, Range.of(min, max)));
        }
        return List.of(
            new NamedOffset(minType, Range.of(min)),
            new NamedOffset(maxType, Range.of(max)));
    }

    public static FlexibleHeightSettings fromHeightProvider(final HeightProvider provider) {
        if (provider instanceof CommonHeightAccessor a) {
            return new FlexibleHeightSettings(a.getMinInclusive(), a.getMaxInclusive());
        }
        return new FlexibleHeightSettings(NEVER, NEVER);
    }

    public HeightProvider toHeightProvider() {
        if (this.min instanceof Absolute aMin && this.max instanceof Absolute aMax) {
            return new SimpleHeight(aMin.y(), aMax.y());
        }
        return UniformHeight.of(this.min, this.max); // FlexiblePlacement handles trapezoid height and bias
    }

    public boolean isAbsoluteRange() {
        return this.min instanceof Absolute && this.max instanceof Absolute;
    }

    public Range toAbsoluteRange() {
        if (this.min instanceof Absolute aMin && this.max instanceof Absolute aMax) {
            return new Range(aMin.y(), aMax.y());
        }
        throw new UnsupportedOperationException("not an absolute range");
    }

    public static record NamedOffset(String type, Range y) {
        private static final Codec<NamedOffset> BOTTOM_CODEC = codecForOffsetType(BOTTOM);
        private static final Codec<NamedOffset> TOP_CODEC = codecForTop();
        private static final Codec<NamedOffset> ABSOLUTE_CODEC = codecForOffsetType(ABSOLUTE);
        private static final Codec<NamedOffset> SEA_CODEC = codecForOffsetType(SEA);

        public static final Codec<NamedOffset> CODEC =
            new SimpleAnyCodec<>(BOTTOM_CODEC, TOP_CODEC, ABSOLUTE_CODEC, SEA_CODEC)
                .withEncoder(NamedOffset::getEncoder);

        private static Codec<NamedOffset> codecForOffsetType(final String type) {
            return Range.CODEC.fieldOf(type).xmap(r -> new NamedOffset(type, r), NamedOffset::y).codec();
        }

        private static Codec<NamedOffset> codecForTop() { // translates from above top to below top
            return Range.CODEC.fieldOf(TOP).xmap(r -> new NamedOffset(TOP, invert(r)), o -> invert(o.y)).codec();
        }

        private static Range invert(final Range range) {
            return new Range(-range.min, -range.max);
        }

        public FlexibleHeightSettings toSimpleHeight() {
            final VerticalAnchor min = anchorOf(this.type, this.y.min);
            if (this.y.diff() == 0) {
                return new FlexibleHeightSettings(min, min);
            }
            return new FlexibleHeightSettings(min, anchorOf(this.type, this.y.max));
        }

        public VerticalAnchor getMinAnchor() {
            return anchorOf(this.type, this.y.min);
        }

        public VerticalAnchor getMaxAnchor() {
            return anchorOf(this.type, this.y.max);
        }

        private Encoder<NamedOffset> getEncoder() {
            return switch (this.type) {
                case BOTTOM -> BOTTOM_CODEC;
                case TOP -> TOP_CODEC;
                case ABSOLUTE -> ABSOLUTE_CODEC;
                default -> SEA_CODEC;
            };
        }
    }

    private static String typeOf(final VerticalAnchor anchor) {
        if (anchor instanceof AboveBottom) {
            return BOTTOM;
        } else if (anchor instanceof Absolute) {
            return ABSOLUTE;
        } else if (anchor instanceof BelowTop) {
            return TOP;
        }
        return SEA;
    }

    private static int valueOf(final VerticalAnchor anchor) {
        if (anchor instanceof AboveBottom a) {
            return a.offset();
        } else if (anchor instanceof Absolute a) {
            return a.y();
        } else if (anchor instanceof BelowTop a) {
            return a.offset();
        } else if (anchor instanceof SeaLevelVerticalAnchor a) {
            return a.offset();
        }
        return 0;
    }

    private static VerticalAnchor anchorOf(final String type, final int y) {
        return switch (type) {
            case BOTTOM -> VerticalAnchor.aboveBottom(y);
            case ABSOLUTE -> VerticalAnchor.absolute(y);
            case TOP -> VerticalAnchor.belowTop(y);
            default -> new SeaLevelVerticalAnchor(y);
        };
    }
}
