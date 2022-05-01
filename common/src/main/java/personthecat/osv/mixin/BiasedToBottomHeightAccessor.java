package personthecat.osv.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import personthecat.osv.preset.reader.CommonHeightAccessor;

@Mixin(BiasedToBottomHeight.class)
public interface BiasedToBottomHeightAccessor extends CommonHeightAccessor {

    @Accessor
    @Override
    VerticalAnchor getMinInclusive();

    @Accessor
    @Override
    VerticalAnchor getMaxInclusive();
}
