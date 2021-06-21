package com.personthecat.orestonevariants.properties;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.personthecat.orestonevariants.util.CommonMethods.runEx;
import static com.personthecat.orestonevariants.util.CommonMethods.runExF;
import static com.personthecat.orestonevariants.util.HjsonTools.getBool;
import static com.personthecat.orestonevariants.util.HjsonTools.getFloat;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;

@EqualsAndHashCode
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class ContainerProperties {

    /** The name of the other properties that spawn inside of this ore. */
    String type;

    /** The 0 ~ 1 chance of using this variant instead of the parent. */
    @Default double chance = 0.09;

    /** Whether to crash the game if the variant isn't found. */
    @Default boolean required = false;

    private static ContainerProperties from(JsonObject json) {
        final ContainerPropertiesBuilder builder = builder().type(getString(json, "type")
            .orElseThrow(() -> runEx("Missing type in container")));
        getFloat(json, "chance", builder::chance);
        getBool(json, "required", builder::required);
        return builder.build();
    }

    public static List<ContainerProperties> list(JsonArray array) {
        if (array.isEmpty()) {
            return Collections.emptyList();
        }
        final List<ContainerProperties> containers = new ArrayList<>();
        for (JsonValue value : array) {
            if (!value.isObject()) {
                throw runExF("Expecting object array: {}", value);
            }
            containers.add(from(value.asObject()));
        }
        return containers;
    }
}
