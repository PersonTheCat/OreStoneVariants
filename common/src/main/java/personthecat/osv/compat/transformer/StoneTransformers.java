package personthecat.osv.compat.transformer;

import personthecat.catlib.serialization.json.JsonTransformer;
import personthecat.catlib.serialization.json.JsonTransformer.ObjectResolver;

public class StoneTransformers {
    public static final ObjectResolver ROOT =
        JsonTransformer.root()
            .relocate("block.location", "stone")
            .freeze();
}
