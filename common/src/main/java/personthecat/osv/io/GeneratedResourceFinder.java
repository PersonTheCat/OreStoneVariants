package personthecat.osv.io;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeneratedResourceFinder implements RepositorySource {

    @Override
    public void loadPacks(Consumer<Pack> packs, Pack.PackConstructor constructor) {
        final String name = ModFolders.RESOURCE_DIR.getName();
        final Supplier<PackResources> pack = () -> ResourceHelper.RESOURCES;

        packs.accept(Pack.create(name, true, pack, constructor, Pack.Position.TOP, PackSource.BUILT_IN));
    }
}
