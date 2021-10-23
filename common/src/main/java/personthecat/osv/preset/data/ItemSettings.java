package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.preset.resolver.FoodPropertiesResolver;
import personthecat.osv.preset.resolver.StateMapResolver;
import personthecat.osv.util.StateMap;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.CodecUtils.ofEnum;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;
import static personthecat.catlib.serialization.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class ItemSettings implements DynamicSerializable<ItemSettings> {

    int maxStackSize;
    Rarity rarity;
    boolean isFireResistant;
    @Nullable ResourceLocation craftRemainingItem;
    @Nullable SoundEvent eatingSound;
    @Nullable FoodProperties foodProperties;
    @Nullable StateMap<String> variants;

    public static final Codec<ItemSettings> CODEC = codecOf(
        defaulted(Codec.intRange(0, Integer.MAX_VALUE), Fields.maxStackSize, 64, ItemSettings::getMaxStackSize),
        defaulted(ofEnum(Rarity.class), Fields.rarity, Rarity.COMMON, ItemSettings::getRarity),
        defaulted(Codec.BOOL, Fields.isFireResistant, false, ItemSettings::isFireResistant),
        nullable(ResourceLocation.CODEC, Fields.craftRemainingItem, ItemSettings::getCraftRemainingItem),
        nullable(SoundEvent.CODEC, Fields.eatingSound, ItemSettings::getEatingSound),
        nullable(FoodPropertiesResolver.CODEC, Fields.foodProperties, ItemSettings::getFoodProperties),
        nullable(StateMapResolver.STRING, Fields.variants, ItemSettings::getVariants),
        ItemSettings::new
    );

    public static final ItemSettings EMPTY = new ItemSettings(64, Rarity.COMMON, false, null, null, null, null);

    @Override
    public Codec<ItemSettings> codec() {
        return null;
    }
}
