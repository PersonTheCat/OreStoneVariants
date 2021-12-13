package personthecat.osv.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.CreativeModeTab;

public class VariantTab {

    public static final CreativeModeTab INSTANCE = createInstance();

    private VariantTab() {}

    @ExpectPlatform
    private static CreativeModeTab createInstance() {
        throw new AssertionError();
    }
}
