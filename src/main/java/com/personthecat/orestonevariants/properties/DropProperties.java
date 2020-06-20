package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.Range;
import net.minecraft.item.ItemStack;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.List;

import static com.personthecat.orestonevariants.util.HjsonTools.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class DropProperties {
    public final Lazy<ItemStack> drop;
    public final Range count;
    public final Range xp;
    public final double chance;

    public DropProperties(String lookup, Range count, Range xp, double chance) {
        this.drop = new Lazy<>(() -> getStack(lookup)
            .orElseThrow(() -> noItemNamed(lookup)));
        this.count = count;
        this.xp = xp;
        this.chance = chance;
    }

    public DropProperties(JsonObject json) {
        this(
            getGuaranteedString(json, "item"),
            getRangeOr(json, "range", Range.of(1, 1)),
            getRangeOr(json, "xp", Range.of(0)),
            getFloatOr(json, "chance", 100)
        );
    }

    public static List<DropProperties> list(JsonArray array) {
        final List<DropProperties> properties = new ArrayList<>();
        for (JsonValue value : array) {
            properties.add(new DropProperties(value.asObject()));
        }
        return properties;
    }
}