package personthecat.catlib.util;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

public class HjsonUtilsMod {

    /**
     * Returns a "shallow" clone of the given value. In other words, if this
     * value is an object or array, a new container will be constructed. If
     * it is any other value, the <em>reference</em> will be copied, meaning
     * the original comments will share the same address in memory.
     *
     * @param value The value being copied.
     * @return A shallow copy of the original value.
     */
    public static JsonValue shallowCopy(final JsonValue value) {
        if (value.isArray()) {
            final JsonArray copy = new JsonArray();
            for (final JsonValue v2 : value.asArray()) {
                copy.add(shallowCopy(v2));
            }
            return copy;
        } else if (value.isObject()) {
            final JsonObject copy = new JsonObject();
            for (final JsonObject.Member member : value.asObject()) {
                copy.add(member.getName(), shallowCopy(member.getValue()));
            }
            return copy;
        }
        return value;
    }
}
