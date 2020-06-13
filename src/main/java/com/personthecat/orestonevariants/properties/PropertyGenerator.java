package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.ExtendedResourceLocation;
import com.personthecat.orestonevariants.util.ValueLookup;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.World;
import org.hjson.JsonObject;

import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;

public class PropertyGenerator {
    /** A list of patterns representing common ore texture paths. */
    private static final String[] TEXTURE_TEMPLATES = {
        "blocks/{}",
        "blocks/ore/{}",
        "blocks/ores/{}",
        "blocks/{}_vanilla",
        "/ore/{}",
        "/ores/",
        "{}"
    };

    public static JsonObject getBlockInfo(IBlockState ore, World world, Optional<String> blockName) {
        final String name = blockName.orElse(formatState(ore));
        final ResourceLocation location = nullable(ore.getBlock().getRegistryName())
            .orElseThrow(() -> runEx("Error with input block's registry information."));
        final String mod = location.getNamespace();
        final String actualName = location.getPath();
        final JsonObject json = new JsonObject();

        json.setComment(
            "This preset was generated automatically. To enable\n" +
            "custom drops and/or generation settings, you must\n" +
            "manually define `loot` and `gen`. See TUTORIAL.hjson."
        );
        json.set("name", name);
        json.set("mod", mod);
        json.set("block", getBlock(ore, world));
        json.set("texture", getTexture(mod, actualName));
        json.set("recipe", getRecipe(ore));

        return json;
    }

    private static JsonObject getBlock(IBlockState ore, World world) {
        final JsonObject json = new JsonObject();
        final Block block = ore.getBlock();
        final BlockPos pos = BlockPos.ORIGIN;
        final Entity entity = new EntityItem(world, 0, 0, 0);
        final ExtendedResourceLocation location = ExtendedResourceLocation.fromState(ore);
        final BlockPropertiesHelper helper = new BlockPropertiesHelper(block);
        final String material = ValueLookup.serialize(ore.getMaterial())
            .orElseThrow(() -> runEx("Only vanilla material types are supported."));
        final String sound = ValueLookup.serialize(block.getSoundType(ore, world, pos, entity))
            .orElseThrow(() -> runEx("Only vanilla sound types are supported."));
        final float slipperiness = formatDecimal(block.getSlipperiness(ore, world, pos, entity));

        json.set("location", location.toString());
        json.set("level", block.getHarvestLevel(ore));
        json.set("material", material);
        json.set("soundType", sound);
        json.set("lightLevel", ore.getLightValue(world, pos));
        json.set("resistance", helper.getResistance()); // Too difficult to guarantee value.
        json.set("hardness", ore.getBlockHardness(world, pos));
        json.set("ticksRandomly", block.getTickRandomly());
        json.set("slipperiness", slipperiness);
        json.set("tool", block.getHarvestTool(ore));
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

    private static JsonObject getRecipe(IBlockState ore) {
        final JsonObject json = new JsonObject();
        final ItemStack result = RecipeHelper.resultOf(toStack(ore))
            .orElseThrow(() -> runExF("Unable to find smelting result for {}.", ore));
        final ExtendedResourceLocation location = ExtendedResourceLocation.fromStack(result);
        final float xp = formatDecimal(RecipeHelper.getExperience(result));

        json.set("result", location.toString());
        json.set("xp", xp);
        json.set("quantity", result.getCount());
        return json;
    }

    /** Formats the input float to 3 decimal places. */
    private static float formatDecimal(float f) {
        return Math.round(f * 1000) / 1000f;
    }
}