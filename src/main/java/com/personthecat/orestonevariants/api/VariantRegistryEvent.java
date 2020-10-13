package com.personthecat.orestonevariants.api;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.properties.OreProperties;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

/**
 * This class is intended for other mod makers to register
 * their own ore variants to be generated through OSV. It
 * can be used by subscribing to this event and simply adding
 * your own {@link BaseOreVariant} blocks.
 *
 * Note that in order for these blocks to spawn in the world,
 * you must also subscribe to {@link PropertyRegistryEvent} and
 * add your custom {@link OreProperties} there.
 */
public class VariantRegistryEvent extends Event {

    private final Set<BaseOreVariant> registry;

    public VariantRegistryEvent(Set<BaseOreVariant> registry) {
        this.registry = registry;
    }

    public Set<BaseOreVariant> getRegistry() {
        return registry;
    }

}
