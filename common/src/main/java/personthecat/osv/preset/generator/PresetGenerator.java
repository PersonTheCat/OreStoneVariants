package personthecat.osv.preset.generator;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.registry.CommonRegistries;
import personthecat.catlib.serialization.codec.CodecUtils;
import personthecat.catlib.serialization.json.XjsUtils;
import personthecat.catlib.util.ValueLookup;
import personthecat.osv.exception.PresetGenerationException;
import personthecat.osv.io.ModFolders;
import personthecat.osv.mixin.*;
import personthecat.osv.preset.data.BlockSettings;
import personthecat.osv.preset.data.ItemSettings;
import personthecat.osv.preset.data.OreSettings;
import personthecat.osv.preset.data.PlacedFeatureSettings;
import personthecat.osv.preset.data.RecipeSettings;
import personthecat.osv.preset.data.StateSettings;
import personthecat.osv.preset.data.StoneSettings;
import personthecat.osv.preset.data.TextureSettings;
import personthecat.osv.preset.data.VariantSettings;
import personthecat.osv.preset.reader.FoodPropertiesReader;
import personthecat.osv.preset.reader.MaterialReader;
import personthecat.osv.preset.reader.StateMapReader;
import personthecat.osv.preset.resolver.FeatureSettingsResolver;
import personthecat.osv.preset.resolver.RecipeResolver;
import personthecat.osv.preset.resolver.StateMapResolver;
import personthecat.osv.preset.resolver.TextureResolver;
import personthecat.osv.util.Reference;
import personthecat.osv.util.StateMap;
import xjs.core.Json;
import xjs.core.JsonObject;
import xjs.core.JsonValue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class PresetGenerator {

    private final Level level;
    private final BlockPos pos;
    private final Block block;
    private final BlockPropertiesAccessor props;
    private final boolean verbose;
    private final JsonObject preset;
    private final ResourceLocation id;

    private PresetGenerator(final Level level, final BlockPos pos, final Block block, final boolean verbose) {
        this.level = level;
        this.pos = pos;
        this.block = block;
        this.props = (BlockPropertiesAccessor) ((BlockBehaviourAccessor) block).getProperties();
        this.verbose = verbose;
        this.preset = new JsonObject();
        this.id = CommonRegistries.BLOCKS.getKey(this.block);
        Objects.requireNonNull(this.id, "Illegal block reference: not registered");
    }

    public static void generateOre(final Level level, final BlockPos pos, final BlockState state) {
        new PresetGenerator(level, pos, state.getBlock(), false)
            .generateOreSettings()
            .write(ModFolders.ORE_DIR);
    }

    public static void generateStone(final Level level, final BlockPos pos, final BlockState state) {
        new PresetGenerator(level, pos, state.getBlock(), false)
            .generateStoneSettings()
            .write(ModFolders.STONE_DIR);
    }

    public static void generateVerbose(final Level level, final BlockPos pos, final BlockState state) {
        new PresetGenerator(level, pos, state.getBlock(), true)
            .generateOreSettings()
            .write(ModFolders.ORE_DIR);
    }

    private PresetGenerator generateOreSettings() {
        this.preset.add(OreSettings.Fields.variant, this.generateVariantSettings())
            .add(OreSettings.Fields.recipe, this.generateRecipeSettings())
            .add(OreSettings.Fields.texture, this.generateTextureSettings())
            .add(OreSettings.Fields.gen, this.generateGenSettings());
        if (this.verbose) {
            this.preset.add(OreSettings.Fields.block, this.generateBlockSettings())
                .add(OreSettings.Fields.state, this.generateStateSettings())
                .add(OreSettings.Fields.item, this.generateItemSettings())
                .add(OreSettings.Fields.loot, this.generateLootSettings());
        }
        return this;
    }

    private PresetGenerator generateStoneSettings() {
        this.preset.add(StoneSettings.Fields.source, this.generateSourceSettings())
            .add(StoneSettings.Fields.gen, this.generateGenSettings());
        return this;
    }

    private JsonValue generateVariantSettings() {
        final JsonObject settings = new JsonObject();
        add(settings, VariantSettings.Fields.original, this.id::toString);
        if (this.verbose) {
            if (this.block instanceof OreBlockAccessor ore) {
                add(settings, VariantSettings.Fields.xp, () -> {
                    final UniformInt xp = ore.getXpRange();
                    return Json.array(xp.getMinValue(), xp.getMaxValue());
                });
            }
            add(settings, VariantSettings.Fields.translationKey, this.block::getDescriptionId);
        }
        return settings;
    }

    private JsonValue generateRecipeSettings() {
        final JsonObject settings = new JsonObject();
        final RecipeSettings.Checked generated =
            RecipeResolver.resolve(this.level.getRecipeManager(), this.block);
        if (generated == RecipeSettings.NONE_CHECKED) {
            return Json.value(RecipeSettings.NONE_KEY);
        }
        add(settings, RecipeSettings.Fields.result, () -> {
            final ResourceLocation id = CommonRegistries.ITEMS.getKey(generated.getResult());
            Objects.requireNonNull(id, "Illegal item reference: not registered");
            return id.toString();
        });
        return settings.add(RecipeSettings.Fields.count, generated.getCount())
            .add(RecipeSettings.Fields.time, generated.getTime())
            .add(RecipeSettings.Fields.xp, generated.getXp())
            .add(RecipeSettings.Fields.group, generated.getGroup());
    }

    private JsonValue generateTextureSettings() {
        final StateMap<List<ResourceLocation>> originals;
        try {
            originals = TextureResolver.resolveOriginals(this.id);
        } catch (final RuntimeException e) {
            return error("Unresolvable texture data", e);
        }
        final JsonObject settings = new JsonObject();
        add(settings, TextureSettings.Fields.original, () ->
            XjsUtils.writeThrowing(StateMapReader.IDS, originals));
        add(settings, TextureSettings.Fields.background, () ->
            TextureResolver.resolveBackground(originals).toString());
        return settings;
    }

    private JsonValue generateGenSettings() {
        return getValue(
            () -> FeatureSettingsResolver.resolveFeatures(this.block.defaultBlockState()),
            CodecUtils.easyList(PlacedFeatureSettings.CODEC));
    }

    private JsonValue generateBlockSettings() {
        final JsonObject settings = new JsonObject();
        add(settings, BlockSettings.Fields.material, () ->
            XjsUtils.writeThrowing(MaterialReader.CODEC, this.props.getMaterial()));
        add(settings, BlockSettings.Fields.soundType, () ->
            XjsUtils.writeThrowing(ValueLookup.SOUND_CODEC, this.props.getSoundType()));
        return settings.add(BlockSettings.Fields.hasCollision, this.props.getHasCollision())
            .add(BlockSettings.Fields.explosionResistance, this.props.getExplosionResistance())
            .add(BlockSettings.Fields.destroyTime, this.props.getDestroyTime())
            .add(BlockSettings.Fields.isRandomlyTicking, this.props.getIsRandomlyTicking())
            .add(BlockSettings.Fields.friction, this.props.getFriction())
            .add(BlockSettings.Fields.speedFactor, this.props.getSpeedFactor())
            .add(BlockSettings.Fields.jumpFactor, this.props.getJumpFactor())
            .add(BlockSettings.Fields.canOcclude, this.props.getCanOcclude())
            .add(BlockSettings.Fields.isAir, this.props.getIsAir())
            .add(BlockSettings.Fields.dynamicShape, this.props.getDynamicShape());
    }

    private JsonValue generateStateSettings() {
        return new JsonObject()
            .add(StateSettings.Fields.materialColor,
                getValue(() ->
                    StateMapResolver.resolveMaterialColor(this.props.getMaterialColor(), this.block),
                    StateMapReader.MATERIAL_COLOR))
            .add(StateSettings.Fields.lightEmission,
                getValue(() ->
                    StateMapResolver.resolveInteger(this.props.getLightEmission(), this.block),
                    StateMapReader.INT))
            .add(StateSettings.Fields.isValidSpawn,
                getValue(() ->
                    StateMapResolver.resolveEntityTypes(this.props.getIsValidSpawn(), this.level, this.pos, this.block),
                    StateMapReader.ENTITIES))
            .add(StateSettings.Fields.isRedstoneConductor,
                getValue(() ->
                    StateMapResolver.resolveBoolean(this.props.getIsRedstoneConductor(), this.level, this.pos, this.block),
                    StateMapReader.BOOL))
            .add(StateSettings.Fields.isSuffocating,
                getValue(() ->
                    StateMapResolver.resolveBoolean(this.props.getIsSuffocating(), this.level, this.pos, this.block),
                    StateMapReader.BOOL))
            .add(StateSettings.Fields.isViewBlocking,
                getValue(() ->
                    StateMapResolver.resolveBoolean(this.props.getIsViewBlocking(), this.level, this.pos, this.block),
                    StateMapReader.BOOL))
            .add(StateSettings.Fields.hasPostProcess,
                getValue(() ->
                    StateMapResolver.resolveBoolean(this.props.getHasPostProcess(), this.level, this.pos, this.block),
                    StateMapReader.BOOL))
            .add(StateSettings.Fields.emissiveRendering,
                getValue(() ->
                    StateMapResolver.resolveBoolean(this.props.getEmissiveRendering(), this.level, this.pos, this.block),
                    StateMapReader.BOOL));
    }

    private JsonValue generateItemSettings() {
        final Item item = CommonRegistries.ITEMS.lookup(this.id);
        if (item == null) {
            return error("No item for this block");
        }
        if (item instanceof ItemAccessor accessor) {
            final JsonObject settings = new JsonObject()
                .add(ItemSettings.Fields.isFireResistant, item.isFireResistant())
                .add(ItemSettings.Fields.maxStackSize, item.getMaxStackSize())
                .add(ItemSettings.Fields.rarity, accessor.getRarity().name());
            add(settings, ItemSettings.Fields.craftRemainingItem, () -> {
                if (item.getCraftingRemainingItem() == null) {
                    return null;
                }
                final ResourceLocation remaining =
                    CommonRegistries.ITEMS.getKey(item.getCraftingRemainingItem());
                Objects.requireNonNull(remaining, "Unregistered item");
                return remaining.toString();
            });
            add(settings, ItemSettings.Fields.eatingSound, () ->
                XjsUtils.writeThrowing(SoundEvent.CODEC, item.getEatingSound()));
            add(settings, ItemSettings.Fields.foodProperties, () ->
                XjsUtils.writeThrowing(FoodPropertiesReader.CODEC, item.getFoodProperties()));
            return settings;
        }
        return error("Unresolvable item data");
    }

    private JsonValue generateLootSettings() {
        final MinecraftServer server = this.level.getServer();
        if (server == null) {
            return error("Minecraft server not available");
        }
        final LootTable loot =
            server.getLootTables().get(this.block.getLootTable());
        final String json =
            LootTablesAccessor.getGson().toJson(loot);
        return Json.parse(json).unformatted();
    }

    private JsonValue generateSourceSettings() {
        return Json.value("stone").setComment("Unimplemented generator");
    }

    private static <T> void add(final JsonObject json, final String field, final CheckedGetter<T> getter) {
        final JsonValue value;
        try {
            value = Json.any(getter.get());
        } catch (final Exception e) {
            json.add(field, error(e.getClass().getSimpleName(), e));
            return;
        }
        json.add(field, value);
    }

    private static <T> JsonValue getValue(
            final CheckedGetter<T> getter,
            final Codec<T> codec) {
        final T t;
        try {
            t = getter.get();
        } catch (final Exception e) {
            return error("Unresolvable data", e);
        }
        try {
            return XjsUtils.writeThrowing(codec, t);
        } catch (final RuntimeException e) {
            return error("Could not write data", e);
        }
    }

    private static JsonValue error(final String msg, final Exception e) {
        return error(msg + ": " + e.getMessage());
    }

    private static JsonValue error(final String msg) {
        return Json.value(null).setComment(msg);
    }

    private void write(final File dir) {
        final String path = this.id.getPath();
        final String filename =
            path + "/" + path + "_" + this.id.getNamespace() + ".xjs";
        final File file = new File(dir, filename);
        try {
            this.preset.write(file);
        } catch (final IOException e) {
            final String data = this.preset.toString();
            final PresetGenerationException formatted =
                new PresetGenerationException(dir, file, data, e);
            LibErrorContext.error(Reference.MOD, formatted);
        }
    }

    @FunctionalInterface
    private interface CheckedGetter<T> {
        T get() throws Exception;
    }
}
