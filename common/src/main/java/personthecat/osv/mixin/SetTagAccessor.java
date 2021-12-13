package personthecat.osv.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.tags.SetTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(SetTag.class)
public interface SetTagAccessor<T> {

    @Accessor
    ImmutableList<T> getValuesList();

    @Mutable
    @Accessor
    void setValuesList(final ImmutableList<T> values);

    @Accessor
    Set<T> getValues();

    @Mutable
    @Accessor
    void setValues(final Set<T> values);
}
