package personthecat.osv.tag;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SetTag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.catlib.data.MultiValueHashMap;
import personthecat.catlib.data.MultiValueMap;
import personthecat.fresult.Result;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.Cfg;
import personthecat.osv.mixin.SetTagAccessor;

@Log4j2
public class TagHelper {

    public static void injectTags(final TagContainer tags) {
        if (Cfg.copyTags()) {
            log.info("Injecting OSV tags for all variants.");

            final TagUpdateContext ctx = new TagUpdateContext(tags);
            Result.suppress(ctx::locateAll)
                .ifErr(e -> log.error("Locating tags", e));
            Result.suppress(ctx::copyAll)
                .ifErr(e -> log.error("Copying tags", e))
                .ifOk(v -> log.info("Copied {} block and {} item tags.",
                    ctx.blocksCopied, ctx.itemsCopied));
        }
    }

    private static class TagUpdateContext {
        final TagCollection<Block> blockTags;
        final TagCollection<Item> itemTags;
        final MultiValueMap<ResourceLocation, Block> blockTagsToContents;
        final MultiValueMap<ResourceLocation, Item> itemTagsToContents;
        int blocksCopied;
        int itemsCopied;

        TagUpdateContext(final TagContainer tags) {
            this.blockTags = tags.getBlocks();
            this.itemTags = tags.getItems();
            this.blockTagsToContents = new MultiValueHashMap<>();
            this.itemTagsToContents = new MultiValueHashMap<>();
            this.blocksCopied = 0;
            this.itemsCopied = 0;
        }

        void locateAll() {
            for (final OreVariant ore : ModRegistries.VARIANTS) {
                if (ore.getPreset().getVariant().isCopyTags()) {
                    if (Cfg.copyBgTags()) this.locateTags(ore, ore.getBg());
                    if (Cfg.copyFgTags()) this.locateTags(ore, ore.getFg());
                }
            }
        }

        void locateTags(final OreVariant ore, final Block wrapped) {
            if (Cfg.copyBlockTags()) {
                for (final ResourceLocation tag : this.blockTags.getMatchingTags(wrapped)) {
                    this.blockTagsToContents.add(tag, ore);
                    this.blocksCopied++;
                }
            }
            if (Cfg.copyItemTags()) {
                for (final ResourceLocation tag : this.itemTags.getMatchingTags(wrapped.asItem())) {
                    this.itemTagsToContents.add(tag, ore.asItem());
                    this.itemsCopied++;

                    // Todo: this should copy all additional tags and not specifically dense.
                    if (Cfg.copyDenseTags() && ore.getPreset().canBeDense()) {
                        final BlockState dense = ore.defaultBlockState().setValue(AdditionalProperties.DENSE, true);
                        this.itemTagsToContents.add(tag, ore.asItem(dense));
                        this.itemsCopied++;
                    }
                }
            }
        }

        void copyAll() {
            this.copy(this.blockTagsToContents, this.blockTags);
            this.copy(this.itemTagsToContents, this.itemTags);
        }

        @SuppressWarnings("unchecked")
        <T> void copy(final MultiValueMap<ResourceLocation, T> tagsToContents, final TagCollection<T> tags) {
            tagsToContents.forEach((id, values) -> {
                final Tag<T> tag = tags.getTag(id);
                if (tag instanceof SetTag) {
                    final SetTagAccessor<T> concrete = (SetTagAccessor<T>) tag;
                    concrete.setValues(ImmutableSet.<T>builder()
                        .addAll(concrete.getValues())
                        .addAll(values)
                        .build());
                    concrete.setValuesList(ImmutableList.<T>builder()
                        .addAll(concrete.getValuesList())
                        .addAll(values)
                        .build());
                }
            });
        }
    }
}
