package personthecat.osv.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.CreativeModeTab;

public class DenseVariantTab {

    public static final CreativeModeTab INSTANCE = createInstance();

    private DenseVariantTab() {}

    @ExpectPlatform
    private static CreativeModeTab createInstance() {
        throw new AssertionError();
    }
}
