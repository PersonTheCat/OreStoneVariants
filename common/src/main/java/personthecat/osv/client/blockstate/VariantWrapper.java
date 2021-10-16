package personthecat.osv.client.blockstate;

import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.osv.client.model.ModelWrapper;

import java.util.Optional;

@Value
public class VariantWrapper {
    ResourceLocation model;
    JsonObject settings;

    static Optional<VariantWrapper> tryCreate(final JsonObject definition) {
        final JsonValue model = definition.get("model");
        if (model != null && model.isString()) {
            definition.remove("model");
            return Optional.of(new VariantWrapper(new ResourceLocation(model.asString()), definition));
        }
        return Optional.empty();
    }

    public ModelWrapper withDefinition(final JsonObject definition) {
        return new ModelWrapper(this.model, definition, this.settings);
    }
}
