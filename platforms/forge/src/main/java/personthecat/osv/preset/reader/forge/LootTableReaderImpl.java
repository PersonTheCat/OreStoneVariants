package personthecat.osv.preset.reader.forge;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class LootTableReaderImpl {

    private static final AtomicReference<LootTables> TABLES = new AtomicReference<>();

    @Nullable
    public static LootTable loadWithHooks(final ResourceLocation id, final JsonElement gson) {
        return ForgeHooks.loadLootTable(LootTables.GSON, id, gson, true, TABLES.get());
    }

    public static synchronized void updateTables(final LootTables tables) {
        TABLES.set(tables);
    }
}
