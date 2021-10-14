package personthecat.osv.preset.data;

import architectury_inject_CatLib_common_ff3189371b5e4d619e34f5cb2202876a.PlatformMethods;
import com.mojang.serialization.Codec;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@FieldNameConstants
public class OreSettings {

    VariantSettings variant;
    BlockSettings block;
    StateSettings state;
    PlatformBlockSettings platform;
    DropSettings loot;
    GenerationSettings gen;
    RecipeSettings recipe;
    TextureSettings texture;
    ModelSettings model;

    public static final Codec<OreSettings> CODEC = codecOf(
        defaulted(VariantSettings.CODEC, Fields.variant, VariantSettings.EMPTY, OreSettings::getVariant),
        defaulted(BlockSettings.CODEC, Fields.block, BlockSettings.EMPTY, OreSettings::getBlock),
        defaulted(StateSettings.CODEC, Fields.state, StateSettings.EMPTY, OreSettings::getState),
        defaulted(PlatformBlockSettings.getCodec(), PlatformMethods.getCurrentTarget(), PlatformBlockSettings.getEmpty(), OreSettings::getPlatform),
        defaulted(DropSettings.CODEC, Fields.loot, DropSettings.EMPTY, OreSettings::getLoot),
        defaulted(GenerationSettings.CODEC, Fields.gen, GenerationSettings.EMPTY, OreSettings::getGen),
        defaulted(RecipeSettings.CODEC, Fields.recipe, RecipeSettings.EMPTY, OreSettings::getRecipe),
        defaulted(TextureSettings.CODEC, Fields.texture, TextureSettings.EMPTY, OreSettings::getTexture),
        defaulted(ModelSettings.CODEC, Fields.model, ModelSettings.EMPTY, OreSettings::getModel),
        OreSettings::new
    );

    public static OreSettings forBlock(final ResourceLocation id) {
        return new OreSettings(VariantSettings.withOriginal(id), BlockSettings.EMPTY, StateSettings.EMPTY,
            PlatformBlockSettings.getEmpty(), DropSettings.EMPTY, GenerationSettings.EMPTY, RecipeSettings.EMPTY,
            TextureSettings.EMPTY, ModelSettings.EMPTY);
    }
}
