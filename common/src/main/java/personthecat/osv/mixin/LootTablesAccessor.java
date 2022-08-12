package personthecat.osv.mixin;

import com.google.gson.Gson;
import net.minecraft.world.level.storage.loot.LootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootTables.class)
public interface LootTablesAccessor {

    @Accessor("GSON")
    static Gson getGson() {
        throw new AssertionError();
    }
}
