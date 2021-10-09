package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.ValueLookup;
import personthecat.osv.preset.resolver.MaterialResolver;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class BlockSettings implements DynamicSerializable<BlockSettings> {

    @Nullable Material material;
    @Nullable Boolean hasCollision;
    @Nullable SoundType soundType;
    @Nullable Float explosionResistance;
    @Nullable Float destroyTime;
    @Nullable Boolean requiresCorrectToolForDrops;
    @Nullable Boolean isRandomlyTicking;
    @Nullable Float friction;
    @Nullable Float speedFactor;
    @Nullable Float jumpFactor;
    @Nullable Boolean canOcclude;
    @Nullable Boolean isAir;
    @Nullable Boolean dynamicShape;

    public static final Codec<BlockSettings> CODEC = codecOf(
        nullable(MaterialResolver.CODEC, Fields.material, BlockSettings::getMaterial),
        nullable(Codec.BOOL, Fields.hasCollision, BlockSettings::getHasCollision),
        nullable(ValueLookup.SOUND_CODEC, Fields.soundType, BlockSettings::getSoundType),
        nullable(Codec.FLOAT, Fields.explosionResistance, BlockSettings::getExplosionResistance),
        nullable(Codec.FLOAT, Fields.destroyTime, BlockSettings::getDestroyTime),
        nullable(Codec.BOOL, Fields.requiresCorrectToolForDrops, BlockSettings::getRequiresCorrectToolForDrops),
        nullable(Codec.BOOL, Fields.isRandomlyTicking, BlockSettings::getIsRandomlyTicking),
        nullable(Codec.FLOAT, Fields.friction, BlockSettings::getFriction),
        nullable(Codec.FLOAT, Fields.speedFactor, BlockSettings::getSpeedFactor),
        nullable(Codec.FLOAT, Fields.jumpFactor, BlockSettings::getJumpFactor),
        nullable(Codec.BOOL, Fields.canOcclude, BlockSettings::getCanOcclude),
        nullable(Codec.BOOL, Fields.isAir, BlockSettings::getIsAir),
        nullable(Codec.BOOL, Fields.dynamicShape, BlockSettings::getDynamicShape),
        BlockSettings::new
    );

    public static final BlockSettings EMPTY =
        new BlockSettings(null, null, null, null, null, null, null, null, null, null, null, null, null);

    @Override
    public Codec<BlockSettings> codec() {
        return CODEC;
    }
}
