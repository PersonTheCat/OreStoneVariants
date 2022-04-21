package personthecat.osv.init.forge;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.function.Consumer;

public class DeferredRegistryHelper {

    public static Consumer<RegistryEvent.Register<Biome>> defer(final Runnable fn) {
        return e -> {
            ((ForgeRegistry<?>) ForgeRegistries.BLOCKS).unfreeze();
            ((ForgeRegistry<?>) ForgeRegistries.ITEMS).unfreeze();
            fn.run();
            ((ForgeRegistry<?>) ForgeRegistries.BLOCKS).freeze();
            ((ForgeRegistry<?>) ForgeRegistries.ITEMS).freeze();
        };
    }
}
