package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import personthecat.catlib.util.McUtils;
import personthecat.osv.client.model.ModelGenerator;
import personthecat.osv.client.model.OverlayModelGenerator;
import personthecat.osv.client.model.SingleLayerModelGenerator;
import personthecat.osv.config.Cfg;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaultGet;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;
import static personthecat.catlib.serialization.CodecUtils.ofEnum;

@Value
@FieldNameConstants
public class ModelSettings implements DynamicSerializable<ModelSettings> {

    Type type;
    boolean shade;

    public static final Codec<ModelSettings> CODEC = codecOf(
        defaultGet(Type.CODEC, Fields.type, Cfg::modelType, ModelSettings::getType),
        defaultGet(Codec.BOOL, Fields.shade, Cfg::shadeOverlays, ModelSettings::isShade),
        ModelSettings::new
    );

    public static final ModelSettings EMPTY = new ModelSettings(Cfg.modelType(), Cfg.shadeOverlays());

    @Override
    public Codec<ModelSettings> codec() {
        return CODEC;
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        SINGLE(new SingleLayerModelGenerator()),
        OVERLAY(new OverlayModelGenerator());

        ModelGenerator generator;

        public static final Codec<Type> CODEC = ofEnum(Type.class);
    }
}
