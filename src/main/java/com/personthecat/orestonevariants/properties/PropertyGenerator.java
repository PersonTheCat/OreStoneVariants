package com.personthecat.orestonevariants.properties;

import com.mojang.authlib.GameProfile;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.ValueLookup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.hjson.JsonArray;
import org.hjson.JsonObject;

import java.util.*;

import static com.personthecat.orestonevariants.io.SafeFileIO.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PropertyGenerator {

    // Todo: It should be possible to extract WorldGenProperties in 1.13+.
    // However, not a lot of mods (any?) currently add new ores in 1.16.2.
    // As a result, I'm not sure it's worth it yet.
    // Todo: false -> do it

    /** The number of times to generate xp. Higher numbers are more accurate. */
    private static final int XP_SAMPLES = 300;

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

    public static JsonObject getBlockInfo(BlockState ore, World world, Optional<String> blockName) {
        final String name = blockName.orElse(formatState(ore));
        final ResourceLocation location = nullable(ore.getBlock().getRegistryName())
            .orElseThrow(() -> runEx("Error with input block's registry information."));
        final String mod = location.getNamespace();
        final String actualName = location.getPath();
        final BlockPos dummy = BlockPos.ZERO;
        final PlayerEntity entity = getFakePlayer(world);
        final JsonObject json = new JsonObject();

        json.setComment(
            "This preset was generated automatically. To enable\n" +
            "custom generation settings, you must manually define\n" +
            "`gen`. See TUTORIAL.hjson."
        );
        json.set("name", name);
        json.set("mod", mod);
        json.set("block", getBlock(ore, world, dummy, entity));
        json.set("texture", getTexture(mod, actualName));
        json.set("recipe", getRecipe(world.getRecipeManager(), Item.getItemFromBlock(ore.getBlock())));
        json.set("loot", ore.getBlock().getLootTable().toString());
        return json;
    }

    private static JsonObject getBlock(BlockState ore, World world, BlockPos pos, PlayerEntity entity) {
        final JsonObject json = new JsonObject();
        final Block block = ore.getBlock();
        final ResourceLocation location = block.getRegistryName();
        final BlockPropertiesHelper helper = new BlockPropertiesHelper(Block.Properties.from(block));
        final String material = ValueLookup.serialize(ore.getMaterial())
            .orElseThrow(() -> runEx("Only vanilla material types are supported."));
        final String sound = ValueLookup.serialize(block.getSoundType(ore, world, pos, entity))
            .orElseThrow(() -> runEx("Only vanilla sound types are supported."));
        final float slipperiness = formatDecimal(block.getSlipperiness(ore, world, pos, entity));

        json.set("location", location.toString());
        json.set("material", material);
        json.set("soundType", sound);
        json.set("light", ore.getLightValue(world, pos));
        json.set("resistance", helper.getResistance()); // Too difficult to guarantee value.
        json.set("hardness", ore.getBlockHardness(world, pos));
        json.set("ticksRandomly", block.ticksRandomly(ore));
        json.set("slipperiness", slipperiness);
        json.set("speedFactor", block.getSpeedFactor());
        json.set("jumpFactor", block.getJumpFactor());
        json.set("isSolid", ore.isSolid());
        json.set("level", block.getHarvestLevel(ore));
        json.set("tool", block.getHarvestTool(ore).getName());
        json.set("variableOpacity", block.isVariableOpacity());
        json.set("xp", getXp(ore, world, pos));
        return json;
    }

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

    private static JsonObject getRecipe(RecipeManager recipes, Item ore) {
        final JsonObject json = new JsonObject();
        final AbstractCookingRecipe recipe = RecipeHelper.byInput(recipes, ore)
            .orElseThrow(() -> runExF("Unable to find smelting result for {}.", ore));
        final ItemStack result = recipe.getRecipeOutput();

        json.set("result", result.getItem().getRegistryName().toString());
        json.set("xp", recipe.getExperience());
        json.set("time", recipe.getCookTime());
        if (!recipe.getGroup().isEmpty()) {
            json.set("group", recipe.getGroup());
        }
        return json;
    }

    private static PlayerEntity getFakePlayer(World world) {
        final GameProfile profile = new GameProfile(new UUID(0, 0), "");
        return new FakePlayer(world.getServer().getWorlds().iterator().next(), profile);
    }

    private static JsonArray getXp(BlockState ore, World world, BlockPos pos) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < XP_SAMPLES; i++) {
            final int xp = ore.getExpDrop(world, pos, 0, 0);
            min = getMin(min, xp);
            max = getMax(max, xp);
        }
        if (min > max) { // Unless XP_SAMPLES == 0, this is probably impossible.
            return new JsonArray().add(0);
        }
        return new JsonArray().add(min).add(max)
            .setCondensed(true);
    }

    /** Formats the input float to 3 decimal places. */
    private static float formatDecimal(float f) {
        return Math.round(f * 1000) / 1000f;
    }
}