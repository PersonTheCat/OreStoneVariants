package com.personthecat.orestonevariants.api;

import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

import java.util.Set;

/**
 * This class is intended for other mod makers to register
 * their own ore variants to be generated through OSV. It
 * can be used by subscribing to this event and simply adding
 * your own {@link OreProperties}.
 *
 * Note that in order for these properties to be used, you must
 * also subscribe to {@link VariantRegistryEvent} and add your
 * custom {@link BaseOreVariant}s.
 */
public class PropertyRegistryEvent extends Event implements IModBusEvent {

    private final Set<OreProperties> registry;

    public PropertyRegistryEvent(Set<OreProperties> registry) {
        this.registry = registry;
    }

    public Set<OreProperties> getRegistry() {
        return registry;
    }
}
