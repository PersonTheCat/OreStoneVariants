package personthecat.osv.compat;

import lombok.extern.log4j.Log4j2;
import org.hjson.JsonObject;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.JsonTransformer;
import personthecat.catlib.util.JsonTransformer.ObjectResolver;
import personthecat.osv.compat.transformer.OreTransformers;
import personthecat.osv.compat.transformer.StoneTransformers;

import java.io.File;

@Log4j2
public class PresetCompat {

    public static final ObjectResolver ORE_TRANSFORMER =
        JsonTransformer.root()
            .include(OreTransformers.ROOT)
            .include(OreTransformers.VARIANT)
            .include(OreTransformers.BLOCK)
            .include(OreTransformers.PLATFORM)
            .include(OreTransformers.STATE)
            .freeze();

    public static final ObjectResolver STONE_TRANSFORMER =
        JsonTransformer.root()
            .include(StoneTransformers.ROOT)
            .include(OreTransformers.GEN)
            .freeze();

    public static void transformOrePreset(final File file, final JsonObject preset) {
        ORE_TRANSFORMER.updateAll(preset);

        HjsonUtils.writeJson(preset, file)
            .ifErr(e -> log.warn("Unable to record transformations. Ignoring... ({})", file.getName()))
            .ifOk(v -> log.debug("Ore preset updated successfully! ({})", file.getName()));
    }

    public static void transformStonePreset(final File file, final JsonObject preset) {
        STONE_TRANSFORMER.updateAll(preset);

        HjsonUtils.writeJson(preset, file)
            .ifErr(e -> log.warn("Unable to record transformations. Ignoring... ({})", file.getName()))
            .ifOk(v -> log.debug("Stone preset updated successfully! ({})", file.getName()));
    }
}
