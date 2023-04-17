package personthecat.osv.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import personthecat.osv.preset.reader.ValueHeightAccessor;

@Mixin(ConstantHeight.class)
public interface ConstantHeightAccessor extends ValueHeightAccessor {

    @Accessor
    @Override
    VerticalAnchor getValue();
}
