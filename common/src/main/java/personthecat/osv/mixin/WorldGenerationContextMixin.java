package personthecat.osv.mixin;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import personthecat.osv.preset.reader.WorldGenerationContextAccessor;

@Mixin(WorldGenerationContext.class)
public class WorldGenerationContextMixin implements WorldGenerationContextAccessor {

    private int osvInjectedSeaLevel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectValues(
            final ChunkGenerator chunk, final LevelHeightAccessor height, final CallbackInfo ci) {
        this.osvInjectedSeaLevel = chunk.getSeaLevel();
    }

    @Override
    public int getSeaLevel() {
        return this.osvInjectedSeaLevel;
    }
}
