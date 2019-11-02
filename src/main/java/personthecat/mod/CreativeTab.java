package personthecat.mod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import personthecat.mod.config.Cfg;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresBase;


public class CreativeTab
{
    public static CreativeTabs ORE_VARIANTS, DENSE_VARIANTS;
    public static ItemStack ORE_STACK, DENSE_STACK;

    static
    {
        ORE_STACK = new ItemStack(Blocks.AIR);
        DENSE_STACK = new ItemStack(Blocks.AIR);

        ORE_VARIANTS = new CreativeTabs("ore_variants")
        {
            @Override
            public ItemStack createIcon()
            {
                return ORE_STACK;
            }
        };

        if (Cfg.DenseCat.GeneralDenseCat.denseVariants)
        {
            DENSE_VARIANTS = new CreativeTabs("dense_variants")
            {
                @Override
                public ItemStack createIcon()
                {
                    return DENSE_STACK;
                }
            };
        }
    }

    public static void postBlockInit()
    {
        if (BlockInit.BLOCKS.size() > 0)
        {
            if (Cfg.disableVanillaVariants())
            {
                setCreativeTabIcons(BlockInit.BLOCKS.get(0));
            }
            //Couldn't use a registry name here if I tried. c:
            else for (BlockOresBase ore : BlockInit.BLOCKS)
            {
                if (ore.getOriginalName().equals("coal_ore"))
                {
                    setCreativeTabIcons(ore, 2);
                }
            }
        }
    }

    private static void setCreativeTabIcons(BlockOresBase fromBlock)
    {
        setCreativeTabIcons(fromBlock, 0);
    }

    private static void setCreativeTabIcons(BlockOresBase fromBlock, int meta)
    {
        ORE_STACK = new ItemStack(fromBlock.getItem(), 1, meta);

        if (Cfg.DenseCat.GeneralDenseCat.denseVariants)
        {
            BlockOresBase denseOre = fromBlock.getDenseVariant();

            DENSE_STACK = new ItemStack(denseOre.getItem(), 1, meta);
        }
    }
}