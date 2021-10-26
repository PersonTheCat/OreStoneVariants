package personthecat.osv.preset;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Dynamic;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.ParseException;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.data.Lazy;
import personthecat.catlib.data.LazyFunction;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.PathUtils;
import personthecat.fresult.Result;
import personthecat.osv.block.BlockPropertiesHelper;
import personthecat.osv.client.texture.Modifier;
import personthecat.osv.client.texture.OverlayGenerator;
import personthecat.osv.client.texture.SimpleOverlayGenerator;
import personthecat.osv.client.texture.ThresholdOverlayGenerator;
import personthecat.osv.compat.PresetCompat;
import personthecat.osv.config.Cfg;
import personthecat.osv.exception.CorruptPresetException;
import personthecat.osv.exception.InvalidPresetArgumentException;
import personthecat.osv.exception.PresetLoadException;
import personthecat.osv.exception.PresetSyntaxException;
import personthecat.osv.io.ModFolders;
import personthecat.osv.io.OsvPaths;
import personthecat.osv.item.ItemPropertiesHelper;
import personthecat.osv.preset.data.*;
import personthecat.osv.preset.reader.LootTableReader;
import personthecat.osv.preset.resolver.FeatureSettingsResolver;
import personthecat.osv.preset.resolver.RecipeResolver;
import personthecat.osv.preset.resolver.TextureResolver;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static personthecat.catlib.util.Shorthand.f;
import static personthecat.catlib.util.Shorthand.map;
import static personthecat.catlib.util.PathUtils.noExtension;

@Value
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public class OrePreset {

    OreSettings settings;
    String name;
    String mod;
    File file;
    JsonObject raw;

    @NonFinal volatile boolean updated;

    private OrePreset(final OreSettings settings, final ResourceLocation id, final File file, final JsonObject raw) {
        this.settings = settings;
        this.name = id.getPath();
        this.mod = id.getNamespace();
        this.file = file;
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

    Lazy<ResourceLocation> backgroundTexture = Lazy.of(() -> {
        final ResourceLocation background = this.getTexture().getBackground();
        if (background == null) {
            this.updated = true;
            return TextureResolver.resolveBackground(this.getBackgroundIds());
        }
        return background;
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
        this.getBackgroundIds().mapTo(ids -> map(ids, PathUtils::asTexturePath))
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
        this.getOverlayIds().mapTo(ids -> map(ids, PathUtils::asTexturePath))
    );

    Lazy<StateMap<List<Modifier>>> overlayModifiers = Lazy.of(() -> {
        final StateMap<List<Modifier>> modifiers = this.getTexture().getModifiers();
        if (modifiers != null) {
            return this.canBeDense() ? modifiers : modifiers.without("dense", "false");
        }
        return Modifier.createDefault(this.canBeDense(), this.getTexture().isShade());
    });

    Lazy<StateMap<List<ResourceLocation>>> variantModels = Lazy.of(() -> {
        final StateMap<List<ResourceLocation>> map = new StateMap<>();

        StateMap.forEachPair(this.getOverlayIds().with(this.getOverlayModifiers()), (key, ids, modifiers) -> {
            final List<ResourceLocation> modified = new ArrayList<>();
            final String affix = Modifier.format(modifiers);

            for (final ResourceLocation id : ids) {
                final String path = affix.isEmpty() ? id.getPath() : id.getPath() + "_" + affix;
                modified.add(new ResourceLocation(id.getNamespace(), path));
            }
            map.put(key, modified);
        });
        return map;
    });

    Lazy<ResourceLocation> primaryModel = Lazy.of(() -> {
        final StateMap<List<ResourceLocation>> models = this.getVariantModels();

        final List<ResourceLocation> def = models.get("");
        if (def != null && !def.isEmpty()) {
            return def.get(0);
        }

        for (final List<ResourceLocation> list : models.values()) {
            if (!list.isEmpty()) {
                return list.get(0);
            }
        }
        return this.getOreId();
    });

    Lazy<OverlayGenerator> overlayGenerator = Lazy.of(() -> {
       if (this.getTexture().getThreshold() != null) {
           return ThresholdOverlayGenerator.INSTANCE;
       }
        return SimpleOverlayGenerator.INSTANCE;
    });

    Lazy<StateMap<String>> itemVariants = Lazy.of(() -> {
        final StateMap<String> variants = this.getItem().getVariants();
        if (variants != null) {
            return this.canBeDense() ? variants : variants.without("dense", "false");
        }
        return this.canBeDense() ? StateMap.singleton("dense=true", "dense") : StateMap.empty();
    });

    Lazy<Optional<LootTable>> customLoot = Lazy.of(() -> {
        final Either<ResourceLocation, Dynamic<?>> loot = this.getLoot().getValue();
        if (loot != null && loot.right().isPresent()) {
            return LootTableReader.read(loot.right().get())
                .get(e -> LibErrorContext.registerSingle(Reference.MOD_NAME, e));
        }
        return Optional.empty();
    });

    LazyFunction<RecipeManager, RecipeSettings.Checked> checkedRecipe = LazyFunction.of(recipes -> {
        final RecipeSettings recipe = this.getRecipe();
        if (recipe.isSufficient()) {
            return recipe.checked(this.getName(), this.getOriginal().getBlock());
        }
        this.updated = true;
        return RecipeResolver.resolve(recipes, this);
    });

    Lazy<List<DecoratedFeatureSettings<?, ?>>> features = Lazy.of(() -> {
        // Todo: support containers at the root level? if so, inject them here
        final List<DecoratedFeatureSettings<?, ?>> features = this.getGen().getFeatures();
        if (features == null) {
            if (this.isCustom()) return Collections.emptyList();
            this.updated = true;
            return FeatureSettingsResolver.resolveFeatures(this.getOriginal());
        }
        return features;
    });

    public static Optional<OrePreset> fromFile(final File file) throws PresetLoadException {
        final JsonObject json = readContents(file, getContents(file));
        
        // There will be other settings through CatLib soon.
        if (Cfg.updatePresets()) {
            PresetCompat.transformOrePreset(file, json);
        }

        if (Cfg.modEnabled(readMod(json))) {
            try {
                final OreSettings settings = HjsonUtils.readThrowing(OreSettings.CODEC, json);
                final String mod = Optional.ofNullable(settings.getVariant().getOriginal())
                    .map(ResourceLocation::getNamespace).orElse(Reference.MOD_ID);

                return Optional.of(new OrePreset(settings, new ResourceLocation(mod, noExtension(file)), file, json));
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

    public static OrePreset createDynamic(final ResourceLocation id, final String name) {
        final File output = new File(f("{}/{}/{}.hjson", ModFolders.ORE_DIR, id.getNamespace(), name));
        return new OrePreset(OreSettings.forBlock(id), id, output, new JsonObject());
    }

    public boolean isCustom() {
        return Reference.MOD_ID.equals(this.getMod());
    }

    public boolean canBeDense() {
        return Cfg.denseOres() && this.getVariant().isCanBeDense();
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

    public ItemSettings getItem() {
        return this.settings.getItem();
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

    public ResourceLocation getBackgroundTexture() {
        return this.backgroundTexture.get();
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

    public StateMap<List<Modifier>> getOverlayModifiers() {
        return this.overlayModifiers.get();
    }

    public StateMap<List<ResourceLocation>> getVariantModels() {
        return this.variantModels.get();
    }

    public ResourceLocation getPrimaryModel() {
        return this.primaryModel.get();
    }

    public OverlayGenerator getOverlayGenerator() {
        return this.overlayGenerator.get();
    }

    public StateMap<String> getItemVariants() {
        return this.itemVariants.get();
    }

    public boolean hasLootId() {
        final Either<ResourceLocation, Dynamic<?>> loot = this.getLoot().getValue();
        return loot != null && loot.left().isPresent();
    }

    @Nullable
    public LootTable getCustomLoot() {
        return this.customLoot.get().orElse(null);
    }

    public RecipeSettings.Checked getCheckedRecipe(final RecipeManager recipes) {
        return this.checkedRecipe.apply(recipes);
    }

    @Nullable
    public RecipeSettings.Checked getCheckedRecipe() {
        return this.checkedRecipe.expose();
    }

    public List<DecoratedFeatureSettings<?, ?>> getFeatures() {
        return this.features.get();
    }

    public BlockBehaviour.Properties generateBehavior(final Block bg, final Block fg) {
        return BlockPropertiesHelper.merge(this, bg, fg);
    }

    public Item.Properties generateBehavior(final Item fg, final BlockState state) {
        return ItemPropertiesHelper.create(this, fg, state);
    }
}
