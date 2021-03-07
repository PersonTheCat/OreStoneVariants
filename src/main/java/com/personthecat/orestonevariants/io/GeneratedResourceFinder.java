package com.personthecat.orestonevariants.io;

import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackInfo.Priority;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class GeneratedResourceFinder implements IPackFinder {

    private static final IPackNameDecorator DECORATOR = IPackNameDecorator.BUILTIN;

    @Override
    public void findPacks(Consumer<ResourcePackInfo> packs, ResourcePackInfo.IFactory factory) {
        final String name = ResourceHelper.DIR.getName();
        final Supplier<IResourcePack> pack = () -> ResourceHelper.RESOURCES;

        packs.accept(ResourcePackInfo.createResourcePack(name, true, pack, factory, Priority.TOP, DECORATOR));
    }
}
