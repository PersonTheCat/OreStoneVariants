package personthecat.osv.preset;

import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
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
import personthecat.catlib.util.PathUtilsMod;
import personthecat.fresult.Result;
import personthecat.osv.block.BlockPropertiesHelper;
import personthecat.osv.compat.PresetCompat;
import personthecat.osv.config.Cfg;
import personthecat.osv.exception.CorruptPresetException;
import personthecat.osv.exception.InvalidPresetArgumentException;
import personthecat.osv.exception.PresetLoadException;
import personthecat.osv.exception.PresetSyntaxException;
import personthecat.osv.io.ModFolders;
import personthecat.osv.io.OsvPaths;
import personthecat.osv.preset.data.*;
import personthecat.osv.preset.resolver.TextureResolver;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static personthecat.catlib.util.Shorthand.map;
import static personthecat.catlib.util.PathUtils.noExtension;

@Value
@ToString(doNotUseGetters = true)
public class OrePreset {

    OreSettings settings;
    String name;
    String mod;
    JsonObject raw;

    @NonFinal volatile boolean updated;

    private OrePreset(final OreSettings settings, final ResourceLocation id, final JsonObject raw) {
        this.settings = settings;
        this.name = id.getPath();
        this.mod = id.getNamespace();
        this.raw = raw;
    }

    Lazy<BlockState> original = Lazy.of(() -> {
        if (this.isCustom()) {
            return Blocks.ANDESITE.defaultBlockState();
        }
        final Block b = CommonRegistries.BLOCKS.lookup(this.getOreId());
        Objects.requireNonNull(b, "Unchecked ore preset");
        return b.defaultBlockState();
    });

    Lazy<ResourceLocation> oreId = Lazy.of(() -> {
        final ResourceLocation original = getVariant().getOriginal();
        return original != null ? original : new ResourceLocation(this.getMod(), this.getName());
    });

    Lazy<StateMap<List<ResourceLocation>>> backgroundIds = Lazy.of(() -> {
        final StateMap<List<ResourceLocation>> ids = this.getTexture().getOriginal();
        if (ids == null) {
            this.updated = true;
            return TextureResolver.resolveOriginals(this.getOreId());
        }
        return ids;
    });

    Lazy<StateMap<List<String>>> backgroundPaths = Lazy.of(() ->
        this.getBackgroundIds().mapTo(ids -> map(ids, PathUtilsMod::asTexturePath))
    );

    Lazy<StateMap<List<ResourceLocation>>> overlayIds = Lazy.of(() -> {
        final StateMap<List<ResourceLocation>> ids = this.getTexture().getOverlay();
        if (ids == null) {
            this.updated = true;
            return this.getBackgroundIds().mapTo(l -> map(l, OsvPaths::toOsvTextureId));
        }
        return ids;
    });

    Lazy<StateMap<List<String>>> overlayPaths = Lazy.of(() ->
        this.getOverlayIds().mapTo(ids -> map(ids, PathUtilsMod::asTexturePath))
    );

    Lazy<List<DecoratedFeatureSettings<?, ?>>> features = Lazy.of(() -> {
        throw new UnsupportedOperationException();
    });

    public static Optional<OrePreset> fromFile(final File file) throws PresetLoadException {
        final JsonObject json = readContents(file, getContents(file));

        // Todo: let the user determine if / when to update presets.
        PresetCompat.transformOrePreset(file, json);

        if (Cfg.modEnabled(readMod(json))) {
            try {
                final OreSettings settings = HjsonUtils.readThrowing(OreSettings.CODEC, json);
                final String mod = Optional.ofNullable(settings.getVariant().getOriginal())
                    .map(ResourceLocation::getNamespace).orElse(Reference.MOD_ID);

                return Optional.of(new OrePreset(settings, new ResourceLocation(mod, noExtension(file)), json));
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
        return HjsonUtils.getObject(json, OreSettings.Fields.variant)
            .flatMap(ore -> HjsonUtils.getId(ore, VariantSettings.Fields.original))
            .map(ResourceLocation::getNamespace)
            .orElse(Reference.MOD_ID);
    }

    public static OrePreset createDynamic(final ResourceLocation id) {
        return new OrePreset(OreSettings.forBlock(id), id, new JsonObject());
    }

    public boolean isCustom() {
        return Reference.MOD_ID.equals(this.getMod());
    }

    public ResourceLocation getOreId() {
        return this.oreId.get();
    }

    public BlockState getOriginal() {
        return this.original.get();
    }

    public VariantSettings getVariant() {
        return this.settings.getVariant();
    }

    public BlockSettings getBlock() {
        return this.settings.getBlock();
    }

    public StateSettings getState() {
        return this.settings.getState();
    }

    public PlatformBlockSettings getPlatform() {
        return this.settings.getPlatform();
    }

    public DropSettings getLoot() {
        return this.settings.getLoot();
    }

    public GenerationSettings getGen() {
        return this.settings.getGen();
    }

    public RecipeSettings getRecipe() {
        return this.settings.getRecipe();
    }

    public TextureSettings getTexture() {
        return this.settings.getTexture();
    }

    public ModelSettings getModel() {
        return this.settings.getModel();
    }

    public StateMap<List<ResourceLocation>> getBackgroundIds() {
        return this.backgroundIds.get();
    }

    public StateMap<List<String>> getBackgroundPaths() {
        return this.backgroundPaths.get();
    }

    public StateMap<List<ResourceLocation>> getOverlayIds() {
        return this.overlayIds.get();
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
