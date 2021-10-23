package personthecat.osv.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {

    @Accessor
    Rarity getRarity();
}
