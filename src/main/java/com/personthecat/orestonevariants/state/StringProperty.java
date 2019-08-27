package com.personthecat.orestonevariants.state;

import net.minecraft.state.Property;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

/** Testing this. It's probably a very bad idea to use it. */
public class StringProperty extends Property<String> {
    private static final Set<String> registeredValues = new HashSet<>();

    public StringProperty(String s) {
        super(s, String.class);
        registeredValues.add(s);
    }

    public Collection<String> getAllowedValues() {
        return registeredValues;
    }

    public static StringProperty create(String s) {
        return new StringProperty(s);
    }

    public Optional<String> parseValue(String s) {
        return full(s);
    }

    public String getName(String s) {
        return s;
    }
}