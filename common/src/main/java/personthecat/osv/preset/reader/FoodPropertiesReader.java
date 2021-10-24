package personthecat.osv.preset.reader;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import personthecat.osv.mixin.FoodPropertiesBuilderAccessor;

import java.util.List;

import static personthecat.catlib.serialization.CodecUtils.dynamic;
import static personthecat.catlib.serialization.CodecUtils.easyList;
import static personthecat.catlib.serialization.DynamicField.field;

public class FoodPropertiesReader {

    public static final Codec<FoodProperties> CODEC = dynamic(FoodPropertiesBuilder::new, FoodPropertiesBuilder::build).create(
        field(Codec.INT, "nutrition", FoodProperties::getNutrition, (f, i) -> f.accessor.setNutrition(i)),
        field(Codec.FLOAT, "saturationModifier", FoodProperties::getSaturationModifier, (f, m) -> f.accessor.setSaturationModifier(m)),
        field(Codec.BOOL, "isMeat", FoodProperties::isMeat, (f, b) -> f.accessor.setIsMeat(b)),
        field(Codec.BOOL, "canAlwaysEat", FoodProperties::canAlwaysEat, (f, b) -> f.accessor.setCanAlwaysEat(b)),
        field(Codec.BOOL, "fastFood", FoodProperties::isFastFood, (f, b) -> f.accessor.setFastFood(b)),
        field(easyList(MobEffectReader.CODEC), "effects", FoodProperties::getEffects, FoodPropertiesBuilder::addEffects)
    );

    private static class FoodPropertiesBuilder {
        FoodProperties.Builder wrapped;
        FoodPropertiesBuilderAccessor accessor;

        FoodPropertiesBuilder() {
            this.wrapped = new FoodProperties.Builder();
            this.accessor = (FoodPropertiesBuilderAccessor) this.wrapped;
        }

        // Forge modifies this field, breaking the accessor.
        void addEffects(final List<Pair<MobEffectInstance, Float>> effects) {
            effects.forEach(p -> this.wrapped.effect(p.getFirst(), p.getSecond()));
        }

        FoodProperties build() {
            return this.wrapped.build();
        }
    }
}
