package personthecat.mod.objects.blocks.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import personthecat.mod.advancements.AdvancementMap;
import personthecat.mod.config.Cfg;
import personthecat.mod.objects.blocks.BlockOresBase;
import personthecat.mod.util.ShortTrans;

public class ItemBlockVariants extends ItemBlock
{
    BlockOresBase ore;

    public ItemBlockVariants(BlockOresBase ore)
    {
        super(ore);

        this.ore = ore;

        if (ore.hasEnumBlockStates()) setHasSubtypes(true);

        setMaxDamage(0);
        setRegistryName(ore.getRegistryName());
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    /**
     * This may actually be extremely inefficient. To-Do.
     */
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (Cfg.BlocksCat.miscCat.enableAdvancements)
        {
            AdvancementMap.grantAdvancement(AdvancementMap.getAdvancementFromMap(ore.getOriginalName(), worldIn), entityIn);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int meta = stack.getItemDamage();

        if (meta > 16) return getUnlocalizedName() + "_?";

        return getUnlocalizedName() + "_" + ore.getBackgroundStack(meta).getUnlocalizedName();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (stack.getItemDamage() > 16) return ShortTrans.formatted(getUnlocalizedName() + ".name") + " (?)";

        ItemStack backgroundStack = ore.getBackgroundStack(stack.getItemDamage());

        if (ShortTrans.canTranslate(backgroundStack.getUnlocalizedName() + ".name"))
        {
            return getOreText() + " (" + ShortTrans.formatted(backgroundStack.getUnlocalizedName() + ".name") + ")";
        }

        return ShortTrans.formatted(getUnlocalizedName() + "_" + backgroundStack.getUnlocalizedName() + ".name");
    }

    private String getOreText()
    {
        String oreText = ore.getProperties().getLocalizedName();

        if (ore.isDenseVariant())
        {
            return ShortTrans.formatted("ore_stone_variants.denseKey") + " " + oreText;
        }

        return oreText;
    }
}