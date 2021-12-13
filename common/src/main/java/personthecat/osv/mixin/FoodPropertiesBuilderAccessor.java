package personthecat.osv.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(FoodProperties.Builder.class)
public interface FoodPropertiesBuilderAccessor {

    @Accessor
    int getNutrition();

    @Accessor
    void setNutrition(final int nutrition);

    @Accessor
    float getSaturationModifier();

    @Accessor
    void setSaturationModifier(final float modifier);

    @Accessor
    boolean getIsMeat();

    @Accessor
    void setIsMeat(final boolean b);

    @Accessor
    boolean getCanAlwaysEat();

    @Accessor
    void setCanAlwaysEat(final boolean b);

    @Accessor
    boolean getFastFood();

    @Accessor
    void setFastFood(final boolean b);

    @Accessor
    List<Pair<MobEffectInstance, Float>> getEffects();

    @Mutable
    @Accessor
    void setEffects(final List<Pair<MobEffectInstance, Float>> effects);
}
