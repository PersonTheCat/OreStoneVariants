package personthecat.osv.compat.transformer.forge;

import personthecat.catlib.util.JsonTransformer;
import personthecat.catlib.util.JsonTransformer.ObjectResolver;

public class OreTransformersImpl {

    public static ObjectResolver createPlatform() {
        return JsonTransformer.root()
            .relocate("block.level", "forge.harvestLevel")
            .relocate("block.tool", "forge.harvestTool")
            .freeze();
    }
}
