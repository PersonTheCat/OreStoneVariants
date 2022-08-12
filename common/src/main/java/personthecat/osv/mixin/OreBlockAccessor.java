package personthecat.osv.mixin;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.OreBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OreBlock.class)
public interface OreBlockAccessor {

    @Accessor
    UniformInt getXpRange();
}
