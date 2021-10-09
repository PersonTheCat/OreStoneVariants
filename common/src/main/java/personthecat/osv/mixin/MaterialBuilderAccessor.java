package personthecat.osv.mixin;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Material.Builder.class)
public interface MaterialBuilderAccessor {

    @Accessor
    PushReaction getPushReaction();

    @Accessor
    void setPushReaction(final PushReaction reaction);

    @Accessor
    boolean getBlocksMotion();

    @Accessor
    void setBlocksMotion(final boolean b);

    @Accessor
    boolean getFlammable();

    @Accessor
    void setFlammable(final boolean b);

    @Accessor
    boolean getLiquid();

    @Accessor
    void setLiquid(final boolean b);

    @Accessor
    boolean getReplaceable();

    @Accessor
    void setReplaceable(final boolean b);

    @Accessor
    boolean getSolid();

    @Accessor
    void setSolid(final boolean b);

    @Accessor
    MaterialColor getColor();

    @Mutable
    @Accessor
    void setColor(final MaterialColor color);

    @Accessor
    boolean getSolidBlocking();

    @Accessor
    void setSolidBlocking(final boolean b);
}
