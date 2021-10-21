package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.preset.resolver.StateMapResolver;
import personthecat.osv.util.StateMap;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class ItemSettings implements DynamicSerializable<ItemSettings> {

    @Nullable StateMap<String> variants;

    public static final Codec<ItemSettings> CODEC = codecOf(
        nullable(StateMapResolver.STRING, Fields.variants, ItemSettings::getVariants),
        ItemSettings::new
    );

    public static final ItemSettings EMPTY = new ItemSettings(null);

    @Override
    public Codec<ItemSettings> codec() {
        return null;
    }
}
