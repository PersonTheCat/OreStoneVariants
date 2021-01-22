package com.personthecat.orestonevariants.init;

import com.personthecat.orestonevariants.util.unsafe.ReflectionTools;
import net.minecraftforge.registries.*;
import net.minecraftforge.registries.IForgeRegistry.BakeCallback;

import java.lang.reflect.Field;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.runExF;

/** Unused. Might try to refactor this into something better before release. */
class DeferredRegistrationHelper {
    private final Field onBake;

    private DeferredRegistrationHelper(Field onBake) {
        this.onBake = onBake;
    }

    static DeferredRegistrationHelper setup() {
        final Field onBake = ReflectionTools.getField(ForgeRegistry.class, "bake", 15);
        ReflectionTools.setMutable(onBake);
        return new DeferredRegistrationHelper(onBake);
    }

    <T extends IForgeRegistryEntry<T>> void addListener(IForgeRegistry<T> registry, BakeCallback<T> listener) {
        if (!(registry instanceof ForgeRegistry)){
            throw runExF("Attempted to add listener to unsupported type: {}", registry.getClass());
        }
        final Optional<BakeCallback<T>> recursive = ReflectionTools.getOptionalValue(onBake, registry);
        final BakeCallback<T> updated = (owner, stage) -> {
            final ForgeRegistry<T> concrete = (ForgeRegistry<T>) registry;
            concrete.unfreeze();
            listener.onBake(owner, stage);
            recursive.ifPresent(r -> r.onBake(owner, stage));
            concrete.freeze();
        };
        ReflectionTools.setValue(onBake, registry, updated);
    }
}
