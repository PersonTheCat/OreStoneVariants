package com.personthecat.orestonevariants.item;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.LazyFunction;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class VariantItem extends ItemBlock {

    /** A lazy value storing the advancement belonging to this item, if applicable. */
    private final LazyFunction<World, Optional<Advancement>> advancement = new LazyFunction<>(
        this::getAdvancement
    );

    public VariantItem(BaseOreVariant block) {
        this(block, block.getRegistryName());
    }

    protected VariantItem(BaseOreVariant block, ResourceLocation name) {
        super(block);
        setRegistryName(name);
        setHasSubtypes(true);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        advancement.apply(world).ifPresent(adv -> {
            if (entity instanceof EntityPlayerMP) {
                applyAdvancement((EntityPlayerMP) entity, adv);
            }
        });
    }

    /** Checks if a player has the given advancement, else gives it to them. */
    private static void applyAdvancement(EntityPlayerMP player, Advancement advancement) {
        final PlayerAdvancements playerAdvancements = player.getAdvancements();
        final AdvancementProgress progress = playerAdvancements.getProgress(advancement);
        if (progress.isDone()) {
            for (String criteria : progress.getRemaningCriteria()) {
                playerAdvancements.grantCriterion(advancement, criteria);
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        final StringBuilder name = new StringBuilder();
        if (stack.getMetadata() > 0) {
            name.append(new TextComponentTranslation("osv.denseKey").getFormattedText());
            name.append(" ");
        }
        final IBlockState ore = ((BaseOreVariant) block).properties.ore.get();
        if (ore.getBlock().getTranslationKey().equals(block.getTranslationKey())) {
            name.append(toStack(ore).getDisplayName());
        } else {
            name.append(block.getLocalizedName());
        }
        return name.append(" (")
            .append(getBackgroundKey())
            .append(")")
            .toString();
    }

    private String getBackgroundKey() {
        return toStack(((BaseOreVariant) getBlock()).bgBlock).getDisplayName();
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    /** Attempts to retrieve an advancement for the input world. */
    private Optional<Advancement> getAdvancement(World world) {
        return ((BaseOreVariant) block).properties.advancement
            .filter(a -> Cfg.BlocksCat.enableAdvancements)
            .flatMap(location -> nullable(world.getMinecraftServer())
            .map(server -> server.getAdvancementManager().getAdvancement(location)));
    }
}