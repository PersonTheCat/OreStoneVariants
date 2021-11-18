package personthecat.osv.compat.transformer;

import org.apache.commons.lang3.tuple.Pair;
import org.hjson.JsonValue;
import personthecat.catlib.util.JsonTransformer;
import personthecat.catlib.util.JsonTransformer.ObjectResolver;

public class CommonConfigTransformers {

    public static final ObjectResolver BLOCK_REGISTRY =
        JsonTransformer.withPath("blockRegistry")
            .history("testForDuplicates", "checkForDuplicates")
            .freeze();

    public static final ObjectResolver WORLD_GEN =
        JsonTransformer.withPath("worldGen")
            .transform("enableVanillaOres", (k, v) -> invertRename("autoDisableOres", v))
            .transform("enableVanillaStone", (k, v) -> invertRename("autoDisableStone", v))
            .freeze();

    private static Pair<String, JsonValue> invertRename(final String name, final JsonValue value) {
        return Pair.of(name, value.isBoolean() ? JsonValue.valueOf(!value.asBoolean()) : value);
    }
}
