package personthecat.osv.compat.transformer.forge;

import personthecat.catlib.serialization.json.JsonTransformer;
import personthecat.catlib.serialization.json.JsonTransformer.ObjectResolver;

public class OreTransformersImpl {

    public static ObjectResolver createPlatform() {
        return JsonTransformer.root()
            .relocate("block.level", "forge.harvestLevel")
            .relocate("block.tool", "forge.harvestTool")
            .freeze();
    }
}
