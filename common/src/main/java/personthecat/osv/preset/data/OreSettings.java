package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.util.McUtils;

import java.util.Collections;
import java.util.List;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@FieldNameConstants
public class OreSettings {

    VariantSettings variant;
    BlockSettings block;
    StateSettings state;
    PlatformBlockSettings platform;
    ItemSettings item;
    DropSettings loot;
    GenerationSettings gen;
    RecipeSettings recipe;
    TextureSettings texture;
    ModelSettings model;
    List<NestedSettings> nested;

    public static final Codec<OreSettings> CODEC = codecOf(
        defaulted(VariantSettings.CODEC, Fields.variant, VariantSettings.EMPTY, OreSettings::getVariant),
        defaulted(BlockSettings.CODEC, Fields.block, BlockSettings.EMPTY, OreSettings::getBlock),
        defaulted(StateSettings.CODEC, Fields.state, StateSettings.EMPTY, OreSettings::getState),
        defaulted(PlatformBlockSettings.getCodec(), McUtils.getPlatform(), PlatformBlockSettings.getEmpty(), OreSettings::getPlatform),
        defaulted(ItemSettings.CODEC, Fields.item, ItemSettings.EMPTY, OreSettings::getItem),
        defaulted(DropSettings.CODEC, Fields.loot, DropSettings.EMPTY, OreSettings::getLoot),
        defaulted(GenerationSettings.CODEC, Fields.gen, GenerationSettings.EMPTY, OreSettings::getGen),
        defaulted(RecipeSettings.CODEC, Fields.recipe, RecipeSettings.EMPTY, OreSettings::getRecipe),
        defaulted(TextureSettings.CODEC, Fields.texture, TextureSettings.EMPTY, OreSettings::getTexture),
        defaulted(ModelSettings.CODEC, Fields.model, ModelSettings.EMPTY, OreSettings::getModel),
        defaulted(NestedSettings.LIST, Fields.nested, Collections.emptyList(), OreSettings::getNested),
        OreSettings::new
    );

    public static OreSettings forBlock(final ResourceLocation id) {
        return new OreSettings(VariantSettings.withOriginal(id), BlockSettings.EMPTY, StateSettings.EMPTY,
            PlatformBlockSettings.getEmpty(), ItemSettings.EMPTY, DropSettings.EMPTY, GenerationSettings.EMPTY,
            RecipeSettings.EMPTY, TextureSettings.EMPTY, ModelSettings.EMPTY, Collections.emptyList());
    }
}
