package personthecat.osv.mixin.forge;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemColors.class)
public interface ItemColorsAccessor {

    @Accessor(value = "f_92674_", remap = false)
    Map<IRegistryDelegate<Item>, ItemColor> getItemColors();
}
