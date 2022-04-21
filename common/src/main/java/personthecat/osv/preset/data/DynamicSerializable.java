package personthecat.osv.preset.data;

import com.mojang.serialization.Codec;
import personthecat.catlib.serialization.codec.XjsOps;
import xjs.core.JsonObject;
import xjs.core.JsonValue;

public interface DynamicSerializable<T> {
    Codec<T> codec();

    @SuppressWarnings("unchecked")
    default JsonObject toJson() {
        return this.codec().encodeStart(XjsOps.INSTANCE, (T) this).result()
            .map(JsonValue::asObject)
            .orElseGet(JsonObject::new);
    }
}
