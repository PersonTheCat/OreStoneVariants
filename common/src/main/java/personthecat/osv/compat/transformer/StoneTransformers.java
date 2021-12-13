package personthecat.osv.compat.transformer;

import personthecat.catlib.util.JsonTransformer;
import personthecat.catlib.util.JsonTransformer.ObjectResolver;

public class StoneTransformers {
    public static final ObjectResolver ROOT =
        JsonTransformer.root()
            .relocate("block.location", "stone")
            .freeze();
}
