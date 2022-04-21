package personthecat.osv.client.model;

import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import xjs.core.JsonObject;

@Value
public class ModelWrapper {
    ResourceLocation id;
    JsonObject model;
    JsonObject settings;
}
