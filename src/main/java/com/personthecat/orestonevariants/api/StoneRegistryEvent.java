package com.personthecat.orestonevariants.api;

import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.properties.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraft.block.Block;

import java.util.Set;

/**
 * This class is intended for mod developers who want to
 * register new blocks in world generation without having
 * to use {@link BaseOreVariant}. It can be used by subscribing
 * to this event and simply adding your own {@link Block} and
 * custom {@link WorldGenProperties}.
 *
 * For this to work, any ores that you register here will be
 * added as custom {@link StoneProperties}.
 */
public class StoneRegistryEvent extends Event {

    private final Set<StoneProperties> registry;

    public StoneRegistryEvent(Set<StoneProperties> registry) {
        this.registry = registry;
    }

    public Set<StoneProperties> getRegistry() {
        return registry;
    }
}
