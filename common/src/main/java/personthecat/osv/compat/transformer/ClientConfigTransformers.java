package personthecat.osv.compat.transformer;

import org.apache.commons.lang3.tuple.Pair;
import personthecat.catlib.serialization.json.JsonTransformer;
import personthecat.catlib.serialization.json.JsonTransformer.ObjectResolver;
import xjs.core.Json;

public class ClientConfigTransformers {

    public static final ObjectResolver ROOT =
        JsonTransformer.root()
            .relocate("blocks.overlaysFromRP", "resources.overlaysFromRP")
            .relocate("blocks.autoRefresh", "resources.autoRefresh")
            .relocate("blocks.enableTransparency", "models.overlayTransparency")
            .relocate("blocks.shadeOverlays", "models.overlayShade")
            .relocate("blocks.modelScale", "models.overlayScale")
            .relocate("blocks.modelType", "models.type")
            .relocate("blocks.enableShadedTextures", "textures.shadeModifier")
            .freeze();

    public static final ObjectResolver RESOURCES =
        JsonTransformer.withPath("resources")
            .transform("overlaysFromRP", (k, v) -> Pair.of("assetsFromRP", Json.value(true)))
            .freeze();
}
