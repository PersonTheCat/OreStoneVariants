package personthecat.mod.util;

import static personthecat.mod.Main.logger;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommonMethods
{
    public static IBlockState getBlockState(String input)
    {
        String[] split = input.split(":");

        ResourceLocation location = null;
        int meta = 0;

        if (StringUtils.isNumeric(split[split.length - 1]))
        {
            meta = Integer.parseInt(split[split.length - 1]);
            location = new ResourceLocation(input.replaceAll(":" + split[split.length - 1], ""));
        }
        else if (split.length == 1 || split.length == 2)
        {
            location = new ResourceLocation(input);
        }
        else logger.warn("Syntax error: Could not determine blockstate from " + input);

        return ForgeRegistries.BLOCKS.getValue(location).getStateFromMeta(meta);
    }

    public static ModelResourceLocation getModelResourceLocation(IBlockState state)
    {
        BlockStateMapper stateMapper = new BlockStateMapper();

        Map<IBlockState, ModelResourceLocation> locationMapped = stateMapper.getVariants(state.getBlock());

        return locationMapped.get(state);
    }

    @Deprecated
    public static String getOre(String name)
    {
        name = name.replace(".", "~").toLowerCase().replaceAll("tile~", "").replaceAll("~name", "");
        String[] nameCorrector = name.split("_");

        if (nameCorrector.length < 1) return null;

        //Keep taking off the last piece until it reads "ore."
        for (int i = nameCorrector.length - 1; i > 0; i--)
        {
            if (!name.endsWith("_ore"))
            {
                int amountToRemove = nameCorrector[i].length() + 1;

                name = name.substring(0, name.length() - amountToRemove);
            }
            else return name;
        }

        return name;
    }

    /**
     * Returns the part of the string that comes immediately before "_ore"
     */
    @Deprecated
    public static String getActualName(String name)
    {
        String[] nameSplit = getOre(name).split("_");

        if (nameSplit[nameSplit.length - 1].equals("ore"))
        {
            return nameSplit[nameSplit.length - 2];
        }

        logger.warn("Error: tried to retrieve actual name from an invalid format.");

        return null;
    }

    public static String formatRL(ResourceLocation location)
    {
        String domain = location.getResourceDomain() + "_";
        String path = location.getResourcePath();

        if (domain.equals("minecraft_")) domain = "";

        return domain + path;
    }

    public static String formatStateName(IBlockState state)
    {
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);

        String blockModelName = block.getRegistryName().toString().replace(":", "_");

        if (meta > 0) blockModelName += "_" + meta;

        if (blockModelName.startsWith("minecraft_"))
        {
            blockModelName = blockModelName.substring(10);
        }

        return blockModelName;
    }
}