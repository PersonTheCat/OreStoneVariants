package com.personthecat.orestonevariants.tags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.personthecat.orestonevariants.blocks.OreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.util.MultiValueMap;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TagsUpdatedEvent;
import personthecat.fresult.Result;

@Log4j2
public class TagHelper {

    /**
     * Capitalizing on this event to modify existing tags rather than creating custom tag
     * JSONs. This is necessary because tags can change unpredictably and thus whatever
     * generated contents we could put in a tag JSON would most likely go stale.
     */
    public static void onTagsUpdated(final TagsUpdatedEvent.CustomTagTypes event) {
        log.info("Updating tags.");
        final TagUpdateContext ctx = new TagUpdateContext(event);
        Result.of(ctx::locateAll).ifErr(e -> log.warn("Locating tags", e));
        Result.of(ctx::copyAll).ifErr(e -> log.warn("Updating tags", e));
    }

    private static class TagUpdateContext {
        final ITagCollection<Block> blockTags;
        final ITagCollection<Item> itemTags;
        final MultiValueMap<ResourceLocation, Block> blockTagsToContents;
        final MultiValueMap<ResourceLocation, Item> itemTagsToContents;

        TagUpdateContext(TagsUpdatedEvent event) {
            this.blockTags = event.getTagManager().getBlockTags();
            this.itemTags = event.getTagManager().getItemTags();
            this.blockTagsToContents = new MultiValueMap<>();
            this.itemTagsToContents = new MultiValueMap<>();
        }

        void locateAll() {
            for (OreVariant b : LazyRegistries.BLOCKS) {
                if (b.properties.copyTags) {
                    if (Cfg.copyBgTags.get()) this.locateTags(b, b.bgState.getBlock());
                    if (Cfg.copyFgTags.get()) this.locateTags(b, b.fgState.getBlock());
                }
            }
        }

        void locateTags(OreVariant b, Block wrapped) {
            if (Cfg.copyBlockTags.get()) {
                for (ResourceLocation tag : this.blockTags.getOwningTags(wrapped)) {
                    this.blockTagsToContents.add(tag, b);
                }
            }
            if (Cfg.copyItemTags.get()) {
                for (ResourceLocation tag : this.itemTags.getOwningTags(wrapped.asItem())) {
                    this.itemTagsToContents.add(tag, b.normalItem.get());
                    if (Cfg.copyDenseTags.get()) {
                        this.itemTagsToContents.add(tag, b.denseItem.get());
                    }
                }
            }
        }

        void copyAll() {
            this.copy(this.blockTagsToContents, this.blockTags);
            this.copy(this.itemTagsToContents, this.itemTags);
        }

        <T> void copy(MultiValueMap<ResourceLocation, T> tagsToContents, ITagCollection<T> tags) {
            tagsToContents.forEach((id, values) -> {
                final ITag<T> tag = tags.getTagByID(id);
                if (tag instanceof Tag) {
                    final Tag<T> concrete = (Tag<T>) tag;
                    concrete.contents = ImmutableSet.<T>builder()
                        .addAll(concrete.contents)
                        .addAll(values)
                        .build();
                    concrete.immutableContents = ImmutableList.<T>builder()
                        .addAll(concrete.immutableContents)
                        .addAll(values)
                        .build();
                }
            });
        }
    }
}
