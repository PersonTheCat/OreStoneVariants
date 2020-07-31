package com.personthecat.orestonevariants.properties;

import com.mojang.authlib.GameProfile;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.util.ExtendedResourceLocation;
import com.personthecat.orestonevariants.util.ValueLookup;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.hjson.JsonArray;
import org.hjson.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.io.SafeFileIO.*;

public class PropertyGenerator {

    /** The number of times to generate ore drops. Higher numbers are more accurate. */
    private static final int DROP_SAMPLES = 300;

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
        final BlockPos dummy = BlockPos.ORIGIN;
        final EntityPlayer entity = getFakePlayer(world);
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
        json.set("recipe", getRecipe(ore));
        json.set("loot", getDrops(ore, world, dummy, entity));

        return json;
    }

    private static JsonObject getBlock(IBlockState ore, World world, BlockPos pos, EntityPlayer entity) {
        final JsonObject json = new JsonObject();
        final Block block = ore.getBlock();
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

    private static JsonArray getDrops(IBlockState ore, World world, BlockPos pos, EntityPlayer player) {
        final JsonArray array = new JsonArray();
        final List<ItemStack> drops = getAllDrops(ore, world, pos, player);
        final List<DropData> data = getDropData(drops);
        // XP may get doubled
        final DropData xpData = getXPData(ore, world, pos, player);
        data.forEach(dat -> {
            final ItemStack stack = dat.stack;
            final float chance = dat.numStacks / (float) DROP_SAMPLES;
            final JsonObject json = new JsonObject()
                .set("item", ExtendedResourceLocation.fromStack(stack).toString())
                .set("count", new JsonArray()
                    .add((int) dat.min)
                    .add((int) dat.max)
                    .setCondensed(true))
                .set("xp", new JsonArray()
                    .add(xpData.min * chance) // Avoid stacking XP. That's probably
                    .add(xpData.max * chance) // Not how the original mod did it.
                    .setCondensed(true))
                .set("chance", chance);
            array.add(json);
        });
        return array;
    }

    private static List<ItemStack> getAllDrops(IBlockState ore, World world, BlockPos pos, EntityPlayer player) {
        final List<ItemStack> drops = new ArrayList<>();
        final Block block = ore.getBlock();
        for (int i = 0; i < DROP_SAMPLES; i++) {
            // Simulate clicking. BaseOreVariant's combining of
            // drop+xp makes this very complicated in 1.12.
            block.onBlockClicked(world, pos, player);
            final List<ItemStack> sample = block.getDrops(world, pos, ore, 0);
            drops.addAll(reduce(sample));
        }
        return drops;
    }

    private static List<ItemStack> reduce(List<ItemStack> drops) {
        return getDropData(drops).stream()
            .map(data -> {
                data.stack.setCount((int) data.total);
                return data.stack;
            })
            .collect(Collectors.toList());
    }

    private static List<DropData> getDropData(List<ItemStack> drops) {
        final List<DropData> data = new ArrayList<>();
        for (ItemStack drop : drops) {
            final int index = Collections.binarySearch(data, drop);
            if (index < 0) { // Data not present.
                data.add(-index - 1, new DropData(drop));
            } else {
                data.get(index).update(drop.getCount());
            }
        }
        return data;
    }

    private static DropData getXPData(IBlockState ore, World world, BlockPos pos, EntityPlayer player) {
        final Block block = ore.getBlock();
        final DropData xpData = new DropData(block.getExpDrop(ore, world, pos, 0));
        for (int i = 1; i < DROP_SAMPLES; i++) {
            block.onBlockClicked(world, pos, player);
            xpData.update(block.getExpDrop(ore, world, pos, 0));
        }
        return xpData;
    }

    /** Used to track the minimum / maximum drop count and xp for each ore. */
    private static class DropData implements Comparable<ItemStack> {
        final ItemStack stack;
        float min, max;
        float numStacks = 1;
        float total;

        DropData(ItemStack stack) {
            this.stack = stack;
            this.min = this.max = this.total = stack.getCount();
            stack.setCount(1);
        }

        /** Just keep track of the number. */
        DropData(float count) {
            this.stack = ItemStack.EMPTY;
            this.min = this.max = count;
            this.total = 1;
        }

        void update(float count) {
            min = getMin(min, count);
            max = getMax(max, count);
            total += count;
            numStacks++;
        }

        @Override
        public int compareTo(ItemStack other) {
            return Integer.compare(getHash(stack), getHash(other));
        }

        private static int getHash(ItemStack stack) {
            return (Item.getIdFromItem(stack.getItem()) << 4) | stack.getMetadata();
        }
    }

    private static EntityPlayer getFakePlayer(World world) {
        final GameProfile profile = new GameProfile(new UUID(0, 0), "");
        return new FakePlayer(world.getMinecraftServer().getWorld(0), profile);
    }

    /** Formats the input float to 3 decimal places. */
    private static float formatDecimal(float f) {
        return Math.round(f * 1000) / 1000f;
    }
}