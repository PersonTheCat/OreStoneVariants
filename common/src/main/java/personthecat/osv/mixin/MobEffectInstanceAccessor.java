package personthecat.osv.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {

    @Accessor
    boolean getSplash();

    @Accessor
    void setSplash(final boolean b);

    @Accessor
    MobEffectInstance getHiddenEffect();
}
