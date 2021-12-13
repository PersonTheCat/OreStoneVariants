package personthecat.osv.mixin;

import net.minecraft.commands.Commands;
import net.minecraft.server.ServerResources;
import net.minecraft.world.level.storage.loot.LootTables;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import personthecat.osv.preset.reader.forge.LootTableReaderImpl;

@Mixin(ServerResources.class)
public class ServerResourcesMixin {

    @Final
    @Shadow
    private LootTables lootTables;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onLoad(final Commands.CommandSelection envType, final int permissionsLevel, final CallbackInfo ci) {
        LootTableReaderImpl.updateTables(this.lootTables);
    }
}
