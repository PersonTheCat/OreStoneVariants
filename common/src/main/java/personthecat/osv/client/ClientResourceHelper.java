package personthecat.osv.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import personthecat.catlib.util.PathUtils;
import personthecat.osv.config.Cfg;
import personthecat.osv.io.ResourceHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientResourceHelper {

    public static boolean hasResource(final String path) {
        if (Cfg.assetsFromRP()) {
            final ResourceLocation id = PathUtils.getResourceLocation(path);
            final Iterator<PackResources> enabled = getEnabledPacks().iterator();

            while (enabled.hasNext()) {
                final PackResources rp = enabled.next();
                if (rp.hasResource(PackType.CLIENT_RESOURCES, id)) {
                    return true;
                }
            }
        }
        return ResourceHelper.hasResource(path);
    }

    public static Optional<InputStream> locateResource(final String path) {
        if (Cfg.assetsFromRP()) {
            final ResourceLocation id = PathUtils.getResourceLocation(path);
            final Iterator<PackResources> enabled = getEnabledPacks().iterator();

            while (enabled.hasNext()) {
                final PackResources rp = enabled.next();
                if (rp.hasResource(PackType.CLIENT_RESOURCES, id)) {
                    try {
                        return Optional.of(rp.getResource(PackType.CLIENT_RESOURCES, id));
                    } catch (final IOException | NullPointerException ignored) {}
                }
            }
        }
        return ResourceHelper.getResource(path);
    }

    private static Stream<PackResources> getEnabledPacks() {
        final PackRepository repository = Minecraft.getInstance().getResourcePackRepository();
        if (repository.getAvailablePacks().isEmpty()) {
            repository.reload();
        }
        return repository.getSelectedPacks().stream().map(Pack::open);
    }
}
