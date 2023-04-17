package personthecat.osv.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import personthecat.osv.preset.reader.CommonHeightAccessor;

@Mixin(TrapezoidHeight.class)
public interface TrapezoidHeightAccessor extends CommonHeightAccessor {

    @Accessor
    @Override
    VerticalAnchor getMinInclusive();

    @Accessor
    @Override
    VerticalAnchor getMaxInclusive();

    @Accessor
    @Override
    int getPlateau();
}
