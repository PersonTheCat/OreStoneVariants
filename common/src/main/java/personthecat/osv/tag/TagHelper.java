package personthecat.osv.tag;

import com.google.common.collect.ImmutableList;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import personthecat.catlib.data.collections.MultiValueHashMap;
import personthecat.catlib.data.collections.MultiValueMap;
import personthecat.catlib.exception.UnreachableException;
import personthecat.catlib.registry.CommonRegistries;
import personthecat.fresult.Result;
import personthecat.osv.ModRegistries;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.config.Cfg;
import personthecat.osv.mixin.NamedHolderSetAccessor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class TagHelper {

    private static final String MINEABLE_PREFIX = "mineable/";

    public static void injectTags() {
        if (Cfg.copyTags()) {
            log.info("Injecting OSV tags for all variants.");

            final TagUpdateContext ctx = new TagUpdateContext();
            Result.suppress(ctx::locateAll)
                .ifErr(e -> log.error("Locating tags", e));
            Result.suppress(ctx::copyAll)
                .ifErr(e -> log.error("Copying tags", e))
                .ifOk(v -> log.info("Copied {} block and {} item tags.",
                    ctx.blocksCopied, ctx.itemsCopied));
        }
    }

    private static class TagUpdateContext {
        final Map<TagKey<Block>, HolderSet.Named<Block>> blockTags;
        final Map<TagKey<Item>, HolderSet.Named<Item>> itemTags;
        final MultiValueMap<TagKey<Block>, Block> blockTagsToContents;
        final MultiValueMap<TagKey<Item>, Item> itemTagsToContents;
        int blocksCopied;
        int itemsCopied;

        TagUpdateContext() {
            this.blockTags = CommonRegistries.BLOCKS.getTags();
            this.itemTags = CommonRegistries.ITEMS.getTags();
            this.blockTagsToContents = new MultiValueHashMap<>();
            this.itemTagsToContents = new MultiValueHashMap<>();
            this.blocksCopied = 0;
            this.itemsCopied = 0;
        }

        void locateAll() {
            for (final OreVariant ore : ModRegistries.VARIANTS) {
                if (ore.getPreset().getVariant().isCopyTags()) {
                    if (Cfg.copyBgTags()) this.locateTags(ore, ore.getBg(), false);
                    if (Cfg.copyFgTags()) this.locateTags(ore, ore.getFg(), true);
                }
            }
        }

        void locateTags(final OreVariant ore, final Block wrapped, boolean fg) {
            if (Cfg.copyBlockTags()) {
                for (final TagKey<Block> key : getMatchingTags(this.blockTags, ore, wrapped, fg)) {
                    this.blockTagsToContents.add(key, ore);
                    this.blocksCopied++;
                }
            }
            if (Cfg.copyItemTags()) {
                for (final TagKey<Item> key : getMatchingTags(this.itemTags, ore, wrapped.asItem(), fg)) {
                    this.itemTagsToContents.add(key, ore.asItem());
                    this.itemsCopied++;

                    // Todo: this should copy all additional tags and not specifically dense.
                    if (Cfg.copyDenseTags() && ore.getPreset().canBeDense()) {
                        final BlockState dense = ore.defaultBlockState().setValue(AdditionalProperties.DENSE, true);
                        this.itemTagsToContents.add(key, ore.asItem(dense));
                        this.itemsCopied++;
                    }
                }
            }
        }

        static <T> Collection<TagKey<T>> getMatchingTags(
                final Map<TagKey<T>, HolderSet.Named<T>> tags, final OreVariant ore, final T t, final boolean fg) {
            return tags.entrySet().stream()
                .filter(entry -> entryContainsValue(entry, t))
                .map(Map.Entry::getKey)
                .flatMap(key -> filterKey(tags, key, ore, fg))
                .collect(Collectors.toList());
        }

        static <T> boolean entryContainsValue(final Map.Entry<TagKey<T>, HolderSet.Named<T>> tagEntry, final T t) {
            return tagEntry.getValue().stream().anyMatch(holder -> holder.value().equals(t));
        }

        static <T> Stream<TagKey<T>> filterKey(
                final Map<TagKey<T>, HolderSet.Named<T>> tags,
                final TagKey<T> key,
                final OreVariant ore,
                final boolean fg) {
            // We have to copy mineable tags from somewhere
            if (fg && isMineable(key)) {
                if (Cfg.copyBgTags()) {
                    return Stream.empty();
                }
                final TagKey<T> toCopy =
                    Cfg.bgImitation() && ore.getPreset().getVariant().isBgImitation()
                        ? getMineable(tags, ore).orElse(key)
                        : key;
                return Stream.of(toCopy);
            }
            return getPlatformKeys(key, ore, fg);
        }

        static <T> boolean isMineable(final TagKey<T> key) {
            return key.location().getPath().startsWith(MINEABLE_PREFIX);
        }

        static <T> Optional<TagKey<T>> getMineable(
                final Map<TagKey<T>, HolderSet.Named<T>> tags,
                final OreVariant ore) {
            // Todo: this was a hack to resolve a generic parameter mismatch
            final ResourceLocation bgLocation = CommonRegistries.BLOCKS.getKey(ore.getBg());
            return tags.entrySet()
                .stream()
                .filter(entry -> isMineable(entry.getKey()) && entryContainsId(entry, bgLocation))
                .findFirst()
                .map(Map.Entry::getKey);
        }

        static <T> boolean entryContainsId(
                final Map.Entry<TagKey<T>, HolderSet.Named<T>> tagEntry,
                final ResourceLocation id) {
            final Registry<?> registry = Registry.REGISTRY.get(tagEntry.getKey().registry().location());
            if (registry == null) return false;
            final Object value = registry.get(id);
            if (value == null) return false;
            return tagEntry.getValue().stream().anyMatch(holder -> holder.value().equals(value));
        }

        @ExpectPlatform
        @SuppressWarnings("unused")
        static <T> Stream<TagKey<T>> getPlatformKeys(final TagKey<T> key, final OreVariant ore, final boolean fg) {
            throw new AssertionError();
        }

        void copyAll() {
            this.copy(Registry.BLOCK, this.blockTagsToContents);
            this.copy(Registry.ITEM, this.itemTagsToContents);
        }

        @SuppressWarnings("unchecked")
        <T> void copy(
                final Registry<T> registry,
                final MultiValueMap<TagKey<T>, T> tagsToContents) {
            tagsToContents.forEach((id, values) -> {
                final HolderSet.Named<T> tag = registry.getOrCreateTag(id);
                if (tag instanceof NamedHolderSetAccessor accessor) {
                    accessor.setContents(ImmutableList.<Holder<T>>builder()
                        .addAll(accessor.getContents())
                        .addAll(getHolders(registry, values))
                        .build());
                }
            });
        }

        static <T> List<Holder<T>> getHolders(final Registry<T> registry, List<T> values) {
            return values.stream()
                .map(value -> registry.getHolder(registry.getId(value))
                    .orElseThrow(UnreachableException::new))
                .collect(Collectors.toList());
        }
    }
}
