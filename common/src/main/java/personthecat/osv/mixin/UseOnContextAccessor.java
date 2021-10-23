package personthecat.osv.mixin;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UseOnContext.class)
public interface UseOnContextAccessor {

    @Mutable
    @Accessor
    void setLevel(final Level level);
}
