package personthecat.catlib.util;

import com.mojang.serialization.Codec;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import personthecat.catlib.exception.JsonFormatException;
import personthecat.catlib.serialization.HjsonOps;

import java.util.Optional;
import java.util.function.Supplier;

public class HjsonUtilsMod {

    public static <A> Optional<JsonValue> writeOptional(final Codec<A> codec, final A a) {
        return codec.encodeStart(HjsonOps.INSTANCE, a).result();
    }

    public static <A> JsonValue writeOrThrow(final Codec<A> codec, final A a) {
        return codec.encodeStart(HjsonOps.INSTANCE, a).result()
            .orElseThrow(() -> new JsonFormatException("Writing object: " + a));
    }

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

    public static JsonValue getOrCreate(final JsonObject json, final String field, final Supplier<JsonValue> ifAbsent) {
        final JsonValue get = json.get(field);
        if (get != null) return get;

        final JsonValue set = ifAbsent.get();
        json.set(field, set);
        return set;
    }

    public static void setRecursivelyIfAbsent(final JsonObject json, final JsonObject toSet) {
        for (final JsonObject.Member member : toSet) {
            final JsonValue get = json.get(member.getName());
            if (get != null) {
                if (get.isObject() && member.getValue().isObject()) {
                    setRecursivelyIfAbsent(get.asObject(), member.getValue().asObject());
                }
            } else if (!member.getValue().isNull()) {
                json.set(member.getName(), member.getValue());
            }
        }
    }

    public static void setAllIfAbsent(final JsonObject json, final JsonObject toSet) {
        for (final JsonObject.Member member : toSet) {
            if (!json.has(member.getName())) {
                json.set(member.getName(), member.getValue());
            }
        }
    }

    public static void setIfAbsent(final JsonObject json, final String field, final String value) {
        if (!json.has(field)) json.set(field, value);
    }

    public static void setIfAbsent(final JsonObject json, final String field, final int value) {
        if (!json.has(field)) json.set(field, value);
    }

    public static void setIfAbsent(final JsonObject json, final String field, final float value) {
        if (!json.has(field)) json.set(field, value);
    }

    public static void setIfAbsent(final JsonObject json, final String field, final boolean value) {
        if (!json.has(field)) json.set(field, value);
    }

    public static void setIfAbsent(final JsonObject json, final String field, final JsonValue value) {
        if (!json.has(field)) json.set(field, value);
    }
}
