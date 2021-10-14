package personthecat.osv.client.model;

import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;

@Value
public class ModelWrapper {
    ResourceLocation id;
    JsonObject model;
    JsonObject settings;
}
