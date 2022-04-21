package personthecat.osv.preset.reader;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.MaterialColor;
import personthecat.catlib.data.collections.InfinitySet;
import personthecat.catlib.data.collections.InvertibleSet;
import personthecat.catlib.registry.CommonRegistries;
import personthecat.catlib.exception.MissingElementException;
import personthecat.catlib.serialization.codec.CodecUtils;
import personthecat.catlib.serialization.codec.ValueMapCodec;
import personthecat.catlib.util.ValueLookup;
import personthecat.osv.client.texture.Modifier;
import personthecat.osv.util.StateMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static personthecat.catlib.serialization.codec.CodecUtils.*;

public class StateMapReader {

    private static final Pattern VARIANT_PATTERN = Pattern.compile("\\w+=\\w+(,\\w+=\\w+)*");

    private static final Codec<List<ResourceLocation>> IDS_CODEC = easyList(ResourceLocation.CODEC);
    public static final Codec<StateMap<Boolean>> BOOL = createCodec(CodecUtils.BOOLEAN_MAP);
    public static final Codec<StateMap<Integer>> INT = createCodec(CodecUtils.INT_MAP);
    public static final Codec<StateMap<String>> STRING = createCodec(CodecUtils.STRING_MAP);
    public static final Codec<StateMap<List<ResourceLocation>>> IDS = createCodec(mapOf(IDS_CODEC));

    public static final Codec<StateMap<List<Modifier>>> MODIFIERS =
        createCodec(mapOf(easyList(ofEnum(Modifier.class))));

    public static final Codec<StateMap<MaterialColor>> COLORS =
        createCodec(mapOf(ValueLookup.COLOR_CODEC));

    public static final Codec<StateMap<List<Component>>> COMPONENTS =
        createCodec(mapOf(easyList(ComponentReader.CODEC)));

    private static final Codec<Set<EntityType<?>>> ENTITY_SET = Codec.either(Codec.BOOL, IDS_CODEC)
        .xmap(StateMapReader::entitiesFromEither, StateMapReader::entitiesToEither);

    public static final Codec<StateMap<Set<EntityType<?>>>> ENTITIES = createCodec(mapOf(ENTITY_SET));

    private static <T> Codec<StateMap<T>> createCodec(final ValueMapCodec<T> tMap) {
        return Codec.either(tMap.getType(), tMap).xmap(
            either -> either.map(StateMap::all, StateMap::new),
            map -> map.isSimple() ? Either.left(map.get("")) : Either.right(map.asRaw())
        ).flatXmap(StateMapReader::validateKeys, StateMapReader::validateKeys);
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
        return either.map(b -> b ? getAllEntities() : Collections.emptySet(), StateMapReader::getEntitiesOf);
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
