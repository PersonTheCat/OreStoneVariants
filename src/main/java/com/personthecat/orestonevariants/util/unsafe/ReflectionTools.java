package com.personthecat.orestonevariants.util.unsafe;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

/** A convenient wrapper for ObfuscationReflectionHelper using Result. */
public class ReflectionTools {
    public static Field getField(Class clazz, String name) {
        return (Field) Result.of(() -> ObfuscationReflectionHelper.findField(clazz, name))
            .expect("Build error: invalid field name used in reflection.");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Field f, Object instance) {
        return (T) Result.of(() -> f.get(instance))
            .expect("Build error: field not marked as accessible.");
    }

    public static <T> T getValue(Class clazz, String name, Object instance) {
        return getValue(getField(clazz, name), instance);
    }

    public static void setValue(Field f, Object instance, Object value) {
        Result.of(() -> f.set(instance, value))
            .expect("Build error: field not marked as accessible.");
    }
}