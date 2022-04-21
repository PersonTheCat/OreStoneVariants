package personthecat.osv.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// Todo: use the accessor in CatLib
@Mixin(HolderSet.Named.class)
public interface NamedHolderSetAccessor<T> {

    @Accessor
    List<Holder<T>> getContents();

    @Accessor
    void setContents(final List<Holder<T>> contents);
}
