package personthecat.osv.preset.data;

import com.mojang.serialization.*;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.preset.resolver.StateMapResolver;
import personthecat.osv.util.StateMap;

import java.util.Set;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class StateSettings implements DynamicSerializable<StateSettings> {

    @Nullable StateMap<MaterialColor> materialColor;
    @Nullable StateMap<Integer> lightEmission;
    @Nullable StateMap<Set<EntityType<?>>> isValidSpawn;
    @Nullable StateMap<Boolean> isRedstoneConductor;
    @Nullable StateMap<Boolean> isSuffocating;
    @Nullable StateMap<Boolean> isViewBlocking;
    @Nullable StateMap<Boolean> hasPostProcess;
    @Nullable StateMap<Boolean> emissiveRendering;

    public static final Codec<StateSettings> CODEC = codecOf(
        nullable(StateMapResolver.COLOR_CODEC, Fields.materialColor, s -> s.materialColor),
        nullable(StateMapResolver.INTEGER_CODEC, Fields.lightEmission, s -> s.lightEmission),
        nullable(StateMapResolver.ENTITY_CODEC, Fields.isValidSpawn, s -> s.isValidSpawn),
        nullable(StateMapResolver.BOOLEAN_CODEC, Fields.isRedstoneConductor, s -> s.isRedstoneConductor),
        nullable(StateMapResolver.BOOLEAN_CODEC, Fields.isSuffocating, s -> s.isSuffocating),
        nullable(StateMapResolver.BOOLEAN_CODEC, Fields.isViewBlocking, s -> s.isViewBlocking),
        nullable(StateMapResolver.BOOLEAN_CODEC, Fields.hasPostProcess, s -> s.hasPostProcess),
        nullable(StateMapResolver.BOOLEAN_CODEC, Fields.emissiveRendering, s -> s.emissiveRendering),
        StateSettings::new
    );

    public static final StateSettings EMPTY =
        new StateSettings(null, null, null, null, null, null, null, null);

    @Override
    public Codec<StateSettings> codec() {
        return CODEC;
    }
}
