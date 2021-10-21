package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.client.texture.Modifier;
import personthecat.osv.preset.resolver.StateMapResolver;
import personthecat.osv.util.StateMap;

import java.util.List;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class TextureSettings implements DynamicSerializable<TextureSettings> {

    ResourceLocation background;
    boolean shade;
    @Nullable Float threshold;
    @Nullable StateMap<List<ResourceLocation>> original;
    @Nullable StateMap<List<ResourceLocation>> overlay;
    @Nullable StateMap<List<Modifier>> modifiers;

    private static final ResourceLocation DEFAULT_BG = new ResourceLocation("block/stone");

    public static final Codec<TextureSettings> CODEC = codecOf(
        defaulted(ResourceLocation.CODEC, Fields.background,  DEFAULT_BG, TextureSettings::getBackground),
        defaulted(Codec.BOOL, Fields.shade, true, TextureSettings::isShade),
        nullable(Codec.FLOAT, Fields.threshold, TextureSettings::getThreshold),
        nullable(StateMapResolver.IDS, Fields.original, TextureSettings::getOriginal),
        nullable(StateMapResolver.IDS, Fields.overlay, TextureSettings::getOverlay),
        nullable(StateMapResolver.MODIFIERS, Fields.modifiers,  TextureSettings::getModifiers),
        TextureSettings::new
    );

    public static final TextureSettings EMPTY = new TextureSettings(DEFAULT_BG, true, null, null, null, null);

    @Override
    public Codec<TextureSettings> codec() {
        return CODEC;
    }
}
