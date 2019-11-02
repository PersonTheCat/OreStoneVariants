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
        if (Cfg.BlocksCat.MiscCat.enableAdvancements)
        {
            AdvancementMap.grantAdvancement(AdvancementMap.getAdvancementFromMap(ore.getOriginalName(), worldIn), entityIn);
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        int meta = stack.getItemDamage();

        if (meta > 16) return getTranslationKey() + "_?";

        return getTranslationKey() + "_" + ore.getBackgroundStack(meta).getTranslationKey();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (stack.getItemDamage() > 16) return ShortTrans.formatted(getTranslationKey() + ".name") + " (?)";

        ItemStack backgroundStack = ore.getBackgroundStack(stack.getItemDamage());

        if (ShortTrans.canTranslate(backgroundStack.getTranslationKey() + ".name"))
        {
            return getOreText() + " (" + ShortTrans.formatted(backgroundStack.getTranslationKey() + ".name") + ")";
        }

        return ShortTrans.formatted(getTranslationKey() + "_" + backgroundStack.getTranslationKey() + ".name");
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