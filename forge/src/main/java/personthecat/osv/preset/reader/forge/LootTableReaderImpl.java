package personthecat.osv.preset.reader.forge;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.common.ForgeHooks;
import personthecat.osv.mixin.forge.LootTablesAccessor;

import java.util.concurrent.atomic.AtomicReference;

public class LootTableReaderImpl {

    private static final AtomicReference<LootTables> TABLES = new AtomicReference<>();

    public static LootTable loadWithHooks(final ResourceLocation id, final JsonElement gson) {
        return ForgeHooks.loadLootTable(LootTablesAccessor.getGson(), id, gson, true, TABLES.get());
    }

    public static synchronized void updateTables(final LootTables tables) {
        TABLES.set(tables);
    }
}
