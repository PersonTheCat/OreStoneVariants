package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.catlib.serialization.HjsonOps;

public interface DynamicSerializable<T> {
    Codec<T> codec();

    @SuppressWarnings("unchecked")
    default JsonObject toJson() {
        return this.codec().encodeStart(HjsonOps.INSTANCE, (T) this).result()
            .map(JsonValue::asObject)
            .orElseGet(JsonObject::new);
    }
}
