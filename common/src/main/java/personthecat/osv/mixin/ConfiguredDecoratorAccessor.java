package personthecat.osv.mixin;

import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfiguredDecorator.class)
public interface ConfiguredDecoratorAccessor<DC extends DecoratorConfiguration> {

    @Accessor
    FeatureDecorator<DC> getDecorator();
}
