package personthecat.osv.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.PathUtils;
import personthecat.osv.config.Cfg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientResourceHelper {

    public static boolean hasResource(final String path) {
        if (Cfg.overlaysFromRp()) {
            final ResourceLocation id = PathUtils.getResourceLocation(path);
            final Iterator<PackResources> enabled = getEnabledPacks().iterator();

            while (enabled.hasNext()) {
                final PackResources rp = enabled.next();
                if (rp.hasResource(PackType.CLIENT_RESOURCES, id)) {
                    return true;
                }
            }
        }
        return FileIO.resourceExists(path);
    }

    public static Optional<InputStream> locateResource(final String path) {
        if (Cfg.overlaysFromRp()) {
            final ResourceLocation id = PathUtils.getResourceLocation(path);
            final Iterator<PackResources> enabled = getDescendingPackIterator();

            while (enabled.hasNext()) {
                final PackResources rp = enabled.next();
                if (rp.hasResource(PackType.CLIENT_RESOURCES, id)) {
                    try {
                        return Optional.of(rp.getResource(PackType.CLIENT_RESOURCES, id));
                    } catch (final IOException | NullPointerException ignored) {}
                }
            }
        }
        return FileIO.getResource(path);
    }

    private static Iterator<PackResources> getDescendingPackIterator() {
        return getEnabledPacks().collect(Collectors.toCollection(LinkedList::new)).descendingIterator();
    }

    private static Stream<PackResources> getEnabledPacks() {
        return Minecraft.getInstance()
            .getResourcePackRepository()
            .getAvailablePacks()
            .stream()
            .map(Pack::open);
    }
}