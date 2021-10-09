package personthecat.osv.preset.resolver;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.MaterialColor;
import personthecat.catlib.data.InfinitySet;
import personthecat.catlib.data.InvertibleSet;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.exception.MissingElementException;
import personthecat.catlib.serialization.CodecUtils;
import personthecat.catlib.serialization.ValueMapCodec;
import personthecat.catlib.util.ValueLookup;
import personthecat.osv.util.StateMap;

import java.util.*;
import java.util.regex.Pattern;

import static personthecat.catlib.serialization.CodecUtils.mapOf;

public class StateMapResolver {

    private static final Pattern VARIANT_PATTERN = Pattern.compile("\\w+=\\w+(,\\w+=\\w+)*");

    private static final Codec<List<ResourceLocation>> IDS_CODEC = CodecUtils.easyList(ResourceLocation.CODEC);
    public static final Codec<StateMap<Boolean>> BOOLEAN_CODEC = createCodec(CodecUtils.BOOLEAN_MAP);
    public static final Codec<StateMap<Integer>> INTEGER_CODEC = createCodec(CodecUtils.INT_MAP);
    public static final Codec<StateMap<List<ResourceLocation>>> ID_CODEC = createCodec(CodecUtils.mapOf(IDS_CODEC));

    public static final Codec<StateMap<MaterialColor>> COLOR_CODEC =
        createCodec(mapOf(ValueLookup.COLOR_CODEC));

    private static final Codec<Set<EntityType<?>>> ENTITY_SET = Codec.either(Codec.BOOL, IDS_CODEC)
        .xmap(StateMapResolver::entitiesFromEither, StateMapResolver::entitiesToEither);

    public static final Codec<StateMap<Set<EntityType<?>>>> ENTITY_CODEC = createCodec(mapOf(ENTITY_SET));

    private static <T> Codec<StateMap<T>> createCodec(final ValueMapCodec<T> tMap) {
        return Codec.either(tMap.getType(), tMap).xmap(
            either -> either.map(StateMap::all, StateMap::new),
            map -> map.isSimple() ? Either.left(map.get("")) : Either.right(map.asRaw())
        ).flatXmap(StateMapResolver::validateKeys, StateMapResolver::validateKeys);
    }

    private static <T> DataResult<StateMap<T>> validateKeys(final StateMap<T> map) {
        for (final String key : map.asRaw().keySet()) {
            if (!(key.isEmpty() || VARIANT_PATTERN.matcher(key).matches())) {
                return DataResult.error("Expected k=v: " + key);
            }
        }
        return DataResult.success(map);
    }

    private static Set<EntityType<?>> entitiesFromEither(final Either<Boolean, List<ResourceLocation>> either) {
        return either.map(b -> b ? getAllEntities() : Collections.emptySet(), StateMapResolver::getEntitiesOf);
    }

    private static Set<EntityType<?>> getAllEntities() {
        final Set<EntityType<?>> all = new HashSet<>();
        CommonRegistries.ENTITIES.forEach(all::add);
        return new InfinitySet<>(all);
    }

    private static Set<EntityType<?>> getEntitiesOf(final List<ResourceLocation> ids) {
        final Set<EntityType<?>> set = new HashSet<>();
        for (final ResourceLocation id : ids) {
            final EntityType<?> entity = CommonRegistries.ENTITIES.lookup(id);
            if (entity == null) throw new MissingElementException("No such entity: " + id);
            set.add(entity);
        }
        return new InvertibleSet<>(set, false).optimize(Collections.emptySet());
    }

    private static Either<Boolean, List<ResourceLocation>> entitiesToEither(final Set<EntityType<?>> entities) {
        if (entities instanceof InfinitySet) {
            return Either.left(true);
        } else if (entities.isEmpty()) {
            return Either.left(false);
        }
        return Either.right(getIdsOf(entities));
    }

    private static List<ResourceLocation> getIdsOf(final Set<EntityType<?>> entities) {
        final ImmutableList.Builder<ResourceLocation> ids = ImmutableList.builder();
        for (final EntityType<?> entity : entities) {
            final ResourceLocation id = CommonRegistries.ENTITIES.getKey(entity);
            if (id == null) throw new MissingElementException("No key for entity: " + entity);
            ids.add(id);
        }
        return ids.build();
    }
}
