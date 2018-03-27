package personthecat.mod.util;

import net.minecraftforge.client.event.ModelBakeEvent;
import personthecat.mod.init.BlockInit;
import personthecat.mod.objects.blocks.BlockOresEnumerated;
import personthecat.mod.util.handlers.BlockStateGenerator;

public interface IHasModel {
	public void registerModels();
}
