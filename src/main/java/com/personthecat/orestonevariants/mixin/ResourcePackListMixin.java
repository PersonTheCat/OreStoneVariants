package com.personthecat.orestonevariants.mixin;

import com.personthecat.orestonevariants.io.GeneratedResourceFinder;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Log4j2
@Mixin(ResourcePackList.class)
public abstract class ResourcePackListMixin {

    @Final
    @Shadow
    private Set<IPackFinder> packFinders;

    /**
     * This guarantees that the resource / data pack will always be active
     * without needing to reload resources and risk any concurrency issues.
     * It is also essential for the generated resources to function as a
     * data pack.
     */
    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/resources/ResourcePackInfo$IFactory;[Lnet/minecraft/resources/IPackFinder;)V")
    private void addOSVResources(CallbackInfo ci) {
        log.info("Enabling resource pack.");
        this.packFinders.add(new GeneratedResourceFinder());
    }
}
