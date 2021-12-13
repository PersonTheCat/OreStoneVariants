package personthecat.osv.preset.reader;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import personthecat.catlib.util.ValueLookup;
import personthecat.osv.mixin.MaterialBuilderAccessor;

import static personthecat.catlib.serialization.CodecUtils.*;
import static personthecat.catlib.serialization.DynamicField.field;

public class MaterialReader {

    private static final Codec<Material> MATERIAL_OBJECT = dynamic(MaterialBuilder::new, MaterialBuilder::build).create(
        field(ofEnum(PushReaction.class), "pushReaction", Material::getPushReaction, (m, r) -> m.accessor.setPushReaction(r)),
        field(Codec.BOOL, "blocksMotion", Material::blocksMotion, (m, b) -> m.accessor.setBlocksMotion(b)),
        field(Codec.BOOL, "flammable", Material::isFlammable, (m, b) -> m.accessor.setFlammable(b)),
        field(Codec.BOOL, "liquid", Material::isLiquid, (m, b) -> m.accessor.setLiquid(b)),
        field(Codec.BOOL, "replaceable", Material::isReplaceable, (m, b) -> m.accessor.setReplaceable(b)),
        field(Codec.BOOL, "solid", Material::isSolid, (m, b) -> m.accessor.setSolid(b)),
        field(ValueLookup.COLOR_CODEC, "color", Material::getColor, (m, c) -> m.accessor.setColor(c)),
        field(Codec.BOOL, "solidBlocking", Material::isSolidBlocking, (m, b) -> m.accessor.setSolidBlocking(b))
    );

    public static final Codec<Material> CODEC = simpleEither(ValueLookup.MATERIAL_CODEC, MATERIAL_OBJECT)
        .withEncoder(material -> ValueLookup.serialize(material).isPresent() ? ValueLookup.MATERIAL_CODEC : MATERIAL_OBJECT);

    private static class MaterialBuilder {
        Material.Builder wrapped;
        MaterialBuilderAccessor accessor;

        @SuppressWarnings("ConstantConditions")
        MaterialBuilder() {
            this.wrapped = new Material.Builder(MaterialColor.COLOR_GRAY);
            this.accessor = (MaterialBuilderAccessor) this.wrapped;
        }

        Material build() {
            return this.wrapped.build();
        }
    }
}
