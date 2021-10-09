package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.McUtils;
import personthecat.osv.config.Cfg;
import personthecat.osv.preset.resolver.StateMapResolver;
import personthecat.osv.util.StateMap;

import java.util.List;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaultGet;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;
import static personthecat.catlib.serialization.CodecUtils.ofEnum;

@Value
@FieldNameConstants
public class ModelSettings implements DynamicSerializable<ModelSettings> {

    Type type;
    double scale;
    boolean shade;
    @Nullable StateMap<List<ResourceLocation>> original;

    public static final Codec<ModelSettings> CODEC = codecOf(
        defaultGet(Type.CODEC, Fields.type, Cfg::modelType, ModelSettings::getType),
        defaultGet(Codec.DOUBLE, Fields.scale, Cfg::getModelScale, ModelSettings::getScale),
        defaultGet(Codec.BOOL, Fields.shade, Cfg::shadeOverlays, ModelSettings::isShade),
        nullable(StateMapResolver.ID_CODEC, Fields.original, ModelSettings::getOriginal),
        ModelSettings::new
    );

    public static final ModelSettings EMPTY =
        new ModelSettings(Cfg.modelType(), Cfg.getModelScale(), Cfg.shadeOverlays(), null);

    public double getOverlaySize() {
        if (Cfg.useOptifineHack() && McUtils.isModLoaded("optifine")) {
            return this.scale + ((this.scale - 1.0) * 2.0);
        }
        return this.scale;
    }

    @Override
    public Codec<ModelSettings> codec() {
        return CODEC;
    }

    public enum Type {
        SINGLE,
        OVERLAY;

        public static final Codec<Type> CODEC = ofEnum(Type.class);
    }
}
