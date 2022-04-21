package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.CodecUtils.easyList;
import static personthecat.catlib.serialization.codec.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.codec.FieldDescriptor.field;

@Value
@FieldNameConstants
public class NestedSettings implements DynamicSerializable<NestedSettings> {

    @NotNull String type;
    double chance;
    boolean required;

    public static final Codec<NestedSettings> CODEC = codecOf(
        field(Codec.STRING, Fields.type, NestedSettings::getType),
        defaulted(Codec.DOUBLE, Fields.chance, 0.09, NestedSettings::getChance),
        defaulted(Codec.BOOL, Fields.required, false, NestedSettings::isRequired),
        NestedSettings::new
    );

    public static final Codec<List<NestedSettings>> LIST = easyList(CODEC);

    @Override
    public Codec<NestedSettings> codec() {
        return CODEC;
    }
}
