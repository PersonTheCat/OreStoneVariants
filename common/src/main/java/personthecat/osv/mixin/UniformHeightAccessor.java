package personthecat.osv.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import personthecat.osv.preset.reader.CommonHeightAccessor;

@Mixin(UniformHeight.class)
public interface UniformHeightAccessor extends CommonHeightAccessor {

    @Accessor
    @Override
    VerticalAnchor getMinInclusive();

    @Accessor
    @Override
    VerticalAnchor getMaxInclusive();
}
