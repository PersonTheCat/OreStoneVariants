package personthecat.osv.mixin.forge;
import net.minecraft.server.packs.repository.Pack.PackConstructor;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import personthecat.osv.io.GeneratedResourceFinder;

import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Final
    @Shadow
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)V")
    private void addGeneratedResources(PackConstructor packConstructor, RepositorySource[] repositorySources, CallbackInfo ci) {
        this.sources.add(new GeneratedResourceFinder());
    }
}
