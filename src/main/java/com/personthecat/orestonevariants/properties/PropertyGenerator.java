package com.personthecat.orestonevariants.properties;

import com.mojang.authlib.GameProfile;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.*;
import com.personthecat.orestonevariants.properties.WorldGenProperties.WorldGenPropertiesBuilder;
import com.personthecat.orestonevariants.world.VariantFeatureConfig;
import com.personthecat.orestonevariants.world.VariantPlacementConfig;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.util.TriConsumer;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.io.SafeFileIO.getResource;
import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.formatBlock;
import static com.personthecat.orestonevariants.util.CommonMethods.full;
import static com.personthecat.orestonevariants.util.CommonMethods.nullable;
import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Log4j2
public class PropertyGenerator {

    /** The number of times to generate xp. Higher numbers are more accurate. */
    private static final int XP_SAMPLES = 300;

    /** The header to append at the top of generated files. */
    private static final String GENERATED_HEADER =
        "Generated data. Some values are estimated.\n"
        + "See TUTORIAL.hjson for more info.";

    /** A list of patterns representing common ore texture paths. */
    private static final String[] TEXTURE_TEMPLATES = {
        "block/{}",
        "block/ore/{}",
        "block/ores/{}",
        "block/{}_vanilla",
        "/ore/{}",
        "/ores/{}",
        "{}"
    };

    /** Compiles all of the block data from `ore` into a single JSON object. */
    public static JsonObject getBlockInfo(BlockState ore, ServerWorld world, Optional<String> blockName) {
        final String name = blockName.orElse(formatBlock(ore.getBlock()));
        final ResourceLocation location = nullable(ore.getBlock().getRegistryName())
            .orElseThrow(() -> runEx("Error with input block's registry information."));
        final String mod = location.getNamespace();
        final String actualName = location.getPath();
        final BlockPos dummy = BlockPos.ZERO;
        final PlayerEntity entity = getFakePlayer(world);
        final JsonObject json = new JsonObject();

        json.setComment(GENERATED_HEADER);
        json.set("name", name);
        json.set("mod", mod);
        json.set("block", getBlock(ore, world, dummy, entity));
        json.set("texture", getTexture(mod, actualName));
        getRecipe(world.getRecipeManager(), Item.getItemFromBlock(ore.getBlock()))
            .ifPresent(recipe -> json.set("recipe", recipe));
        json.set("loot", ore.getBlock().getLootTable().toString());
        json.set("gen", getGen(world, ore));
        return json;
    }

    private static JsonObject getBlock(BlockState ore, ServerWorld world, BlockPos pos, PlayerEntity entity) {
        final JsonObject json = new JsonObject();
        final Block block = ore.getBlock();
        final ResourceLocation location = Objects.requireNonNull(block.getRegistryName(), "");
        final BlockPropertiesHelper helper = new BlockPropertiesHelper(Block.Properties.from(block));
        final Optional<String> material = ValueLookup.serialize(ore.getMaterial());
        final Optional<String> sound = ValueLookup.serialize(block.getSoundType(ore, world, pos, entity));
        final float slipperiness = formatDecimal(block.getSlipperiness(ore, world, pos, entity));

        if (!material.isPresent()) {
            log.warn("Unsupported material: {}. Skipping...", ore.getMaterial());
        }
        if (!sound.isPresent()) {
            log.warn("Unsupported sound: {}. Skipping...", block.getSoundType(ore, world, pos, entity));
        }

        json.set("location", location.toString());
        json.set("light", ore.getLightValue(world, pos));
        json.set("resistance", helper.getResistance()); // Too difficult to guarantee value.
        json.set("hardness", ore.getBlockHardness(world, pos));
        json.set("ticksRandomly", block.ticksRandomly(ore));
        json.set("slipperiness", slipperiness);
        json.set("speedFactor", block.getSpeedFactor());
        json.set("jumpFactor", block.getJumpFactor());
        json.set("isSolid", ore.isSolid());
        json.set("level", block.getHarvestLevel(ore));
        json.set("tool", nullable(block.getHarvestTool(ore)).map(ToolType::getName).orElse("pickaxe"));
        json.set("variableOpacity", block.isVariableOpacity());
        json.set("requiresTool", helper.getRequiresTool());
        json.set("xp", getXp(ore, world, pos));
        material.ifPresent(m -> json.set("material", m));
        sound.ifPresent(s -> json.set("soundType", s));
        return json;
    }

    // Todo: find closest background.
    private static JsonObject getTexture(String mod, String name) {
        final JsonObject json = new JsonObject();
        getTextureLocation(mod, name).ifPresent(path ->
            json.set("original", path)
        );
        return json;
    }

    private static Optional<String> getTextureLocation(String mod, String name) {
        for (String template : TEXTURE_TEMPLATES) {
            final String path = f(template, name);
            final String expanded = f("/assets/{}/textures/{}.png", mod, path);
            if (getResource(expanded).isPresent()) {
                return full(f("{}:{}", mod, path));
            }
        }
        return empty();
    }

    private static Optional<JsonObject> getRecipe(RecipeManager recipes, Item ore) {
        return RecipeHelper.byInput(recipes, ore).map(PropertyGenerator::getRecipe);
    }

    private static JsonObject getRecipe(AbstractCookingRecipe recipe) {
        final JsonObject json = new JsonObject();
        final ItemStack result = recipe.getRecipeOutput();
        final ResourceLocation id = Objects.requireNonNull(result.getItem().getRegistryName(), "Result is unregistered.");
        json.set("result", id.toString());
        json.set("xp", recipe.getExperience());
        json.set("time", recipe.getCookTime());
        if (!recipe.getGroup().isEmpty()) {
            json.set("group", recipe.getGroup());
        }
        return json;
    }

    private static PlayerEntity getFakePlayer(ServerWorld world) {
        final GameProfile profile = new GameProfile(new UUID(0, 0), "");
        return new FakePlayer(world, profile);
    }

    private static JsonValue getXp(BlockState ore, World world, BlockPos pos) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < XP_SAMPLES; i++) {
            final int xp = ore.getExpDrop(world, pos, 0, 0);
            min = Math.min(min, xp);
            max = Math.max(max, xp);
        }
        if (min == max) {
            return JsonValue.valueOf(min);
        }
        return new JsonArray().add(min).add(max).setCondensed(true);
    }

    private static JsonArray getGen(World world, BlockState state) {
        final MultiValueMap<WorldGenProperties, Biome> map = new MultiValueMap<>();
        forAllFeatures(world, (b, stage, feature) ->
            getOreFeature(feature, state).ifPresent(ore -> {
                final WorldGenPropertiesBuilder builder = WorldGenProperties.builder();
                builder.stage(stage);
                copyOreFeatures(ore, builder);
                copyAllPlacements(feature, builder);
                map.add(builder.build(), b);
            })
        );
        final JsonArray array = new JsonArray();
        for (WorldGenProperties gen : reduceMap(map)) {
            array.add(gen.toJson());
        }
        return array;
    }

    /** Places every biome from a multi value map inside of the builder it's mapped to. */
    private static List<WorldGenProperties> reduceMap(MultiValueMap<WorldGenProperties, Biome> map) {
        final List<WorldGenProperties> list = new ArrayList<>();
        for (Map.Entry<WorldGenProperties, List<Biome>> entry : map.entrySet()) {
            final InvertableSet<Biome> asSet = InvertableSet.wrap(new HashSet<>(entry.getValue()));
            list.add(entry.getKey().toBuilder().biomes(new Lazy<>(asSet)).build());
        }
        return list;
    }

    /** Perform an operation for every Biome -> ConfiguredFeature pair currently registered. */
    private static void forAllFeatures(World world, TriConsumer<Biome, Decoration, ConfiguredFeature<?, ?>> f) {
        // Attempting to retrieve the updated biomes instead of their initial forms.
        final Iterable<Biome> biomes = nullable(world.getServer())
            .map(server -> (Iterable<Biome>) server.func_244267_aX().getRegistry(Registry.BIOME_KEY))
            .orElse(ForgeRegistries.BIOMES.getValues());
        for (Biome b : biomes) {
            final BiomeGenerationSettings settings = b.getGenerationSettings();
            final int lastStage = Math.min(settings.getFeatures().size(), Decoration.values().length);
            for (int i = 0; i < lastStage; i++) {
                final Decoration stage = Decoration.values()[i];
                for (Supplier<ConfiguredFeature<?, ?>> supplier : settings.getFeatures().get(i)) {
                    f.accept(b, stage, supplier.get());
                }
            }
        }
    }

    /** Copies any applicable settings from this ore feature config into the builder. */
    private static void copyOreFeatures(IFeatureConfig ore, WorldGenPropertiesBuilder builder) {
        if (ore instanceof OreFeatureConfig) {
            final OreFeatureConfig config = (OreFeatureConfig) ore;
            builder.size(config.size);
        } else if (ore instanceof VariantFeatureConfig) {
            final VariantFeatureConfig config = (VariantFeatureConfig) ore;
            builder.size(config.size);
            builder.denseRatio(config.denseChance);
        } else if (!(ore instanceof ReplaceBlockConfig)) {
            throw runExF("Unsupported feature type: {}", ore.getClass());
        }
    }

    /** Copies all of the values from every configured placement in this feature into the builder. */
    private static void copyAllPlacements(ConfiguredFeature<?, ?> feature, WorldGenPropertiesBuilder builder) {
        getAllPlacements(feature).forEach(decorator -> copyPlacement(decorator, builder));
    }

    /** Copies all of the values from a single configured placement into this builder. */
    private static void copyPlacement(ConfiguredPlacement<?> decorator, WorldGenPropertiesBuilder builder) {
        // Not sure why this value isn't exposed.
        final Placement<?> placement = decorator.decorator;
        final IPlacementConfig config = decorator.func_242877_b();
        if (placement instanceof Height4To32) {
            builder.height(Range.of(4, 32)).count(Range.of(3, 8));
        } else if (placement instanceof Spread32AbovePlacement) {
            // We assume this is called in this correct order.
            final Range height = builder.build().height;
            builder.height(Range.of(height.min, height.max + 32));
        }
        // Todo: account for bias.
        if (config instanceof TopSolidRangeConfig) {
            final TopSolidRangeConfig tsr = (TopSolidRangeConfig) config;
            builder.height(Range.of(tsr.bottomOffset, tsr.maximum - tsr.topOffset));
        } else if (config instanceof ChanceConfig) {
            builder.chance(((ChanceConfig) config).chance);
        } else if (config instanceof FeatureSpreadConfig) {
            final FeatureSpread count = ((FeatureSpreadConfig) config).func_242799_a();
            builder.count(Range.of(count.base, count.base + count.spread));
        } else if (config instanceof VariantPlacementConfig) {
            final VariantPlacementConfig variant = (VariantPlacementConfig) config;
            builder.height(Range.of(variant.minHeight, variant.minHeight + variant.incrHeight));
            builder.count(Range.of(variant.minCount, variant.maxCount));
            builder.chance(variant.chance);
        }
    }

    /** Gets an ore feature config from this feature, if possible. */
    private static Optional<IFeatureConfig> getOreFeature(ConfiguredFeature<?, ?> feature, BlockState ore) {
        return getAllFeatures(feature)
            .filter(config -> checkState(config, ore))
            .findFirst();
    }

    /** Determines whether the input feature config matches the given block. */
    private static boolean checkState(IFeatureConfig feature, BlockState ore) {
        if (feature instanceof OreFeatureConfig) {
            return ((OreFeatureConfig) feature).state.equals(ore);
        } else if (feature instanceof VariantFeatureConfig) {
            return ((VariantFeatureConfig) feature).target.ore.get().equals(ore);
        } else if (feature instanceof BlockWithContextConfig) {
            return ((BlockWithContextConfig) feature).toPlace.equals(ore);
        } else if (feature instanceof ReplaceBlockConfig) {
            return ((ReplaceBlockConfig) feature).state.equals(ore);
        }
        return false;
    }

    /** A stream of every unique ConfiguredPlacement in this feature.s */
    private static Stream<ConfiguredPlacement<?>> getAllPlacements(ConfiguredFeature<?, ?> feature) {
        return getAllFeatures(feature).filter(config -> config instanceof DecoratedFeatureConfig)
            .map(config -> ((DecoratedFeatureConfig) config).decorator);
    }

    /** A stream of every unique config separated out of this feature. */
    private static Stream<IFeatureConfig> getAllFeatures(ConfiguredFeature<?, ?> feature) {
        return Stream.concat(Stream.of(feature), feature.func_242768_d())
            .map(ConfiguredFeature::getConfig);
    }

    /** Formats the input float to 3 decimal places. */
    private static float formatDecimal(float f) {
        return Math.round(f * 1000) / 1000f;
    }
}