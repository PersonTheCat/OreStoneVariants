package personthecat.osv.compat;

import xjs.core.JsonObject;
import personthecat.catlib.serialization.json.JsonTransformer;
import personthecat.osv.compat.transformer.ClientConfigTransformers;
import personthecat.osv.compat.transformer.CommonConfigTransformers;

public class ConfigCompat {

    public static final JsonTransformer.ObjectResolver COMMON_CONFIG_TRANSFORMER =
        JsonTransformer.root()
            .include(CommonConfigTransformers.BLOCK_REGISTRY)
            .include(CommonConfigTransformers.WORLD_GEN)
            .freeze();

    public static final JsonTransformer.ObjectResolver CLIENT_CONFIG_TRANSFORMER =
        JsonTransformer.root()
            .include(ClientConfigTransformers.ROOT)
            .include(ClientConfigTransformers.RESOURCES)
            .freeze();

    public static void transformCommonConfig(final JsonObject config) {
        COMMON_CONFIG_TRANSFORMER.updateAll(config);
    }

    public static void transformClientConfig(final JsonObject config) {
        CLIENT_CONFIG_TRANSFORMER.updateAll(config);
    }
}
