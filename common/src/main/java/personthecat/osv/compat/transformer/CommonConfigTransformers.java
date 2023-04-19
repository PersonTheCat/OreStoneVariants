package personthecat.osv.compat.transformer;

import org.apache.commons.lang3.tuple.Pair;
import personthecat.catlib.serialization.json.JsonTransformer;
import personthecat.catlib.serialization.json.JsonTransformer.ObjectResolver;
import xjs.core.Json;
import xjs.core.JsonValue;

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
        return Pair.of(name, value.isBoolean() ? Json.value(!value.asBoolean()) : value);
    }
}
