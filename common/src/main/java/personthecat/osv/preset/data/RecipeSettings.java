package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class RecipeSettings implements DynamicSerializable<RecipeSettings> {

    @Nullable ResourceLocation item;
    @Nullable String group;
    @Nullable Integer time;
    @Nullable Float xp;

    public static final Codec<RecipeSettings> CODEC = codecOf(
        nullable(ResourceLocation.CODEC, Fields.item, RecipeSettings::getItem),
        nullable(Codec.STRING, Fields.group, RecipeSettings::getGroup),
        nullable(Codec.INT, Fields.time, RecipeSettings::getTime),
        nullable(Codec.FLOAT, Fields.xp, RecipeSettings::getXp),
        RecipeSettings::new
    );

    public static final RecipeSettings EMPTY = new RecipeSettings(null, null, null, null);

    @Override
    public Codec<RecipeSettings> codec() {
        return CODEC;
    }
}
