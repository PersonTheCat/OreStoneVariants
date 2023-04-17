package personthecat.osv.world.providers;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import personthecat.osv.preset.reader.WorldGenerationContextAccessor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record SeaLevelVerticalAnchor(int offset) implements VerticalAnchor {

    @Override
    public int resolveY(final WorldGenerationContext ctx) {
        return this.offset + ((WorldGenerationContextAccessor) ctx).getSeaLevel();
    }

    @Override
    public String toString() {
        return this.offset + " above sea level";
    }
}
