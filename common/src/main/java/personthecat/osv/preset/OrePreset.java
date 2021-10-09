package personthecat.osv.preset;

import architectury_inject_CatLib_common_ff3189371b5e4d619e34f5cb2202876a.PlatformMethods;
import com.mojang.serialization.Codec;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.ParseException;
import personthecat.catlib.data.Lazy;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.LibStringUtils;
import personthecat.fresult.Result;
import personthecat.osv.block.BlockPropertiesHelper;
import personthecat.osv.compat.PresetCompat;
import personthecat.osv.config.Cfg;
import personthecat.osv.exception.CorruptPresetException;
import personthecat.osv.exception.InvalidPresetArgumentException;
import personthecat.osv.exception.PresetLoadException;
import personthecat.osv.exception.PresetSyntaxException;
import personthecat.osv.io.ModFolders;
import personthecat.osv.preset.data.*;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static personthecat.catlib.serialization.CodecUtils.codecOf;
import static personthecat.catlib.serialization.FieldDescriptor.defaulted;

@Value
@Builder
@FieldNameConstants
public class OrePreset {

    VariantSettings variant;
    BlockSettings block;
    StateSettings state;
    PlatformBlockSettings platform;
    DropSettings loot;
    GenerationSettings gen;
    RecipeSettings recipe;
    TextureSettings texture;
    ModelSettings model;

    Lazy<BlockState> original = Lazy.of(() -> {
        if (this.isCustom()) {
            return Blocks.ANDESITE.defaultBlockState();
        }
        final Block b = CommonRegistries.BLOCKS.lookup(this.getOreId());
        Objects.requireNonNull(b, "Unchecked ore preset");
        return b.defaultBlockState();
    });

    Lazy<ResourceLocation> oreId = Lazy.of(() -> {
        final ResourceLocation original = this.getVariant().getOriginal();
        if (original != null) return original;
        final String id = "dynamic_" + LibStringUtils.randId(8);
        return new ResourceLocation(Reference.MOD_ID, id);
    });

    Lazy<StateMap<List<String>>> backgroundPaths = Lazy.of(() -> {
        throw new UnsupportedOperationException();
    });

    Lazy<StateMap<List<String>>> overlayPaths = Lazy.of(() -> {
        throw new UnsupportedOperationException();
    });

    Lazy<List<DecoratedFeatureSettings<?, ?>>> features = Lazy.of(() -> {
        throw new UnsupportedOperationException();
    });

    public static final Codec<OrePreset> CODEC = codecOf(
        defaulted(VariantSettings.CODEC, Fields.variant, VariantSettings.EMPTY, OrePreset::getVariant),
        defaulted(BlockSettings.CODEC, Fields.block, BlockSettings.EMPTY, OrePreset::getBlock),
        defaulted(StateSettings.CODEC, Fields.state, StateSettings.EMPTY, OrePreset::getState),
        defaulted(PlatformBlockSettings.getCodec(), PlatformMethods.getCurrentTarget(), PlatformBlockSettings.getEmpty(), OrePreset::getPlatform),
        defaulted(DropSettings.CODEC, Fields.loot, DropSettings.EMPTY, OrePreset::getLoot),
        defaulted(GenerationSettings.CODEC, Fields.gen, GenerationSettings.EMPTY, OrePreset::getGen),
        defaulted(RecipeSettings.CODEC, Fields.recipe, RecipeSettings.EMPTY, OrePreset::getRecipe),
        defaulted(TextureSettings.CODEC, Fields.texture, TextureSettings.EMPTY, OrePreset::getTexture),
        defaulted(ModelSettings.CODEC, Fields.model, ModelSettings.EMPTY, OrePreset::getModel),
        OrePreset::new
    );

    public static Optional<OrePreset> fromFile(final File file) throws PresetLoadException {
        final JsonObject json = readContents(file, getContents(file));

        // Todo: let the user determine if / when to update presets.
        PresetCompat.transformOrePreset(file, json);

        if (Cfg.modEnabled(readMod(json))) {
            try {
                return Optional.of(HjsonUtils.readThrowing(CODEC, json));
            } catch (final RuntimeException e) {
                throw new InvalidPresetArgumentException(ModFolders.ORE_DIR, file, e);
            }
        }
        return Optional.empty();
    }

    private static String getContents(final File file) throws CorruptPresetException {
        final Result<String, IOException> result = FileIO.readFile(file);
        final Optional<IOException> error = result.getErr();
        if (error.isPresent()) {
            throw new CorruptPresetException(ModFolders.ORE_DIR, file, error.get());
        }
        return result.unwrap();
    }

    private static JsonObject readContents(final File file, final String contents) throws PresetSyntaxException {
        final Result<JsonValue, ParseException> result = HjsonUtils.readValue(contents);
        final Optional<ParseException> error = result.getErr();
        if (error.isPresent()) {
            throw new PresetSyntaxException(ModFolders.ORE_DIR, file, contents, error.get());
        }
        return result.unwrap().asObject();
    }

    private static String readMod(final JsonObject json) {
        return HjsonUtils.getObject(json, Fields.variant)
            .flatMap(ore -> HjsonUtils.getId(ore, VariantSettings.Fields.original))
            .map(ResourceLocation::getNamespace)
            .orElse(Reference.MOD_ID);
    }

    public static OrePreset createDynamic(final ResourceLocation id) {
        return new OrePreset(VariantSettings.withOriginal(id), BlockSettings.EMPTY, StateSettings.EMPTY,
            PlatformBlockSettings.getEmpty(), DropSettings.EMPTY, GenerationSettings.EMPTY, RecipeSettings.EMPTY,
            TextureSettings.EMPTY, ModelSettings.EMPTY);
    }

    public boolean isCustom() {
        return Reference.MOD_ID.equals(this.getMod());
    }

    public String getMod() {
        final ResourceLocation id = this.getOreId();
        return id != null ? id.getNamespace(): Reference.MOD_ID;
    }

    public ResourceLocation getOreId() {
        return this.oreId.get();
    }

    public BlockState getOriginal() {
        return this.original.get();
    }

    public StateMap<List<String>> getBackgroundPaths() {
        return this.backgroundPaths.get();
    }

    public StateMap<List<String>> getOverlayPaths() {
        return this.overlayPaths.get();
    }

    public List<DecoratedFeatureSettings<?, ?>> getFeatures() {
        return this.features.get();
    }

    public BlockBehaviour.Properties generateBehavior(final Block bg, final Block fg) {
        return BlockPropertiesHelper.merge(this, bg, fg);
    }
}
