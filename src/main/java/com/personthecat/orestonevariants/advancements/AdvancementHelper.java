package com.personthecat.orestonevariants.advancements;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.LazyRegistries;
import com.personthecat.orestonevariants.item.VariantItem;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.randomId;

// temporary until we can implement better support for item tags
@Log4j2
public class AdvancementHelper {

    public static void handleAdvancements(AdvancementManager registry) {
        if (Cfg.enableAdvancements.get()) {
            log.info("Updating advancements from foreground items.");
            injectAll(registry);
        }
    }

    private static void injectAll(AdvancementManager registry) {
        for (VariantItem variant : LazyRegistries.ITEMS) {
            final ItemStack ore = new ItemStack(variant.getOre().getBlock());
            updateAdvancementsForItem(registry, ore, variant);
        }
        log.info("Successfully updated advancements with AdvancementManger!");
    }

    private static void updateAdvancementsForItem(AdvancementManager registry, ItemStack item, Item copy) {
        registry.getAllAdvancements().stream()
            .flatMap(a -> getTriggersForItem(a, item))
            .forEach(t -> updateTrigger(t, copy));
    }

    private static Stream<MatchingPredicate> getTriggersForItem(Advancement a, ItemStack item) {
        return a.getCriteria().values().stream()
            .flatMap(c -> getMatchingPredicate(a, c, item));
    }

    private static Stream<MatchingPredicate> getMatchingPredicate(Advancement a, Criterion c, ItemStack item) {
        final ICriterionInstance instance = c.getCriterionInstance();
        if (instance instanceof InventoryChangeTrigger.Instance) {
            final InventoryChangeTrigger.Instance trigger = (InventoryChangeTrigger.Instance) instance;
            for (ItemPredicate predicate : trigger.items) {
                if (predicate.test(item)) {
                    return Stream.of(new MatchingPredicate(a, trigger, predicate));
                }
            }
        }
        return Stream.empty();
    }

    // In the future, we'll convert these predicates to use item tags instead.
    private static void updateTrigger(MatchingPredicate data, Item copy) {
        data.advancement.criteria = new HashMap<>(data.advancement.criteria);
        data.advancement.criteria.put(randomId(), copyCriterion(data.matching, copy));
    }

    private static Criterion copyCriterion(ItemPredicate matching, Item copy) {
        return new Criterion(InventoryChangeTrigger.Instance.forItems(copyPredicate(matching, copy)));
    }

    private static ItemPredicate copyPredicate(ItemPredicate matching, Item copy) {
        return new ItemPredicate(null, copy, matching.count, matching.durability, matching.enchantments,
            matching.bookEnchantments, matching.potion, matching.nbt);
    }

    @AllArgsConstructor
    private static class MatchingPredicate {
        final Advancement advancement;
        final InventoryChangeTrigger.Instance trigger;
        final ItemPredicate matching;
    }
}
