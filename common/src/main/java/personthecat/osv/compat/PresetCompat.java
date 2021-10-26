package personthecat.osv.compat;

import lombok.extern.log4j.Log4j2;
import org.hjson.JsonObject;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.JsonTransformer;
import personthecat.catlib.util.JsonTransformer.ObjectResolver;
import personthecat.osv.compat.transformer.OreTransformers;

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

    public static void transformOrePreset(final File file, final JsonObject preset) {
        ORE_TRANSFORMER.updateAll(preset);

        HjsonUtils.writeJson(preset, file)
            .ifErr(e -> log.info("Unable to record transformations. Ignoring... ({})", file.getName()))
            .ifOk(v -> log.info("Ore preset updated successfully! ({})", file.getName()));
    }
}
