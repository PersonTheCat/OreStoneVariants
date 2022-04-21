package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;
import personthecat.osv.preset.reader.FoodPropertiesReader;
import personthecat.osv.preset.reader.StateMapReader;
import personthecat.osv.util.StateMap;

import java.util.List;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.CodecUtils.ofEnum;
import static personthecat.catlib.serialization.codec.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class ItemSettings implements DynamicSerializable<ItemSettings> {

    @Nullable Boolean isFireResistant;
    @Nullable Integer maxStackSize;
    @Nullable Rarity rarity;
    @Nullable ResourceLocation craftRemainingItem;
    @Nullable SoundEvent eatingSound;
    @Nullable FoodProperties foodProperties;
    @Nullable StateMap<String> variants;
    @Nullable StateMap<List<Component>> formatters;

    public static final Codec<ItemSettings> CODEC = codecOf(
        nullable(Codec.BOOL, Fields.isFireResistant, ItemSettings::getIsFireResistant),
        nullable(Codec.intRange(0, Integer.MAX_VALUE), Fields.maxStackSize, ItemSettings::getMaxStackSize),
        nullable(ofEnum(Rarity.class), Fields.rarity, ItemSettings::getRarity),
        nullable(ResourceLocation.CODEC, Fields.craftRemainingItem, ItemSettings::getCraftRemainingItem),
        nullable(SoundEvent.CODEC, Fields.eatingSound, ItemSettings::getEatingSound),
        nullable(FoodPropertiesReader.CODEC, Fields.foodProperties, ItemSettings::getFoodProperties),
        nullable(StateMapReader.STRING, Fields.variants, ItemSettings::getVariants),
        nullable(StateMapReader.COMPONENTS, Fields.formatters, ItemSettings::getFormatters),
        ItemSettings::new
    );

    public static final ItemSettings EMPTY = new ItemSettings(false, null, null,  null, null, null, null, null);

    @Override
    public Codec<ItemSettings> codec() {
        return null;
    }
}
