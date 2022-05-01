package personthecat.osv.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import personthecat.osv.preset.reader.CommonHeightAccessor;

@Mixin(VeryBiasedToBottomHeight.class)
public interface VeryBiasedToBottomHeightAccessor extends CommonHeightAccessor {

    @Accessor
    @Override
    VerticalAnchor getMinInclusive();

    @Accessor
    @Override
    VerticalAnchor getMaxInclusive();
}
