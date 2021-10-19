package personthecat.osv.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {

    @Accessor
    PathNavigation getNavigation();

    @Accessor
    void setNavigation(final PathNavigation navigation);

    @Invoker
    PathNavigation invokeCreateNavigation(final Level level);

    @Invoker
    void invokeRegisterGoals();
}
